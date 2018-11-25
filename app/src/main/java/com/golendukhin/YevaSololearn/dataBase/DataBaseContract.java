package com.golendukhin.YevaSololearn.dataBase;

        import android.provider.BaseColumns;

public final class DataBaseContract {
    private DataBaseContract() {
    }

    public static class FeedEntry implements BaseColumns {
        public static final String COLUMN_FEED_ID = "feedId";
        public static final String COLUMN_TITLE = "title";
        public static final String COLUMN_CATEGORY = "category";
        public static final String COLUMN_IMAGE_URL = "imageUrl";
        public static final String COLUMN_WEB_URL = "webUrl";
        static final int DATABASE_VERSION = 1;
        static final String DATABASE_NAME = "feed.db";
        static final String FEEDS_TABLE_NAME = "feeds";
        static final String WATCHED_TABLE_NAME = "watched_items";
        static final String _ID = BaseColumns._ID;
    }
}