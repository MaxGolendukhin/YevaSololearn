package com.golendukhin.YevaSololearn;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DataBaseHelper extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "feed.db";
    public static final String TABLE_NAME = "feeds";

    public static final String COLUMN_FEED_ID = "feedId";
    public static final String COLUMN_TITLE = "title";
    public static final String COLUMN_CATEGORY = "category";
    public static final String COLUMN_IMAGE_URL = "imageUrl";
    public static final String COLUMN_WEB_URL = "webUrl";

    private static final String SQL_DELETE_ENTRIES = "DROP TABLE " + TABLE_NAME + ";";

    public DataBaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        String SQL_CREATE_ENTRIES = "CREATE TABLE " + TABLE_NAME + "(" +
                COLUMN_FEED_ID + " TEXT NOT NULL, " +
                COLUMN_TITLE + " TEXT NOT NULL, " +
                COLUMN_CATEGORY + " TEXT NOT NULL, " +
                COLUMN_IMAGE_URL + " TEXT NOT NULL, " +
                COLUMN_WEB_URL + " TEXT NOT NULL);";
        sqLiteDatabase.execSQL(SQL_CREATE_ENTRIES);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL(SQL_DELETE_ENTRIES);
        onCreate(sqLiteDatabase);
    }

    boolean addItem(Feed feed) {
        SQLiteDatabase sqLiteDatabase = getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COLUMN_FEED_ID, feed.getFeedId());
        contentValues.put(COLUMN_TITLE, feed.getTitle());
        contentValues.put(COLUMN_CATEGORY, feed.getCategory());
        contentValues.put(COLUMN_IMAGE_URL, feed.getImageUrl());
        contentValues.put(COLUMN_WEB_URL, feed.getWebUrl());

        return sqLiteDatabase.insert(TABLE_NAME, null, contentValues) != -1;
    }

    Cursor fetchAllData() {
        SQLiteDatabase sqLiteDatabase = getReadableDatabase();
        return sqLiteDatabase.query(TABLE_NAME, null, null, null, null, null, null
        );
    }

    boolean deleteItem(Feed feed) {
        SQLiteDatabase sqLiteDatabase = getWritableDatabase();

        String selection = COLUMN_FEED_ID + " = ?";
        String[] selectionArgs = { feed.getFeedId() };

        return sqLiteDatabase.delete(TABLE_NAME, selection, selectionArgs) > 0;
    }
}