package org.ekkoproject.android.player.services;

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
import android.util.Xml;

public final class ManifestManager {
    private static final Logger LOG = LoggerFactory.getLogger(ManifestManager.class);

    private static final Map<Long, Manifest> MANIFESTS = new HashMap<Long, Manifest>();
    private static final Map<Long, Object> LOADINGLOCKS = new HashMap<Long, Object>();

    private static final String[] PROJECTION_MANIFEST = new String[] { Contract.Course.COLUMN_NAME_MANIFEST_FILE,
            Contract.Course.COLUMN_NAME_MANIFEST_VERSION };

    public static final Manifest getManifest(final Context context, final long courseId) {
        // check to see if the manifest has been loaded already
        synchronized (MANIFESTS) {
            if (MANIFESTS.containsKey(courseId)) {
                return MANIFESTS.get(courseId);
            }
        }

        // load the manifest
        return loadManifest(context, courseId);
    }

    private static final Manifest loadManifest(final Context context, final long courseId) {
        return loadManifest(context, courseId, true);
    }

    private static final Manifest loadManifest(final Context context, final long courseId, final boolean force) {
        // lock this manifest for loading
        synchronized (getLock(LOADINGLOCKS, courseId)) {
            // short-circuit if the manifest has been loaded and we aren't force
            // reloading it
            if (!force) {
                synchronized (MANIFESTS) {
                    if (MANIFESTS.containsKey(courseId)) {
                        return MANIFESTS.get(courseId);
                    }
                }
            }

            final EkkoDao dao = new EkkoDao(context);
            try {
                // fetch the course object, we don't care about resources
                final Course course = dao.findCourse(courseId, false);

                // short-circuit if we don't have a valid course
                if (course == null) {
                    synchronized (MANIFESTS) {
                        MANIFESTS.put(courseId, null);
                    }
                    return null;
                }

                // load the manifest from disk if it exists
                final String manifestName = course.getManifestFile();
                if (manifestName != null) {
                    try {
                        // parse the manifest
                        final Manifest manifest = parseManifestFile(context, manifestName);

                        // store and return the manifest
                        synchronized (MANIFESTS) {
                            MANIFESTS.put(courseId, manifest);
                        }
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
                return downloadManifest(context, dao, courseId);
            } finally {
                dao.close();
            }
        }
    }

    private static final Manifest parseManifestFile(final Context context, final String manifestFile)
            throws XmlPullParserException, IOException {
        InputStream in = null;
        try {
            // parse the manifest
            in = context.openFileInput(manifestFile);
            final XmlPullParser parser = Xml.newPullParser();
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, true);
            parser.setInput(in, "UTF-8");
            parser.nextTag();
            return Manifest.fromXml(parser);
        } finally {
            IOUtils.closeQuietly(in);
        }
    }

    public static final Manifest downloadManifest(final Context context, final long courseId) {
        final EkkoDao dao = new EkkoDao(context);
        try {
            // trigger the actual download
            return downloadManifest(context, dao, courseId);
        } finally {
            dao.close();
        }
    }

    private static final Manifest downloadManifest(final Context context, final EkkoDao dao, final long courseId) {
        // lock this manifest for downloading
        synchronized (getLock(LOADINGLOCKS, courseId)) {
            // fetch the course object, we don't care about resources.
            // we fetch the Course instead of passing it as a parameter to
            // ensure it's fresh
            final Course course = dao.findCourse(courseId, false);

            // short-circuit if we don't have a valid course
            if (course == null) {
                synchronized (MANIFESTS) {
                    MANIFESTS.put(courseId, null);
                }
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
                out = context.openFileOutput(newName, 0);
                new EkkoHubApi(context).streamManifest(course.getId(), out);
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
                manifest = parseManifestFile(context, newName);
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
                dao.update(course, PROJECTION_MANIFEST);

                // store the manifest
                synchronized (MANIFESTS) {
                    MANIFESTS.put(courseId, manifest);
                }

                // TODO broadcast updated manifest

                // delete the old manifest
                if (oldName != null) {
                    context.deleteFile(oldName);
                }
            }

            // return the newly parsed manifest
            return manifest;
        }
    }

    private static final <T> Object getLock(final Map<? super T, Object> locks, final T key) {
        synchronized (locks) {
            if (!locks.containsKey(key)) {
                locks.put(key, new Object());
            }
            return locks.get(key);
        }
    }
}
