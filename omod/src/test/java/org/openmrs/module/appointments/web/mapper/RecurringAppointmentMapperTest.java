package org.openmrs.module.appointments.web.mapper;

import org.apache.commons.lang.time.DateUtils;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.openmrs.module.appointments.model.Appointment;
import org.openmrs.module.appointments.model.AppointmentRecurringPattern;
import org.openmrs.module.appointments.service.impl.RecurringAppointmentType;
import org.openmrs.module.appointments.web.contract.AppointmentDefaultResponse;
import org.openmrs.module.appointments.web.contract.RecurringAppointmentDefaultResponse;
import org.openmrs.module.appointments.web.contract.RecurringPattern;
import org.openmrs.module.appointments.web.util.AppointmentBuilder;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class RecurringAppointmentMapperTest {

    @InjectMocks
    private RecurringAppointmentMapper recurringAppointmentMapper;

    @Mock
    private AppointmentMapper appointmentMapper;

    @Mock
    private RecurringPatternMapper recurringPatternMapper;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void shouldSetRecurringPatternWithFrequencyToAppointmentResponseObject() {
        RecurringPattern recurringPattern = new RecurringPattern();
        recurringPattern.setType("DAY");
        recurringPattern.setFrequency(3);
        recurringPattern.setPeriod(2);
        Appointment appointmentOne = new AppointmentBuilder()
                .withStartDateTime(DateUtils.addDays(new Date(), -2))
                .withEndDateTime(DateUtils.addDays(new Date(), -2))
                .build();
        Appointment appointmentTwo = new AppointmentBuilder()
                .withStartDateTime(new Date())
                .withEndDateTime(new Date())
                .build();
        Appointment appointmentThree = new AppointmentBuilder()
                .withStartDateTime(DateUtils.addDays(new Date(), 2))
                .withEndDateTime(DateUtils.addDays(new Date(), 2))
                .build();
        when(appointmentMapper.constructResponse(any(Appointment.class))).thenReturn(new AppointmentDefaultResponse());
        when(recurringPatternMapper.mapToResponse(any(AppointmentRecurringPattern.class))).thenReturn(recurringPattern);

        List<RecurringAppointmentDefaultResponse> recurringAppointmentDefaultResponses =
                recurringAppointmentMapper.constructResponse(Arrays.asList(appointmentOne, appointmentTwo, appointmentThree));

        assertEquals(new Integer(3), recurringAppointmentDefaultResponses.get(0).getRecurringPattern().getFrequency());
        assertEquals(new Integer(3), recurringAppointmentDefaultResponses.get(1).getRecurringPattern().getFrequency());
        assertEquals(new Integer(3), recurringAppointmentDefaultResponses.get(2).getRecurringPattern().getFrequency());
        assertNull(recurringAppointmentDefaultResponses.get(0).getRecurringPattern().getEndDate());
        assertNull(recurringAppointmentDefaultResponses.get(1).getRecurringPattern().getEndDate());
        assertNull(recurringAppointmentDefaultResponses.get(2).getRecurringPattern().getEndDate());
        assertEquals(2, recurringAppointmentDefaultResponses.get(0).getRecurringPattern().getPeriod());
        assertEquals(2, recurringAppointmentDefaultResponses.get(1).getRecurringPattern().getPeriod());
        assertEquals(2, recurringAppointmentDefaultResponses.get(2).getRecurringPattern().getPeriod());
        assertEquals("DAY", recurringAppointmentDefaultResponses.get(0).getRecurringPattern().getType());
        assertEquals("DAY", recurringAppointmentDefaultResponses.get(1).getRecurringPattern().getType());
        assertEquals("DAY", recurringAppointmentDefaultResponses.get(2).getRecurringPattern().getType());
        assertNull(recurringAppointmentDefaultResponses.get(0).getRecurringPattern().getDaysOfWeek());
        assertNull(recurringAppointmentDefaultResponses.get(1).getRecurringPattern().getDaysOfWeek());
        assertNull(recurringAppointmentDefaultResponses.get(2).getRecurringPattern().getDaysOfWeek());
        verify(appointmentMapper, times(3)).constructResponse(any(Appointment.class));
        verify(recurringPatternMapper, times(3)).mapToResponse(any());
    }

    @Test
    public void shouldSetEndDateAndDaysOfWeekWhenRecurringPatternHasEndDate() throws ParseException {
        RecurringPattern recurringPattern = new RecurringPattern();
        recurringPattern.setType("WEEK");
        Date endDate = new Date();
        recurringPattern.setEndDate(endDate);
        recurringPattern.setPeriod(1);
        recurringPattern.setDaysOfWeek(Arrays.asList("MONDAY", "TUESDAY"));
        Appointment appointmentOne = new AppointmentBuilder()
                .withStartDateTime(new SimpleDateFormat("dd/MM/yyyy").parse("17/06/2019"))
                .withEndDateTime(new SimpleDateFormat("dd/MM/yyyy").parse("17/06/2019"))
                .build();
        Appointment appointmentTwo = new AppointmentBuilder()
                .withStartDateTime(new SimpleDateFormat("dd/MM/yyyy").parse("18/06/2019"))
                .withEndDateTime(new SimpleDateFormat("dd/MM/yyyy").parse("18/06/2019"))
                .build();
        when(appointmentMapper.constructResponse(any(Appointment.class))).thenReturn(new AppointmentDefaultResponse());
        when(recurringPatternMapper.mapToResponse(any(AppointmentRecurringPattern.class))).thenReturn(recurringPattern);

        List<RecurringAppointmentDefaultResponse> recurringAppointmentDefaultResponses =
                recurringAppointmentMapper.constructResponse(Arrays.asList(appointmentOne, appointmentTwo));

        assertEquals(endDate, recurringAppointmentDefaultResponses.get(0).getRecurringPattern().getEndDate());
        assertEquals(endDate, recurringAppointmentDefaultResponses.get(1).getRecurringPattern().getEndDate());
        assertNull(recurringAppointmentDefaultResponses.get(0).getRecurringPattern().getFrequency());
        assertNull(recurringAppointmentDefaultResponses.get(1).getRecurringPattern().getFrequency());
        assertEquals(1, recurringAppointmentDefaultResponses.get(0).getRecurringPattern().getPeriod());
        assertEquals(1, recurringAppointmentDefaultResponses.get(1).getRecurringPattern().getPeriod());
        assertEquals(RecurringAppointmentType.WEEK.name(), recurringAppointmentDefaultResponses.get(0).getRecurringPattern().getType());
        assertEquals(RecurringAppointmentType.WEEK.name(), recurringAppointmentDefaultResponses.get(1).getRecurringPattern().getType());
        assertEquals(Arrays.asList("MONDAY", "TUESDAY"),
                recurringAppointmentDefaultResponses.get(0).getRecurringPattern().getDaysOfWeek());
        assertEquals(Arrays.asList("MONDAY", "TUESDAY"),
                recurringAppointmentDefaultResponses.get(1).getRecurringPattern().getDaysOfWeek());
        verify(appointmentMapper, times(2)).constructResponse(any(Appointment.class));
        verify(recurringPatternMapper, times(2)).mapToResponse(any());
    }

    @Test
    public void shouldSetFrequencyAndDaysOfWeekToRecurringPatternWhenDaysOfWeekIsNotNull() throws ParseException {
        RecurringPattern recurringPattern = new RecurringPattern();
        recurringPattern.setType("WEEK");
        recurringPattern.setFrequency(1);
        recurringPattern.setPeriod(1);
        recurringPattern.setDaysOfWeek(Arrays.asList("Monday", "Tuesday"));
        Appointment appointmentOne = new AppointmentBuilder()
                .withStartDateTime(new SimpleDateFormat("dd/MM/yyyy").parse("17/06/2019"))
                .withEndDateTime(new SimpleDateFormat("dd/MM/yyyy").parse("17/06/2019"))
                .build();
        when(appointmentMapper.constructResponse(any(Appointment.class))).thenReturn(new AppointmentDefaultResponse());
        when(recurringPatternMapper.mapToResponse(any(AppointmentRecurringPattern.class))).thenReturn(recurringPattern);

        List<RecurringAppointmentDefaultResponse> recurringAppointmentDefaultResponses =
                recurringAppointmentMapper.constructResponse(Arrays.asList(appointmentOne));

        assertEquals(new Integer(1), recurringAppointmentDefaultResponses.get(0).getRecurringPattern().getFrequency());
        assertEquals(1, recurringAppointmentDefaultResponses.get(0).getRecurringPattern().getPeriod());
        assertEquals(RecurringAppointmentType.WEEK.name(), recurringAppointmentDefaultResponses.get(0).getRecurringPattern().getType());
        assertEquals(Arrays.asList("Monday", "Tuesday"),
                recurringAppointmentDefaultResponses.get(0).getRecurringPattern().getDaysOfWeek());
        assertNull(recurringAppointmentDefaultResponses.get(0).getRecurringPattern().getEndDate());
        verify(appointmentMapper).constructResponse(any(Appointment.class));
        verify(recurringPatternMapper).mapToResponse(any(AppointmentRecurringPattern.class));
    }
}
