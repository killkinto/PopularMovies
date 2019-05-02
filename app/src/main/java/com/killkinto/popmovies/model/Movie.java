package com.killkinto.popmovies.model;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;
import android.databinding.BindingAdapter;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.widget.ImageView;

import com.killkinto.popmovies.data.MovieContract;
import com.killkinto.popmovies.utils.NetworkUtils;

@Entity
public class Movie implements Parcelable {

    @PrimaryKey
    @ColumnInfo(name = MovieContract.MovieEntry._ID)
    public Integer id;

    @ColumnInfo(name = MovieContract.MovieEntry.COLUMN_ORIGINAL_TITLE)
    public String originalTitle;
    public String title;

    @ColumnInfo(name = MovieContract.MovieEntry.COLUMN_POSTER_PATH)
    public String posterPath;
    @Ignore
    public String overview;
    @Ignore
    public String voteAverage;
    @ColumnInfo(name = MovieContract.MovieEntry.COLUMN_RELEASE_DATE)
    public String releaseDate;

    public Movie(){}

    @Ignore
    public Movie(@NonNull Integer id, String posterPath) {
        this.id = id;
        this.posterPath = posterPath;
    }

    @Ignore
    public Movie(Parcel in) {
        id = in.readInt();
        originalTitle = in.readString();
        title = in.readString();
        posterPath = in.readString();
        overview = in.readString();
        voteAverage = in.readString();
        releaseDate = in.readString();
    }

    public static final Creator<Movie> CREATOR = new Creator<Movie>() {
        @Override
        public Movie createFromParcel(Parcel in) {
            return new Movie(in);
        }

        @Override
        public Movie[] newArray(int size) {
            return new Movie[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(originalTitle);
        dest.writeString(title);
        dest.writeString(posterPath);
        dest.writeString(overview);
        dest.writeString(voteAverage);
        dest.writeString(releaseDate);
    }

    @BindingAdapter({"bind:imageUrl"})
    public static void loadImage(ImageView imageView, String imageUrl) {
        NetworkUtils.loadPosterImage(imageView.getContext(), imageUrl, imageView);
    }
}
