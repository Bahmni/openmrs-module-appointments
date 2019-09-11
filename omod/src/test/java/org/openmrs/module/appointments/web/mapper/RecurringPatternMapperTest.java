package org.openmrs.module.appointments.web.mapper;

import org.junit.Before;
import org.junit.Test;
import org.openmrs.module.appointments.model.AppointmentRecurringPattern;
import org.openmrs.module.appointments.service.impl.RecurringAppointmentType;
import org.openmrs.module.appointments.web.contract.RecurringPattern;

import java.util.Arrays;
import java.util.Date;

import static org.junit.Assert.assertEquals;

public class RecurringPatternMapperTest {

    private RecurringPatternMapper recurringPatternMapper;

    @Before
    public void setUp() {
        recurringPatternMapper = new RecurringPatternMapper();
    }

    @Test
    public void shouldSetAllFieldsOfAppointmentRecurringPatternWithRecurringTypeDay() {
        RecurringPattern recurringPattern = new RecurringPattern();
        recurringPattern.setType("DAY");
        recurringPattern.setPeriod(1);
        recurringPattern.setFrequency(2);

        AppointmentRecurringPattern appointmentRecurringPattern = recurringPatternMapper.fromRequest(recurringPattern);

        assertEquals(appointmentRecurringPattern.getType(), RecurringAppointmentType.DAY);
        assertEquals(appointmentRecurringPattern.getPeriod(), new Integer(1));
        assertEquals(appointmentRecurringPattern.getFrequency(), new Integer(2));
    }

    @Test
    public void shouldSetAllFieldsOfAppointmentRecurringPatternWithRecurringTypeWeek() {
        RecurringPattern recurringPattern = new RecurringPattern();
        recurringPattern.setType("WEEK");
        recurringPattern.setPeriod(1);
        Date endDate = new Date();
        recurringPattern.setEndDate(endDate);
        recurringPattern.setDaysOfWeek(Arrays.asList("MON", "TUE"));

        AppointmentRecurringPattern appointmentRecurringPattern = recurringPatternMapper.fromRequest(recurringPattern);

        assertEquals(appointmentRecurringPattern.getType(), RecurringAppointmentType.WEEK);
        assertEquals(appointmentRecurringPattern.getPeriod(), new Integer(1));
        assertEquals(appointmentRecurringPattern.getEndDate(), endDate);
        assertEquals(appointmentRecurringPattern.getDaysOfWeek(), "MON,TUE");
    }

    @Test
    public void shouldMapDatabaseObjectToResponseWhenTypeIsDay() {
        AppointmentRecurringPattern appointmentRecurringPattern = new AppointmentRecurringPattern();
        appointmentRecurringPattern.setFrequency(2);
        appointmentRecurringPattern.setPeriod(1);
        appointmentRecurringPattern.setType(RecurringAppointmentType.DAY);

        RecurringPattern recurringPattern = recurringPatternMapper.mapToResponse(appointmentRecurringPattern);

        assertEquals("DAY", recurringPattern.getType());
        assertEquals(1, recurringPattern.getPeriod());
        assertEquals(new Integer(2), recurringPattern.getFrequency());
    }

    @Test
    public void shouldMapDatabaseObjectToResponseWhenTypeIsWeek() {
        AppointmentRecurringPattern appointmentRecurringPattern = new AppointmentRecurringPattern();
        Date endDate = new Date();
        appointmentRecurringPattern.setEndDate(endDate);
        appointmentRecurringPattern.setPeriod(1);
        appointmentRecurringPattern.setType(RecurringAppointmentType.WEEK);
        appointmentRecurringPattern.setDaysOfWeek("MON,TUE");

        RecurringPattern recurringPattern = recurringPatternMapper.mapToResponse(appointmentRecurringPattern);

        assertEquals("WEEK", recurringPattern.getType());
        assertEquals(1, recurringPattern.getPeriod());
        assertEquals(endDate, recurringPattern.getEndDate());
        assertEquals(2, recurringPattern.getDaysOfWeek().size());
    }
}
