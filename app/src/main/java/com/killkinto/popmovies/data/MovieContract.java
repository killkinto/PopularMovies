package com.killkinto.popmovies.data;

import android.net.Uri;
import android.provider.BaseColumns;


public class MovieContract {

    static final String AUTHORITY = "com.killkinto.popmovies";
    static final Uri BASE_CONTENT_URI = Uri.parse("content://" + AUTHORITY);
    static final String PATH_MOVIES = "movies";

    public static final class MovieEntry implements BaseColumns {

        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_MOVIES).build();

        static final String TABLE_NAME = "movie";

        public static final String COLUMN_ORIGINAL_TITLE = "origem_title";
        public static final String COLUMN_TITLE = "title";
        public static final String COLUMN_POSTER_PATH = "poster_path";
        public static final String COLUMN_OVERVIEW = "overview";
        public static final String COLUMN_VOTE_AVERAGE = "vote_average";
        public static final String COLUMN_RELEASE_DATE = "release_date";
    }
}
