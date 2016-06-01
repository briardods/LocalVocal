package com.example.briar.localvocal;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

/**
 * Created by Briar on 28/05/2016.
 */
public class FeedReaderDbHelper extends SQLiteOpenHelper {
    // If you change the database schema, you must increment the database version.
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "FeedReader.db";

    private static final String TEXT_TYPE = " TEXT";
    private static final String NUM_TYPE = " NUMERIC";
    private static final String COMMA_SEP = ",";
    private static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + FeedEntry.TABLE_NAME + " (" +
                    FeedEntry.COLUMN_NAME_CITY + TEXT_TYPE + COMMA_SEP +
                    FeedEntry.COLUMN_NAME_LAT + NUM_TYPE + COMMA_SEP +
                    FeedEntry.COLUMN_NAME_LONG + NUM_TYPE + COMMA_SEP +
                    "UNIQUE(" +  FeedEntry.COLUMN_NAME_CITY + COMMA_SEP + FeedEntry.COLUMN_NAME_LAT + COMMA_SEP + FeedEntry.COLUMN_NAME_LONG +
    " ))";

    private static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + FeedEntry.TABLE_NAME;

    public FeedReaderDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_ENTRIES);
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over
        db.execSQL(SQL_DELETE_ENTRIES);
        onCreate(db);
    }

    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }

    public static abstract class FeedEntry implements BaseColumns {
        public static final String TABLE_NAME = "location";
        public static final String COLUMN_NAME_CITY = "city";
        public static final String COLUMN_NAME_LAT = "latitude";
        public static final String COLUMN_NAME_LONG = "longitude";
    }
}