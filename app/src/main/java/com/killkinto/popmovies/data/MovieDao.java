package com.killkinto.popmovies.data;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.database.Cursor;

import com.killkinto.popmovies.model.Movie;

@Dao
public interface MovieDao {

    @Query("SELECT * FROM " + MovieContract.MovieEntry.TABLE_NAME)
    Cursor getFavoriteMovies();

    @Query("SELECT * FROM movie WHERE _id = :id")
    Cursor getMovie(int id);

    @Insert
    long insert(Movie movie);

    @Delete
    int delete(Movie movie);
}
