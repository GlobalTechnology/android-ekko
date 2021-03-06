package org.ekkoproject.android.player.db;

import static org.ekkoproject.android.player.BuildConfig.THEKEY_CLIENTID;
import static org.ekkoproject.android.player.Constants.GUID_GUEST;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import me.thekey.android.lib.TheKeyImpl;

public class EkkoDbHelper extends SQLiteOpenHelper {
    /*
     * Version history
     * 
     * 7: 6/28/2013
     * 8: 10/16/2013
     * 9: 10/18/2013
     * 10: 10/25/2013
     * 11: 10/30/2013
     * 12: 11/12/2013
     * 13: 11/13/2013
     * 14: 01/06/2014
     * 15: 01/07/2014
     * v0.9.3
     * 16: 01/24/2014
     * 17: 01/27/2014
     * v0.9.4 - v0.9.5
     * 18: 08/14/2014
     */
    public static final int DATABASE_VERSION = 18;
    public static final String DATABASE_NAME = "Ekko.db";

    private final Context mContext;

    public EkkoDbHelper(final Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        mContext = context;
    }

    @Override
    public void onCreate(final SQLiteDatabase db) {
        db.execSQL(Contract.Permission.SQL_CREATE_TABLE);
        db.execSQL(Contract.Course.SQL_CREATE_TABLE);
        db.execSQL(Contract.Course.Resource.SQL_CREATE_TABLE);
        db.execSQL(Contract.CachedArclightResource.SQL_CREATE_TABLE);
        db.execSQL(Contract.CachedEcvResource.SQL_CREATE_TABLE);
        db.execSQL(Contract.CachedFileResource.SQL_CREATE_TABLE);
        db.execSQL(Contract.CachedUriResource.SQL_CREATE_TABLE);
        db.execSQL(Contract.Progress.SQL_CREATE_TABLE);
        db.execSQL(Contract.Answer.SQL_CREATE_TABLE);
    }

    @Override
    @SuppressWarnings("deprecation")
    public void onUpgrade(final SQLiteDatabase db, final int oldVersion, final int newVersion) {
        final String guid = TheKeyImpl.getInstance(mContext, THEKEY_CLIENTID).getGuid();

        switch (oldVersion) {
            case 7:
                if (newVersion <= 7) {
                    break;
                }
                db.execSQL(Contract.Permission.SQL_V8_CREATE_TABLE);
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
                db.execSQL(Contract.Permission.SQL_V11_ALTER_HIDDEN);
                db.execSQL(Contract.Permission.SQL_V11_DEFAULT_HIDDEN);
            case 11:
                if (newVersion <= 11) {
                    break;
                }
                db.execSQL(Contract.Progress.SQL_V12_RENAME_TABLE);
                db.execSQL(Contract.Progress.SQL_V12_CREATE_TABLE);
                db.execSQL(Contract.Progress.SQL_V12_MIGRATE_DATA, new Object[] {guid != null ? guid : GUID_GUEST});
                db.execSQL(Contract.Progress.SQL_V12_DELETE_TABLE);
            case 12:
                if (newVersion <= 12) {
                    break;
                }
                db.execSQL(Contract.Answer.SQL_V13_RENAME_TABLE);
                db.execSQL(Contract.Answer.SQL_V13_CREATE_TABLE);
                db.execSQL(Contract.Answer.SQL_V13_MIGRATE_DATA, new Object[] {guid != null ? guid : GUID_GUEST});
                db.execSQL(Contract.Answer.SQL_V13_DELETE_TABLE);
            case 13:
                if (newVersion <= 13) {
                    break;
                }
                db.execSQL(Contract.Course.Resource.SQL_V14_ALTER_VIDEO_ID);
            case 14:
                if (newVersion <= 14) {
                    break;
                }
                db.execSQL(Contract.CachedEcvResource.SQL_V15_CREATE_TABLE);
            case 15:
                if (newVersion <= 15) {
                    break;
                }
                db.execSQL(Contract.Course.Resource.SQL_V16_ALTER_REF_ID);
            case 16:
                if (newVersion <= 16) {
                    break;
                }
                db.execSQL(Contract.CachedArclightResource.SQL_V17_CREATE_TABLE);
            case 17:
                if (newVersion <= 17) {
                    break;
                }
                db.execSQL(Contract.Course.SQL_V18_ALTER_AUTHOR_NAME);
                db.execSQL(Contract.Course.SQL_V18_ALTER_COPYRIGHT);
            case 18:
                if (newVersion <= 18) {
                    break;
                }
                break;
            default:
                // unrecognized version, let's just reset the database
                // XXX: this includes versions that are too old
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
        db.execSQL(Contract.CachedFileResource.SQL_DELETE_TABLE);
        db.execSQL(Contract.CachedEcvResource.SQL_DELETE_TABLE);
        db.execSQL(Contract.CachedArclightResource.SQL_DELETE_TABLE);
        db.execSQL(Contract.Course.Resource.SQL_DELETE_TABLE);
        db.execSQL(Contract.Course.SQL_DELETE_TABLE);
        db.execSQL(Contract.Permission.SQL_DELETE_TABLE);
        this.onCreate(db);
    }
}
