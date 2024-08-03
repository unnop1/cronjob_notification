package com.nt.cronjob_notification.util;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalTime;

public class DateTime {

    public static final Timestamp getTimeStampNow(){
        Instant instant = Instant.now();
        return Timestamp.from(instant);
    }

    public static boolean isCurrentTimeInRange(LocalTime startTime, LocalTime endTime) {
        LocalTime currentTime = LocalTime.now();
        return !currentTime.isBefore(startTime) && !currentTime.isAfter(endTime);
    }
}
