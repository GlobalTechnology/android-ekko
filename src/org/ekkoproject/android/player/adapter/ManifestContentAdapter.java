package org.ekkoproject.android.player.adapter;

import static org.ekkoproject.android.player.Constants.DEFAULT_LAYOUT;

import java.util.Collections;
import java.util.List;

import org.appdev.entity.CourseContent;
import org.appdev.entity.Lesson;
import org.appdev.entity.Quiz;
import org.ekkoproject.android.player.R;
import org.ekkoproject.android.player.model.Manifest;
import org.ekkoproject.android.player.services.CourseManager;

import android.content.Context;
import android.view.View;
import android.widget.TextView;

public class ManifestContentAdapter extends AbstractManifestAdapter<CourseContent> {
    private static final List<CourseContent> NO_CONTENT = Collections.emptyList();

    private static final int VIEW_TYPE_LESSON = 0;
    private static final int VIEW_TYPE_QUIZ = 1;

    private List<CourseContent> content = NO_CONTENT;

    private int lessonView = DEFAULT_LAYOUT;
    private int quizView = DEFAULT_LAYOUT;

    public ManifestContentAdapter(final Context context) {
        super(context);
    }

    public void setLessonView(final int layout) {
        this.lessonView = layout;
    }

    public void setQuizView(final int layout) {
        this.quizView = layout;
    }

    @Override
    protected void onNewManifest(final Manifest manifest) {
        super.onNewManifest(manifest);

        if (manifest != null) {
            this.content = manifest.getContent();
        } else {
            this.content = NO_CONTENT;
        }
    }

    @Override
    public int getCount() {
        return this.content.size();
    }

    @Override
    public CourseContent getItem(final int position) {
        return this.content.get(position);
    }

    @Override
    public long getItemId(final int position) {
        return CourseManager.convertId(this.getCourseId(), this.content.get(position).getId());
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public int getItemViewType(int position) {
        final CourseContent item = this.content.get(position);
        if (item instanceof Lesson) {
            return VIEW_TYPE_LESSON;
        } else if (item instanceof Quiz) {
            return VIEW_TYPE_QUIZ;
        }

        return IGNORE_ITEM_VIEW_TYPE;
    }

    @Override
    protected int getLayout(final int viewType) {
        switch (viewType) {
        case VIEW_TYPE_LESSON:
            return this.lessonView;
        case VIEW_TYPE_QUIZ:
            return this.quizView;
        default:
            return super.getLayout();
        }
    }

    @Override
    protected void bindView(final View view, final CourseContent object) {
        if (object instanceof Lesson) {
            this.bindLessonView(view, (Lesson) object);
        } else if (object instanceof Quiz) {
            this.bindQuizView(view, (Quiz) object);
        }
    }

    private void bindLessonView(final View view, final Lesson lesson) {
        View v = view.findViewById(R.id.title);
        if (v instanceof TextView) {
            ((TextView) v).setText(lesson.getLesson_title());
        }
    }

    private void bindQuizView(final View view, final Quiz lesson) {
        View v = view.findViewById(R.id.title);
        if (v instanceof TextView) {
            ((TextView) v).setText("Quiz");
        }
    }
}
