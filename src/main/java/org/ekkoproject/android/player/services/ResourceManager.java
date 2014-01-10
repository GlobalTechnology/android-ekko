package org.ekkoproject.android.player.services;

import static java.net.HttpURLConnection.HTTP_OK;
import static org.ekkoproject.android.player.model.Resource.INVALID_VIDEO;
import static org.ekkoproject.android.player.model.Resource.PROVIDER_NONE;
import static org.ekkoproject.android.player.util.ThreadUtils.assertNotOnUiThread;
import static org.ekkoproject.android.player.util.ThreadUtils.getLock;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.support.v4.util.LruCache;

import org.ccci.gto.android.common.api.ApiSocketException;
import org.ccci.gto.android.common.api.InvalidSessionApiException;
import org.ccci.gto.android.common.util.IOUtils;
import org.ekkoproject.android.player.api.EkkoHubApi;
import org.ekkoproject.android.player.db.EkkoDao;
import org.ekkoproject.android.player.model.CachedEcvResource;
import org.ekkoproject.android.player.model.CachedFileResource;
import org.ekkoproject.android.player.model.CachedUriResource;
import org.ekkoproject.android.player.model.Course;
import org.ekkoproject.android.player.model.Manifest;
import org.ekkoproject.android.player.model.Resource;
import org.ekkoproject.android.player.util.MultiKeyLruCache;
import org.ekkoproject.android.player.util.StringUtils;
import org.ekkoproject.android.player.util.WeakMultiKeyLruCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.DigestOutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Random;

public final class ResourceManager {
    private static final Logger LOG = LoggerFactory.getLogger(ResourceManager.class);

    private static class Key {
        private final long courseId;
        private final String sha1;
        private final String uri;
        private final int provider;
        private final long videoId;
        private final boolean thumb;

        public Key(final Resource resource) {
            this(resource, false);
        }

        public Key(final Resource resource, final boolean thumb) {
            if (resource == null) {
                throw new IllegalArgumentException("resource cannot be null");
            }
            this.courseId = resource.getCourseId();

            // file resource attributes
            this.sha1 = resource.isFile() ? resource.getResourceSha1() : null;

            // uri resource attributes
            this.uri = resource.isUri() ? resource.getUri() : null;
            this.provider = resource.isUri() ? resource.getProvider() : PROVIDER_NONE;

            // ecv resource attributes
            this.videoId = resource.isEcv() ? resource.getVideoId() : INVALID_VIDEO;
            this.thumb = thumb;
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) {
                return true;
            }
            if (o instanceof Key) {
                final Key key = (Key) o;
                return this.courseId == key.courseId && ((this.sha1 == null && key.sha1 == null) ||
                        (this.sha1 != null && this.sha1.equals(key.sha1))) &&
                        ((this.uri == null && key.uri == null) || (this.uri != null && this.uri.equals(key.uri))) &&
                        this.provider == key.provider && this.videoId == key.videoId && this.thumb == key.thumb;
            }

            return false;
        }

