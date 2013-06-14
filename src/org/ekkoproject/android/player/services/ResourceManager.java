package org.ekkoproject.android.player.services;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.DigestOutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.appdev.entity.Resource;
import org.appdev.utils.StringUtils;
import org.ekkoproject.android.player.api.ApiSocketException;
import org.ekkoproject.android.player.api.EkkoHubApi;
import org.ekkoproject.android.player.api.InvalidSessionApiException;
import org.ekkoproject.android.player.db.EkkoDao;
import org.ekkoproject.android.player.model.CachedResource;
import org.ekkoproject.android.player.model.Course;
import org.ekkoproject.android.player.model.Manifest;
import org.ekkoproject.android.player.util.IOUtils;
import org.ekkoproject.android.player.util.MultiKeyLruCache;
import org.ekkoproject.android.player.util.WeakMultiKeyLruCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;

public final class ResourceManager {
    private static final Logger LOG = LoggerFactory.getLogger(ResourceManager.class);

    private class Key {
        private final long courseId;
        private final String sha1;

        public Key(final Resource resource) {
            if (resource == null) {
                throw new IllegalArgumentException("resource cannot be null");
            }
            this.courseId = resource.getCourseId();
            this.sha1 = resource.getResourceSha1();
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) {
                return true;
            }
            if (o instanceof Key) {
                final Key key = (Key) o;
                return this.courseId == key.courseId
                        && ((this.sha1 == null && key.sha1 == null) || (this.sha1 != null && this.sha1.equals(key.sha1)));
            }

            return false;
        }

