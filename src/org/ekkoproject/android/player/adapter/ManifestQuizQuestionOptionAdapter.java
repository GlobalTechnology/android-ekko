package org.ekkoproject.android.player.adapter;

import java.util.Collections;
import java.util.List;

import org.appdev.entity.Option;
import org.appdev.entity.Question;
import org.ekkoproject.android.player.R;
import org.ekkoproject.android.player.services.CourseManager;

import android.content.Context;
import android.text.Html;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;

public class ManifestQuizQuestionOptionAdapter extends AbstractManifestQuizQuestionAdapter<Option> {
    private static final List<Option> NO_OPTIONS = Collections.emptyList();

    private List<Option> options = NO_OPTIONS;

    public ManifestQuizQuestionOptionAdapter(final Context context, final String quizId, final String questionId) {
        super(context, quizId, questionId);
    }

    @Override
    public int getCount() {
        return this.options.size();
    }

    @Override
    public Option getItem(final int position) {
        return this.options.get(position);
    }

    @Override
    public long getItemId(final int position) {
        return CourseManager.convertId(this.getCourseId(), this.options.get(position).getId());
    }

    @Override
    protected void bindView(final View v, final Option option) {
        final View optionView = v.findViewById(R.id.option);
        if (optionView instanceof TextView) {
            ((TextView) optionView).setText(Html.fromHtml(option.getValue()));
        } else if (optionView instanceof CheckBox) {
            ((CheckBox) optionView).setText(Html.fromHtml(option.getValue()));
        }
    }

    @Override
    protected void onNewQuestion(final Question question) {
        super.onNewQuestion(question);

        if (question != null) {
            this.options = question.getOptions();
        } else {
            this.options = NO_OPTIONS;
        }
    }
}
