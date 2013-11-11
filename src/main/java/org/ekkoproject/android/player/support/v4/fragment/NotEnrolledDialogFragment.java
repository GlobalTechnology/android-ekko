package org.ekkoproject.android.player.support.v4.fragment;

import static org.ekkoproject.android.player.Constants.GUID_GUEST;
import static org.ekkoproject.android.player.Constants.INVALID_COURSE;
import static org.ekkoproject.android.player.fragment.Constants.ARG_COURSEID;
import static org.ekkoproject.android.player.fragment.Constants.ARG_GUID;
import static org.ekkoproject.android.player.tasks.EnrollmentRunnable.ENROLL;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.text.Html;

import org.ccci.gto.android.common.support.v4.app.SimpleLoaderCallbacks;
import org.ccci.gto.android.common.support.v4.fragment.AbstractDialogFragment;
import org.ekkoproject.android.player.OnNavigationListener;
import org.ekkoproject.android.player.model.Course;
import org.ekkoproject.android.player.support.v4.content.CourseLoader;
import org.ekkoproject.android.player.tasks.EnrollmentRunnable;

public class NotEnrolledDialogFragment extends AbstractDialogFragment {
    private static final int LOADER_COURSE = 1;

    private long courseId = INVALID_COURSE;
    private String guid = GUID_GUEST;

    protected static Bundle buildArgs(final long courseId, final String guid) {
        final Bundle args = new Bundle();
        args.putLong(ARG_COURSEID, courseId);
        args.putString(ARG_GUID, guid);
        return args;
    }

    public static NotEnrolledDialogFragment newInstance(final long courseId, final String guid) {
        final NotEnrolledDialogFragment fragment = new NotEnrolledDialogFragment();
        fragment.setArguments(buildArgs(courseId, guid));
        return fragment;
    }

    /* BEGIN lifecycle */

    @Override
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR1)
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // process arguments
        final Bundle args = getArguments();
        this.courseId = args.getLong(ARG_COURSEID, INVALID_COURSE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR1) {
            this.guid = args.getString(ARG_GUID, GUID_GUEST);
        } else {
            this.guid = args.getString(ARG_GUID);
        }
    }

    @Override
    public void onActivityCreated(final Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        this.startLoaders();
    }

    @Override
    public Dialog onCreateDialog(final Bundle savedInstanceState) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Enroll in course?").setMessage("(no description)")
                .setPositiveButton("Enroll", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(final DialogInterface dialog, final int which) {
                        final EnrollmentRunnable task = new EnrollmentRunnable(getActivity(), ENROLL, guid, courseId);
                        task.setOnNavigationListener(getListener(OnNavigationListener.class));
                        task.schedule();
                    }
                }).setNegativeButton("Cancel", null);
        return builder.create();
    }

    private void onUpdateCourse(final Course course) {
        if (course == null) {
            this.dismiss();
        } else {
            this.updateCourse(course);
        }
    }

    /* END lifecycle */

    private void startLoaders() {
        final LoaderManager manager = this.getLoaderManager();
        manager.initLoader(LOADER_COURSE, null, new CourseLoaderCallbacks()).startLoading();
    }

    private void updateCourse(final Course course) {
        final Dialog dialog = this.getDialog();
        if (dialog instanceof AlertDialog) {
            dialog.setTitle(course.getTitle());
            ((AlertDialog) dialog).setMessage(Html.fromHtml(course.getDescription()));
        }
    }

    private class CourseLoaderCallbacks extends SimpleLoaderCallbacks<Course> {
        @Override
        public Loader<Course> onCreateLoader(final int id, final Bundle args) {
            switch (id) {
                case LOADER_COURSE:
                    return new CourseLoader(getActivity(), courseId);
                default:
                    return null;
            }
        }

        @Override
        public void onLoadFinished(final Loader loader, final Course course) {
            switch (loader.getId()) {
                case LOADER_COURSE:
                    onUpdateCourse(course);
                    break;
            }
        }
    }
}
