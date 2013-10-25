package org.ekkoproject.android.player.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class EkkoDbHelper extends SQLiteOpenHelper {
    /*
     * Version history
     * 
     * 5: 6/25/2013
     * 6: 6/25/2013
     * 7: 6/28/2013
     * 8: 10/16/2013
     * 9: 10/18/2013
     * 10: 10/25/2013
     */
    public static final int DATABASE_VERSION = 10;
    public static final String DATABASE_NAME = "Ekko.db";

    public EkkoDbHelper(final Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(final SQLiteDatabase db) {
        db.execSQL(Contract.Permission.SQL_CREATE_TABLE);
        db.execSQL(Contract.Course.SQL_CREATE_TABLE);
        db.execSQL(Contract.Course.Resource.SQL_CREATE_TABLE);
        db.execSQL(Contract.CachedResource.SQL_CREATE_TABLE);
        db.execSQL(Contract.CachedUriResource.SQL_CREATE_TABLE);
        db.execSQL(Contract.Progress.SQL_CREATE_TABLE);
        db.execSQL(Contract.Answer.SQL_CREATE_TABLE);
    }

    @Override
    @SuppressWarnings("deprecation")
    public void onUpgrade(final SQLiteDatabase db, final int oldVersion, final int newVersion) {
        switch (oldVersion) {
        case 0:
            // version too old, reset database
            resetDatabase(db);
            break;
        case 1:
            if (newVersion <= 1) {
                break;
            }
            db.execSQL(Contract.CachedResource.SQL_CREATE_TABLE);
        case 2:
            if (newVersion <= 2) {
                break;
            }
            db.execSQL(Contract.CachedUriResource.SQL_CREATE_TABLE);
        case 3:
            if (newVersion <= 3) {
                break;
            }
            db.execSQL(Contract.Progress.SQL_CREATE_TABLE);
        case 4:
            if (newVersion <= 4) {
                break;
            }
            db.execSQL(Contract.Answer.SQL_CREATE_TABLE);
        case 5:
            if (newVersion <= 5) {
                break;
            }
            db.execSQL(Contract.Course.Resource.SQL_DROP_INDEX_COURSE_ID);
            db.execSQL(Contract.CachedResource.SQL_DROP_INDEX_COURSE_ID);
            db.execSQL(Contract.CachedUriResource.SQL_DROP_INDEX_COURSE_ID);
            db.execSQL(Contract.Progress.SQL_DROP_INDEX_COURSE_ID);
        case 6:
            if (newVersion <= 6) {
                break;
            }
            db.execSQL(Contract.Course.SQL_V7_ALTER_ACCESSIBLE);
            db.execSQL(Contract.Course.SQL_V7_DEFAULT_ACCESSIBLE);
        case 7:
            if (newVersion <= 7) {
                break;
            }
            db.execSQL(Contract.Permission.SQL_CREATE_TABLE);
            case 8:
                if (newVersion <= 8) {
                    break;
                }
                db.execSQL(Contract.Course.SQL_V9_ALTER_ENROLLMENT_TYPE);
                db.execSQL(Contract.Course.SQL_V9_ALTER_PUBLIC);
                db.execSQL(Contract.Course.SQL_V9_DEFAULT_PUBLIC_ENROLLMENT_TYPE);
            case 9:
                if (newVersion <= 9) {
                    break;
                }
                db.execSQL(Contract.Course.SQL_V10_ALTER_DESCRIPTION);
            case 10:
                if (newVersion <= 10) {
                    break;
                }
                break;
            default:
                // unrecognized version, let's just reset the database
                this.resetDatabase(db);
        }
    }

    @Override
    public void onDowngrade(final SQLiteDatabase db, final int oldVersion, final int newVersion) {
        // don't try downgrading, just reset the database
        this.resetDatabase(db);
    }

    private void resetDatabase(final SQLiteDatabase db) {
        db.execSQL(Contract.Answer.SQL_DELETE_TABLE);
        db.execSQL(Contract.Progress.SQL_DELETE_TABLE);
        db.execSQL(Contract.CachedUriResource.SQL_DELETE_TABLE);
        db.execSQL(Contract.CachedResource.SQL_DELETE_TABLE);
        db.execSQL(Contract.Course.Resource.SQL_DELETE_TABLE);
        db.execSQL(Contract.Course.SQL_DELETE_TABLE);
        db.execSQL(Contract.Permission.SQL_DELETE_TABLE);
        this.onCreate(db);
    }
}
