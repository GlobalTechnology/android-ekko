package org.ekkoproject.android.player.support.v4.fragment;

import static org.ekkoproject.android.player.fragment.Constants.ARG_CONTENTID;

import java.util.HashSet;
import java.util.Set;

import org.ekkoproject.android.player.R;
import org.ekkoproject.android.player.adapter.ManifestQuizQuestionOptionAdapter;
import org.ekkoproject.android.player.model.Manifest;
import org.ekkoproject.android.player.model.Question;
import org.ekkoproject.android.player.model.Quiz;
import org.ekkoproject.android.player.services.CourseManager;

import android.annotation.TargetApi;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

public class QuestionFragment extends AbstractManifestAndProgressAwareFragment implements View.OnClickListener,
        AdapterView.OnItemClickListener {
    private static final String ARG_QUESTIONID = QuestionFragment.class.getName() + ".ARG_QUESTIONID";

    private String quizId = null;
    private String questionId = null;

    private TextView questionView = null;
    private ListView optionsView = null;
    private ProgressBar progressBar = null;
    private View nextButton = null;
    private View prevButton = null;

    public static QuestionFragment newInstance(final long courseId, final String quizId, final String questionId) {
        final QuestionFragment fragment = new QuestionFragment();

        // handle arguments
        final Bundle args = buildArgs(courseId);
        args.putString(ARG_CONTENTID, quizId);
        args.putString(ARG_QUESTIONID, questionId);
        fragment.setArguments(args);

        return fragment;
    }

    /* BEGIN lifecycle */

    @Override
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR1)
    public void onCreate(final Bundle savedState) {
        super.onCreate(savedState);

        // process arguments
        final Bundle args = getArguments();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR1) {
            this.quizId = args.getString(ARG_CONTENTID, null);
            this.questionId = args.getString(ARG_QUESTIONID, null);
        } else {
            this.quizId = args.getString(ARG_CONTENTID);
            this.questionId = args.getString(ARG_QUESTIONID);
        }
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_quiz_question, container, false);
    }

    @Override
    public void onActivityCreated(final Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        this.findViews();
        this.setupNavButtons();
        this.setupOptionsView();
    }

    @Override
    protected void onManifestUpdate(final Manifest manifest) {
        super.onManifestUpdate(manifest);
        this.updateQuestion(manifest);
        this.updateManifestAdapters(manifest, this.optionsView);
    }

    @Override
    protected void onProgressUpdate(final Set<String> progress) {
        super.onProgressUpdate(progress);
        this.updateSelectedAnswers(progress);
    }

    @Override
    public void onItemClick(final AdapterView<?> parent, final View view, final int position, final long id) {
        final long courseId = this.getCourseId();
        final long[] answerIds = this.optionsView.getCheckedItemIds();
        final Set<String> answers = new HashSet<String>();
        for (final long answerId : answerIds) {
            answers.add(CourseManager.convertId(courseId, answerId));
        }
        this.getProgressManager().recordAnswersAsync(courseId, this.questionId,
                answers.toArray(new String[answers.size()]));
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
    public void onDestroyView() {
        super.onDestroyView();
        this.clearViews();
    }

    /* END lifecycle */

    public String getQuestionId() {
        return this.questionId;
    }

    private void findViews() {
        this.questionView = findView(TextView.class, R.id.question);
        this.optionsView = findView(ListView.class, R.id.options);
        this.progressBar = findView(ProgressBar.class, R.id.progress);
        this.nextButton = findView(View.class, R.id.nextButton);
        this.prevButton = findView(View.class, R.id.prevButton);
    }

    private void clearViews() {
        this.questionView = null;
        this.optionsView = null;
        this.progressBar = null;
        this.nextButton = null;
        this.prevButton = null;
    }

    private void setupNavButtons() {
        if (this.nextButton != null) {
            this.nextButton.setOnClickListener(this);
        }
        if (this.prevButton != null) {
            this.prevButton.setOnClickListener(this);
        }
    }

    private void setupOptionsView() {
        if (this.optionsView != null) {
            // attach the data adapter
            final ManifestQuizQuestionOptionAdapter adapter = new ManifestQuizQuestionOptionAdapter(getActivity(),
                    this.quizId, this.questionId);
            adapter.setLayout(R.layout.list_item_quiz_question_option);
            this.optionsView.setAdapter(adapter);

            // setup an item click listener
            this.optionsView.setOnItemClickListener(this);
        }
    }

    private void updateQuestion(final Manifest manifest) {
        if (this.questionView != null || this.progressBar != null) {
            // find the quiz & question
            final Quiz quiz = manifest != null ? manifest.getQuiz(this.quizId) : null;
            final Question question = quiz != null ? quiz.getQuestion(this.questionId) : null;

            // update the question text
            if (this.questionView != null) {
                this.questionView.setText("");
                if (question != null) {
                    this.questionView.setText(Html.fromHtml(question.getQuestion()));
                }
            }

            // update the progress bar (we handle quiz progress bars different
            // to not give away answers)
            if (this.progressBar != null) {
                if (quiz != null) {
                    this.progressBar.setMax(quiz.getQuestions().size());
                    this.progressBar.setProgress(quiz.findQuestion(this.questionId));
                } else {
                    this.progressBar.setMax(1);
                    this.progressBar.setProgress(0);
                }
            }
        }
    }

    private void updateSelectedAnswers(final Set<String> progress) {
        if (this.optionsView != null) {
            final long courseId = this.getCourseId();

            // iterate over all options
            for (int position = 0; position < this.optionsView.getCount(); position++) {
                final long id = this.optionsView.getItemIdAtPosition(position);
                this.optionsView.setItemChecked(position, progress.contains(CourseManager.convertId(courseId, id)));
            }
        }
    }

    public interface Listener {
        void onNavigatePrevious();

        void onNavigateNext();
    }
}
