package org.openmrs.module.appointments.web.helper;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.openmrs.module.appointments.web.contract.RecurringAppointmentRequest;
import org.openmrs.module.appointments.web.contract.RecurringPattern;
import org.openmrs.module.appointments.web.service.AbstractRecurringAppointmentsGenerationService;
import org.openmrs.module.appointments.web.service.impl.RecurringAppointmentsService;
import org.springframework.beans.factory.annotation.Qualifier;

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

        recurringAppointmentsService.generateRecurringAppointments(recurringAppointmentRequest);

        verify(dailyRecurringAppointmentsGenerationService).getAppointments(recurringAppointmentRequest);
    }

    @Test
    public void shouldCallWeeklyRecurringAppointmentServiceWhenRecurringRequestIsOfTypeWeek() {
        RecurringAppointmentRequest recurringAppointmentRequest = mock(RecurringAppointmentRequest.class);
        RecurringPattern recurringPattern = mock(RecurringPattern.class);
        when(recurringAppointmentRequest.getRecurringPattern()).thenReturn(recurringPattern);
        when(recurringPattern.getType()).thenReturn("WeeK");

        recurringAppointmentsService.generateRecurringAppointments(recurringAppointmentRequest);

        verify(weeklyRecurringAppointmentsGenerationService).getAppointments(recurringAppointmentRequest);
    }
}
