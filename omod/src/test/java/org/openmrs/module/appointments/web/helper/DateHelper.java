package org.openmrs.module.appointments.web.helper;

import java.util.Calendar;
import java.util.Date;

public class DateHelper {
    private static Calendar calendar = Calendar.getInstance();

    public static Date getDate(int year, int month, int day) {
        calendar.set(year, month, day);
        return calendar.getTime();
    }

    public static Date getDate(int year, int month, int day, int hour, int minute, int second) {
        calendar.set(year, month, day, hour, minute, second);
        return calendar.getTime();
    }
}
