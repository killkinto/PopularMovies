package br.com.killkinto.popmovies.model;

import android.databinding.BindingAdapter;
import android.os.Parcel;
import android.os.Parcelable;
import android.widget.ImageView;

import java.util.ArrayList;

import br.com.killkinto.popmovies.utils.NetworkUtils;

public class Movie implements Parcelable {

    public Integer id;

    public String originalTitle;
    public String title;

    public String posterPath;
    public String overview;
    public String voteAverage;
    public String releaseDate;

    public Movie(){}

    public Movie(Integer id, String posterPath) {
        this.id = id;
        this.posterPath = posterPath;
    }

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
