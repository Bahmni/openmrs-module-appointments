package org.openmrs.module.appointments.web.helper;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.openmrs.module.appointments.model.Appointment;
import org.openmrs.module.appointments.model.AppointmentRecurringPattern;
import org.openmrs.module.appointments.web.contract.RecurringAppointmentRequest;
import org.openmrs.module.appointments.web.contract.RecurringPattern;
import org.openmrs.module.appointments.web.mapper.RecurringPatternMapper;
import org.openmrs.module.appointments.web.service.AbstractRecurringAppointmentsGenerationService;
import org.openmrs.module.appointments.web.service.impl.RecurringAppointmentsService;
import org.springframework.beans.factory.annotation.Qualifier;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

public class RecurringAppointmentsServiceTest {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @InjectMocks
    private RecurringAppointmentsService recurringAppointmentsService;

    @Mock
    @Qualifier("dailyRecurringAppointmentsGenerationService")
    AbstractRecurringAppointmentsGenerationService dailyRecurringAppointmentsGenerationService;

    @Mock
    @Qualifier("weeklyRecurringAppointmentsGenerationService")
    AbstractRecurringAppointmentsGenerationService weeklyRecurringAppointmentsGenerationService;

    @Mock
    RecurringPatternMapper recurringPatternMapper;

    @Before
    public void setUp() {
        initMocks(this);
    }

    @Test
    public void shouldCallDailyRecurringAppointmentServiceWhenRecurringRequestIsOfTypeDay() {
        RecurringAppointmentRequest recurringAppointmentRequest = mock(RecurringAppointmentRequest.class);
        RecurringPattern recurringPattern = mock(RecurringPattern.class);
        when(recurringAppointmentRequest.getRecurringPattern()).thenReturn(recurringPattern);
        when(recurringPattern.getType()).thenReturn("DaY");
        Appointment appointmentOne = new Appointment(); appointmentOne.setUuid("uuid1");
        Appointment appointmentTwo = new Appointment(); appointmentOne.setUuid("uuid2");
        List<Appointment> appointments = Arrays.asList(appointmentOne, appointmentTwo);
        when(dailyRecurringAppointmentsGenerationService.generateAppointments(recurringAppointmentRequest))
                .thenReturn(appointments);
        AppointmentRecurringPattern appointmentRecurringPattern = new AppointmentRecurringPattern();
        when(recurringPatternMapper.fromRequest(recurringPattern)).thenReturn(appointmentRecurringPattern);

        recurringAppointmentsService.generateAppointmentRecurringPatternWithAppointments(recurringAppointmentRequest);

        verify(recurringPatternMapper).fromRequest(recurringPattern);
        verify(dailyRecurringAppointmentsGenerationService).generateAppointments(recurringAppointmentRequest);
        assertEquals(2, appointmentRecurringPattern.getAppointments().size());
    }

    @Test
    public void shouldCallWeeklyRecurringAppointmentServiceWhenRecurringRequestIsOfTypeWeek() {
        RecurringAppointmentRequest recurringAppointmentRequest = mock(RecurringAppointmentRequest.class);
        RecurringPattern recurringPattern = mock(RecurringPattern.class);
        when(recurringAppointmentRequest.getRecurringPattern()).thenReturn(recurringPattern);
        when(recurringPattern.getType()).thenReturn("WeeK");
        List<Appointment> appointments = Collections.singletonList(new Appointment());
        when(weeklyRecurringAppointmentsGenerationService.generateAppointments(recurringAppointmentRequest))
                .thenReturn(appointments);
        AppointmentRecurringPattern appointmentRecurringPattern = new AppointmentRecurringPattern();
        when(recurringPatternMapper.fromRequest(recurringPattern)).thenReturn(appointmentRecurringPattern);

        recurringAppointmentsService.generateAppointmentRecurringPatternWithAppointments(recurringAppointmentRequest);

        verify(recurringPatternMapper).fromRequest(recurringPattern);
        verify(weeklyRecurringAppointmentsGenerationService).generateAppointments(recurringAppointmentRequest);
        assertEquals(1, appointmentRecurringPattern.getAppointments().size());
    }
}
