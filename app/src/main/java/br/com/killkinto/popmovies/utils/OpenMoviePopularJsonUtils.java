package br.com.killkinto.popmovies.utils;

import android.content.Context;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.HttpURLConnection;
import java.util.Date;

import br.com.killkinto.popmovies.model.Movie;


public final class OpenMoviePopularJsonUtils {

    public static int totalPages = Integer.MAX_VALUE;
    public static int totalResults;

    public static Movie[] getMoviesFromJson(String moviesList)
            throws JSONException, OpenMoviePopularJSonException {
        //Specify which page to query.
        final String PAGE = "page";
        //Total pages
        final String TOTAL_PAGES = "total_pages";
        //Total results
        final String TOTAL_RESULTS = "total_results";
        //Movie List Result Object

        final String STATUS_CODE = "status_code";
        //Message erro
        final String STATUS_MESSAGE = "status_message";

        JSONObject moviesJson = new JSONObject(moviesList);

        if (moviesJson.has(STATUS_CODE)) {
            int codeResponse = moviesJson.getInt(STATUS_CODE);

            switch (codeResponse) {
                case HttpURLConnection.HTTP_OK:
                    break;
                case HttpURLConnection.HTTP_UNAUTHORIZED:
                case HttpURLConnection.HTTP_NOT_FOUND:
                    throw new OpenMoviePopularJSonException(moviesJson.getString(STATUS_MESSAGE));
                default:
                    return null;
            }
        }

        totalPages = moviesJson.getInt(TOTAL_PAGES);
        totalResults = moviesJson.getInt(TOTAL_RESULTS);

        return parseResults(moviesJson);

    }

    private static Movie[] parseResults(JSONObject moviesJson) throws JSONException {
        final String RESULTS = "results";
        //Can return null
        final String POSTER_PATH = "poster_path";

        final String OVERVIEW = "overview";
        final String MOVIE_ID = "id";
        final String VOTE_AVERAGE = "vote_average";
        final String RELEASE_DATE = "release_date";

        final String ORIGINAL_TITLE = "original_title";
        final String TITLE = "title";

        JSONArray resultsJson = moviesJson.getJSONArray(RESULTS);

        Movie[] movies = new Movie[resultsJson.length()];
        Movie movie;

        for (int i = 0; i < resultsJson.length(); i++) {
            JSONObject resultJson = resultsJson.getJSONObject(i);

            movie = new Movie();
            movie.id = resultJson.getInt(MOVIE_ID);
            movie.posterPath = resultJson.getString(POSTER_PATH);
            movie.overview = resultJson.getString(OVERVIEW);
            movie.originalTitle = resultJson.getString(ORIGINAL_TITLE);
            movie.title = resultJson.getString(TITLE);
            movie.releaseDate = resultJson.getString(RELEASE_DATE);
            Date releaseDate = DateUtils.convertToDate(movie.releaseDate, "yyyy-MM-dd");
            movie.releaseDate = DateUtils.convertToString(releaseDate);
            movie.voteAverage = resultJson.getString(VOTE_AVERAGE);

            movies[i] = movie;
        }
        return movies;
    }

    public static class OpenMoviePopularJSonException extends Exception {
        public OpenMoviePopularJSonException(String message) {
            super(message);
        }
    }
}
