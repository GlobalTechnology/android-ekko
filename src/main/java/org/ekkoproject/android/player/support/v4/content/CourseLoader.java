package org.ekkoproject.android.player.support.v4.content;

import android.content.Context;

import org.ekkoproject.android.player.db.EkkoDao;
import org.ekkoproject.android.player.model.Course;

public class CourseLoader extends AbstractCourseLoader<Course> {
    private final EkkoDao mDao;

    public CourseLoader(final Context context, final long courseId) {
        super(context, courseId);
        mDao = EkkoDao.getInstance(context);
    }

    @Override
    public Course loadInBackground() {
        return mDao.find(Course.class, mCourseId);
    }
}
