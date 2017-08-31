package br.com.killkinto.popmovies;

import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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
import java.net.URL;

import br.com.killkinto.popmovies.data.MovieContract;
import br.com.killkinto.popmovies.model.Movie;
import br.com.killkinto.popmovies.utils.NetworkUtils;
import br.com.killkinto.popmovies.utils.OpenMoviePopularJsonUtils;

public class MainActivity extends AppCompatActivity
        implements MovieAdapter.MovieAdapterListener, LoaderManager.LoaderCallbacks<Cursor> {

    private static final String TAG = MainActivity.class.getSimpleName();

    private static final String SORT_ORDER = "sort_order";
    private static final int MOVIE_LOADER_FAVORITES_ID = 145;

    private MovieAdapter mAdapter;
    private RecyclerView mRecyclerView;
    private ProgressBar mLoadingIndicatorProgressBar;
    private Button mTryAgainButton;
    private Menu mMainMenu;
    private String mSortOrder = NetworkUtils.POPULAR_SORT_ORDER_RESOURCE;
    private int mPage = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if(savedInstanceState != null && savedInstanceState.containsKey(SORT_ORDER)) {
            mSortOrder = savedInstanceState.getString(SORT_ORDER, NetworkUtils.POPULAR_SORT_ORDER_RESOURCE);
        }

        mRecyclerView = (RecyclerView) findViewById(R.id.rv_movie_poster);
        mLoadingIndicatorProgressBar = (ProgressBar) findViewById(R.id.pb_loading_indicator);
        mTryAgainButton = (Button) findViewById(R.id.bt_try_again);

        RecyclerView.LayoutManager layoutManager = new GridLayoutManager(this, 2);
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setHasFixedSize(true);

        mAdapter = new MovieAdapter(this);
        mRecyclerView.setAdapter(mAdapter);

        loadMovieData();
    }

    @Override
    public void onSaveInstanceState(Bundle bundle) {
        bundle.putString(SORT_ORDER, mSortOrder);
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

        if (mSortOrder.equals(NetworkUtils.POPULAR_SORT_ORDER_RESOURCE)) {
            menu.findItem(R.id.action_sort_order_most_popular).setVisible(false);
        } else {
            menu.findItem(R.id.action_sort_order_top_rated).setVisible(false);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();

        loadMovieData();
        switch (itemId) {
            case R.id.action_sort_order_most_popular:
                mAdapter.addMovies(null);
                mSortOrder = NetworkUtils.POPULAR_SORT_ORDER_RESOURCE;
                mPage = 1;
                loadMovieData();
                item.setVisible(false);
                mMainMenu.findItem(R.id.action_sort_order_top_rated).setVisible(true);
                return true;
            case R.id.action_sort_order_top_rated:
                mAdapter.addMovies(null);
                mSortOrder = NetworkUtils.TOP_RATED_SORT_ORDER_RESOURCE;
                mPage = 1;
                loadMovieData();
                item.setVisible(false);
                mMainMenu.findItem(R.id.action_sort_order_most_popular).setVisible(true);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showFavoritesCollection() {

    }

    @Override
    public Loader<Cursor> onCreateLoader(int loaderId, final Bundle args) {
        if (loaderId == MOVIE_LOADER_FAVORITES_ID) {
            return loaderFavorites();
        } else {
            return null;
        }
    }

    private Loader<Cursor> loaderFavorites() {
        return new AsyncTaskLoader<Cursor>(this) {

            Cursor mMoviesData = null;

            @Override
            protected void onStartLoading() {
                if (mMoviesData != null) {
                    deliverResult(mMoviesData);
                } else {
                    forceLoad();
                }
            }

            @Override
            public Cursor loadInBackground() {
                try {
                    return getContentResolver().query(MovieContract.MovieEntry.CONTENT_URI,
                            null, null, null, null);
                } catch (Exception e) {
                    Log.e(TAG, e.getMessage());
                    return null;
                }
            }

            @Override
            public void deliverResult(Cursor data) {
                mMoviesData = data;
                super.deliverResult(data);
            }
        };
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        //TODO atualizar o Adapter
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mAdapter.addMovies(null);
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
           URL url = NetworkUtils.buildUrl(mSortOrder, mPage);

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
                showMovieDataView();
                mAdapter.addMovies(movies);
            } else {
                showErrorMessage(getString(R.string.error_message));
            }
        }
    }
}
