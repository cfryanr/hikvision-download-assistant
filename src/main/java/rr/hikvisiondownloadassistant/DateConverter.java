// Copyright (c) 2020 Ryan Richard

package rr.hikvisiondownloadassistant;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class DateConverter {

    private static final SimpleDateFormat apiDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
    private static final SimpleDateFormat localDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
    private static final SimpleDateFormat localFilenameDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH-mm-ss");
    private static final SimpleDateFormat localHumanDateFormat = new SimpleDateFormat("EEEE MMM d, yyyy 'at' h:mm:ss aaa z");

    static {
        apiDateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
    }

    public static Date apiStringToDate(String timeString) {
        try {
            return apiDateFormat.parse(timeString);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

    public static String dateToApiString(Date time) {
        return apiDateFormat.format(time);
    }

    public static String dateToLocalString(Date time) {
        return localDateFormat.format(time);
    }

    public static String dateToLocalFilenameString(Date time) {
        return localFilenameDateFormat.format(time);
    }

    public static String dateToLocalHumanString(Date time) {
        return localHumanDateFormat.format(time);
    }

}
