package org.ekkoproject.android.player.support.v4.content;

import static org.ekkoproject.android.player.BroadcastUtils.EXTRA_COURSES;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import com.google.common.primitives.Longs;

import org.ccci.gto.android.common.support.v4.content.AsyncTaskBroadcastReceiverLoader;
import org.ccci.gto.android.common.support.v4.content.LoaderBroadcastReceiver;
import org.ekkoproject.android.player.BroadcastUtils;

public abstract class AbstractCourseLoader<T> extends AsyncTaskBroadcastReceiverLoader<T> {
    final long mCourseId;

    public AbstractCourseLoader(final Context context, final long courseId, final IntentFilter... filters) {
        super(context, filters);
        mCourseId = courseId;
        this.setBroadcastReceiver(new LoaderBroadcastReceiver(this) {
            @Override
            public void onReceive(final Context context, final Intent intent) {
                final long[] courses = intent.getLongArrayExtra(EXTRA_COURSES);
                if (courses == null || courses.length == 0 || Longs.contains(courses, mCourseId)) {
                    super.onReceive(context, intent);
                }
            }
        });
        this.addIntentFilter(BroadcastUtils.updateCoursesFilter());
    }
}
