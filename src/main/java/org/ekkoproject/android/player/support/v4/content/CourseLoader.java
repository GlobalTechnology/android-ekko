package org.ekkoproject.android.player.support.v4.content;

import android.content.Context;

import org.ccci.gto.android.common.support.v4.content.AsyncTaskBroadcastReceiverLoader;
import org.ekkoproject.android.player.db.EkkoDao;
import org.ekkoproject.android.player.model.Course;

public class CourseLoader extends AsyncTaskBroadcastReceiverLoader<Course> {
    private final EkkoDao mDao;

    private final long mCourseId;

    public CourseLoader(final Context context, final long courseId) {
        super(context);
        mDao = EkkoDao.getInstance(context);
        mCourseId = courseId;
    }

    @Override
    public Course loadInBackground() {
        return mDao.find(Course.class, mCourseId);
    }
}
