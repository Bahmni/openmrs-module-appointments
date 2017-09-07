package org.openmrs.module.appointments.validator.impl;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.context.Context;
import org.openmrs.module.appointments.model.Appointment;
import org.openmrs.module.appointments.model.AppointmentStatus;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.ArrayList;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import java.util.List;

@RunWith(PowerMockRunner.class)
@PrepareForTest({Context.class})
public class DefaultAppointmentStatusChangeValidatorTest {

	private Appointment appointment;

	@Mock
	private AdministrationService administrationService;

	@InjectMocks
	private DefaultAppointmentStatusChangeValidator validator;

	@Before
	public void init() {
		MockitoAnnotations.initMocks(this);
		PowerMockito.mockStatic(Context.class);

		when(administrationService.getGlobalProperty("disableDefaultAppointmentValidations")).thenReturn("false");
		when(Context.getAdministrationService()).thenReturn(administrationService);
	}

	@Before
	public void setUp() {
		appointment = new Appointment();
	}

	@Test
	public void shouldChangeStatusFromScheduledToCheckedIn() throws Exception {
		appointment.setStatus(AppointmentStatus.Scheduled);
		List<String> errors = new ArrayList<>();
		validator.validate(appointment, AppointmentStatus.CheckedIn, errors);
		assertEquals(0, errors.size());
	}

	@Test
	public void shouldChangeStatusFromScheduledToCompleted() throws Exception {
		appointment.setStatus(AppointmentStatus.Scheduled);
		List<String> errors = new ArrayList<>();
		validator.validate(appointment, AppointmentStatus.Completed, errors);
		assertEquals(0, errors.size());
	}

	@Test
	public void shouldChangeStatusFromScheduledToCancelled() throws Exception {
		appointment.setStatus(AppointmentStatus.Scheduled);
		List<String> errors = new ArrayList<>();
		validator.validate(appointment, AppointmentStatus.Cancelled, errors);
		assertEquals(0, errors.size());
	}

	@Test
	public void shouldChangeStatusFromScheduledToMissed() throws Exception {
		appointment.setStatus(AppointmentStatus.Scheduled);
		List<String> errors = new ArrayList<>();
		validator.validate(appointment, AppointmentStatus.Missed, errors);
		assertEquals(0, errors.size());
	}

	@Test
	public void shouldChangeStatusFromCheckedInToCompleted() throws Exception {
		appointment.setStatus(AppointmentStatus.CheckedIn);
		List<String> errors = new ArrayList<>();
		validator.validate(appointment, AppointmentStatus.Completed, errors);
		assertEquals(0, errors.size());
	}

	@Test
	public void shouldChangeStatusFromCheckedInToCancelled() throws Exception {
		appointment.setStatus(AppointmentStatus.CheckedIn);
		List<String> errors = new ArrayList<>();
		validator.validate(appointment, AppointmentStatus.Cancelled, errors);
		assertEquals(0, errors.size());
	}

	@Test
	public void shouldChangeStatusFromCheckedInToMissed() throws Exception {
		appointment.setStatus(AppointmentStatus.CheckedIn);
		List<String> errors = new ArrayList<>();
		validator.validate(appointment, AppointmentStatus.Missed, errors);
		assertEquals(0, errors.size());
	}

	@Test
	public void shouldNotChangeStatusFromScheduledToScheduled() throws Exception {
		appointment.setStatus(AppointmentStatus.Scheduled);
		List<String> errors = new ArrayList<>();
		validator.validate(appointment, AppointmentStatus.Scheduled, errors);
		assertEquals(1, errors.size());
		String message = "Appointment status can not be changed from " + appointment.getStatus() + " to "+ AppointmentStatus.Scheduled;
		assertEquals(message, errors.get(0));
	}

	@Test
	public void shouldNotChangeStatusFromCheckedInToScheduled() throws Exception {
		appointment.setStatus(AppointmentStatus.CheckedIn);
		List<String> errors = new ArrayList<>();
		validator.validate(appointment, AppointmentStatus.Scheduled, errors);
		assertEquals(1, errors.size());
		String message = "Appointment status can not be changed from " + appointment.getStatus() + " to "+ AppointmentStatus.Scheduled;
		assertEquals(message, errors.get(0));
	}

