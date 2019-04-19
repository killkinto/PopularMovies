package com.killkinto.popmovies;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.databinding.DataBindingUtil;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.transition.TransitionInflater;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;
import android.widget.Button;

import org.json.JSONException;

import java.io.IOException;
import java.net.URL;
import java.util.List;

import com.killkinto.popmovies.databinding.ActivityDetailMovieBinding;
import com.killkinto.popmovies.model.Movie;
import com.killkinto.popmovies.model.Trailer;
import com.killkinto.popmovies.utils.NetworkUtils;
import com.killkinto.popmovies.utils.OpenMoviePopularJsonUtils;

import static com.killkinto.popmovies.data.MovieContract.MovieEntry;

public class DetailMovieActivity extends AppCompatActivity
        implements TrailerAdapter.TrailerAdapterListener, LoaderManager.LoaderCallbacks<List<Trailer>> {

    private static final String TAG = DetailMovieActivity.class.getSimpleName();

    public static final String EXTRA_MOVIE = "com.killkinto.popmovies.EXTRA_MOVIE";
    public static final String EXTRA_MOVIE_ID = "com.killkinto.popmovies.EXTRA_MOVIE_ID";

    private static final int ID_TRAILER_LOADER = 74;

    private Movie mMovie;

    private RecyclerView mTrailerRecyclerView;
    private TrailerAdapter mTrailerAdapter;
    private Button mFavoriteButton;
    private boolean mFavorito;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_movie);

        Intent intent = getIntent();

        if (intent != null && intent.hasExtra(EXTRA_MOVIE)) {
            mMovie = intent.getParcelableExtra(EXTRA_MOVIE);
            //Se for um favorito
            if (mMovie.id != null) {
                Uri uri = MovieEntry.CONTENT_URI.buildUpon().appendPath(String.valueOf(mMovie.id)).build();
                Cursor cursor = getContentResolver().query( uri, null, null, null, null);
                if (cursor!= null && cursor.moveToFirst()) {
                    mMovie.title = cursor.getString(cursor.getColumnIndex(MovieEntry.COLUMN_TITLE));
                    mMovie.originalTitle = cursor.getString(cursor.getColumnIndex(MovieEntry.COLUMN_ORIGINAL_TITLE));
                    mMovie.title = cursor.getString(cursor.getColumnIndex(MovieEntry.COLUMN_TITLE));
                    mMovie.releaseDate = cursor.getString(cursor.getColumnIndex(MovieEntry.COLUMN_RELEASE_DATE));
                    mMovie.voteAverage = cursor.getString(cursor.getColumnIndex(MovieEntry.COLUMN_VOTE_AVERAGE));
                    mMovie.overview = cursor.getString(cursor.getColumnIndex(MovieEntry.COLUMN_OVERVIEW));
                    mFavorito = true;
                    cursor.close();
                }
            }
            ActivityDetailMovieBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_detail_movie);
            binding.setMovie(mMovie);

            Bundle bundle = new Bundle();
            bundle.putInt(EXTRA_MOVIE_ID, mMovie.id);

            buttonFavorite();

            LoaderManager.getInstance(this).initLoader(ID_TRAILER_LOADER, bundle, this);
            //getSupportLoaderManager().initLoader(ID_TRAILER_LOADER, bundle, this);

            getWindow().setSharedElementEnterTransition(TransitionInflater.from(this)
                .inflateTransition(R.transition.curve));

            animateViewsIn();
        }
    }

    private void animateViewsIn() {
        ViewGroup root = findViewById(R.id.root);
        int count = root.getChildCount();
        float offset = 300;
        Interpolator interpolator = AnimationUtils.loadInterpolator(this, android.R.interpolator.linear_out_slow_in);

        for (int i = 0; i < count; i++) {
            View view = root.getChildAt(i);
            view.setVisibility(View.VISIBLE);
            view.setTranslationY(offset);
            view.setAlpha(0.85f);
            view.animate()
                    .translationY(0f)
                    .alpha(1f)
                    .setInterpolator(interpolator)
                    .setDuration(3000)
                    .start();
            offset *= 1.5f;
        }
    }

    private void buttonFavorite() {
        mFavoriteButton = (Button) findViewById(R.id.bt_favorite);

        if (mFavorito) {
            mFavoriteButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_star_yellow, 0, 0, 0);
        }

        mFavoriteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Drawable star = mFavoriteButton.getCompoundDrawables()[0];
                if (mFavorito) {
                    desmakeAsFavorite(star);
                } else {
                    makeAsFavorito(star);
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.getMenuInflater().inflate(R.menu.detail, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            supportFinishAfterTransition();
            return true;
        }
        if (item.getItemId() ==  R.id.action_reviews) {
            Intent reviewIntent = new Intent(this, ReviewActivity.class);
            reviewIntent.putExtra(EXTRA_MOVIE_ID, mMovie.id);
            startActivity(reviewIntent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void makeAsFavorito(@NonNull final Drawable star) {
        if (mMovie != null) {
            ContentValues cv = new ContentValues();
            cv.put(MovieEntry._ID, mMovie.id);
            cv.put(MovieEntry.COLUMN_ORIGINAL_TITLE, mMovie.originalTitle);
            cv.put(MovieEntry.COLUMN_TITLE, mMovie.title);
            cv.put(MovieEntry.COLUMN_POSTER_PATH, mMovie.posterPath);
            cv.put(MovieEntry.COLUMN_OVERVIEW, mMovie.overview);
            cv.put(MovieEntry.COLUMN_RELEASE_DATE, mMovie.releaseDate);
            cv.put(MovieEntry.COLUMN_VOTE_AVERAGE, mMovie.voteAverage);

            Uri uri = getContentResolver().insert(MovieEntry.CONTENT_URI, cv);

            if (uri != null) {

                star.setColorFilter(ContextCompat.getColor(this, R.color.colorYellow), PorterDuff.Mode.SRC_IN);
                mFavorito = true;
            }
        }
    }

    private void desmakeAsFavorite(@NonNull final Drawable star) {
        Uri uri = MovieEntry.CONTENT_URI.buildUpon().appendPath(String.valueOf(mMovie.id)).build();
        int rows = getContentResolver().delete(uri, null, null);

        if (rows > 0) {
            star.setColorFilter(ContextCompat.getColor(this, R.color.colorWhite), PorterDuff.Mode.SRC_IN);
            mFavorito = false;
        }
    }

    @Override
    public void onClick(Trailer trailer) {
        if (trailer != null && !TextUtils.isEmpty(trailer.mKey)) {
            startActivity(new Intent(Intent.ACTION_VIEW, NetworkUtils.buildUrlYoutube(trailer.mKey)));
        }
    }

    @Override
    public Loader<List<Trailer>> onCreateLoader(int id, final Bundle args) {
        return new AsyncTaskLoader<List<Trailer>>(this) {

            List<Trailer> mTrailers = null;

            @Override
            protected void onStartLoading() {
                if (mTrailers != null) {
                    deliverResult(mTrailers);
                }else {
                    forceLoad();
                }
            }

            @Override
            public List<Trailer> loadInBackground() {
                int movieId = args.getInt(EXTRA_MOVIE_ID);
                URL url = NetworkUtils.buildUrlTrailers(movieId);

                try {
                    String response = NetworkUtils.getResponseFromHttpUrl(url);
                    mTrailers =  OpenMoviePopularJsonUtils.parseResultTrailers(response);
                } catch (IOException | JSONException | OpenMoviePopularJsonUtils.OpenMoviePopularJSonException e) {
                    Log.e(TAG, e.getMessage());
                }
                return mTrailers;
            }

            @Override
            public void deliverResult(List<Trailer> data) {
                mTrailers = data;
                super.deliverResult(data);
            }
        };
    }

    @Override
    public void onLoadFinished(Loader<List<Trailer>> loader, List<Trailer> data) {
        mTrailerRecyclerView = (RecyclerView) findViewById(R.id.rv_trailers);
        mTrailerAdapter = new TrailerAdapter(this, this);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        mTrailerRecyclerView.setLayoutManager(layoutManager);
        mTrailerRecyclerView.setHasFixedSize(true);
        mTrailerRecyclerView.setAdapter(mTrailerAdapter);
        mTrailerAdapter.swapTrailers(data);
    }

    @Override
    public void onLoaderReset(Loader<List<Trailer>> loader) {
        mTrailerAdapter.swapTrailers(null);
    }
}
