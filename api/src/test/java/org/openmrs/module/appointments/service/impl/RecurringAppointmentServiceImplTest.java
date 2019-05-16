package org.openmrs.module.appointments.service.impl;


import org.junit.Before;
import org.junit.Test;
import org.openmrs.module.appointments.model.AppointmentRecurringPattern;
import org.openmrs.module.appointments.service.RecurringAppointmentService;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class RecurringAppointmentServiceImplTest {

    private RecurringAppointmentService recurringAppointmentService;

    @Before
    public void setUp() throws Exception {
        recurringAppointmentService = new RecurringAppointmentServiceImpl();
    }

    @Test
    public void shouldReturnThreeAppointmentsForGivenRecurringPatternAndStartDateTime() throws Exception {
        Calendar calendar = Calendar.getInstance();

        calendar.set(2019, Calendar.MAY, 14, 18, 20, 00);
        Date appointmentStartDateTime = calendar.getTime();

        AppointmentRecurringPattern appointmentRecurringPattern = new AppointmentRecurringPattern();
        appointmentRecurringPattern.setFrequency(3);
        appointmentRecurringPattern.setPeriod(1);
        appointmentRecurringPattern.setType(RecurringAppointmentType.DAY);

        List<Date> recurringDates = recurringAppointmentService.getRecurringDates(appointmentStartDateTime,
                appointmentRecurringPattern);
        assertEquals(3, recurringDates.size());
        assertEquals(getDate(2019, Calendar.MAY, 14, 00, 00, 00).toString(),
                recurringDates.get(0).toString());
        assertEquals(getDate(2019, Calendar.MAY, 15, 00, 00, 00).toString(),
                recurringDates.get(1).toString());
        assertEquals(getDate(2019, Calendar.MAY, 16, 00, 00, 00).toString(),
                recurringDates.get(2).toString());

    }

    @Test
    public void shouldReturnFourAppointmentsAcrossMonths() throws Exception {
        Calendar calendar = Calendar.getInstance();

        calendar.set(2019, Calendar.MAY, 14, 18, 20, 00);
        Date appointmentStartDateTime = calendar.getTime();

        AppointmentRecurringPattern appointmentRecurringPattern = new AppointmentRecurringPattern();
        appointmentRecurringPattern.setFrequency(4);
        appointmentRecurringPattern.setPeriod(15);
        appointmentRecurringPattern.setType(RecurringAppointmentType.DAY);

        List<Date> recurringDates = recurringAppointmentService.getRecurringDates(appointmentStartDateTime,
                appointmentRecurringPattern);
        assertEquals(4, recurringDates.size());
        assertEquals(getDate(2019, Calendar.MAY, 14, 00, 00, 00).toString(),
                recurringDates.get(0).toString());
        assertEquals(getDate(2019, Calendar.MAY, 29, 00, 00, 00).toString(),
                recurringDates.get(1).toString());
        assertEquals(getDate(2019, Calendar.JUNE, 13, 00, 00, 00).toString(),
                recurringDates.get(2).toString());
        assertEquals(getDate(2019, Calendar.JUNE, 28, 00, 00, 00).toString(),
                recurringDates.get(3).toString());

    }

    @Test
    public void shouldReturnFourAppointmentsAcrossTwoYears() throws Exception {
        Calendar calendar = Calendar.getInstance();

        calendar.set(2019, Calendar.MAY, 14, 18, 20, 00);
        Date appointmentStartDateTime = calendar.getTime();

        AppointmentRecurringPattern appointmentRecurringPattern = new AppointmentRecurringPattern();
        appointmentRecurringPattern.setFrequency(4);
        appointmentRecurringPattern.setPeriod(100);
        appointmentRecurringPattern.setType(RecurringAppointmentType.DAY);

        List<Date> recurringDates = recurringAppointmentService.getRecurringDates(appointmentStartDateTime,
                appointmentRecurringPattern);
        assertEquals(4, recurringDates.size());
        assertEquals(getDate(2019, Calendar.MAY, 14, 00, 00, 00).toString(),
                recurringDates.get(0).toString());
        assertEquals(getDate(2019, Calendar.AUGUST, 22, 00, 00, 00).toString(),
                recurringDates.get(1).toString());
        assertEquals(getDate(2019, Calendar.NOVEMBER, 30, 00, 00, 00).toString(),
                recurringDates.get(2).toString());
        assertEquals(getDate(2020, Calendar.MARCH, 9, 00, 00, 00).toString(),
                recurringDates.get(3).toString());

    }

    private static Date getDate(int year, int month, int day, int hour, int minute, int second) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month, day, hour, minute, second);
        return calendar.getTime();
    }

}
