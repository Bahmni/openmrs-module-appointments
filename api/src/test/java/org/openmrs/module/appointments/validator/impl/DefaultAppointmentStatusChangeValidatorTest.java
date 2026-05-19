package org.openmrs.module.appointments.validator.impl;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.context.Context;
import org.openmrs.module.appointments.model.Appointment;
import org.openmrs.module.appointments.model.AppointmentStatus;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

public class DefaultAppointmentStatusChangeValidatorTest {

    private Appointment appointment;

    @Mock
    private AdministrationService administrationService;

    @InjectMocks
    private DefaultAppointmentStatusChangeValidator validator;

    private MockedStatic<Context> contextMockedStatic;
    private AutoCloseable mocks;

    @Before
    public void init() {
        mocks = MockitoAnnotations.openMocks(this);
        contextMockedStatic = mockStatic(Context.class);

        when(administrationService.getGlobalProperty("disableDefaultAppointmentValidations")).thenReturn("false");
        when(Context.getAdministrationService()).thenReturn(administrationService);
        appointment = new Appointment();
    }

    @After
    public void tearDown() throws Exception {
        if (contextMockedStatic != null) contextMockedStatic.close();
        if (mocks != null) mocks.close();
    }

    @Test
    public void shouldChangeStatusFromScheduledToCheckedIn() {
        appointment.setStatus(AppointmentStatus.Scheduled);
        List<String> errors = new ArrayList<>();
        validator.validate(appointment, AppointmentStatus.CheckedIn, errors);
        assertEquals(0, errors.size());
    }

    @Test
    public void shouldChangeStatusFromScheduledToCompleted() {
        appointment.setStatus(AppointmentStatus.Scheduled);
        List<String> errors = new ArrayList<>();
        validator.validate(appointment, AppointmentStatus.Completed, errors);
        assertEquals(0, errors.size());
    }

    @Test
    public void shouldChangeStatusFromScheduledToCancelled() {
        appointment.setStatus(AppointmentStatus.Scheduled);
        List<String> errors = new ArrayList<>();
        validator.validate(appointment, AppointmentStatus.Cancelled, errors);
        assertEquals(0, errors.size());
    }

    @Test
    public void shouldChangeStatusFromScheduledToMissed() {
        appointment.setStatus(AppointmentStatus.Scheduled);
        List<String> errors = new ArrayList<>();
        validator.validate(appointment, AppointmentStatus.Missed, errors);
        assertEquals(0, errors.size());
    }

    @Test
    public void shouldChangeStatusFromCheckedInToCompleted() {
        appointment.setStatus(AppointmentStatus.CheckedIn);
        List<String> errors = new ArrayList<>();
        validator.validate(appointment, AppointmentStatus.Completed, errors);
        assertEquals(0, errors.size());
    }

    @Test
    public void shouldChangeStatusFromCheckedInToCancelled() {
        appointment.setStatus(AppointmentStatus.CheckedIn);
        List<String> errors = new ArrayList<>();
        validator.validate(appointment, AppointmentStatus.Cancelled, errors);
        assertEquals(0, errors.size());
    }

    @Test
    public void shouldChangeStatusFromCheckedInToMissed() {
        appointment.setStatus(AppointmentStatus.CheckedIn);
        List<String> errors = new ArrayList<>();
        validator.validate(appointment, AppointmentStatus.Missed, errors);
        assertEquals(0, errors.size());
    }

    @Test
    public void shouldChangeStatusFromCheckedInToScheduled() {
        appointment.setStatus(AppointmentStatus.CheckedIn);
        List<String> errors = new ArrayList<>();
        validator.validate(appointment, AppointmentStatus.Scheduled, errors);
        assertEquals(0, errors.size());
    }

    @Test
    public void shouldNotChangeStatusFromCheckedInToCheckedIn() {
        appointment.setStatus(AppointmentStatus.CheckedIn);
        List<String> errors = new ArrayList<>();
        validator.validate(appointment, AppointmentStatus.CheckedIn, errors);
        assertEquals(1, errors.size());
        String message = "Appointment status can not be changed from " + appointment.getStatus() + " to " + AppointmentStatus.CheckedIn;
        assertEquals(message, errors.get(0));
    }

    @Test
    public void shouldChangeStatusFromCompletedToScheduled() {
        appointment.setStatus(AppointmentStatus.Completed);
        List<String> errors = new ArrayList<>();
        validator.validate(appointment, AppointmentStatus.Scheduled, errors);
        assertEquals(0, errors.size());
    }

    @Test
    public void shouldNotChangeStatusFromCompletedToCheckedIn() {
        appointment.setStatus(AppointmentStatus.Completed);
        List<String> errors = new ArrayList<>();
        validator.validate(appointment, AppointmentStatus.CheckedIn, errors);
        assertEquals(1, errors.size());
        String message = "Appointment status can not be changed from " + appointment.getStatus() + " to " + AppointmentStatus.CheckedIn;
        assertEquals(message, errors.get(0));
    }

    @Test
    public void shouldNotChangeStatusFromCompletedToCompleted() {
        appointment.setStatus(AppointmentStatus.Completed);
        List<String> errors = new ArrayList<>();
        validator.validate(appointment, AppointmentStatus.Completed, errors);
        assertEquals(1, errors.size());
        String message = "Appointment status can not be changed from " + appointment.getStatus() + " to " + AppointmentStatus.Completed;
        assertEquals(message, errors.get(0));
    }

