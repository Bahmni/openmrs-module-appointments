package org.openmrs.module.appointments.validator.impl;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.context.Context;
import org.openmrs.util.PrivilegeConstants;
import org.openmrs.module.appointments.model.Appointment;
import org.openmrs.module.appointments.model.AppointmentStatus;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.when;
import org.mockito.MockedStatic;
import org.junit.After;
import org.mockito.Mockito;

@RunWith(MockitoJUnitRunner.class)
public class DefaultAppointmentStatusChangeValidatorTest {
    private MockedStatic<Context> contextMock;


    private Appointment appointment;

    @Mock
    private AdministrationService administrationService;

    @InjectMocks
    private DefaultAppointmentStatusChangeValidator validator;

    @Before
    public void init() {
        contextMock = Mockito.mockStatic(Context.class);

        when(administrationService.getGlobalProperty("disableDefaultAppointmentValidations")).thenReturn("false");
        contextMock.when(Context::getAdministrationService).thenReturn(administrationService);
    }

    @Before
    public void setUp() {
        appointment = new Appointment();
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

    @Test
    public void shouldAddAndRemoveProxyPrivilegeAroundGlobalPropertyRead() {
        appointment.setStatus(AppointmentStatus.Scheduled);
        validator.validate(appointment, AppointmentStatus.CheckedIn, new ArrayList<>());
        contextMock.verify(() -> Context.addProxyPrivilege(PrivilegeConstants.GET_GLOBAL_PROPERTIES));
        contextMock.verify(() -> Context.removeProxyPrivilege(PrivilegeConstants.GET_GLOBAL_PROPERTIES));
    }

    @Test
    public void shouldRemoveProxyPrivilegeWhenGlobalPropertyReadThrows() {
        when(administrationService.getGlobalProperty("disableDefaultAppointmentValidations"))
                .thenThrow(new IllegalStateException("boom"));
        appointment.setStatus(AppointmentStatus.Scheduled);
        try {
            validator.validate(appointment, AppointmentStatus.CheckedIn, new ArrayList<>());
            fail("expected exception");
        } catch (IllegalStateException expected) {
            // proxy privilege must still be removed
        }
        contextMock.verify(() -> Context.removeProxyPrivilege(PrivilegeConstants.GET_GLOBAL_PROPERTIES));
    }

    @After
    public void tearDownContextMock() {
        if (contextMock != null) {
            contextMock.close();
            contextMock = null;
        }
    }
}
