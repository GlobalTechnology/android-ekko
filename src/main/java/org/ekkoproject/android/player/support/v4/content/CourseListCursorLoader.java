package org.ekkoproject.android.player.support.v4.content;

import static org.ekkoproject.android.player.sync.EkkoSyncService.ACTION_UPDATE_COURSES;

import android.content.Context;
import android.content.IntentFilter;
import android.database.Cursor;
import android.util.Pair;

import org.ccci.gto.android.common.support.v4.content.CursorBroadcastReceiverLoader;
import org.ekkoproject.android.player.db.Contract;
import org.ekkoproject.android.player.db.EkkoDao;
import org.ekkoproject.android.player.model.Course;
import org.ekkoproject.android.player.model.Permission;

public class CourseListCursorLoader extends CursorBroadcastReceiverLoader {
    private final EkkoDao dao;
    private final String guid;
    private final boolean all;

    public CourseListCursorLoader(final Context context, final String guid, final boolean all) {
        super(context, new IntentFilter(ACTION_UPDATE_COURSES));
        this.dao = EkkoDao.getInstance(context);
        this.guid = guid != null ? guid.toUpperCase() : "";
        this.all = all;
    }

    @SuppressWarnings("unchecked")
    private static final Pair<String, Class<?>>[] SQL_JOINS =
            (Pair<String, Class<?>>[]) new Pair<?, ?>[] {Pair.create((String) null, Permission.class)};
    private static final String[] PROJECTION =
            new String[] {Contract.Course.SQL_PREFIX + Contract.Course.COLUMN_NAME_COURSE_ID,
                    Contract.Course.SQL_PREFIX + Contract.Course.COLUMN_NAME_TITLE,
                    Contract.Course.SQL_PREFIX + Contract.Course.COLUMN_NAME_BANNER_RESOURCE,
                    Contract.Course.SQL_PREFIX + Contract.Course.COLUMN_ENROLLMENT_TYPE,
                    Contract.Permission.SQL_PREFIX + Contract.Permission.COLUMN_ENROLLED,
                    Contract.Permission.SQL_PREFIX + Contract.Permission.COLUMN_PENDING,
                    Contract.Permission.SQL_PREFIX + Contract.Permission.COLUMN_CONTENT_VISIBLE,};
    private static final String SQL_ORDERBY =
            Contract.Course.SQL_PREFIX + Contract.Course.COLUMN_NAME_TITLE + " COLLATE NOCASE";
    private static final String SQL_WHERE_ALL =
            Contract.Permission.SQL_PREFIX + Contract.Permission.COLUMN_GUID + " = ?";
    private static final String SQL_WHERE_MY =
            Contract.Permission.SQL_PREFIX + Contract.Permission.COLUMN_GUID + " = ? AND " +
                    Contract.Permission.SQL_PREFIX + Contract.Permission.COLUMN_CONTENT_VISIBLE + " = 1";

    @Override
    protected Cursor getCursor() {
        return this.dao.getCursor(Course.class, SQL_JOINS, PROJECTION, all ? SQL_WHERE_ALL : SQL_WHERE_MY,
                                  new String[] {this.guid}, SQL_ORDERBY);
    }
}
