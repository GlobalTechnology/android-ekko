package org.ekkoproject.android.player.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class EkkoDbHelper extends SQLiteOpenHelper {
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "Ekko.db";

    public EkkoDbHelper(final Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(final SQLiteDatabase db) {
        db.execSQL(Contract.Course.SQL_CREATE_TABLE);
        db.execSQL(Contract.Course.Resource.SQL_CREATE_TABLE);
        db.execSQL(Contract.Course.Resource.SQL_INDEX_COURSE_ID);
    }

    @Override
    public void onUpgrade(final SQLiteDatabase db, final int oldVersion, final int newVersion) {
        switch (oldVersion) {
        case 1:
            break;
        default:
            db.execSQL(Contract.Course.Resource.SQL_DELETE_TABLE);
            db.execSQL(Contract.Course.SQL_DELETE_TABLE);
            this.onCreate(db);
        }
    }
}
