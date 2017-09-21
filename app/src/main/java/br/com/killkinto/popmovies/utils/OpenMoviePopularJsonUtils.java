package br.com.killkinto.popmovies.utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import br.com.killkinto.popmovies.model.Movie;
import br.com.killkinto.popmovies.model.Trailer;


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

        JSONObject moviesJson = new JSONObject(moviesList);

        validateStatus(moviesJson);

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

    public static List<Trailer> parseResultTrailers(String trailerList) throws JSONException, OpenMoviePopularJSonException {
        final String RESULTS = "results";
        final String NAME = "name";
        final String KEY = "key";
        final String TYPE = "type";
        final String TRAILER_TYPE = "Trailer";
        final String SITE = "site";

        JSONObject trailerJson = new JSONObject(trailerList);

        validateStatus(trailerJson);

        JSONArray resultsJson = trailerJson.getJSONArray(RESULTS);

        List<Trailer> trailers = new ArrayList<>(5);

        for (int i = 0; i < resultsJson.length(); i++) {
            JSONObject resultJson = resultsJson.getJSONObject(i);

            if (resultJson.getString(TYPE).equals(TRAILER_TYPE)) {
                trailers.add(new Trailer(resultJson.getString(NAME),
                        resultJson.getString(KEY),
                        resultJson.getString(SITE)));
            }
        }
        return trailers;
    }

    private static void validateStatus(JSONObject object) throws JSONException, OpenMoviePopularJSonException {
        final String STATUS_CODE = "status_code";
        //Message erro
        final String STATUS_MESSAGE = "status_message";

        if (object.has(STATUS_CODE)) {
            int codeResponse = object.getInt(STATUS_CODE);

            switch (codeResponse) {
                case HttpURLConnection.HTTP_OK:
                    break;
                case HttpURLConnection.HTTP_UNAUTHORIZED:
                case HttpURLConnection.HTTP_NOT_FOUND:
                    throw new OpenMoviePopularJSonException(object.getString(STATUS_MESSAGE));
            }
        }
    }

    public static class OpenMoviePopularJSonException extends Exception {
        public OpenMoviePopularJSonException(String message) {
            super(message);
        }
    }
}
