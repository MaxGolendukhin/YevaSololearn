package com.golendukhin.YevaSololearn.dataBase;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import android.database.DatabaseUtils;

import com.golendukhin.YevaSololearn.Feed;
import static com.golendukhin.YevaSololearn.dataBase.DataBaseContract.FeedEntry.COLUMN_CATEGORY;
import static com.golendukhin.YevaSololearn.dataBase.DataBaseContract.FeedEntry.COLUMN_FEED_ID;
import static com.golendukhin.YevaSololearn.dataBase.DataBaseContract.FeedEntry.COLUMN_IMAGE_URL;
import static com.golendukhin.YevaSololearn.dataBase.DataBaseContract.FeedEntry.COLUMN_TITLE;
import static com.golendukhin.YevaSololearn.dataBase.DataBaseContract.FeedEntry.COLUMN_WEB_URL;
import static com.golendukhin.YevaSololearn.dataBase.DataBaseContract.FeedEntry.DATABASE_NAME;
import static com.golendukhin.YevaSololearn.dataBase.DataBaseContract.FeedEntry.DATABASE_VERSION;
import static com.golendukhin.YevaSololearn.dataBase.DataBaseContract.FeedEntry.FEEDS_TABLE_NAME;
import static com.golendukhin.YevaSololearn.dataBase.DataBaseContract.FeedEntry.WATCHED_TABLE_NAME;
import static com.golendukhin.YevaSololearn.dataBase.DataBaseContract.FeedEntry._ID;

public class DataBaseHelper extends SQLiteOpenHelper {
    public DataBaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL("CREATE TABLE " + FEEDS_TABLE_NAME + "(" +
                                _ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                                COLUMN_FEED_ID + " TEXT NOT NULL, " +
                                COLUMN_TITLE + " TEXT NOT NULL, " +
                                COLUMN_CATEGORY + " TEXT NOT NULL, " +
                                COLUMN_IMAGE_URL + " TEXT NOT NULL, " +
                                COLUMN_WEB_URL + " TEXT NOT NULL," +
                                "UNIQUE(" + COLUMN_FEED_ID + " ));"
                                );

        sqLiteDatabase.execSQL("CREATE TABLE " + WATCHED_TABLE_NAME + "(" +
                                _ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                                COLUMN_FEED_ID + " TEXT NOT NULL, " +
                                "UNIQUE(" + COLUMN_FEED_ID + "));"
                            );
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL("DROP TABLE " + FEEDS_TABLE_NAME + ";");
        sqLiteDatabase.execSQL("DROP TABLE " + WATCHED_TABLE_NAME + ";");
        onCreate(sqLiteDatabase);
    }

    public Cursor fetchAllFeedItems() {
        SQLiteDatabase sqLiteDatabase = getReadableDatabase();
        return sqLiteDatabase.query(FEEDS_TABLE_NAME, null, null, null, null, null, null);
    }

    public boolean addFeedItem(Feed feed) {
        SQLiteDatabase sqLiteDatabase = getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COLUMN_FEED_ID, feed.getFeedId());
        contentValues.put(COLUMN_TITLE, feed.getTitle());
        contentValues.put(COLUMN_CATEGORY, feed.getCategory());
        contentValues.put(COLUMN_IMAGE_URL, feed.getImageUrl());
        contentValues.put(COLUMN_WEB_URL, feed.getWebUrl());

        return sqLiteDatabase.insert(FEEDS_TABLE_NAME, null, contentValues) != -1;
    }

    public boolean removeFeedItem(Feed feed) {
        SQLiteDatabase sqLiteDatabase = getWritableDatabase();
        String selection = COLUMN_FEED_ID + " = ?";
        String[] selectionArgs = { feed.getFeedId() };

        return sqLiteDatabase.delete(FEEDS_TABLE_NAME, selection, selectionArgs) > 0;
    }

    public boolean addWatchedItem(String feedId) {
        SQLiteDatabase sqLiteDatabase = getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COLUMN_FEED_ID, feedId);

//        long a = sqLiteDatabase.insert(WATCHED_TABLE_NAME, null, contentValues);
//        Cursor c = sqLiteDatabase.query(WATCHED_TABLE_NAME, null, null, null, null, null, null);
//        int sfdsa = c.getCount();
//
////        String f = DatabaseUtils.dumpCursorToString(c);
//        while (c.moveToNext()) {
//            String  ss = c.getString(c.getColumnIndex(COLUMN_FEED_ID));
//        }



        return sqLiteDatabase.insert(WATCHED_TABLE_NAME, null, contentValues) != -1;
    }

    public boolean inWatchedItems(String itemId) {
        SQLiteDatabase sqLiteDatabase = getReadableDatabase();
        String selection = COLUMN_FEED_ID + " = ?";
        String[] selectionArgs = { itemId };

//        Cursor c = sqLiteDatabase.query(
//                        WATCHED_TABLE_NAME,
//                null,
//                selection
//                ,selectionArgs
//                ,null
//                ,null
//                ,null);

        return sqLiteDatabase.query(
                WATCHED_TABLE_NAME,
                null,
                selection
                ,selectionArgs
                ,null
                ,null
                ,null
        ).getCount() > 0;
    }
}