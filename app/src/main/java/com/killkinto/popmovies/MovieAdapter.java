package com.killkinto.popmovies;


import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.Arrays;

import com.killkinto.popmovies.model.Movie;
import com.killkinto.popmovies.utils.NetworkUtils;

public class MovieAdapter extends RecyclerView.Adapter<MovieAdapter.MovieAdapterHolder> {

    private ArrayList<Movie> mMovies;
    private Context mContext;
    private MovieAdapterListener mMovieListener;

   interface MovieAdapterListener {
       void loadMovieData();
       void onClick(Movie movie, ImageView imageView);
       boolean isFavorite();
    }

    public MovieAdapter(MovieAdapterListener movieAdapterListener) {
        mMovieListener = movieAdapterListener;
    }

    @Override
    public MovieAdapterHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(mContext = parent.getContext());
        View view = inflater.inflate(R.layout.movie_poster_grid_item, parent, false);
        return new MovieAdapterHolder(view);
    }

    @Override
    public void onBindViewHolder(MovieAdapterHolder holder, int position) {
        holder.bind(mMovies.get(position));

        if (!mMovieListener.isFavorite() && position == mMovies.size() - 5) {
            mMovieListener.loadMovieData();
        }
    }

    @Override
    public int getItemCount() {
        if (mMovies == null) {
            return 0;
        }
        return mMovies.size();
    }

    public ArrayList<Movie> swapMovies(Movie[] movies) {
        ArrayList<Movie> temp = mMovies;

        if (movies != null) {
            mMovies = new ArrayList<>(Arrays.asList(movies));
            notifyDataSetChanged();
        } else {
            mMovies = null;
        }
        return temp;
    }

    public void addMovies(Movie[] movies) {
        if (movies == null) {
            mMovies = null;
        } else if (mMovies == null) {
            mMovies = new ArrayList<>(Arrays.asList(movies));
        } else {
            mMovies.addAll(Arrays.asList(movies));
        }
        notifyDataSetChanged();
    }

    class MovieAdapterHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        final ImageView mPosterImageView;

        public MovieAdapterHolder(View itemView) {
            super(itemView);
            mPosterImageView = (ImageView) itemView.findViewById(R.id.iv_poster);
            itemView.setOnClickListener(this);
        }

        public void bind(Movie movie) {
            NetworkUtils.loadPosterImage(mContext, movie.posterPath, mPosterImageView);
        }

        @Override
        public void onClick(View v) {
            mMovieListener.onClick(mMovies.get(getAdapterPosition()), mPosterImageView);
        }
    }
}
