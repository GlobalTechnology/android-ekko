package org.ekkoproject.android.player.support.v4.fragment;

import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.text.Html;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import org.ekkoproject.android.player.R;
import org.ekkoproject.android.player.adapter.ManifestQuizQuestionOptionAdapter;
import org.ekkoproject.android.player.model.Manifest;
import org.ekkoproject.android.player.model.Question;
import org.ekkoproject.android.player.model.Quiz;
import org.ekkoproject.android.player.services.CourseManager;

import java.util.HashSet;
import java.util.Set;

public class QuestionFragment extends AbstractContentFragment implements AdapterView.OnItemClickListener {
    private static final String ARG_QUESTIONID = QuestionFragment.class.getName() + ".ARG_QUESTIONID";

    private TextView questionView = null;
    private ListView optionsView = null;
    private ManifestQuizQuestionOptionAdapter optionsViewAdapter = null;
    private BroadcastReceiver mReceiver = null;

    /* final attributes */
    private String questionId = null;

    /* non-final attributes */
    private boolean mShowAnswers = false;

    public static QuestionFragment newInstance(final String guid, final long courseId, final String quizId,
                                               final String questionId) {
        final QuestionFragment fragment = new QuestionFragment();

        // handle arguments
        final Bundle args = buildArgs(guid, courseId, quizId);
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
            this.questionId = args.getString(ARG_QUESTIONID, null);
        } else {
            this.questionId = args.getString(ARG_QUESTIONID);
        }
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedState) {
        return inflater.inflate(R.layout.fragment_quiz_question, container, false);
    }

    @Override
    public void onActivityCreated(final Bundle savedState) {
        super.onActivityCreated(savedState);
        this.findViews();
        this.createBroadcastReceiver();
        this.setupOptionsView();
    }

    @Override
    public void onStart() {
        super.onStart();
        this.startBroadcastReceiver();
        this.syncShowAnswers();
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
    public void onStop() {
        super.onStop();
        this.stopBroadcastReceiver();
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

    void setShowAnswers(final boolean showAnswers) {
        final boolean changed = mShowAnswers != showAnswers;
        mShowAnswers = showAnswers;
        if (changed && this.optionsViewAdapter != null) {
            this.optionsViewAdapter.setShowAnswers(mShowAnswers);
        }
    }

    private void findViews() {
        this.questionView = findView(TextView.class, R.id.question);
        this.optionsView = findView(ListView.class, R.id.options);
        this.optionsViewAdapter = null;
    }

    private void clearViews() {
        this.questionView = null;
        this.optionsView = null;
        this.optionsViewAdapter = null;
    }

    private void syncShowAnswers() {
        final QuizFragment quiz = this.getAncestorFragment(QuizFragment.class);
        if (quiz != null) {
            this.setShowAnswers(quiz.getShowAnswers());
        }
    }

    private void createBroadcastReceiver() {
        mReceiver = new QuizFragment.QuizBroadcastReceiver() {
            @Override
            protected void onShowAnswers(final boolean showAnswers) {
                setShowAnswers(showAnswers);
            }
        };
    }

    private void startBroadcastReceiver() {
        if (mReceiver != null) {
            LocalBroadcastManager.getInstance(this.getActivity()).registerReceiver(mReceiver, QuizFragment.Broadcasts
                    .showAnswersFilter(getCourseId(), getContentId()));
        }
    }

    private void stopBroadcastReceiver() {
        if (mReceiver != null) {
            LocalBroadcastManager.getInstance(this.getActivity()).unregisterReceiver(mReceiver);
        }
    }

    private void setupOptionsView() {
        if (this.optionsView != null) {
            // attach the data adapter
            this.optionsViewAdapter = new ManifestQuizQuestionOptionAdapter(getActivity(), this.getContentId(),
                                                                            this.questionId, mShowAnswers);
            this.optionsViewAdapter.setLayout(R.layout.list_item_quiz_question_option);
            this.optionsViewAdapter.setAnswerLayout(R.layout.list_item_quiz_question_option_answer);
            this.optionsView.setAdapter(this.optionsViewAdapter);

            // setup an item click listener
            this.optionsView.setOnItemClickListener(this);
        }
    }

    private void updateQuestion(final Manifest manifest) {
        if (this.questionView != null) {
            // find the quiz & question
            final Quiz quiz = manifest != null ? manifest.getQuiz(this.getContentId()) : null;
            final Question question = quiz != null ? quiz.getQuestion(this.questionId) : null;

            // update the question text
            if (this.questionView != null) {
                this.questionView.setText("");
                if (question != null) {
                    this.questionView.setText(Html.fromHtml(question.getQuestion()));
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

    @Override
    protected Pair<Integer, Integer> getProgress(Manifest manifest, Set<String> progress) {
        if (manifest != null) {
            final Quiz quiz = manifest.getQuiz(this.getContentId());
            if (quiz != null) {
                return Pair.create(quiz.findQuestion(this.questionId), quiz.getQuestions().size());
            }
        }
        return null;
    }
}
