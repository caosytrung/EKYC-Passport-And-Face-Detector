package com.fast.ekyc.utils.checksum;

import java.util.Calendar;
import java.util.Date;

public class DateTimeUtils {
    public static Date atBeginningOfDay() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTime();
    }


    public static Date addTime(Date sendDate, int hours, int minutes, int seconds) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(sendDate);
        calendar.add(Calendar.HOUR_OF_DAY, hours);
        calendar.add(Calendar.MINUTE, minutes);
        calendar.add(Calendar.SECOND, seconds);
        return calendar.getTime();
    }


    public static long timeDiffInMillis(Date sendDate, Date currDate) {
        return (sendDate.getTime() - currDate.getTime());
    }
}
