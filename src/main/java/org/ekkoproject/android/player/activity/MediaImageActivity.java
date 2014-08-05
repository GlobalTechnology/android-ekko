package org.ekkoproject.android.player.activity;

import static org.ekkoproject.android.player.Constants.EXTRA_COURSEID;
import static org.ekkoproject.android.player.Constants.INVALID_COURSE;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import org.ekkoproject.android.player.R;
import org.ekkoproject.android.player.util.ResourceUtils;

public class MediaImageActivity extends Activity {
    private static final String EXTRA_RESOURCEID = MediaImageActivity.class.getName() + ".EXTRA_RESOURCEID";

    private long courseId = INVALID_COURSE;
    private String resourceId = null;

    private ImageView mImage = null;

    public static Intent newIntent(final Context context, final long courseId, final String resourceId) {
        final Intent intent = new Intent(context, MediaImageActivity.class);
        intent.putExtra(EXTRA_COURSEID, courseId);
        intent.putExtra(EXTRA_RESOURCEID, resourceId);
        return intent;
    }

    /** BEGIN lifecycle */

    @Override
    protected void onCreate(final Bundle savedState) {
        super.onCreate(savedState);

        final Intent intent = getIntent();
        this.courseId = intent.getLongExtra(EXTRA_COURSEID, INVALID_COURSE);
        this.resourceId = intent.getStringExtra(EXTRA_RESOURCEID);

        this.setContentView(R.layout.activity_media_image);
        this.findViews();
        this.updateImage();
    }

    /** END lifecycle */

    private void findViews() {
        mImage = findView(ImageView.class, R.id.image);
    }

    private <T extends View> T findView(final Class<T> clazz, final int id) {
        final View view = findViewById(id);
        if (clazz.isInstance(view)) {
            return clazz.cast(view);
        }
        return null;
    }

    private void updateImage() {
        ResourceUtils.setImage(mImage, this.courseId, this.resourceId);
    }
}
