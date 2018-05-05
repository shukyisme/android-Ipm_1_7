package me.kwk.utils;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 * Created by Daniel on 5/3/2018.
 */

public class DateUtils {

    public static final String SERVER_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";
    public static final String DISPLAY_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";

    public static Date parseDate(String dateString, String pattern, String timezone) {
        if(dateString == null || pattern == null) {
            return null;
        }

        DateFormat df = new SimpleDateFormat(pattern);
        TimeZone tz;
        if(timezone == null || timezone.trim().equals("")) {
            tz = TimeZone.getDefault();
        } else {
            tz = TimeZone.getTimeZone(timezone);
        }
        df.setTimeZone(tz);
        Date result =  null;
        try {
            result = df.parse(dateString);
        } catch (ParseException e) {

        }

        return result;
    }

    public static String formatDate(Date date, String pattern, String timezone) {
        if(date == null || pattern == null) {
            return null;
        }

        DateFormat df = new SimpleDateFormat(pattern);
        TimeZone tz;
        if(timezone == null || timezone.trim().equals("")) {
            tz = TimeZone.getDefault();
        } else {
            tz = TimeZone.getTimeZone(timezone);
        }
        df.setTimeZone(tz);

        return df.format(date);
    }

    public static String convertToClientTimezone(String dateString, String pattern, String timezone) {
        if(dateString == null || dateString.trim().equals("")) {
            return dateString;
        }

        Date d = parseDate(dateString, SERVER_DATE_FORMAT, "UTC");
        return formatDate(d, pattern, timezone);
    }

    public static String getTimePassed(String dateString) {
        if(dateString == null || dateString.trim().equals("")) {
            return dateString;
        }

        Date d = parseDate(dateString, SERVER_DATE_FORMAT, "UTC");
        if(d != null) {
            Date currentDate = new Date();
            long diff = currentDate.getTime() - d.getTime();
            long hours = diff / (1000 * 60 * 60);
            long minutes = (diff - hours * (1000 * 60 * 60)) / (1000 * 60);

            return hours + ":" + minutes;
        }

        return "";
    }
}
