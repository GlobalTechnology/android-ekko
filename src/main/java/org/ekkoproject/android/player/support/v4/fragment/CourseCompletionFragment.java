package org.ekkoproject.android.player.support.v4.fragment;

import android.os.Bundle;
import android.text.Html;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.ekkoproject.android.player.R;
import org.ekkoproject.android.player.model.Manifest;
import org.ekkoproject.android.player.services.ProgressManager;

import java.util.Set;

public class CourseCompletionFragment extends AbstractContentFragment implements View.OnClickListener {
    private TextView completionMessageView = null;
    private View courseListButton = null;

    public static CourseCompletionFragment newInstance(final String guid, final long courseId) {
        final CourseCompletionFragment fragment = new CourseCompletionFragment();

        // handle arguments
        fragment.setArguments(buildArgs(guid, courseId));

        return fragment;
    }

    /* BEGIN lifecycle */

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_course_completion, container, false);
    }

    @Override
    public void onActivityCreated(final Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        this.findViews();
        this.setupButtons();
    }

    @Override
    protected void onManifestUpdate(final Manifest manifest) {
        super.onManifestUpdate(manifest);
        this.updateCompletionMessage(manifest);
    }

    @Override
    public void onClick(final View v) {
        switch (v.getId()) {
        case R.id.course_list:
            getActivity().onBackPressed();
            break;
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        this.clearViews();
    }

    /* END lifecycle */

    private void findViews() {
        this.completionMessageView = findView(TextView.class, R.id.completionMessage);
        this.courseListButton = findView(View.class, R.id.course_list);
    }

    private void clearViews() {
        this.completionMessageView = null;
        this.courseListButton = null;
    }

    private void setupButtons() {
        if (this.courseListButton != null) {
            this.courseListButton.setOnClickListener(this);
        }
    }

    private void updateCompletionMessage(final Manifest manifest) {
        if (this.completionMessageView != null) {
            String message = null;
            String title = null;
            if (manifest != null) {
                message = manifest.getCompletionMessage();
                title = manifest.getTitle();
            }

            if (message == null || message.length() == 0) {
                this.completionMessageView
                        .setText(getResources().getString(R.string.course_completion_message_default, (String) title));
            } else {
                this.completionMessageView.setText(Html.fromHtml(message));
            }
        }
    }

    @Override
    protected Pair<Integer, Integer> getProgress(final Manifest manifest, final Set<String> progress) {
        return ProgressManager.getCourseProgress(manifest, progress);
    }
}
