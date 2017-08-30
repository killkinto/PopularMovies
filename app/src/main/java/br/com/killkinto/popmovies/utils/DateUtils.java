package br.com.killkinto.popmovies.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public final class DateUtils {

    public static Date convertToDate(String dateString, String format){
        SimpleDateFormat formatDateHour = new SimpleDateFormat(format, Locale.getDefault());
        try {
            return formatDateHour.parse(dateString);
        } catch (ParseException e) {
            return null;
        }
    }

    public static String convertToString(Date date) {
        if (date == null) {
            return null;
        }
        SimpleDateFormat formatDateHour = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        return formatDateHour.format(date);
    }
}
