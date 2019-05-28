package com.killkinto.popmovies.data;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.content.Context;

import com.killkinto.popmovies.model.Movie;

@Database(entities = {Movie.class}, version = 1)
public abstract class MovieDatabase extends RoomDatabase {
    private static final String DB_NAME = "movieDb.db";
    private static volatile MovieDatabase instance;

    static synchronized MovieDatabase getInstance(Context context) {
        if (instance == null) {
            instance = create(context);
        }
        return instance;
    }

    private static MovieDatabase create(Context context) {
        return Room.databaseBuilder(context, MovieDatabase.class, DB_NAME).build();
    }

    public abstract MovieDao getMovieDao();
}
