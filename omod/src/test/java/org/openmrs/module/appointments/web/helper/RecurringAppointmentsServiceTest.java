package org.openmrs.module.appointments.web.helper;

import org.apache.commons.lang.time.DateUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.openmrs.module.appointments.model.AppointmentRecurringPattern;
import org.openmrs.module.appointments.service.impl.RecurringAppointmentType;
import org.openmrs.module.appointments.web.contract.RecurringAppointmentRequest;
import org.openmrs.module.appointments.web.contract.RecurringPattern;
import org.openmrs.module.appointments.web.service.AbstractRecurringAppointmentsService;
import org.openmrs.module.appointments.web.service.impl.RecurringAppointmentsService;
import org.springframework.beans.factory.annotation.Qualifier;

import java.util.Date;

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
    AbstractRecurringAppointmentsService dailyRecurringAppointmentsGenerationService;

    @Mock
    @Qualifier("weeklyRecurringAppointmentsGenerationService")
    AbstractRecurringAppointmentsService weeklyRecurringAppointmentsGenerationService;

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

        verify(dailyRecurringAppointmentsGenerationService).generateAppointments(recurringAppointmentRequest);
    }

    @Test
    public void shouldCallWeeklyRecurringAppointmentServiceWhenRecurringRequestIsOfTypeWeek() {
        RecurringAppointmentRequest recurringAppointmentRequest = mock(RecurringAppointmentRequest.class);
        RecurringPattern recurringPattern = mock(RecurringPattern.class);
        when(recurringAppointmentRequest.getRecurringPattern()).thenReturn(recurringPattern);
        when(recurringPattern.getType()).thenReturn("WeeK");

        recurringAppointmentsService.generateRecurringAppointments(recurringAppointmentRequest);

        verify(weeklyRecurringAppointmentsGenerationService).generateAppointments(recurringAppointmentRequest);
    }

    @Test
    public void shouldCallAddAppointmentsWhenFrequencyIsIncreasedForTypeDay() {
        AppointmentRecurringPattern appointmentRecurringPattern =
                createAppointmentRecurringPattern(RecurringAppointmentType.DAY, 1, 2, null);
        RecurringAppointmentRequest recurringAppointmentRequest =
                createRecurringAppointmentRequest("DAY", 1, 3, null);

        recurringAppointmentsService.getUpdatedSetOfAppointments(appointmentRecurringPattern, recurringAppointmentRequest);

        verify(dailyRecurringAppointmentsGenerationService).addAppointments(appointmentRecurringPattern, recurringAppointmentRequest);
    }

    @Test
    public void shouldCallRemoveAppointmentsWhenFrequencyIsDecreasedForTypeDay() {
        AppointmentRecurringPattern appointmentRecurringPattern =
                createAppointmentRecurringPattern(RecurringAppointmentType.DAY, 1, 3, null);
        RecurringAppointmentRequest recurringAppointmentRequest =
                createRecurringAppointmentRequest("DAY", 1, 2, null);

        recurringAppointmentsService.getUpdatedSetOfAppointments(appointmentRecurringPattern, recurringAppointmentRequest);

        verify(dailyRecurringAppointmentsGenerationService).removeRecurringAppointments(appointmentRecurringPattern, recurringAppointmentRequest);
    }

    @Test
    public void shouldCallAddAppointmentsWhenEndDateIsIncreasedForTypeDay() {
        AppointmentRecurringPattern appointmentRecurringPattern =
                createAppointmentRecurringPattern(RecurringAppointmentType.DAY, 1, null, new Date());
        RecurringAppointmentRequest recurringAppointmentRequest =
                createRecurringAppointmentRequest("DAY", 1, 3, DateUtils.addDays(new Date(), 2));

        recurringAppointmentsService.getUpdatedSetOfAppointments(appointmentRecurringPattern, recurringAppointmentRequest);

        verify(dailyRecurringAppointmentsGenerationService).addAppointments(appointmentRecurringPattern, recurringAppointmentRequest);
    }

    @Test
    public void shouldCallRemoveAppointmentsWhenEndDateIsDecreasedForTypeDay() {
        AppointmentRecurringPattern appointmentRecurringPattern =
                createAppointmentRecurringPattern(RecurringAppointmentType.DAY, 1, null, DateUtils.addDays(new Date(), 2));
        RecurringAppointmentRequest recurringAppointmentRequest =
                createRecurringAppointmentRequest("DAY", 1, null, new Date());

        recurringAppointmentsService.getUpdatedSetOfAppointments(appointmentRecurringPattern, recurringAppointmentRequest);

        verify(dailyRecurringAppointmentsGenerationService).removeRecurringAppointments(appointmentRecurringPattern, recurringAppointmentRequest);
    }

    @Test
    public void shouldCallAddAppointmentsWhenFrequencyIsIncreasedForTypeWeek() {
        AppointmentRecurringPattern appointmentRecurringPattern =
                createAppointmentRecurringPattern(RecurringAppointmentType.WEEK, 1, 2, null);
        RecurringAppointmentRequest recurringAppointmentRequest =
                createRecurringAppointmentRequest("DAY", 1, 3, null);

        recurringAppointmentsService.getUpdatedSetOfAppointments(appointmentRecurringPattern, recurringAppointmentRequest);

        verify(weeklyRecurringAppointmentsGenerationService).addAppointments(appointmentRecurringPattern, recurringAppointmentRequest);
    }

    @Test
    public void shouldCallRemoveAppointmentsWhenFrequencyIsDecreasedForTypeWeek() {
        AppointmentRecurringPattern appointmentRecurringPattern =
                createAppointmentRecurringPattern(RecurringAppointmentType.WEEK, 1, 3, null);
        RecurringAppointmentRequest recurringAppointmentRequest =
                createRecurringAppointmentRequest("DAY", 1, 2, null);

        recurringAppointmentsService.getUpdatedSetOfAppointments(appointmentRecurringPattern, recurringAppointmentRequest);

        verify(weeklyRecurringAppointmentsGenerationService).removeRecurringAppointments(appointmentRecurringPattern, recurringAppointmentRequest);
    }

    @Test
    public void shouldCallAddAppointmentsWhenEndDateIsIncreasedForTypeWeek() {
        AppointmentRecurringPattern appointmentRecurringPattern =
                createAppointmentRecurringPattern(RecurringAppointmentType.WEEK, 1, null, new Date());
        RecurringAppointmentRequest recurringAppointmentRequest =
                createRecurringAppointmentRequest("DAY", 1, 3, DateUtils.addDays(new Date(), 2));

        recurringAppointmentsService.getUpdatedSetOfAppointments(appointmentRecurringPattern, recurringAppointmentRequest);

        verify(weeklyRecurringAppointmentsGenerationService).addAppointments(appointmentRecurringPattern, recurringAppointmentRequest);
    }

    @Test
    public void shouldCallRemoveAppointmentsWhenEndDateIsDecreasedForTypeWeek() {
        AppointmentRecurringPattern appointmentRecurringPattern =
                createAppointmentRecurringPattern(RecurringAppointmentType.WEEK, 1, null, DateUtils.addDays(new Date(), 2));
        RecurringAppointmentRequest recurringAppointmentRequest =
                createRecurringAppointmentRequest("DAY", 1, null, new Date());

        recurringAppointmentsService.getUpdatedSetOfAppointments(appointmentRecurringPattern, recurringAppointmentRequest);

        verify(weeklyRecurringAppointmentsGenerationService).removeRecurringAppointments(appointmentRecurringPattern, recurringAppointmentRequest);
    }

    private AppointmentRecurringPattern createAppointmentRecurringPattern(RecurringAppointmentType recurringAppointmentType,
                                                                          int period, Integer frequency, Date endDate) {
        AppointmentRecurringPattern appointmentRecurringPattern = new AppointmentRecurringPattern();
        appointmentRecurringPattern.setType(recurringAppointmentType);
        appointmentRecurringPattern.setPeriod(period);
        appointmentRecurringPattern.setFrequency(frequency);
        appointmentRecurringPattern.setEndDate(endDate);
        return appointmentRecurringPattern;
    }

    private RecurringAppointmentRequest createRecurringAppointmentRequest(String type, int period, Integer frequency,
                                                                          Date endDate) {
        RecurringAppointmentRequest recurringAppointmentRequest = new RecurringAppointmentRequest();
        recurringAppointmentRequest.setRecurringPattern(new RecurringPattern());
        recurringAppointmentRequest.getRecurringPattern().setType(type);
        recurringAppointmentRequest.getRecurringPattern().setPeriod(period);
        recurringAppointmentRequest.getRecurringPattern().setFrequency(frequency);
        recurringAppointmentRequest.getRecurringPattern().setEndDate(endDate);
        return recurringAppointmentRequest;
    }
}