	@Test
	public void shouldNotChangeStatusFromCheckedInToCheckedIn() throws Exception {
		appointment.setStatus(AppointmentStatus.CheckedIn);
		List<String> errors = new ArrayList<>();
		validator.validate(appointment, AppointmentStatus.CheckedIn, errors);
		assertEquals(1, errors.size());
		String message = "Appointment status can not be changed from " + appointment.getStatus() + " to "+ AppointmentStatus.CheckedIn;
		assertEquals(message, errors.get(0));
	}

	@Test
	public void shouldNotChangeStatusFromCompletedToScheduled() throws Exception {
		appointment.setStatus(AppointmentStatus.Completed);
		List<String> errors = new ArrayList<>();
		validator.validate(appointment, AppointmentStatus.Scheduled, errors);
		assertEquals(1, errors.size());
		String message = "Appointment status can not be changed from " + appointment.getStatus() + " to "+ AppointmentStatus.Scheduled;
		assertEquals(message, errors.get(0));
	}

	@Test
	public void shouldNotChangeStatusFromCompletedToCheckedIn() throws Exception {
		appointment.setStatus(AppointmentStatus.Completed);
		List<String> errors = new ArrayList<>();
		validator.validate(appointment, AppointmentStatus.CheckedIn, errors);
		assertEquals(1, errors.size());
		String message = "Appointment status can not be changed from " + appointment.getStatus() + " to "+ AppointmentStatus.CheckedIn;
		assertEquals(message, errors.get(0));
	}

	@Test
	public void shouldNotChangeStatusFromCompletedToCompleted() throws Exception {
		appointment.setStatus(AppointmentStatus.Completed);
		List<String> errors = new ArrayList<>();
		validator.validate(appointment, AppointmentStatus.Completed, errors);
		assertEquals(1, errors.size());
		String message = "Appointment status can not be changed from " + appointment.getStatus() + " to "+ AppointmentStatus.Completed;
		assertEquals(message, errors.get(0));
	}

	@Test
	public void shouldNotChangeStatusFromCompletedToCancelled() throws Exception {
		appointment.setStatus(AppointmentStatus.Completed);
		List<String> errors = new ArrayList<>();
		validator.validate(appointment, AppointmentStatus.Cancelled, errors);
		assertEquals(1, errors.size());
		String message = "Appointment status can not be changed from " + appointment.getStatus() + " to "+ AppointmentStatus.Cancelled;
		assertEquals(message, errors.get(0));
	}

	@Test
	public void shouldNotChangeStatusFromCompletedToMissed() throws Exception {
		appointment.setStatus(AppointmentStatus.Completed);
		List<String> errors = new ArrayList<>();
		validator.validate(appointment, AppointmentStatus.Missed, errors);
		assertEquals(1, errors.size());
		String message = "Appointment status can not be changed from " + appointment.getStatus() + " to "+ AppointmentStatus.Missed;
		assertEquals(message, errors.get(0));
	}

	@Test
	public void shouldNotChangeStatusFromCancelledToScheduled() throws Exception {
		appointment.setStatus(AppointmentStatus.Cancelled);
		List<String> errors = new ArrayList<>();
		validator.validate(appointment, AppointmentStatus.Scheduled, errors);
		assertEquals(1, errors.size());
		String message = "Appointment status can not be changed from " + appointment.getStatus() + " to "+ AppointmentStatus.Scheduled;
		assertEquals(message, errors.get(0));
	}

	@Test
	public void shouldNotChangeStatusFromCancelledToCheckedIn() throws Exception {
		appointment.setStatus(AppointmentStatus.Cancelled);
		List<String> errors = new ArrayList<>();
		validator.validate(appointment, AppointmentStatus.CheckedIn, errors);
		assertEquals(1, errors.size());
		String message = "Appointment status can not be changed from " + appointment.getStatus() + " to "+ AppointmentStatus.CheckedIn;
		assertEquals(message, errors.get(0));
	}

