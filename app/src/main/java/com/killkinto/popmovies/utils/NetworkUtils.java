package com.killkinto.popmovies.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.util.Log;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Locale;
import java.util.Scanner;

public final class NetworkUtils {

    private static final String TAG = NetworkUtils.class.getSimpleName();

    private static int TYPE_WIFI = 1;
    private static int TYPE_MOBILE = 2;
    private static int TYPE_NOT_CONNECTED = 0;

    private static final String THEMOVIEDB_API_URL = "http://api.themoviedb.org/3/movie/";
    private static final String VIDEO_PATH = "videos";
    private static final String API_KEY_PARAM = "api_key";
    private static final String API_KEY = "20b0067bc1655a85d13693138118fdc8";
    private static final String LANGUAGE_PARAM = "language";
    private static final String LANGUAGE_DEFULT = "en-US";
    private static final String PAGE_PARAM = "page";

    private static final String URL = "http://image.tmdb.org/t/p/w185";

    //Youtube
    private static final String YOUTUBE_URL = "http://www.youtube.com";
    private static final String YOUTUBE_WATCH_PATH = "watch";
    private static final String YOUTUBE_KEY_PARAM = "v";

    public static URL buildUrl(String sortOrder, int page) {
        Uri builtUri = Uri.parse(THEMOVIEDB_API_URL + sortOrder).buildUpon()
                .appendQueryParameter(API_KEY_PARAM, API_KEY)
                .appendQueryParameter(LANGUAGE_PARAM, Locale.getDefault().toString().replace('_', '-'))
                .appendQueryParameter(PAGE_PARAM, String.valueOf(page))
                .build();
        URL url = null;
        try {
            url = new URL(builtUri.toString());
        } catch (MalformedURLException e) {
            Log.e(TAG, e.getMessage());
        }

        Log.v(TAG, "Built URI " + url);

        return url;
    }

    public static URL buildUrlTrailers(int movieId) {
       Uri builtUri = Uri.parse(THEMOVIEDB_API_URL).buildUpon()
                .appendPath(String.valueOf(movieId))
                .appendPath(VIDEO_PATH)
                .appendQueryParameter(API_KEY_PARAM, API_KEY)
                .appendQueryParameter(LANGUAGE_PARAM, Locale.getDefault().toString().replace('_', '-'))
                .build();
        URL url = null;
        try {
            url = new URL(builtUri.toString());
        } catch (MalformedURLException e) {
            Log.e(TAG, e.getMessage());
        }

        Log.v(TAG, "Built URI " + url);

        return url;
    }

    public static Uri buildUrlYoutube(String key) {
        return Uri.parse(YOUTUBE_URL).buildUpon()
                .appendPath(YOUTUBE_WATCH_PATH)
                .appendQueryParameter(YOUTUBE_KEY_PARAM, key)
                .build();
    }

    public static String getResponseFromHttpUrl(URL url) throws IOException {

        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();

        try {
            InputStream in = urlConnection.getInputStream();

            Scanner scanner = new Scanner(in);
            scanner.useDelimiter("\\A");

            boolean hasInput = scanner.hasNext();
            if (hasInput) {
                return scanner.next();
            } else {
                return null;
            }
        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
            return null;
        } finally {
            urlConnection.disconnect();
        }
    }

    public static void loadPosterImage(Context context, String posterPath, ImageView imageView) {
        posterPath = URL + posterPath;
        Picasso.with(context)
                .load(posterPath)
                .into(imageView);
    }

    public static int getConnectivityStatus(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        if (null != activeNetwork) {
            if(activeNetwork.getType() == ConnectivityManager.TYPE_WIFI)
                return TYPE_WIFI;

            if(activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE)
                return TYPE_MOBILE;
        }
        return TYPE_NOT_CONNECTED;
    }

    public static String getConnectivityStatusString(Context context) {
        int conn = getConnectivityStatus(context);
        String status = null;
        if (conn == TYPE_WIFI) {
            status = "Wifi enabled";
        } else if (conn == TYPE_MOBILE) {
            status = "Mobile data enabled";
        } else if (conn == TYPE_NOT_CONNECTED) {
            status = "Not connected to Internet";
        }
        return status;
    }

    public static boolean isConnected(Context context) {
        return TYPE_NOT_CONNECTED != getConnectivityStatus(context);
    }
}
