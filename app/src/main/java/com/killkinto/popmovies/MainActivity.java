package com.killkinto.popmovies;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import org.json.JSONException;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;

import com.killkinto.popmovies.model.Movie;
import com.killkinto.popmovies.utils.NetworkUtils;
import com.killkinto.popmovies.utils.OpenMoviePopularJsonUtils;

import static com.killkinto.popmovies.data.MovieContract.MovieEntry;

public class MainActivity extends AppCompatActivity
        implements MovieAdapter.MovieAdapterListener,
        LoaderManager.LoaderCallbacks<Movie[]> {

    private static final String TAG = MainActivity.class.getSimpleName();
    private static final String POPULAR_SORT_ORDER_RESOURCE = "popular";
    private static final String TOP_RATED_SORT_ORDER_RESOURCE = "top_rated";
    private static final String FAVORITES_COLLETIONS = "favorites_colletions";

    private static final String OPTION_COLLECTION = "option_colletion";
    private static final String COLLECTION = "colletions";
    private static final int MOVIE_LOADER_FAVORITES_ID = 145;

    private ArrayList<Movie> mMovies;
    private MovieAdapter mAdapter;
    private RecyclerView mRecyclerView;
    private ProgressBar mLoadingIndicatorProgressBar;
    private Button mTryAgainButton;
    private Menu mMainMenu;
    private String mOptionCollection = POPULAR_SORT_ORDER_RESOURCE;
    private int mPage = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mRecyclerView = (RecyclerView) findViewById(R.id.rv_movie_poster);
        mLoadingIndicatorProgressBar = (ProgressBar) findViewById(R.id.pb_loading_indicator);
        mTryAgainButton = (Button) findViewById(R.id.bt_try_again);

        RecyclerView.LayoutManager layoutManager = new GridLayoutManager(this, 2);
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setHasFixedSize(true);

        mAdapter = new MovieAdapter(this);
        mRecyclerView.setAdapter(mAdapter);

        if(savedInstanceState != null && savedInstanceState.containsKey(COLLECTION)) {
            mOptionCollection = savedInstanceState.getString(OPTION_COLLECTION, mOptionCollection);
            mMovies = savedInstanceState.getParcelableArrayList(COLLECTION);
            mAdapter.swapMovies(mMovies.toArray(new Movie[mMovies.size()]));
        } else if (!mOptionCollection.equals(FAVORITES_COLLETIONS)) {
            loadMovieData();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mOptionCollection.equals(FAVORITES_COLLETIONS)) {
            new LoadFavoritesCollection(this).execute();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle bundle) {
        bundle.putParcelableArrayList(COLLECTION, mMovies);
        bundle.putString(OPTION_COLLECTION, mOptionCollection);
        super.onSaveInstanceState(bundle);
    }

    @Override
    public void loadMovieData() {
        if (mPage <= OpenMoviePopularJsonUtils.totalPages) {
            if (NetworkUtils.isConnected(this)) {
                new FetchTheMoviedbTask().execute();
            } else {
                showErrorMessage(getString(R.string.error_no_connection));
            }
        }
    }

    @Override
    public boolean isFavorite() {
        return mOptionCollection.equals(FAVORITES_COLLETIONS);
    }

    @Override
    public void onClick(Movie movie) {
        Intent intent = new Intent(this, DetailMovieActivity.class);
        intent.putExtra(DetailMovieActivity.EXTRA_MOVIE, movie);
        startActivity(intent);
    }

    public void onTryAgain(View v) {
        loadMovieData();
    }

    private void showMovieDataView() {
        mTryAgainButton.setVisibility(View.GONE);
        mRecyclerView.setVisibility(View.VISIBLE);
    }

    private void showErrorMessage(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        mRecyclerView.setVisibility(View.GONE);
        mTryAgainButton.setVisibility(View.VISIBLE);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        mMainMenu = menu;
        super.getMenuInflater().inflate(R.menu.main, menu);

        switch (mOptionCollection) {
            case POPULAR_SORT_ORDER_RESOURCE:
                menu.findItem(R.id.action_sort_order_most_popular).setEnabled(false);
                break;
            case TOP_RATED_SORT_ORDER_RESOURCE:
                menu.findItem(R.id.action_sort_order_top_rated).setEnabled(false);
                break;
            case FAVORITES_COLLETIONS:
                menu.findItem(R.id.action_favorite).setEnabled(false);
                break;
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();

        mMovies.clear();
        switch (itemId) {
            case R.id.action_sort_order_most_popular:
                mAdapter.addMovies(null);
                mOptionCollection = POPULAR_SORT_ORDER_RESOURCE;
                mPage = 1;
                loadMovieData();
                item.setEnabled(false);
                mMainMenu.findItem(R.id.action_sort_order_top_rated).setEnabled(true);
                mMainMenu.findItem(R.id.action_favorite).setEnabled(true);
                return true;
            case R.id.action_sort_order_top_rated:
                mAdapter.addMovies(null);
                mOptionCollection = TOP_RATED_SORT_ORDER_RESOURCE;
                mPage = 1;
                loadMovieData();
                item.setEnabled(false);
                mMainMenu.findItem(R.id.action_sort_order_most_popular).setEnabled(true);
                mMainMenu.findItem(R.id.action_favorite).setEnabled(true);
                return true;
            case R.id.action_favorite:
                new LoadFavoritesCollection(this).execute();
                mOptionCollection = FAVORITES_COLLETIONS;
                item.setEnabled(false);
                mMainMenu.findItem(R.id.action_sort_order_top_rated).setEnabled(true);
                mMainMenu.findItem(R.id.action_sort_order_most_popular).setEnabled(true);
                return true;

        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public Loader<Movie[]> onCreateLoader(int loaderId, final Bundle args) {
        if (loaderId == MOVIE_LOADER_FAVORITES_ID) {
            return loaderFavorites();
        } else {
            return null;
        }
    }

    private Loader<Movie[]> loaderFavorites() {
        return new AsyncTaskLoader<Movie[]>(this) {

            Movie[] mMoviesData = null;

            @Override
            protected void onStartLoading() {
                if (mMoviesData != null) {
                    deliverResult(mMoviesData);
                } else {
                    forceLoad();
                }
            }

            @Override
            public Movie[] loadInBackground() {
                Cursor cursor = null;
                try {
                    cursor = getContentResolver().query(MovieEntry.CONTENT_URI,
                            new String[]{MovieEntry._ID, MovieEntry.COLUMN_POSTER_PATH}, null, null, null);

                    if (cursor != null && cursor.moveToFirst()) {
                        mMoviesData = new Movie[cursor.getCount()];
                        Movie m;
                        int i = 0;
                        while (cursor.moveToNext()) {
                            m = new Movie();
                            m.id = cursor.getInt(cursor.getColumnIndex(MovieEntry._ID));
                            m.posterPath = cursor.getString(cursor.getColumnIndex(MovieEntry.COLUMN_POSTER_PATH));
                            mMoviesData[i++] = m;
                        }
                    }
                } catch (Exception e) {
                    Log.e(TAG, e.getMessage());
                    return null;
                } finally {
                    if (cursor != null) {
                        cursor.close();
                    }
                }
                return mMoviesData;
            }

            @Override
            public void deliverResult(Movie[] data) {
                mMoviesData = data;
                super.deliverResult(data);
            }
        };
    }

    @Override
    public void onLoadFinished(Loader<Movie[]> loader, Movie[] data) {
        mAdapter.swapMovies(data);
    }

    @Override
    public void onLoaderReset(Loader<Movie[]> loader) {
        mAdapter.swapMovies(null);
    }

    //TODO Usar o loader
    private class FetchTheMoviedbTask extends AsyncTask<Void, Void, Movie[]> {

        private final String TAG = NetworkUtils.class.getSimpleName();

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mLoadingIndicatorProgressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected Movie[] doInBackground(Void... params) {
           URL url = NetworkUtils.buildUrl(mOptionCollection, mPage);

            try {
                String response = NetworkUtils.getResponseFromHttpUrl(url);
                Movie[] movies = OpenMoviePopularJsonUtils.getMoviesFromJson(response);
                ++mPage;
                return movies;
            } catch (IOException | JSONException | OpenMoviePopularJsonUtils.OpenMoviePopularJSonException e) {
                Log.e(TAG, e.getMessage());
            }
            return null;
        }

        @Override
        protected void onPostExecute(Movie[] movies) {
            mLoadingIndicatorProgressBar.setVisibility(View.GONE);
            if (movies != null && movies.length > 0) {
                if (mMovies == null) {
                    mMovies = new ArrayList<>(movies.length);
                }
                mMovies.addAll(Arrays.asList(movies));
                showMovieDataView();
                mAdapter.addMovies(movies);
            } else {
                showErrorMessage(getString(R.string.error_message));
            }
        }
    }

    private static class LoadFavoritesCollection extends AsyncTask<Void,Void,Cursor> {

        private WeakReference<MainActivity> mContext;

        LoadFavoritesCollection(MainActivity context) {
            mContext = new WeakReference<>(context);
        }

        @Override
        protected Cursor doInBackground(Void... voids) {
            Uri uri = MovieEntry.CONTENT_URI;
            return mContext.get().getContentResolver().query(uri,  new String[]{MovieEntry._ID, MovieEntry.COLUMN_POSTER_PATH}, null, null, null);
        }

        @Override
        protected void onPostExecute(Cursor cursor) {
            super.onPostExecute(cursor);
            Movie[] favorites = null;

            if (cursor != null) {
                if (cursor.moveToFirst()){
                    favorites = new Movie[cursor.getCount()];
                    int i = 0;
                    do {
                        favorites[i++] = new Movie(cursor.getInt(cursor.getColumnIndex(MovieEntry._ID)),
                                cursor.getString(cursor.getColumnIndex(MovieEntry.COLUMN_POSTER_PATH)));
                    } while (cursor.moveToNext());
                    mContext.get().mMovies.addAll(Arrays.asList(favorites));
                }
                cursor.close();
            }

            mContext.get().mAdapter.swapMovies(favorites);
        }
    }
}
