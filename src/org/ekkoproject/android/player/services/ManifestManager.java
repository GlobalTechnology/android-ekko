package org.ekkoproject.android.player.services;

import static org.ekkoproject.android.player.Constants.EXTRA_COURSEID;
import static org.ekkoproject.android.player.util.ThreadUtils.assertNotOnUiThread;
import static org.ekkoproject.android.player.util.ThreadUtils.getLock;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

import org.ekkoproject.android.player.api.ApiSocketException;
import org.ekkoproject.android.player.api.EkkoHubApi;
import org.ekkoproject.android.player.api.InvalidSessionApiException;
import org.ekkoproject.android.player.db.Contract;
import org.ekkoproject.android.player.db.EkkoDao;
import org.ekkoproject.android.player.model.Course;
import org.ekkoproject.android.player.model.Manifest;
import org.ekkoproject.android.player.util.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Xml;

public final class ManifestManager {
    private static final Logger LOG = LoggerFactory.getLogger(ManifestManager.class);

    public static final int FLAG_FORCE_RELOAD = 1 << 0;
    public static final int FLAG_DONT_DOWNLOAD = 1 << 1;

    /** broadcast actions */
    public static final String ACTION_UPDATE_MANIFEST = "org.ekkoproject.android.player.services.ManifestManager.UPDATE_MANIFEST";

    private static final String[] PROJECTION_MANIFEST = new String[] { Contract.Course.COLUMN_NAME_MANIFEST_FILE,
            Contract.Course.COLUMN_NAME_MANIFEST_VERSION };

    private static ManifestManager instance = null;

    private final Context context;
    private final EkkoHubApi api;
    private final EkkoDao dao;

    private final Map<Long, Manifest> manifests = new HashMap<Long, Manifest>();
    private final Map<Long, Object> locks = new HashMap<Long, Object>();

    private ManifestManager(final Context ctx) {
        this.context = ctx.getApplicationContext();
        this.api = new EkkoHubApi(this.context);
        this.dao = new EkkoDao(this.context);
    }

    public static final ManifestManager getInstance(final Context context) {
        if (instance == null) {
            instance = new ManifestManager(context);
        }
        return instance;
    }

    private static void broadcastManifestUpdate(final Context context, final long courseId) {
        LocalBroadcastManager.getInstance(context).sendBroadcast(
                new Intent().setAction(ACTION_UPDATE_MANIFEST).putExtra(EXTRA_COURSEID, courseId));
    }

    public Manifest getManifest(final long courseId) {
        return this.getManifest(courseId, 0);
    }

    public Manifest getManifest(final long courseId, final int flags) {
        assertNotOnUiThread();

        // check to see if the manifest has been loaded already unless we are
        // forcing a reload
        if (!(FLAG_FORCE_RELOAD == (flags & FLAG_FORCE_RELOAD))) {
            synchronized (this.manifests) {
                if (this.manifests.containsKey(courseId)) {
                    return this.manifests.get(courseId);
                }
            }
        }

        // load the manifest
        return loadManifest(courseId, flags);
    }

