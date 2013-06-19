package org.ekkoproject.android.player.support.v4.fragment.lesson;

import static org.ekkoproject.android.player.fragment.Constants.ARG_CONTENTID;

import java.util.List;

import org.appdev.entity.Lesson;
import org.ekkoproject.android.player.R;
import org.ekkoproject.android.player.model.Manifest;
import org.ekkoproject.android.player.support.v4.fragment.AbstractManifestAwareFragment;

import android.annotation.TargetApi;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class TextFragment extends AbstractManifestAwareFragment {
    private static final String ARG_TEXTID = TextFragment.class.getName() + ".ARG_TEXTID";

    private String lessonId = null;
    private int textId = 0;

    private TextView textView = null;

    public static TextFragment newInstance(final long courseId, final String lessonId, final int textId) {
        final TextFragment fragment = new TextFragment();

        // handle arguments
        final Bundle args = buildArgs(courseId);
        args.putString(ARG_CONTENTID, lessonId);
        args.putInt(ARG_TEXTID, textId);
        fragment.setArguments(args);

        return fragment;
    }

    /** BEGIN lifecycle */

    @Override
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR1)
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // process arguments
        final Bundle args = getArguments();
        this.textId = args.getInt(ARG_TEXTID, 0);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR1) {
            this.lessonId = args.getString(ARG_CONTENTID, null);
        } else {
            this.lessonId = args.getString(ARG_CONTENTID);
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
    protected void onManifestUpdate(final Manifest manifest) {
        super.onManifestUpdate(manifest);
        this.updateTextView(manifest);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        this.clearViews();
    }

    /** END lifecycle */

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
                    final List<String> text = lesson.getText();
                    if (text.size() > this.textId) {
                        this.textView.setText(Html.fromHtml(text.get(this.textId)));
                    }
                }
            }
        }
    }
}
