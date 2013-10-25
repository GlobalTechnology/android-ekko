package org.ekkoproject.android.player.support.v4.fragment;

import static org.ekkoproject.android.player.Constants.INVALID_COURSE;
import static org.ekkoproject.android.player.fragment.Constants.ARG_COURSEID;
import static org.ekkoproject.android.player.tasks.EnrollmentRunnable.ENROLL;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.Loader;
import android.text.Html;

import org.ccci.gto.android.common.support.v4.app.SimpleLoaderCallbacks;
import org.ekkoproject.android.player.api.EkkoHubApi;
import org.ekkoproject.android.player.model.Course;
import org.ekkoproject.android.player.support.v4.content.CourseLoader;
import org.ekkoproject.android.player.tasks.EnrollmentRunnable;

public class NotEnrolledDialogFragment extends DialogFragment {
    private static final int LOADER_COURSE = 1;

    private EkkoHubApi api;

    private long courseId = INVALID_COURSE;

    protected static Bundle buildArgs(final long courseId) {
        final Bundle args = new Bundle();
        args.putLong(ARG_COURSEID, courseId);
        return args;
    }

    public static NotEnrolledDialogFragment newInstance(final long courseId) {
        final NotEnrolledDialogFragment fragment = new NotEnrolledDialogFragment();
        fragment.setArguments(buildArgs(courseId));
        return fragment;
    }

    /* BEGIN lifecycle */

    @Override
    public void onAttach(final Activity activity) {
        super.onAttach(activity);
        this.api = EkkoHubApi.getInstance(activity);
    }

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // process arguments
        this.courseId = getArguments().getLong(ARG_COURSEID, INVALID_COURSE);
    }

    @Override
    public void onActivityCreated(final Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        this.startLoaders();
    }

    @Override
    public Dialog onCreateDialog(final Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Enroll in course?").setMessage("(no description)")
                .setPositiveButton("Enroll", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(final DialogInterface dialog, final int which) {
                        api.async(new EnrollmentRunnable(getActivity(), ENROLL, courseId));
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
        this.getLoaderManager().initLoader(LOADER_COURSE, null, new CourseLoaderCallbacks()).startLoading();
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
