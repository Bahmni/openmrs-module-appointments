package org.openmrs.module.appointments.util;

import org.apache.commons.lang3.StringUtils;
import org.openmrs.Location;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.DateTimeException;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Date;
import java.util.Objects;
import java.util.Optional;
import java.util.TimeZone;

public class DateUtil {

    private static final String TIMEZONE_ATTRIBUTE = "timeZone";

    public enum DateFormatType {
        UTC("yyyy-MM-dd'T'HH:mm:ss.SSS");

        private final String dateFormat;

        DateFormatType(String dateFormat) {
            this.dateFormat = dateFormat;
        }

        public String getDateFormat() {
            return dateFormat;
        }
    }

    public static Date convertToDate(String dateString, DateFormatType dateFormat) throws ParseException {
        if (StringUtils.isEmpty(dateString) || dateFormat == null) {
            return null;
        }
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(dateFormat.getDateFormat());
        return simpleDateFormat.parse(dateString);
    }

    public static Date convertToLocalDateFromUTC(String dateString) throws ParseException {
        if (StringUtils.isEmpty(dateString)) {
            return null;
        }
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(DateUtil.DateFormatType.UTC.dateFormat);
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        return simpleDateFormat.parse(dateString);
    }

    public static String convertUTCToGivenFormat(Date dateTime, String format, String timeZone) {
        if (dateTime == null || StringUtils.isEmpty(format) || StringUtils.isEmpty(timeZone)) {
            return null;
        }
        DateFormat givenFormat = new SimpleDateFormat(format);
        TimeZone givenTimeZone = TimeZone.getTimeZone(timeZone);
        givenFormat.setTimeZone(givenTimeZone);
        return givenFormat.format(dateTime);
    }

    public static Calendar getCalendar(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        return calendar;
    }

    public static Date getStartOfDay() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, calendar.getMinimum(Calendar.HOUR_OF_DAY));
        calendar.set(Calendar.MINUTE, calendar.getMinimum(Calendar.MINUTE));
        calendar.set(Calendar.SECOND, calendar.getMinimum(Calendar.SECOND));
        calendar.set(Calendar.MILLISECOND, calendar.getMinimum(Calendar.MILLISECOND));
        return calendar.getTime();
    }

    public static long getEpochTime(long date) {
        Calendar calendar = getCalendar(new Date(date));
        int hours = calendar.get(Calendar.HOUR_OF_DAY);
        int minutes = calendar.get(Calendar.MINUTE);
        int seconds = calendar.get(Calendar.SECOND);
        long milliSeconds = ((hours * 3600 + minutes * 60 + seconds) * 1000);
        return milliSeconds;
    }

    public static long getEpochTime(long date, ZoneId zone) {
        java.time.ZonedDateTime zdt = new Date(date).toInstant().atZone(zone);
        return (zdt.getHour() * 3600L + zdt.getMinute() * 60L + zdt.getSecond()) * 1000L;
    }

    public static Optional<ZoneId> getLocationZone(Location location) {
        if (location == null) return Optional.empty();
        return location.getActiveAttributes().stream()
                .filter(attr -> TIMEZONE_ATTRIBUTE.equalsIgnoreCase(attr.getAttributeType().getName()))
                .map(attr -> {
                    try {
                        return ZoneId.of(String.valueOf(attr.getValue()));
                    } catch (DateTimeException e) {
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .findFirst();
    }

    public static Date getEndOfDay() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, calendar.getMaximum(Calendar.HOUR_OF_DAY));
        calendar.set(Calendar.MINUTE, calendar.getMaximum(Calendar.MINUTE));
        calendar.set(Calendar.SECOND, calendar.getMaximum(Calendar.SECOND));
        calendar.set(Calendar.MILLISECOND, calendar.getMaximum(Calendar.MILLISECOND));
        return calendar.getTime();
    }
}


