package br.com.killkinto.popmovies;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import br.com.killkinto.popmovies.databinding.ActivityDetailMovieBinding;
import br.com.killkinto.popmovies.model.Movie;

public class DetailMovieActivity extends AppCompatActivity {

    public static final String EXTRA_MOVIE = "br.com.killkinto.popmovies.EXTRA_MOVIE";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_movie);

        Intent intent = getIntent();

        if (intent != null && intent.hasExtra(EXTRA_MOVIE)) {
            Movie movie = intent.getParcelableExtra(EXTRA_MOVIE);
            ActivityDetailMovieBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_detail_movie);
            binding.setMovie(movie);
        }
    }
}
