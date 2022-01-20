package com.mykovolod.mando.utils;


import org.apache.commons.lang3.time.DurationFormatUtils;

import java.time.Duration;
import java.time.Instant;

public class TimeUtils {

    public static String timeSince(Instant start) {
        return timeBetween(start, Instant.now());
    }

    public static String timeBetween(Instant start, Instant end) {
        return DurationFormatUtils.formatDurationWords(Duration.between(start, end).toMillis(),true,true);
    }

}

