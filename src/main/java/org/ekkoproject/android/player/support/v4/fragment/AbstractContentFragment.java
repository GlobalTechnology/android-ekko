package org.ekkoproject.android.player.support.v4.fragment;

import static org.ekkoproject.android.player.fragment.Constants.ARG_CONTENTID;

import android.annotation.TargetApi;
import android.os.Build;
import android.os.Bundle;
import android.util.Pair;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.ekkoproject.android.player.R;
import org.ekkoproject.android.player.model.CourseContent;
import org.ekkoproject.android.player.model.Manifest;

import java.util.Set;

public abstract class AbstractContentFragment extends AbstractManifestAndProgressAwareFragment {
    private String contentId = null;

    private TextView title = null;
    private ProgressBar progressBar = null;
    private View nextButton = null;
    private View prevButton = null;

    protected static Bundle buildArgs(final String guid, final long courseId, final String contentId) {
        final Bundle args = buildArgs(guid, courseId);
        args.putString(ARG_CONTENTID, contentId);
        return args;
    }

    /* BEGIN lifecycle */

    @Override
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR1)
    public void onCreate(final Bundle savedState) {
        super.onCreate(savedState);

        // process arguments
        final Bundle args = getArguments();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR1) {
            this.contentId = args.getString(ARG_CONTENTID, null);
        } else {
            this.contentId = args.getString(ARG_CONTENTID);
        }
    }

    @Override
    public void onActivityCreated(final Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        this.findViews();
        this.setupNavButtons();
    }

    @Override
    protected void onManifestUpdate(final Manifest manifest) {
        super.onManifestUpdate(manifest);
        this.updateMeta(manifest);
        this.updateProgressBar(manifest, this.getProgress());
    }

    @Override
    protected void onProgressUpdate(final Set<String> progress) {
        super.onProgressUpdate(progress);
        this.updateProgressBar(this.getManifest(), progress);
    }

    @Override
    public void onDestroyView() {
        this.clearViews();
        super.onDestroyView();
    }

    /* END lifecycle */

    public String getContentId() {
        return this.contentId;
    }

    private void findViews() {
        this.title = findView(TextView.class, R.id.title);
        this.progressBar = findView(ProgressBar.class, R.id.progress);
        this.nextButton = findView(View.class, R.id.nextButton);
        this.prevButton = findView(View.class, R.id.prevButton);
    }

    private void clearViews() {
        this.title = null;
        this.progressBar = null;
        this.nextButton = null;
        this.prevButton = null;
    }

    private void setupNavButtons() {
        final OnClickListener listener = new NavButtonsOnClickListener();

        if (this.nextButton != null) {
            this.nextButton.setOnClickListener(listener);
        }
        if (this.prevButton != null) {
            this.prevButton.setOnClickListener(listener);
        }
    }

    private void updateMeta(final Manifest manifest) {
        // update the title
        if (this.title != null && manifest != null) {
            final CourseContent content = manifest.getContent(this.contentId);
            this.title.setText(null);
            if (content != null) {
                this.title.setText(content.getTitle());
            }
        }
    }

    private void updateProgressBar(final Manifest manifest, final Set<String> rawProgress) {
        if (this.progressBar != null) {
            final Pair<Integer, Integer> progress = this.getProgress(manifest, rawProgress);
            if (progress != null) {
                this.progressBar.setMax(progress.second);
                this.progressBar.setProgress(progress.first);
            }
        }
    }

    protected abstract Pair<Integer, Integer> getProgress(final Manifest manifest, final Set<String> progress);

    private class NavButtonsOnClickListener implements View.OnClickListener {
        @Override
        public void onClick(final View v) {
            final Object listener = AbstractContentFragment.this.getPotentialListener();
            if (listener instanceof OnNavigateListener) {
                switch (v.getId()) {
                case R.id.nextButton:
                    ((OnNavigateListener) listener).onNavigateNext();
                    return;
                case R.id.prevButton:
                    ((OnNavigateListener) listener).onNavigatePrevious();
                    return;
                }
            }
        }
    }

    public interface OnNavigateListener {
        void onNavigateFirst();

        void onNavigatePrevious();

        void onNavigateNext();
    }
}