        @Override
        public int hashCode() {
            int hash = 0;
            hash = hash * 31 + Long.valueOf(this.courseId).hashCode();
            hash = hash * 31 + (this.sha1 != null ? this.sha1.hashCode() : 0);
            hash = hash * 31 + (this.uri != null ? this.uri.hashCode() : 0);
            hash = hash * 31 + this.provider;
            hash = hash * 31 + Long.valueOf(this.videoId).hashCode();
            hash = hash * 31 + (this.thumb ? 1 : 0);
            return hash;
        }
    }

    private static final class BitmapKey extends Key {
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

    // TODO: flags not supported yet
    public static final int FLAG_NON_BLOCKING = 1 << 0;
    public static final int FLAG_DONT_DOWNLOAD = 1 << 1;
    public static final int FLAG_FORCE_DOWNLOAD = 1 << 2;
    public static final int FLAG_TYPE_IMAGE = 1 << 3;

    @SuppressLint("TrulyRandom")
    private static final Random NAME_RNG = new SecureRandom();

    private final Context context;
    private final EkkoHubApi api;
    private final EkkoDao dao;
    private final ManifestManager manifestManager;

    private final Map<Key, Object> downloadLocks = new HashMap<>();
    private final LruCache<BitmapKey, Bitmap> bitmaps;
    private final Map<Key, BitmapFactory.Options> bitmapMeta = new HashMap<>();
    private final Map<BitmapKey, Object> bitmapLocks = new HashMap<>();

    private static ResourceManager instance;
    private static final Object LOCK_INSTANCE = new Object();

    private ResourceManager(final Context ctx) {
        this.context = ctx.getApplicationContext();
        this.api = EkkoHubApi.getInstance(this.context);
        this.dao = EkkoDao.getInstance(ctx);
        this.manifestManager = ManifestManager.getInstance(this.context);

        this.bitmaps = new WeakMultiKeyLruCache<BitmapKey, Bitmap>((int) (Runtime.getRuntime().maxMemory() / 1024 / 8)) {
            @Override
            @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR1)
            protected int sizeOf(final BitmapKey key, final Bitmap bitmap) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR1) {
                    return bitmap.getByteCount() / 1024;
                } else {
                    return bitmap.getRowBytes() * bitmap.getHeight() / 1024;
                }
            }
        };
    }

    public static ResourceManager getInstance(final Context context) {
        synchronized (LOCK_INSTANCE) {
            if (instance == null) {
                instance = new ResourceManager(context);
            }
        }

        return instance;
    }

    public Bitmap getBitmap(final long courseId, final String resourceId, final int width, final int height) {
        return this.getBitmap(this.resolveResource(courseId, resourceId), width, height);
    }

    public Bitmap getBitmap(final Resource resource, final int width, final int height) {
        assertNotOnUiThread();

        // look for a cached bitmap
        final BitmapKey key = new BitmapKey(resource, width, height);
        final Bitmap img = this.bitmaps.get(key);
        if (img != null) {
            return img;
        }

        // load the bitmap
        return loadBitmap(resource, width, height);
    }

    private Bitmap loadBitmap(final Resource resource, final int width, final int height) {
        final File f = this.getFile(resource, FLAG_TYPE_IMAGE);
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

            // get image meta
            final Key key2 = new Key(resource);
            BitmapFactory.Options meta = bitmapMeta.get(key2);
            if (meta == null) {
                meta = new BitmapFactory.Options();
                meta.inJustDecodeBounds = true;
                try {
                    BitmapFactory.decodeFile(f.getPath(), meta);
                    bitmapMeta.put(key2, meta);
                } catch (final Exception e) {
                    LOG.error("error decoding image", e);
                }
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
                        if (this.bitmaps instanceof MultiKeyLruCache) {
                            ((MultiKeyLruCache<BitmapKey, Bitmap>) this.bitmaps).putMulti(scaledKey, bitmap);
                        } else {
                            this.bitmaps.put(scaledKey, bitmap);
                        }
                    }
                }
            }

            // store the bitmap in the cache
            if (bitmap != null) {
                if (this.bitmaps instanceof MultiKeyLruCache) {
                    ((MultiKeyLruCache<BitmapKey, Bitmap>) this.bitmaps)
                            .putMulti(new BitmapKey(resource, width, height), bitmap);
                } else {
                    this.bitmaps.put(new BitmapKey(resource, width, height), bitmap);
                }
            }

            // return the bitmap
            return bitmap;
        }
    }

    public InputStream getInputStream(final long courseId, final String resourceId) {
        return this.getInputStream(this.resolveResource(courseId, resourceId));
    }

    public InputStream getInputStream(final Resource resource) {
        final File f = this.getFile(resource);
        if (f != null) {
            try {
                return new FileInputStream(f);
            } catch (final FileNotFoundException ignored) {
            }
        }
        return null;
    }

    public File getFile(final long courseId, final String resourceId) {
        return this.getFile(this.resolveResource(courseId, resourceId));
    }

    public File getFile(final Resource resource) {
        return this.getFile(resource, 0);
    }

    public File getFile(final Resource resource, final int flags) {
        assertNotOnUiThread();

        // switch based on resource type
        if (this.isValidFileResource(resource)) {
            return this.getFileResource(resource, flags);
        } else if (this.isDownloadableUriResource(resource)) {
            return this.getUriResource(resource, flags);
        } else if (this.isValidEcvResource(resource)) {
            return this.getEcvResource(resource, flags);
        }

        // default to null
        return null;
    }

    private File getFileResource(final Resource resource, final int flags) {
        // short-circuit if this isn't a valid file resource
        if (!this.isValidFileResource(resource)) {
            return null;
        }

        synchronized (getLock(this.downloadLocks, new Key(resource))) {
            // check for a cached copy of the resource
            final CachedFileResource cachedResource = this.dao.find(CachedFileResource.class, resource.getCourseId(),
                    resource.getResourceSha1());
            if (cachedResource != null && cachedResource.getPath() != null) {
                final File f = new File(cachedResource.getPath());
                if (f.exists()) {
                    return f;
                }
            }

            // XXX: this is disabled to prevent using corrupted downloads
            // // check for a cached copy in the current download directory
            // final File f = this.getFileObject(resource);
            // if (f != null && f.exists()) {
            // return f;
            // }

            // download the resource
            return this.downloadFileResource(resource);
        }
    }

    private File getUriResource(final Resource resource, final int flags) {
        // short-circuit if this isn't a downloadable uri resource
        if (!this.isDownloadableUriResource(resource)) {
            return null;
        }

        synchronized (getLock(this.downloadLocks, new Key(resource))) {
            // check for a cached copy of the resource
            final CachedUriResource cachedResource = this.dao.find(CachedUriResource.class, resource.getCourseId(),
                    resource.getUri());
            if (cachedResource != null && cachedResource.getPath() != null) {
                final File f = new File(cachedResource.getPath());
                if (f.exists()) {
                    return f;
                }
            }

            // download the resource
            return this.downloadUriResource(resource);
        }
    }

    private File getEcvResource(final Resource resource, final int flags) {
        // short-circuit if this isn't a valid file resource
        if (!this.isValidEcvResource(resource)) {
            return null;
        }

        final boolean thumb = (flags & FLAG_TYPE_IMAGE) == FLAG_TYPE_IMAGE;
        synchronized (getLock(this.downloadLocks, new Key(resource, thumb))) {
            // check for a cached copy of the resource
            final CachedEcvResource cachedResource =
                    this.dao.find(CachedEcvResource.class, resource.getCourseId(), resource.getVideoId(), thumb);
            if (cachedResource != null && cachedResource.getPath() != null) {
                final File f = new File(cachedResource.getPath());
                if (f.exists()) {
                    return f;
                }
            }

            // download the resource
            return this.downloadEcvResource(resource, thumb);
        }
    }

    private File downloadFileResource(final Resource resource) {
        return this.downloadFileResource(resource, FLAG_FORCE_DOWNLOAD);
    }

    private File downloadFileResource(final Resource resource, final int flags) {
        // short-circuit if this isn't a valid resource
        if (!this.isValidFileResource(resource)) {
            return null;
        }

        synchronized (getLock(this.downloadLocks, new Key(resource))) {
            // get File object
            final File f = this.getFileObject(resource, null);
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
                size = this.api.downloadFileResource(resource, out);
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
                // create CachedFileResource record
                final CachedFileResource cachedResource =
                        new CachedFileResource(resource.getCourseId(), resource.getResourceSha1());
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

    private File downloadUriResource(final Resource resource) {
        // short-circuit if this isn't a downloadable uri resource
        if (!this.isDownloadableUriResource(resource)) {
            return null;
        }

        synchronized (getLock(this.downloadLocks, new Key(resource))) {
            // create a new file object for writing
            final File f = this.getFileObject(resource, null);
            if (f == null) {
                return null;
            }

            // try downloading the file
            boolean success = false;
            HttpURLConnection conn = null;
            InputStream in = null;
            OutputStream out = null;
            long size = -1;
            long lastModified = System.currentTimeMillis();
            long expires = System.currentTimeMillis();
            try {
                out = new FileOutputStream(f);

                // open the connection
                conn = (HttpURLConnection) new URL(resource.getUri()).openConnection();
                conn.setInstanceFollowRedirects(false);

                // only download 200 responses
                if (conn.getResponseCode() == HTTP_OK) {
                    expires = conn.getHeaderFieldDate("Expires", expires);
                    lastModified = conn.getHeaderFieldDate("Expires", lastModified);

                    in = conn.getInputStream();
                    size = IOUtils.copy(in, out);
                    if (size >= 0) {
                        success = true;
                    }
                }
            } catch (final FileNotFoundException e) {
                // this is an odd exception
                LOG.error("unexpected error opening resource cache file for download", e);
                return null;
            } catch (final MalformedURLException e) {
                LOG.debug("invalid resource uri: {}", resource.getUri(), e);
                return null;
            } catch (final IOException e) {
                LOG.debug("IOException thrown while downloading {}", resource.getUri(), e);
                return null;
            } finally {
                IOUtils.closeQuietly(conn);
                IOUtils.closeQuietly(in);
                IOUtils.closeQuietly(out);

                // delete invalid downloads
                if (!success) {
                    f.delete();
                }
            }

            // store the download
            if (success && f.exists()) {
                // create CachedFileResource record
                final CachedUriResource cachedResource = new CachedUriResource(resource.getCourseId(), resource.getUri());
                cachedResource.setSize(size);
                cachedResource.setPath(f.getPath());
                cachedResource.setExpires(expires);
                cachedResource.setLastModified(lastModified);
                cachedResource.setLastAccessed();
                this.dao.replace(cachedResource);

                // return the File object
                return f;
            }
        }

        return null;
    }

    private File downloadEcvResource(final Resource resource, final boolean thumb) {
        // short-circuit if this isn't a valid resource
        if (!this.isValidEcvResource(resource)) {
            return null;
        }

        synchronized (getLock(this.downloadLocks, new Key(resource))) {
            // get File object
            final File f = this.getFileObject(resource, thumb ? "thumbnail" : "video");
            if (f == null) {
                return null;
            }

            // try downloading the file
            OutputStream out = null;
            long size = -1;
            MessageDigest digest = null;
            try {
                out = new FileOutputStream(f);

                // download the resource
                size = this.api.downloadEcvResource(resource, thumb, out);
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
                if (size < 0) {
                    f.delete();
                }
            }

            if (f.exists()) {
                // create CachedFileResource record
                final CachedEcvResource cachedResource =
                        new CachedEcvResource(resource.getCourseId(), resource.getVideoId(), thumb);
                cachedResource.setPath(f.getPath());
                cachedResource.setSize(size);
                cachedResource.setLastAccessed();
                this.dao.replace(cachedResource);

                // return the File object
                return f;
            }
        }

        return null;
    }

    private File dir() {
        return this.context.getExternalFilesDir("resources");
    }

    private File cacheDir() {
        return new File(this.context.getExternalCacheDir(), "resources");
    }

    private File getFileObject(final Resource resource, final String type) {
        // find/create the directory for the specified resource
        File dir;
        if (this.isValidFileResource(resource) || this.isValidEcvResource(resource)) {
            dir = dir();
        } else if (this.isDownloadableUriResource(resource)) {
            dir = cacheDir();
        } else {
            return null;
        }
        dir = new File(dir, Long.toString(resource.getCourseId()));
        dir = new File(dir, resource.getResourceType());
        if (type != null) {
            dir = new File(dir, type);
        }
        dir.mkdirs();

        // generate the File object based on resource type
        if (this.isValidFileResource(resource)) {
            return new File(dir, resource.getResourceSha1().toLowerCase(Locale.US));
        } else if (this.isValidEcvResource(resource)) {
            return new File(dir, Long.toString(resource.getVideoId()));
        } else if (this.isDownloadableUriResource(resource)) {
            for (int i = 0; i < 10; i++) {
                final byte[] buf = new byte[16];
                NAME_RNG.nextBytes(buf);
                final File f = new File(dir, StringUtils.bytesToHex(buf));
                try {
                    if (f.createNewFile()) {
                        return f;
                    }
                } catch (final IOException ignored) {
                }
            }
        }

        return null;
    }

    private boolean isDownloadableUriResource(final Resource resource) {
        return resource != null && resource.isUri() && resource.getProvider() == PROVIDER_NONE;
    }

    private boolean isValidFileResource(final Resource resource) {
        return resource != null && resource.isFile() && resource.getResourceSha1() != null;
    }

    private boolean isValidEcvResource(final Resource resource) {
        return resource != null && resource.isEcv() && resource.getVideoId() != INVALID_VIDEO;
    }

    private Resource resolveResource(final long courseId, final String resourceId) {
        assertNotOnUiThread();

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
            manifest = this.manifestManager.downloadManifest(courseId, null);
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
