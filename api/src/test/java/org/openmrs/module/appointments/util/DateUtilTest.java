package org.openmrs.module.appointments.util;

import org.junit.Assert;
import org.junit.Test;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import static org.junit.Assert.*;

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
        Date date = DateUtil.convertToLocalDateFromUTC(null);
        assertNull(date);
    }

    @Test
    public void shouldReturnLocalDateWithGivenUTCDate() throws ParseException {
        String dateString = "2017-03-15T16:57:09.0Z";
        DateUtil dateUtil = new DateUtil();
        Date date = dateUtil.convertToLocalDateFromUTC(dateString);
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
}
