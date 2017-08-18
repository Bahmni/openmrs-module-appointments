package org.openmrs.module.appointments.util;

import org.apache.commons.lang.ObjectUtils;
import org.junit.Assert;
import org.junit.Test;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.Date;

import static org.junit.Assert.assertEquals;
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
        assertEquals("Wed Mar 15 16:57:09 IST 2017", date.toString());
    }

    @Test
    public void shouldConvertDateStringInUTCtoDate() throws ParseException {
        Date date = DateUtil.convertToLocalDateFromUTC("2017-03-15T16:57:09.0Z");
        assertEquals("Wed Mar 15 22:27:09 IST 2017", date.toString());
    }

}
