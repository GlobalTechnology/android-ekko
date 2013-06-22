package org.ekkoproject.android.player.adapter;

import org.ekkoproject.android.player.model.CourseContent;
import org.ekkoproject.android.player.model.Manifest;

import android.content.Context;

public abstract class AbstractManifestContentAdapter<T> extends AbstractManifestAdapter<T> {
    private final String contentId;

    private CourseContent content;

    public AbstractManifestContentAdapter(final Context context, final String contentId) {
        super(context);
        this.contentId = contentId;
    }

    @Override
    protected void onNewManifest(final Manifest manifest) {
        super.onNewManifest(manifest);

        // find the requested content
        CourseContent content = null;
        if (manifest != null) {
            content = manifest.getContent(this.contentId);
        }
        this.onNewContent(content);
    }

    protected void onNewContent(final CourseContent content) {
        this.content = content;
    }

    protected String getContentId() {
        return this.contentId;
    }

    protected CourseContent getContent() {
        return this.content;
    }
}