    private Manifest loadManifest(final long courseId, final int flags) {
        // lock this manifest for loading
        synchronized (getLock(this.locks, courseId)) {
            // short-circuit if the manifest has been loaded and we aren't
            // forcing a reload
            if (!(FLAG_FORCE_RELOAD == (flags & FLAG_FORCE_RELOAD))) {
                synchronized (this.manifests) {
                    if (this.manifests.containsKey(courseId)) {
                        return this.manifests.get(courseId);
                    }
                }
            }

            // fetch the course object, we don't care about resources
            final Course course = this.dao.findCourse(courseId, false);

            // short-circuit if we don't have a valid course
            if (course == null) {
                storeManifest(courseId, null);
                return null;
            }

            // load the manifest from disk if it exists
            final String manifestName = course.getManifestFile();
            if (manifestName != null) {
                try {
                    // return a parsed manifest
                    final Manifest manifest = parseManifestFile(manifestName);
                    storeManifest(courseId, manifest);
                    return manifest;
                } catch (final FileNotFoundException e) {
                    // file is missing
                } catch (final XmlPullParserException e) {
                    // xml is invalid, delete the manifest and continue
                    context.deleteFile(manifestName);
                } catch (final IOException e) {
                    // error reading file, not sure what happened so do
                    // nothing
                    LOG.error("error reading manifest", e);
                    return null;
                }

                // we had an error loading the existing manifest, so reset
                // it on the course object
                course.setManifestFile(null);
                course.setManifestVersion(0);
                dao.update(course, PROJECTION_MANIFEST);
            }

            // manifest doesn't exist, try downloading it
            if (!(FLAG_DONT_DOWNLOAD == (flags & FLAG_DONT_DOWNLOAD))) {
                return downloadManifest(courseId, flags);
            }

            return null;
        }
    }

    private Manifest parseManifestFile(final String manifestFile) throws XmlPullParserException, IOException {
        InputStream in = null;
        try {
            // parse the manifest
            in = this.context.openFileInput(manifestFile);
            final XmlPullParser parser = Xml.newPullParser();
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, true);
            parser.setInput(in, "UTF-8");
            parser.nextTag();
            return Manifest.fromXml(parser);
        } finally {
            IOUtils.closeQuietly(in);
        }
    }

    public Manifest downloadManifest(final long courseId) {
        return this.downloadManifest(courseId, FLAG_FORCE_RELOAD);
    }

    public Manifest downloadManifest(final long courseId, final int flags) {
        assertNotOnUiThread();

        // lock this manifest for downloading
        synchronized (getLock(this.locks, courseId)) {
            // fetch the course object, we don't care about resources.
            // we fetch the Course instead of passing it as a parameter to
            // ensure it's fresh
            final Course course = this.dao.findCourse(courseId, false);

            // short-circuit if we don't have a valid course
            if (course == null) {
                storeManifest(courseId, null);
                return null;
            }

            // generate a new file name
            final String oldName = course.getManifestFile();
            String newName = null;
            int i = 0;
            while (newName == null || newName.equals(oldName)) {
                i++;
                newName = "manifest-" + Long.toString(courseId) + "-" + Integer.toString(i) + ".xml";
            }

            // download the manifest
            OutputStream out = null;
            try {
                out = this.context.openFileOutput(newName, 0);
                this.api.streamManifest(course.getId(), out);
            } catch (final FileNotFoundException e) {
                // not sure why this would happen
                return null;
            } catch (final ApiSocketException e) {
                // connection error
                return null;
            } catch (final InvalidSessionApiException e) {
                // invalid session
                // TODO: broadcast the need for a new session
                return null;
            } finally {
                IOUtils.closeQuietly(out);
            }

            // parse the downloaded manifest
            Manifest manifest = null;
            try {
                manifest = parseManifestFile(newName);
            } catch (final FileNotFoundException e) {
                // file is missing (this is odd)
                return null;
            } catch (final XmlPullParserException e) {
                // invalid xml
                return null;
            } catch (final IOException e) {
                // not sure why this happened
                LOG.error("unexpected error parsing downloaded manifest xml", e);
                return null;
            }

            // valid manifest, update necessary objects
            if (manifest != null) {
                // update the course object
                course.setManifestFile(newName);
                course.setManifestVersion(manifest.getVersion());
                this.dao.update(course, PROJECTION_MANIFEST);

                // store the manifest
                storeManifest(courseId, manifest);

                // delete the old manifest
                if (oldName != null) {
                    this.context.deleteFile(oldName);
                }
            }

            // return the newly parsed manifest
            return manifest;
        }
    }

    private void storeManifest(final long courseId, final Manifest manifest) {
        synchronized (this.manifests) {
            this.manifests.put(courseId, manifest);
        }
        broadcastManifestUpdate(this.context, courseId);
    }
}
