package org.openmrs.module.appointments.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.openmrs.module.appointments.util.DateUtil.convertToLocalDateFromUTC;
import static org.openmrs.module.appointments.util.DateUtil.getEpochTime;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import org.junit.Test;

public class DateUtilTest {
    @Test
    public void shouldReturnNullIfDateStringIsEmpty() throws ParseException {
        Date date = DateUtil.convertToDate("", DateUtil.DateFormatType.UTC);
        assertNull(date);
    }

    @Test
    public void shouldReturnNullIfDateFormatTypeIsNull() throws ParseException {
        DateUtil.DateFormatType dateFormatType = null;
        Date date = DateUtil.convertToDate("2017-03-15T16:57:09.0Z", dateFormatType);
        assertNull(date);
    }

    @Test
    public void shouldConvertStringToDate() throws ParseException {
        Date date = DateUtil.convertToDate("2017-03-15T16:57:09.0Z", DateUtil.DateFormatType.UTC);
        assertNotNull(date);
        boolean daylightTime = TimeZone.getDefault().inDaylightTime(date);
        String timeZoneShort = TimeZone.getDefault().getDisplayName(daylightTime, TimeZone.SHORT, Locale.ENGLISH);
        String expectedDateString = "Wed Mar 15 16:57:09 ".concat(timeZoneShort).concat(" 2017");
        assertEquals(expectedDateString, date.toString());
    }

    @Test
    public void shouldReturnNullIfDateStringIsNull() throws ParseException {
        Date date = convertToLocalDateFromUTC(null);
        assertNull(date);
    }

    @Test
    public void shouldReturnLocalDateWithGivenUTCDate() throws ParseException {
        String dateString = "2017-03-15T16:57:09.0Z";
        DateUtil dateUtil = new DateUtil();
        Date date = convertToLocalDateFromUTC(dateString);
        boolean daylightTime = TimeZone.getDefault().inDaylightTime(date);
        String timeZoneShort = TimeZone.getDefault().getDisplayName(daylightTime, TimeZone.SHORT, Locale.ENGLISH);
        assertNotNull(date);
        assertTrue(date.toString().contains(timeZoneShort));
    }

    @Test
    public void shouldReturnCalendarObjectForGivenDate() {
        Date date = new Date();
        Calendar calendar = DateUtil.getCalendar(date);

        assertEquals(calendar.getTime(), date);
    }

    @Test
    public void shouldReturnStartOfDay() {
        Date date = DateUtil.getStartOfDay();
        SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
        String time = dateFormat.format(date);

        assertEquals("00:00:00", time);
    }

    @Test
    public void shouldReturnEndOfDay() {
        Date date = DateUtil.getEndOfDay();
        SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
        String time = dateFormat.format(date);

        assertEquals("23:59:59", time);
    }

    @Test
    public void shouldConvertDateToMilliSeconds() throws ParseException {
    	TimeZone.setDefault(TimeZone.getTimeZone("IST"));
        String dateString = "2017-03-15T16:57:09.0Z";
        Date date = convertToLocalDateFromUTC(dateString);
        long milliSeconds = getEpochTime(date.getTime());
        assertEquals(80829000, milliSeconds);
    }

    @Test
    public void shouldReturnZeroWhenPassedLongIsNegative() {
    	TimeZone.setDefault(TimeZone.getTimeZone("IST"));
        long milliSeconds = getEpochTime(-10000);
        assertEquals(19790000, milliSeconds);
    }
}
