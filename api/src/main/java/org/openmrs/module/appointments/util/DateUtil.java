package org.openmrs.module.appointments.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import org.apache.commons.lang3.StringUtils;

public class DateUtil {

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
}


