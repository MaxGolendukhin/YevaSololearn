package com.golendukhin.YevaSololearn.dataBase;

import android.provider.BaseColumns;

public final class DataBaseContract {
    private DataBaseContract() {}

    /* Inner class that defines the table contents */
    public static class FeedEntry implements BaseColumns {
        public static final int DATABASE_VERSION = 1;
        public static final String DATABASE_NAME = "feed.db";
        public static final String TABLE_NAME = "feeds";

        public static final String _ID = BaseColumns._ID;
        public static final String COLUMN_FEED_ID = "feedId";
        public static final String COLUMN_TITLE = "title";
        public static final String COLUMN_CATEGORY = "category";
        public static final String COLUMN_IMAGE_URL = "imageUrl";
        public static final String COLUMN_WEB_URL = "webUrl";
    }
}