    @Test
    public void shouldNotChangeStatusFromCompletedToCancelled() {
        appointment.setStatus(AppointmentStatus.Completed);
        List<String> errors = new ArrayList<>();
        validator.validate(appointment, AppointmentStatus.Cancelled, errors);
        assertEquals(1, errors.size());
        String message = "Appointment status can not be changed from " + appointment.getStatus() + " to " + AppointmentStatus.Cancelled;
        assertEquals(message, errors.get(0));
    }

    @Test
    public void shouldNotChangeStatusFromCompletedToMissed() {
        appointment.setStatus(AppointmentStatus.Completed);
        List<String> errors = new ArrayList<>();
        validator.validate(appointment, AppointmentStatus.Missed, errors);
        assertEquals(1, errors.size());
        String message = "Appointment status can not be changed from " + appointment.getStatus() + " to " + AppointmentStatus.Missed;
        assertEquals(message, errors.get(0));
    }

    @Test
    public void shouldChangeStatusFromCancelledToScheduled() {
        appointment.setStatus(AppointmentStatus.Cancelled);
        List<String> errors = new ArrayList<>();
        validator.validate(appointment, AppointmentStatus.Scheduled, errors);
        assertEquals(0, errors.size());
    }

    @Test
    public void shouldNotChangeStatusFromCancelledToCheckedIn() {
        appointment.setStatus(AppointmentStatus.Cancelled);
        List<String> errors = new ArrayList<>();
        validator.validate(appointment, AppointmentStatus.CheckedIn, errors);
        assertEquals(1, errors.size());
        String message = "Appointment status can not be changed from " + appointment.getStatus() + " to " + AppointmentStatus.CheckedIn;
        assertEquals(message, errors.get(0));
    }

    @Test
    public void shouldNotChangeStatusFromCancelledToCompleted() {
        appointment.setStatus(AppointmentStatus.Cancelled);
        List<String> errors = new ArrayList<>();
        validator.validate(appointment, AppointmentStatus.Completed, errors);
        assertEquals(1, errors.size());
        String message = "Appointment status can not be changed from " + appointment.getStatus() + " to " + AppointmentStatus.Completed;
        assertEquals(message, errors.get(0));
    }

    @Test
    public void shouldNotChangeStatusFromCancelledToCancelled() {
        appointment.setStatus(AppointmentStatus.Cancelled);
        List<String> errors = new ArrayList<>();
        validator.validate(appointment, AppointmentStatus.Cancelled, errors);
        assertEquals(1, errors.size());
        String message = "Appointment status can not be changed from " + appointment.getStatus() + " to " + AppointmentStatus.Cancelled;
        assertEquals(message, errors.get(0));
    }

    @Test
    public void shouldNotChangeStatusFromCancelledToMissed() {
        appointment.setStatus(AppointmentStatus.Cancelled);
        List<String> errors = new ArrayList<>();
        validator.validate(appointment, AppointmentStatus.Missed, errors);
        assertEquals(1, errors.size());
        String message = "Appointment status can not be changed from " + appointment.getStatus() + " to " + AppointmentStatus.Missed;
        assertEquals(message, errors.get(0));
    }

    @Test
    public void shouldChangeStatusFromMissedToScheduled() {
        appointment.setStatus(AppointmentStatus.Missed);
        List<String> errors = new ArrayList<>();
        validator.validate(appointment, AppointmentStatus.Scheduled, errors);
        assertEquals(0, errors.size());
    }

    @Test
    public void shouldNotChangeStatusFromMissedToCheckedIn() {
        appointment.setStatus(AppointmentStatus.Missed);
        List<String> errors = new ArrayList<>();
        validator.validate(appointment, AppointmentStatus.CheckedIn, errors);
        assertEquals(1, errors.size());
        String message = "Appointment status can not be changed from " + appointment.getStatus() + " to " + AppointmentStatus.CheckedIn;
        assertEquals(message, errors.get(0));
    }

    @Test
    public void shouldNotChangeStatusFromMissedToCompleted() {
        appointment.setStatus(AppointmentStatus.Missed);
        List<String> errors = new ArrayList<>();
        validator.validate(appointment, AppointmentStatus.Completed, errors);
        assertEquals(1, errors.size());
        String message = "Appointment status can not be changed from " + appointment.getStatus() + " to " + AppointmentStatus.Completed;
        assertEquals(message, errors.get(0));
    }

    @Test
    public void shouldNotChangeStatusFromMissedToCancelled() {
        appointment.setStatus(AppointmentStatus.Missed);
        List<String> errors = new ArrayList<>();
        validator.validate(appointment, AppointmentStatus.Cancelled, errors);
        assertEquals(1, errors.size());
        String message = "Appointment status can not be changed from " + appointment.getStatus() + " to " + AppointmentStatus.Cancelled;
        assertEquals(message, errors.get(0));
    }

    @Test
    public void shouldNotChangeStatusFromMissedToMissed() {
        appointment.setStatus(AppointmentStatus.Missed);
        List<String> errors = new ArrayList<>();
        validator.validate(appointment, AppointmentStatus.Missed, errors);
        assertEquals(1, errors.size());
        String message = "Appointment status can not be changed from " + appointment.getStatus() + " to " + AppointmentStatus.Missed;
        assertEquals(message, errors.get(0));
    }

    @Test
    public void shouldNotRunValidationsWhenConfigIsTrue() {
        when(administrationService.getGlobalProperty("disableDefaultAppointmentValidations")).thenReturn("true");
        appointment.setStatus(AppointmentStatus.Scheduled);
        List<String> errors = new ArrayList<>();
        validator.validate(appointment, AppointmentStatus.Scheduled, errors);
        assertEquals(0, errors.size());
    }
}
