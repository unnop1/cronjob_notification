package com.nt.cronjob_notification.util;

import java.sql.Timestamp;
import java.time.Instant;

public class DateTime {

    public static final Timestamp getTimeStampNow(){
        Instant instant = Instant.now();
        return Timestamp.from(instant);
    }

}
