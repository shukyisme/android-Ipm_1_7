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

    public static Date parseDate(String dateString, String pattern, TimeZone tz) {
        if(dateString == null || pattern == null || tz == null) {
            return null;
        }

        DateFormat df = new SimpleDateFormat(pattern);
        df.setTimeZone(tz);
        Date result =  null;
        try {
            result = df.parse(dateString);
        } catch (ParseException e) {

        }

        return result;
    }

    public static String formatDate(Date date, String pattern, TimeZone tz) {
        if(date == null || pattern == null || tz == null) {
            return null;
        }

        DateFormat df = new SimpleDateFormat(pattern);
        df.setTimeZone(tz);
        String result = null;
        result = df.format(date);

        return result;
    }

    public static String convertToClientTimezone(String dateString, String timezone) {
        if(dateString == null || dateString.trim().equals("")) {
            return dateString;
        }

        TimeZone tz;
        if(timezone == null || timezone.trim().equals("")) {
            tz = TimeZone.getDefault();
        } else {
            tz = TimeZone.getTimeZone(timezone);
        }
        Date d = parseDate(dateString, SERVER_DATE_FORMAT, TimeZone.getTimeZone("UTC"));
        return formatDate(d, DISPLAY_DATE_FORMAT, tz);
    }
}
