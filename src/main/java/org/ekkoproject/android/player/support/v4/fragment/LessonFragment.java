package org.ekkoproject.android.player.support.v4.fragment;

import static org.ekkoproject.android.player.fragment.Constants.ARG_CONTENTID;

import android.annotation.TargetApi;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.viewpagerindicator.PageIndicator;

import org.ekkoproject.android.player.R;
import org.ekkoproject.android.player.adapter.ManifestLessonMediaPagerAdapter;
import org.ekkoproject.android.player.adapter.ManifestLessonTextPagerAdapter;
import org.ekkoproject.android.player.model.Lesson;
import org.ekkoproject.android.player.model.Manifest;
import org.ekkoproject.android.player.services.ProgressManager;

import java.util.Set;

public class LessonFragment extends AbstractManifestAndProgressAwareFragment implements View.OnClickListener {
    private static final String ARG_PAGERSTATE = LessonFragment.class.getName() + ".ARG_PAGERSTATE";
    private static final String ARG_MEDIAPAGERSTATE = LessonFragment.class.getName() + ".ARG_MEDIAPAGERSTATE";
    private static final String ARG_TEXTPAGERSTATE = LessonFragment.class.getName() + ".ARG_TEXTPAGERSTATE";
    private String lessonId = null;

    private ViewPager mediaPager = null;
    private PageIndicator mediaPagerIndicator = null;
    private ViewPager textPager = null;
    private TextView title = null;
    private ProgressBar progressBar = null;
    private View nextButton = null;
    private View prevButton = null;

    private boolean needsRestore = false;
    private Bundle pagerState = new Bundle();

    public static LessonFragment newInstance(final String guid, final long courseId, final String lessonId) {
        final LessonFragment fragment = new LessonFragment();

        // handle arguments
        final Bundle args = buildArgs(guid, courseId);
        args.putString(ARG_CONTENTID, lessonId);
        fragment.setArguments(args);

        return fragment;
    }

    /** BEGIN lifecycle */

    @Override
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR1)
    public void onCreate(final Bundle savedState) {
        super.onCreate(savedState);

        // process arguments
        final Bundle args = getArguments();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR1) {
            this.lessonId = args.getString(ARG_CONTENTID, null);
        } else {
            this.lessonId = args.getString(ARG_CONTENTID);
        }

        if (savedState != null) {
            if (savedState.containsKey(ARG_PAGERSTATE)) {
                this.pagerState = savedState.getBundle(ARG_PAGERSTATE);
            }
        }
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_lesson, container, false);
    }

    @Override
    public void onActivityCreated(final Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        this.needsRestore = true;
        this.findViews();
        this.setupMediaPager();
        this.setupTextPagerAdapter();
        this.setupNavButtons();
    }

    @Override
    public void onClick(final View v) {
        final Object listener = this.getPotentialListener();
        if (listener instanceof Listener) {
            switch (v.getId()) {
            case R.id.nextButton:
                ((Listener) listener).onNavigateNext();
                return;
            case R.id.prevButton:
                ((Listener) listener).onNavigatePrevious();
                return;
            }
        }
    }

    @Override
    protected void onManifestUpdate(Manifest manifest) {
        super.onManifestUpdate(manifest);
        this.updateMeta(manifest);
        this.updateManifestAdapters(manifest, this.mediaPager, this.textPager);
        this.updateProgressBar(manifest, this.getProgress());

        // do we need to restore pager state?
        if (this.needsRestore) {
            this.restorePagerState();
            this.needsRestore = false;
        }
    }

    @Override
    protected void onProgressUpdate(final Set<String> progress) {
        super.onProgressUpdate(progress);
        this.updateProgressBar(this.getManifest(), progress);
    }

    @Override
    public void onStop() {
        super.onStop();
        this.savePagerState();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        this.clearViews();
    }

    @Override
    public void onSaveInstanceState(final Bundle outState) {
        super.onSaveInstanceState(outState);

        this.savePagerState();
        outState.putBundle(ARG_PAGERSTATE, this.pagerState);
    }

    /** END lifecycle */

    public String getLessonId() {
        return this.lessonId;
    }

    private void findViews() {
        this.title = findView(TextView.class, R.id.title);
        this.progressBar = findView(ProgressBar.class, R.id.progress);
        this.mediaPager = findView(ViewPager.class, R.id.media);
        this.mediaPagerIndicator = findView(PageIndicator.class, R.id.media_indicator);
        this.textPager = findView(ViewPager.class, R.id.text);
        this.nextButton = findView(View.class, R.id.nextButton);
        this.prevButton = findView(View.class, R.id.prevButton);
    }

    private void clearViews() {
        this.title = null;
        this.progressBar = null;
        this.mediaPager = null;
        this.mediaPagerIndicator = null;
        this.textPager = null;
        this.nextButton = null;
        this.prevButton = null;
    }

    private void updateMeta(final Manifest manifest) {
        if (this.title != null) {
            this.title.setText(null);
            final Lesson lesson = manifest.getLesson(this.lessonId);
            if (lesson != null) {
                this.title.setText(lesson.getTitle());
            }
        }
    }

    private void updateProgressBar(final Manifest manifest, final Set<String> progress) {
        if (this.progressBar != null) {
            // retrieve the progress
            final Lesson lesson = manifest != null ? manifest.getLesson(this.lessonId) : null;
            final Pair<Integer, Integer> rawProgress = ProgressManager.getLessonProgress(this.getCourseId(), lesson,
                    progress);

            // update the progress bar
            this.progressBar.setMax(rawProgress.second);
            this.progressBar.setProgress(rawProgress.first);
        }
    }

    private void setupNavButtons() {
        if (this.nextButton != null) {
            this.nextButton.setOnClickListener(this);
        }
        if (this.prevButton != null) {
            this.prevButton.setOnClickListener(this);
        }
    }

    private void setupMediaPager() {
        if (this.mediaPager != null) {
            // attach the PagerAdapter
            this.mediaPager.setAdapter(
                    new ManifestLessonMediaPagerAdapter(getChildFragmentManager(), getGuid(), this.lessonId));

            // attach to the PageIndicator
            if (this.mediaPagerIndicator != null) {
                this.mediaPagerIndicator.setViewPager(this.mediaPager);
            }
        }
    }

    private void setupTextPagerAdapter() {
        if (this.textPager != null) {
            this.textPager.setAdapter(
                    new ManifestLessonTextPagerAdapter(getChildFragmentManager(), getGuid(), this.lessonId));
        }
    }

    private void savePagerState() {
        if (this.textPager != null) {
            this.pagerState.putParcelable(ARG_TEXTPAGERSTATE, this.textPager.onSaveInstanceState());
        }
        if (this.mediaPager != null) {
            this.pagerState.putParcelable(ARG_MEDIAPAGERSTATE, this.mediaPager.onSaveInstanceState());
        }
    }

    private void restorePagerState() {
        if (this.textPager != null) {
            this.textPager.onRestoreInstanceState(this.pagerState.getParcelable(ARG_TEXTPAGERSTATE));
        }
        if (this.mediaPager != null) {
            this.mediaPager.onRestoreInstanceState(this.pagerState.getParcelable(ARG_MEDIAPAGERSTATE));
        }
    }

    public interface Listener {
        void onNavigatePrevious();

        void onNavigateNext();
    }
}
