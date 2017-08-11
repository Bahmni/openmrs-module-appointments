package org.openmrs.module.appointments.util;

import org.apache.commons.lang.ObjectUtils;
import org.junit.Assert;
import org.junit.Test;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class DateUtilTest {
    @Test
    public void shouldReturnNullifDateStringisEmpty() throws ParseException {
        Date date = DateUtil.convertToDate("", DateUtil.DateFormatType.UTC);
        assertNull(date);
    }

    @Test
    public void shouldReturnNullifDateFormatTypeisNull() throws ParseException {
        DateUtil.DateFormatType dateFormatType = null;
        Date date = DateUtil.convertToDate("2017-03-15T16:57:09.0Z", dateFormatType);
        assertNull(date);
    }

    @Test
    public void shouldConvertStringtoDate() throws ParseException {
        Date date = DateUtil.convertToDate("2017-03-15T16:57:09.0Z", DateUtil.DateFormatType.UTC);
        assertNotNull(date);
        boolean daylightTime = TimeZone.getDefault().inDaylightTime(date);
        String timeZoneShort = TimeZone.getDefault().getDisplayName(daylightTime, TimeZone.SHORT, Locale.ENGLISH);
        String expectedDateString = "Wed Mar 15 16:57:09 ".concat(timeZoneShort).concat(" 2017");
        assertEquals(expectedDateString, date.toString());
    }

    @Test
    public void shouldReturnNUllifDateStringIsNull() throws ParseException {
        Date date = DateUtil.convertToLocalDateFromUTC(null);
        assertNull(date);
    }
}
