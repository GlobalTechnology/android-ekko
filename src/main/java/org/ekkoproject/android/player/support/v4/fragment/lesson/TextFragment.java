package org.ekkoproject.android.player.support.v4.fragment.lesson;

import static org.ekkoproject.android.player.fragment.Constants.ARG_CONTENTID;

import android.annotation.TargetApi;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.ekkoproject.android.player.R;
import org.ekkoproject.android.player.model.Lesson;
import org.ekkoproject.android.player.model.Manifest;
import org.ekkoproject.android.player.model.Text;
import org.ekkoproject.android.player.support.v4.fragment.AbstractManifestAwareFragment;
import org.ekkoproject.android.player.util.StringUtils;

public class TextFragment extends AbstractManifestAwareFragment {
    private static final String ARG_TEXTID = TextFragment.class.getName() + ".ARG_TEXTID";

    private String lessonId = null;
    private String textId = null;

    private TextView textView = null;

    public static TextFragment newInstance(final String guid, final long courseId, final String lessonId,
                                           final String textId) {
        final TextFragment fragment = new TextFragment();

        // handle arguments
        final Bundle args = buildArgs(guid, courseId);
        args.putString(ARG_CONTENTID, lessonId);
        args.putString(ARG_TEXTID, textId);
        fragment.setArguments(args);

        return fragment;
    }

    /* BEGIN lifecycle */

    @Override
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR1)
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // process arguments
        final Bundle args = getArguments();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR1) {
            this.lessonId = args.getString(ARG_CONTENTID, null);
            this.textId = args.getString(ARG_TEXTID, null);
        } else {
            this.lessonId = args.getString(ARG_CONTENTID);
            this.textId = args.getString(ARG_TEXTID);
        }
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_lesson_text, container, false);
    }

    @Override
    public void onActivityCreated(final Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        this.findViews();
    }

    @Override
    public void onUpdateUserVisibleHint(final boolean isVisibleToUser) {
        super.onUpdateUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser) {
            this.getProgressManager().recordProgressAsync(this.getCourseId(), this.textId);
        }
    }

    @Override
    protected void onManifestUpdate(final Manifest manifest) {
        super.onManifestUpdate(manifest);
        this.updateTextView(manifest);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        this.clearViews();
    }

    /* END lifecycle */

    private void findViews() {
        this.textView = findView(TextView.class, R.id.text);
    }

    private void clearViews() {
        this.textView = null;
    }

    private void updateTextView(final Manifest manifest) {
        if (this.textView != null) {
            this.textView.setText("");
            if (manifest != null) {
                // find the specified lesson text
                final Lesson lesson = manifest.getLesson(this.lessonId);
                if (lesson != null) {
                    final Text text = lesson.getText(this.textId);
                    if (text != null) {
                        this.textView.setText(StringUtils.trim(Html.fromHtml(text.getText())));
                    }
                }
            }
        }
    }
}
