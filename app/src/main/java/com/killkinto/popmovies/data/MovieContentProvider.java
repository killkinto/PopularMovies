package com.killkinto.popmovies.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import static com.killkinto.popmovies.data.MovieContract.MovieEntry;


public class MovieContentProvider extends ContentProvider {

    private MovieDbHelper mMovieDbHelper;

    public static final int MOVIES = 100;
    public static final int MOVIES_WITH_ID = 101;

    private static final UriMatcher sUriMATCHER = buildUriMatcher();

    public static UriMatcher buildUriMatcher() {
        UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

        uriMatcher.addURI(MovieContract.AUTHORITY, MovieContract.PATH_MOVIES, MOVIES);
        uriMatcher.addURI(MovieContract.AUTHORITY, MovieContract.PATH_MOVIES + "/#", MOVIES_WITH_ID);

        return uriMatcher;
    }

    @Override
    public boolean onCreate() {
        mMovieDbHelper = new MovieDbHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        final SQLiteDatabase db = mMovieDbHelper.getWritableDatabase();

        Uri returnInsert = null;

        if (MovieContentProvider.sUriMATCHER.match(uri) == MovieContentProvider.MOVIES) {
            long id = db.insert(MovieEntry.TABLE_NAME, null, values);
            if (id > 0) {
                returnInsert = ContentUris.withAppendedId(MovieEntry.CONTENT_URI, id);
            }
        } else {
            throw new SQLException("Erro ao inserir novo filme através de " + uri);
        }

        if (getContext() != null) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return returnInsert;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection,
                        @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        final SQLiteDatabase db = mMovieDbHelper.getReadableDatabase();

        Cursor cursor;

        int matcher = MovieContentProvider.sUriMATCHER.match(uri);

        if (matcher == MOVIES) {
            cursor = db.query(MovieEntry.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
        } else if (matcher == MOVIES_WITH_ID) {
            cursor = db.query(MovieEntry.TABLE_NAME, projection, MovieEntry._ID  + "=?" ,
                    new String[]{uri.getPathSegments().get(1)}, null, null, sortOrder);
        } else {
            throw new UnsupportedOperationException("Uri desconhecida: " + uri);
        }

        if (getContext() != null) {
            cursor.setNotificationUri(getContext().getContentResolver(), uri);
        }

        return cursor;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        return null;
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        final SQLiteDatabase db = mMovieDbHelper.getWritableDatabase();

        int rowsDeletes;

        if (MovieContentProvider.sUriMATCHER.match(uri) == MOVIES_WITH_ID) {
            String id = uri.getPathSegments().get(1);
            rowsDeletes = db.delete(MovieEntry.TABLE_NAME, MovieEntry._ID + " = ?", new String[]{id});
        } else {
            throw new UnsupportedOperationException("URI desconhecida: " + uri);
        }

        if (rowsDeletes > 0 && getContext() != null) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return rowsDeletes;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        return 0;
    }
}
