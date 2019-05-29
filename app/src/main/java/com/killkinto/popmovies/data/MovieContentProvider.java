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

import com.killkinto.popmovies.model.Movie;

import static com.killkinto.popmovies.data.MovieContract.MovieEntry;


public class MovieContentProvider extends ContentProvider {
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
        return true;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues cv) {
        Uri returnInsert = null;

        if (cv != null && MovieContentProvider.sUriMATCHER.match(uri) == MovieContentProvider.MOVIES) {
            Movie movie = new Movie();
            movie.id = cv.getAsInteger(MovieEntry._ID);
            movie.originalTitle = cv.getAsString(MovieEntry.COLUMN_ORIGINAL_TITLE);
            movie.title = cv.getAsString(MovieEntry.COLUMN_TITLE);
            movie.posterPath = cv.getAsString(MovieEntry.COLUMN_POSTER_PATH);
            movie.overview = cv.getAsString(MovieEntry.COLUMN_OVERVIEW);
            movie.releaseDate = cv.getAsString(MovieEntry.COLUMN_RELEASE_DATE);
            movie.voteAverage = cv.getAsString(MovieEntry.COLUMN_VOTE_AVERAGE);
            long id = MovieDatabase.getInstance(getContext()).getMovieDao().insert(movie);

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
        Cursor cursor;

        int matcher = MovieContentProvider.sUriMATCHER.match(uri);

        if (matcher == MOVIES) {
            cursor = MovieDatabase.getInstance(getContext()).getMovieDao().                                                                                           getFavoriteMovies();
        } else if (matcher == MOVIES_WITH_ID) {
            cursor = MovieDatabase.getInstance(getContext()).getMovieDao().getMovie(Integer.parseInt(uri.getPathSegments().get(1)));
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
        int rowsDeletes;

        if (MovieContentProvider.sUriMATCHER.match(uri) == MOVIES_WITH_ID) {
            int id = Integer.parseInt(uri.getPathSegments().get(1));
            rowsDeletes = MovieDatabase.getInstance(getContext()).getMovieDao().delete(new Movie(id, null));
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