        @Override
        public int hashCode() {
            int hash = 0;
            hash = hash * 31 + Long.valueOf(this.courseId).hashCode();
            hash = hash * 31 + (this.sha1 != null ? this.sha1.hashCode() : 0);
            return hash;
        }
    }

    private final class BitmapKey extends Key {
        private final int width;
        private final int height;

        public BitmapKey(final Resource resource, int width, int height) {
            super(resource);
            this.width = width;
            this.height = height;
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) {
                return true;
            }
            if (o instanceof BitmapKey) {
                final BitmapKey key = (BitmapKey) o;
                return super.equals(o) && this.width == key.width && this.height == key.height;
            }

            return false;
        }

        @Override
        public int hashCode() {
            int hash = super.hashCode();
            hash = hash * 31 + width;
            hash = hash * 31 + height;
            return hash;
        }
    }

    private final Context context;
    private final EkkoHubApi api;
    private final EkkoDao dao;
    private final ManifestManager manifestManager;

    private final Map<Key, Object> locks = new HashMap<Key, Object>();
    private final MultiKeyLruCache<BitmapKey, Bitmap> bitmaps;
    private final Map<BitmapKey, Object> bitmapLocks = new HashMap<BitmapKey, Object>();

    private static ResourceManager instance;

    private ResourceManager(final Context ctx) {
        this.context = ctx.getApplicationContext();
        this.api = new EkkoHubApi(this.context);
        this.dao = new EkkoDao(this.context);
        this.manifestManager = ManifestManager.getInstance(this.context);

        this.bitmaps = new WeakMultiKeyLruCache<BitmapKey, Bitmap>((int) (Runtime.getRuntime().maxMemory() / 1024 / 16)) {
            @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR1)
            @Override
            protected int sizeOf(final BitmapKey key, final Bitmap bitmap) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR1) {
                    return bitmap.getByteCount() / 1024;
                } else {
                    return bitmap.getRowBytes() * bitmap.getHeight() / 1024;
                }
            }
        };
    }

    public static final ResourceManager getInstance(final Context context) {
        if (instance == null) {
            instance = new ResourceManager(context);
        }
        return instance;
    }

    public Bitmap getBitmap(final long courseId, final String resourceId, final int width, final int height) {
        return this.getBitmap(this.resolveResource(courseId, resourceId), width, height);
    }

    public InputStream getInputStream(final long courseId, final String resourceId) {
        return this.getInputStream(this.resolveResource(courseId, resourceId));
    }

    public File getFile(final long courseId, final String resourceId) {
        return this.getFile(this.resolveResource(courseId, resourceId));
    }

    public Bitmap getBitmap(final Resource resource, final int width, final int height) {
        // short-circuit if this isn't a valid resource
        if (!this.isValidResource(resource)) {
            return null;
        }

        // look for a cached bitmap
        final BitmapKey key = new BitmapKey(resource, width, height);
        final Bitmap img = this.bitmaps.get(key);
        if (img != null) {
            return img;
        }

        // load the bitmap
        return loadBitmap(resource, width, height);
    }

    public InputStream getInputStream(final Resource resource) {
        final File f = this.getFile(resource);
        if (f != null) {
            try {
                return new FileInputStream(f);
            } catch (final FileNotFoundException e) {
            }
        }
        return null;
    }

    public File getFile(final Resource resource) {
        // short-circuit if this isn't a valid resource
        if (!this.isValidResource(resource)) {
            return null;
        }

        synchronized (getLock(this.locks, new Key(resource))) {
            // check for a cached copy of the resource
            final CachedResource cachedResource = this.dao.find(CachedResource.class, resource.getCourseId(),
                    resource.getResourceSha1());
            if (cachedResource != null && cachedResource.getPath() != null) {
                final File f = new File(cachedResource.getPath());
                if (f.exists()) {
                    return f;
                }
            }

            // check for a cached copy in the current download directory
            final File f = this.getFilePath(resource);
            if (f != null && f.exists()) {
                return f;
            }

            // download the resource
            return this.downloadResource(resource);
        }
    }

    private Bitmap loadBitmap(final Resource resource, final int width, final int height) {
        final File f = this.getFile(resource);
        if (f == null) {
            return null;
        }

        final BitmapKey key = new BitmapKey(resource, width, height);
        synchronized (getLock(this.bitmapLocks, key)) {
            // make sure the bitmap wasn't just loaded
            Bitmap bitmap = this.bitmaps.get(key);
            if (bitmap != null) {
                return bitmap;
            }

            // read image meta
            final BitmapFactory.Options meta = new BitmapFactory.Options();
            meta.inJustDecodeBounds = true;
            try {
                BitmapFactory.decodeFile(f.getPath(), meta);
            } catch (final Exception e) {
                LOG.error("error decoding image", e);
            }
            if (meta.outMimeType == null) {
                // invalid image
                LOG.debug("unrecognized image: course={} sha1={}", resource.getCourseId(), resource.getResourceSha1());
                return null;
            }

            // calculate how much to scale the image by
            int scale = 1;
            while (width * (scale + 1) < meta.outWidth && height * (scale + 1) < meta.outHeight) {
                scale++;
            }

            // check to see if there is a Bitmap at this scale already
            final BitmapKey scaledKey = new BitmapKey(resource, meta.outWidth / scale, meta.outHeight / scale);
            synchronized (getLock(this.bitmapLocks, scaledKey)) {
                bitmap = this.bitmaps.get(scaledKey);

                // we don't have a scaled bitmap, so load one
                if (bitmap == null) {
                    final BitmapFactory.Options opts = new BitmapFactory.Options();
                    opts.inSampleSize = scale;
                    bitmap = BitmapFactory.decodeFile(f.getPath(), opts);
                    if (bitmap != null) {
                        this.bitmaps.putMulti(scaledKey, bitmap);
                    }
                }
            }

            // store the bitmap in the cache
            if (bitmap != null) {
                this.bitmaps.putMulti(new BitmapKey(resource, width, height), bitmap);
            }

            // return the bitmap
            return bitmap;
        }
    }

    private File downloadResource(final Resource resource) {
        // short-circuit if this isn't a valid resource
        if (!this.isValidResource(resource)) {
            return null;
        }

        synchronized (getLock(this.locks, new Key(resource))) {
            // get File object
            final File f = this.getFilePath(resource);
            if (f == null) {
                return null;
            }

            // try downloading the file
            OutputStream out = null;
            long size = -1;
            MessageDigest digest = null;
            try {
                out = new FileOutputStream(f);

                // create a SHA-1 digest if possible for verification
                try {
                    digest = MessageDigest.getInstance("SHA-1");
                    out = new DigestOutputStream(out, digest);
                } catch (final NoSuchAlgorithmException e) {
                    digest = null;
                }

                // download the resource
                size = this.api.streamResource(resource, out);
            } catch (final FileNotFoundException e) {
                // this is an odd exception
                LOG.error("unexpected error opening resource cache file for download", e);
                return null;
            } catch (final ApiSocketException e) {
                // connection error
                LOG.debug("connection error", e);
                return null;
            } catch (final InvalidSessionApiException e) {
                // the users session has expired, ask them to re-authenticate
                LOG.debug("the users session expired");
                return null;
            } finally {
                IOUtils.closeQuietly(out);

                // delete invalid downloads
                final String sha1 = digest != null ? StringUtils.bytesToHex(digest.digest()).toLowerCase(Locale.US)
                        : null;
                if (size == -1 || (resource.getResourceSize() != -1 && size != resource.getResourceSize())
                        || (sha1 != null && !sha1.equals(resource.getResourceSha1()))) {
                    f.delete();
                }
            }

            if (f.exists()) {
                // create CachedResource record
                final CachedResource cachedResource = new CachedResource();
                cachedResource.setCourseId(resource.getCourseId());
                cachedResource.setSha1(resource.getResourceSha1());
                cachedResource.setSize(size);
                cachedResource.setPath(f.getPath());
                cachedResource.setLastAccessed();
                this.dao.replace(cachedResource);

                // return the File object
                return f;
            }
        }

        return null;
    }

    private File dir() {
        final File dir = this.context.getExternalFilesDir("resources");
        dir.mkdirs();
        return dir;
    }

    private File getFilePath(final Resource resource) {
        if (isValidResource(resource)) {
            final File courseDir = new File(dir(), Long.toString(resource.getCourseId()));
            courseDir.mkdirs();
            return new File(courseDir, resource.getResourceSha1().toLowerCase(Locale.US));
        }

        return null;
    }

    private <T> Object getLock(final Map<T, Object> locks, final T key) {
        synchronized (locks) {
            if (!locks.containsKey(key)) {
                locks.put(key, new Object());
            }
            return locks.get(key);
        }
    }

    private boolean isValidResource(final Resource resource) {
        return resource != null && resource.isFile() && resource.getResourceSha1() != null;
    }

    private Resource resolveResource(final long courseId, final String resourceId) {
        // check manifest for resource, we disable downloading of the manifest
        // for now
        Manifest manifest = this.manifestManager.getManifest(courseId, ManifestManager.FLAG_DONT_DOWNLOAD);
        if (manifest != null) {
            final Resource resource = manifest.getResource(resourceId);
            if (resource != null) {
                return resource;
            }
        }

        // check database for resource
        final Course course = this.dao.findCourse(courseId, true);
        if (course != null) {
            final Resource resource = course.getResource(resourceId);
            if (resource != null) {
                return resource;
            }
        }

        // maybe try downloading the manifest (only when we don't already have a
        // manifest)
        if (manifest == null) {
            manifest = this.manifestManager.downloadManifest(courseId);
            if (manifest != null) {
                final Resource resource = manifest.getResource(resourceId);
                if (resource != null) {
                    return resource;
                }
            }
        }

        // we couldn't find a resource object
        return null;
    }
}