	@Test
	public void shouldNotChangeStatusFromCancelledToCompleted() throws Exception {
		appointment.setStatus(AppointmentStatus.Cancelled);
		List<String> errors = new ArrayList<>();
		validator.validate(appointment, AppointmentStatus.Completed, errors);
		assertEquals(1, errors.size());
		String message = "Appointment status can not be changed from " + appointment.getStatus() + " to "+ AppointmentStatus.Completed;
		assertEquals(message, errors.get(0));
	}

	@Test
	public void shouldNotChangeStatusFromCancelledToCancelled() throws Exception {
		appointment.setStatus(AppointmentStatus.Cancelled);
		List<String> errors = new ArrayList<>();
		validator.validate(appointment, AppointmentStatus.Cancelled, errors);
		assertEquals(1, errors.size());
		String message = "Appointment status can not be changed from " + appointment.getStatus() + " to "+ AppointmentStatus.Cancelled;
		assertEquals(message, errors.get(0));
	}

	@Test
	public void shouldNotChangeStatusFromCancelledToMissed() throws Exception {
		appointment.setStatus(AppointmentStatus.Cancelled);
		List<String> errors = new ArrayList<>();
		validator.validate(appointment, AppointmentStatus.Missed, errors);
		assertEquals(1, errors.size());
		String message = "Appointment status can not be changed from " + appointment.getStatus() + " to "+ AppointmentStatus.Missed;
		assertEquals(message, errors.get(0));
	}

	@Test
	public void shouldNotChangeStatusFromMissedToScheduled() throws Exception {
		appointment.setStatus(AppointmentStatus.Missed);
		List<String> errors = new ArrayList<>();
		validator.validate(appointment, AppointmentStatus.Scheduled, errors);
		assertEquals(1, errors.size());
		String message = "Appointment status can not be changed from " + appointment.getStatus() + " to "+ AppointmentStatus.Scheduled;
		assertEquals(message, errors.get(0));
	}

	@Test
	public void shouldNotChangeStatusFromMissedToCheckedIn() throws Exception {
		appointment.setStatus(AppointmentStatus.Missed);
		List<String> errors = new ArrayList<>();
		validator.validate(appointment, AppointmentStatus.CheckedIn, errors);
		assertEquals(1, errors.size());
		String message = "Appointment status can not be changed from " + appointment.getStatus() + " to "+ AppointmentStatus.CheckedIn;
		assertEquals(message, errors.get(0));
	}

	@Test
	public void shouldNotChangeStatusFromMissedToCompleted() throws Exception {
		appointment.setStatus(AppointmentStatus.Missed);
		List<String> errors = new ArrayList<>();
		validator.validate(appointment, AppointmentStatus.Completed, errors);
		assertEquals(1, errors.size());
		String message = "Appointment status can not be changed from " + appointment.getStatus() + " to "+ AppointmentStatus.Completed;
		assertEquals(message, errors.get(0));
	}

	@Test
	public void shouldNotChangeStatusFromMissedToCancelled() throws Exception {
		appointment.setStatus(AppointmentStatus.Missed);
		List<String> errors = new ArrayList<>();
		validator.validate(appointment, AppointmentStatus.Cancelled, errors);
		assertEquals(1, errors.size());
		String message = "Appointment status can not be changed from " + appointment.getStatus() + " to "+ AppointmentStatus.Cancelled;
		assertEquals(message, errors.get(0));
	}

	@Test
	public void shouldNotChangeStatusFromMissedToMissed() throws Exception {
		appointment.setStatus(AppointmentStatus.Missed);
		List<String> errors = new ArrayList<>();
		validator.validate(appointment, AppointmentStatus.Missed, errors);
		assertEquals(1, errors.size());
		String message = "Appointment status can not be changed from " + appointment.getStatus() + " to "+ AppointmentStatus.Missed;
		assertEquals(message, errors.get(0));
	}

	@Test
	public void shouldNotRunValidationsWhenConfigIsTrue() throws Exception {
		when(administrationService.getGlobalProperty("disableDefaultAppointmentValidations")).thenReturn("true");
		appointment.setStatus(AppointmentStatus.Scheduled);
		List<String> errors = new ArrayList<>();
		validator.validate(appointment, AppointmentStatus.Scheduled, errors);
		assertEquals(0, errors.size());
	}
}
