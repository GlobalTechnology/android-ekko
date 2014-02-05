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
import android.net.Uri;
import android.os.Build;
import android.support.v4.util.LruCache;

import com.google.common.base.Objects;
import com.google.common.io.BaseEncoding;

import org.ccci.gto.android.common.api.ApiSocketException;
import org.ccci.gto.android.common.api.InvalidSessionApiException;
import org.ccci.gto.android.common.model.Dimension;
import org.ccci.gto.android.common.util.BitmapUtils;
import org.ccci.gto.android.common.util.IOUtils;
import org.ekkoproject.android.player.api.ArclightApi;
import org.ekkoproject.android.player.api.EkkoHubApi;
import org.ekkoproject.android.player.db.EkkoDao;
import org.ekkoproject.android.player.model.CachedArclightResource;
import org.ekkoproject.android.player.model.CachedEcvResource;
import org.ekkoproject.android.player.model.CachedFileResource;
import org.ekkoproject.android.player.model.CachedResource;
import org.ekkoproject.android.player.model.CachedUriResource;
import org.ekkoproject.android.player.model.Course;
import org.ekkoproject.android.player.model.Manifest;
import org.ekkoproject.android.player.model.Resource;
import org.ekkoproject.android.player.util.MultiKeyLruCache;
import org.ekkoproject.android.player.util.WeakMultiKeyLruCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
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
    public enum StreamType {HLS, MP4, SINGLE_HLS};

    private static final Logger LOG = LoggerFactory.getLogger(ResourceManager.class);

    // boundaries for Bitmap cache size
    private static final int CACHE_MIN_SIZE = 0;
    private static final int CACHE_MAX_SIZE = (int) (Runtime.getRuntime().maxMemory() / 1024 / 2);

    public static final int DEFAULT_MAX_BITMAP_HEIGHT = 2048;
    public static final int DEFAULT_MAX_BITMAP_WIDTH = 2048;
    public static final int DEFAULT_MIN_BITMAP_WIDTH = 50;
    public static final int DEFAULT_MIN_BITMAP_HEIGHT = 50;

    private static class Key {
        private final long courseId;
        private final String sha1;
        private final String uri;
        private final int provider;
        private final long videoId;
        private final String refId;
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

            // arclight resource attributes
            this.refId = resource.isArclight() ? resource.getRefId() : null;
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) {
                return true;
            }
            if (o instanceof Key) {
                final Key key = (Key) o;
                return this.courseId == key.courseId && Objects.equal(this.sha1, key.sha1) &&
                        Objects.equal(this.uri, key.uri) && this.provider == key.provider &&
                        this.videoId == key.videoId && Objects.equal(this.refId, key.refId) && this.thumb == key.thumb;
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
            hash = hash * 31 + (this.refId != null ? this.refId.hashCode() : 0);
            return hash;
        }
    }

    public static class BitmapOptions {
        private final Dimension mSize;
        private final Dimension mMaxSize;

        public BitmapOptions(final int width, final int height) {
            this(new Dimension(width, height), null);
        }

        public BitmapOptions(final Dimension size, final Dimension maxSize) {
            mSize = size;
            mMaxSize = maxSize;
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) {
                return true;
            }
            if (o instanceof BitmapOptions && getClass().equals(o.getClass())) {
                final BitmapOptions that = (BitmapOptions) o;
                return Objects.equal(this.mSize, that.mSize) && Objects.equal(this.mMaxSize, that.mMaxSize);
            }

            return false;
        }

        @Override
        public int hashCode() {
            int hash = 0;
            hash = 31 * hash + (mSize != null ? mSize.hashCode() : 0);
            hash = 31 * hash + (mMaxSize != null ? mMaxSize.hashCode() : 0);
            return hash;
        }
    }

    private static final class BitmapKey extends Key {
        private final BitmapOptions mOpts;

        public BitmapKey(final Resource resource, final BitmapOptions opts) {
            super(resource);
            assert opts != null;
            mOpts = opts;
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) {
                return true;
            }
            if (o instanceof BitmapKey) {
                final BitmapKey key = (BitmapKey) o;
                return super.equals(o) && Objects.equal(mOpts, key.mOpts);
            }

            return false;
        }

        @Override
        public int hashCode() {
            int hash = super.hashCode();
            hash = hash * 31 + (mOpts != null ? mOpts.hashCode() : 0);
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
    private final ArclightApi arclightApi;
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
        this.arclightApi = ArclightApi.getInstance();
        this.dao = EkkoDao.getInstance(ctx);
        this.manifestManager = ManifestManager.getInstance(this.context);

        this.bitmaps = new WeakMultiKeyLruCache<BitmapKey, Bitmap>(CACHE_MAX_SIZE) {
            @Override
            @TargetApi(Build.VERSION_CODES.KITKAT)
            protected int sizeOf(final BitmapKey key, final Bitmap bitmap) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    return bitmap.getAllocationByteCount();
                } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR1) {
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

    public Resource resolveResource(final long courseId, final String resourceId) {
        assertNotOnUiThread();

        // check manifest for resource, we disable downloading of the manifest for now
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

        // maybe try downloading the manifest (only when we don't already have a manifest)
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

    public Bitmap getBitmap(final long courseId, final String resourceId, final BitmapOptions opts) {
        return this.getBitmap(this.resolveResource(courseId, resourceId), opts);
    }

    private Bitmap getBitmap(final Resource resource, final BitmapOptions opts) {
        assertNotOnUiThread();
        assert opts != null;

        // short-circuit if we don't have a valid resource
        if (resource == null) {
            return null;
        }

        // look for a cached bitmap
        final BitmapKey key = new BitmapKey(resource, opts);
        final Bitmap img = this.bitmaps.get(key);
        if (img != null) {
            return img;
        }

        // load the bitmap
        return loadBitmap(resource, opts);
    }

    private Bitmap loadBitmap(final Resource resource, final BitmapOptions opts) {
        final File f = this.getFile(resource, FLAG_TYPE_IMAGE);
        if (f == null) {
            return null;
        }

        final BitmapKey key = new BitmapKey(resource, opts);
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

            // calculate how much to scale the image by. Make sure output size is larger than requested size,
            // but smaller than max width & max height
            final int scale = BitmapUtils.calcScale(opts.mSize, new Dimension(meta.outWidth, meta.outHeight),
                                                    opts.mMaxSize);

            // check to see if there is a Bitmap at this scale already
            final BitmapKey scaledKey =
                    new BitmapKey(resource, new BitmapOptions(meta.outWidth / scale, meta.outHeight / scale));
            synchronized (getLock(this.bitmapLocks, scaledKey)) {
                bitmap = this.bitmaps.get(scaledKey);

                // we don't have a scaled bitmap, so load one
                if (bitmap == null) {
                    final BitmapFactory.Options loadOpts = new BitmapFactory.Options();
                    loadOpts.inSampleSize = scale;

                    int maxSize = this.bitmaps.maxSize();
                    while (true) {
                        try {
                            bitmap = BitmapFactory.decodeFile(f.getPath(), loadOpts);
                            break;
                        } catch (final OutOfMemoryError oom) {
                            maxSize = maxSize > 0 ? maxSize / 2 : -1;
                            if (maxSize >= CACHE_MIN_SIZE) {
                                bitmaps.trimToSize(maxSize);
                                continue;
                            }

                            throw oom;
                        }
                    }

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
                    ((MultiKeyLruCache<BitmapKey, Bitmap>) this.bitmaps).putMulti(key, bitmap);
                } else {
                    this.bitmaps.put(key, bitmap);
                }
            }

            // return the bitmap
            return bitmap;
        }
    }

    public File getFile(final Resource resource) {
        return this.getFile(resource, 0);
    }

    public File getFile(final Resource resource, final int flags) {
        assertNotOnUiThread();

        // only process valid resource types
        if (isValidArclightResource(resource) || isValidEcvResource(resource) || isValidFileResource(resource) ||
                isDownloadableUriResource(resource)) {
            synchronized (getLock(this.downloadLocks, new Key(resource, isThumbResource(resource, flags)))) {
                // look for a cached resource
                final CachedResource cachedResource = this.findCachedResource(resource, flags);
                if (cachedResource != null && cachedResource.getPath() != null) {
                    final File f = new File(cachedResource.getPath());
                    if (f.exists()) {
                        return f;
                    }
                }

                // don't attempt to download this file
                if ((flags & FLAG_DONT_DOWNLOAD) == FLAG_DONT_DOWNLOAD) {
                    return null;
                }

                // download the resource
                return this.downloadResource(resource, flags);
            }
        }

        // default to null
        return null;
    }

    private File downloadResource(final Resource resource, final int flags) {
        // only process valid resource types
        if (isValidArclightResource(resource) || isValidEcvResource(resource) || isValidFileResource(resource)) {
            final boolean thumb = isThumbResource(resource, flags);
            synchronized (getLock(this.downloadLocks, new Key(resource, thumb))) {
                // get File object
                final File f = this.getFileObject(resource, thumb ? "thumbnail" : null);
                if (f == null) {
                    return null;
                }

                // try downloading the file
                OutputStream out = null;
                long size = -1;
                MessageDigest digest = null;
                try {
                    out = new FileOutputStream(f);

                    // we want to digest File Resources while downloading
                    if (resource.isFile()) {
                        try {
                            digest = MessageDigest.getInstance("SHA-1");
                            out = new DigestOutputStream(out, digest);
                        } catch (final NoSuchAlgorithmException e) {
                            digest = null;
                        }
                    }

                    // download the resource
                    switch (resource.getType()) {
                        case FILE:
                        case ECV:
                            size = this.api.downloadResource(resource, thumb, out);
                            break;
                        case ARCLIGHT:
                            size = this.arclightApi.downloadAsset(resource.getRefId(), thumb, out);
                            break;
                    }
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

                    final String sha1 =
                            digest != null ? BaseEncoding.base16().lowerCase().encode(digest.digest()) : null;
                    if (size == -1 || (resource.getResourceSize() != -1 && size != resource.getResourceSize()) ||
                            (sha1 != null && !sha1.equals(resource.getResourceSha1()))) {
                        f.delete();
                    }
                }

                if (f.exists()) {
                    // create CachedResource record
                    final CachedResource cachedResource = this.createCachedResource(resource, flags);
                    if (cachedResource != null) {
                        cachedResource.setSize(size);
                        cachedResource.setPath(f.getPath());
                        cachedResource.setLastAccessed();
                        this.dao.replace(cachedResource);
                    }

                    // return the File object
                    return f;
                }
            }
        } else if (isDownloadableUriResource(resource)) {
            return this.downloadUriResource(resource, flags);
        }

        return null;
    }

    private boolean isThumbResource(final Resource resource, final int flags) {
        return ((flags & FLAG_TYPE_IMAGE) == FLAG_TYPE_IMAGE) && (resource.isArclight() || resource.isEcv());
    }

    private CachedResource createCachedResource(final Resource resource, final int flags) {
        final boolean thumb = isThumbResource(resource, flags);
        switch (resource.getType()) {
            case FILE:
                return new CachedFileResource(resource.getCourseId(), resource.getResourceSha1());
            case ECV:
                return new CachedEcvResource(resource.getCourseId(), resource.getVideoId(), thumb);
            case ARCLIGHT:
                return new CachedArclightResource(resource.getCourseId(), resource.getRefId(), thumb);
            case URI:
                return new CachedUriResource(resource.getCourseId(), resource.getUri());
            default:
                return null;
        }
    }

    private CachedResource findCachedResource(final Resource resource, final int flags) {
        final boolean thumb = isThumbResource(resource, flags);
        switch (resource.getType()) {
            case ARCLIGHT:
                return this.dao.find(CachedArclightResource.class, resource.getCourseId(), resource.getRefId(), thumb);
            case ECV:
                return this.dao.find(CachedEcvResource.class, resource.getCourseId(), resource.getVideoId(), thumb);
            case FILE:
                return this.dao.find(CachedFileResource.class, resource.getCourseId(), resource.getResourceSha1());
            case URI:
                return this.dao.find(CachedUriResource.class, resource.getCourseId(), resource.getUri());
            default:
                return null;
        }
    }

    public Uri getStreamUri(final Resource resource) {
        assertNotOnUiThread();

        // don't attempt streaming on android < ICS, it doesn't seem to support it
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            return null;
        }

        // determine stream type
        final StreamType type;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            // HLS dynamic stream selection is broke in 4.4
            // https://code.google.com/p/android/issues/detail?id=63346
            type = StreamType.SINGLE_HLS;
//        } else if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1) {
//            // multiple issues with HLS before 4.2
//            // http://www.jwplayer.com/blog/the-pain-of-live-streaming-on-android/
//            type = StreamType.MP4;
        } else if(Build.VERSION.SDK_INT < Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            // HLSv3 support was added in ICS, use MP4 for previous versions of Android
            // http://developer.android.com/guide/appendix/media-formats.html
            type = StreamType.MP4;
        } else {
            // we want to default to regular HLS
            type = StreamType.HLS;
        }

        // switch based on resource type
        try {
            if (this.isValidEcvResource(resource)) {
                return this.api.getEcvStreamUri(resource, type);
            } else if (this.isValidArclightResource(resource)) {
                return this.arclightApi.getAssetStreamUri(resource.getRefId());
            }
        } catch (final Exception ignored) {
        }

        return null;
    }

    private File downloadUriResource(final Resource resource, final int flags) {
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

    private File dir() {
        final File external =  this.context.getExternalFilesDir("resources");
        if(external != null) {
            return external;
        }

        final File internal = this.context.getFilesDir();
        if(internal != null) {
            return new File(internal, "resources");
        }

        return null;
    }

    private File cacheDir() {
        return new File(this.context.getExternalCacheDir(), "resources");
    }

    private File randomFile(final File dir, final int len) {
        for (int i = 0; i < 10; i++) {
            final byte[] buf = new byte[len];
            NAME_RNG.nextBytes(buf);
            final File f = new File(dir, BaseEncoding.base16().lowerCase().encode(buf));
            try {
                if (f.createNewFile()) {
                    return f;
                }
            } catch (final IOException ignored) {
            }
        }

        return null;
    }

    private File getFileObject(final Resource resource, final String type) {
        // find/create the directory for the specified resource
        File dir;
        if (this.isValidFileResource(resource) || this.isValidEcvResource(resource) ||
                this.isValidArclightResource(resource)) {
            dir = dir();
        } else if (this.isDownloadableUriResource(resource)) {
            dir = cacheDir();
        } else {
            return null;
        }
        dir = new File(dir, Long.toString(resource.getCourseId()));
        dir = new File(dir, resource.getType().raw());
        if (type != null) {
            dir = new File(dir, type);
        }
        dir.mkdirs();

        // generate the File object based on resource type
        if (resource.isFile()) {
            return new File(dir, resource.getResourceSha1().toLowerCase(Locale.US));
        } else if (resource.isArclight()) {
            return new File(dir,
                            BaseEncoding.base32().omitPadding().lowerCase().encode(resource.getRefId().getBytes()));
        } else if (resource.isEcv()) {
            return new File(dir, Long.toString(resource.getVideoId()));
        } else if (resource.isUri()) {
            return randomFile(dir, 16);
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

    private boolean isValidArclightResource(final Resource resource) {
        return resource != null && resource.isArclight() && resource.getRefId() != null;
    }
}
