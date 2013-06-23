package org.ekkoproject.android.player.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class EkkoDbHelper extends SQLiteOpenHelper {
    public static final int DATABASE_VERSION = 4;
    public static final String DATABASE_NAME = "Ekko.db";

    public EkkoDbHelper(final Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(final SQLiteDatabase db) {
        db.execSQL(Contract.Course.SQL_CREATE_TABLE);
        db.execSQL(Contract.Course.Resource.SQL_CREATE_TABLE);
        db.execSQL(Contract.Course.Resource.SQL_INDEX_COURSE_ID);
        db.execSQL(Contract.CachedResource.SQL_CREATE_TABLE);
        db.execSQL(Contract.CachedResource.SQL_INDEX_COURSE_ID);
        db.execSQL(Contract.CachedUriResource.SQL_CREATE_TABLE);
        db.execSQL(Contract.CachedUriResource.SQL_INDEX_COURSE_ID);
        db.execSQL(Contract.Progress.SQL_CREATE_TABLE);
        db.execSQL(Contract.Progress.SQL_INDEX_COURSE_ID);
    }

    @Override
    public void onUpgrade(final SQLiteDatabase db, final int oldVersion, final int newVersion) {
        switch (oldVersion) {
        case 1:
            if (newVersion <= 1) {
                break;
            }
            db.execSQL(Contract.CachedResource.SQL_CREATE_TABLE);
            db.execSQL(Contract.CachedResource.SQL_INDEX_COURSE_ID);
        case 2:
            if (newVersion <= 2) {
                break;
            }
            db.execSQL(Contract.CachedUriResource.SQL_CREATE_TABLE);
            db.execSQL(Contract.CachedUriResource.SQL_INDEX_COURSE_ID);
        case 3:
            if (newVersion <= 3) {
                break;
            }
            db.execSQL(Contract.Progress.SQL_CREATE_TABLE);
            db.execSQL(Contract.Progress.SQL_INDEX_COURSE_ID);
        case 4:
            if (newVersion <= 4) {
                break;
            }
            break;
        // default:
        // this.resetDatabase(db);
        }
    }

    private void resetDatabase(final SQLiteDatabase db) {
        db.execSQL(Contract.Progress.SQL_DELETE_TABLE);
        db.execSQL(Contract.CachedUriResource.SQL_DELETE_TABLE);
        db.execSQL(Contract.CachedResource.SQL_DELETE_TABLE);
        db.execSQL(Contract.Course.Resource.SQL_DELETE_TABLE);
        db.execSQL(Contract.Course.SQL_DELETE_TABLE);
        this.onCreate(db);
    }
}
