package utils;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.TimeZone;

public class DateTimeUtils {
    public static LocalDateTime parseTimeToCurrentTimeZone(String datetime){
        return ZonedDateTime.parse(datetime).withZoneSameInstant(TimeZone.getDefault().toZoneId()).toLocalDateTime();
    }

    public static void verifyLocalDateTime(LocalDateTime before, LocalDateTime after, LocalDateTime actual){

    }
}
