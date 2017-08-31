package br.com.killkinto.popmovies;

import android.content.ContentValues;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

import br.com.killkinto.popmovies.databinding.ActivityDetailMovieBinding;
import br.com.killkinto.popmovies.model.Movie;
import static br.com.killkinto.popmovies.data.MovieContract.MovieEntry;

public class DetailMovieActivity extends AppCompatActivity {

    public static final String EXTRA_MOVIE = "br.com.killkinto.popmovies.EXTRA_MOVIE";

    private Movie mMovie;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_movie);

        Intent intent = getIntent();

        if (intent != null && intent.hasExtra(EXTRA_MOVIE)) {
            mMovie = intent.getParcelableExtra(EXTRA_MOVIE);
            ActivityDetailMovieBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_detail_movie);
            binding.setMovie(mMovie);
        }
    }

    public void makeAsFavorito() {
        if (mMovie != null) {
            ContentValues cv = new ContentValues();
            cv.put(MovieEntry.COLUMN_ORIGINAL_TITLE, mMovie.originalTitle);
            cv.put(MovieEntry.COLUMN_TITLE, mMovie.title);
            cv.put(MovieEntry.COLUMN_OVERVIEW, mMovie.overview);
            cv.put(MovieEntry.COLUMN_RELEASE_DATE, mMovie.releaseDate);
            cv.put(MovieEntry.COLUMN_VOTE_AVERAGE, mMovie.voteAverage);

            Uri uri = getContentResolver().insert(MovieEntry.CONTENT_URI, cv);

            if (uri != null) {
                //TODO salvar imagem

                //TODO mover string
                Toast.makeText(getBaseContext(), "Filme marcado como favorito!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void desmakeAsFavorite(int movieId) {
        Uri uri = MovieEntry.CONTENT_URI.buildUpon().appendPath(String.valueOf(movieId)).build();
        int rows = getContentResolver().delete(uri, null, null);

        if (rows > 0) {
            //TODO deletar imagem

            //TODO mover string
            Toast.makeText(getBaseContext(), "Filme desmarcado como favorito!", Toast.LENGTH_SHORT).show();
        }
    }
}
