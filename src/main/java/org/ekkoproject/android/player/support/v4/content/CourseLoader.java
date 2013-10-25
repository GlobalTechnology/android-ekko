package org.ekkoproject.android.player.support.v4.content;

import android.content.Context;

import org.ccci.gto.android.common.support.v4.content.AsyncTaskBroadcastReceiverLoader;
import org.ekkoproject.android.player.db.EkkoDao;
import org.ekkoproject.android.player.model.Course;

public class CourseLoader extends AsyncTaskBroadcastReceiverLoader<Course> {
    private final EkkoDao dao;

    private final long courseId;

    public CourseLoader(final Context context, final long courseId) {
        super(context);
        this.dao = EkkoDao.getInstance(context);
        this.courseId = courseId;
    }

    @Override
    public Course loadInBackground() {
        return this.dao.find(Course.class, this.courseId);
    }
}
