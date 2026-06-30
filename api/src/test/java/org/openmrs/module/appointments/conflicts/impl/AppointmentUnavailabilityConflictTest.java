package org.openmrs.module.appointments.conflicts.impl;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.openmrs.Location;
import org.openmrs.Provider;
import org.openmrs.module.appointments.model.Appointment;
import org.openmrs.module.appointments.model.AppointmentProvider;
import org.openmrs.module.appointments.model.AppointmentServiceDefinition;
import org.openmrs.module.appointments.model.AppointmentUnavailability;
import org.openmrs.module.appointments.search.param.AppointmentUnavailabilitySearchParams;
import org.openmrs.module.appointments.service.AppointmentUnavailabilityService;

import java.sql.Date;
import java.sql.Time;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.openmrs.module.appointments.helper.DateHelper.getDate;

@RunWith(MockitoJUnitRunner.class)
public class AppointmentUnavailabilityConflictTest {

    @InjectMocks
    private AppointmentUnavailabilityConflict appointmentUnavailabilityConflict;

    @Mock
    private AppointmentUnavailabilityService appointmentUnavailabilityService;

    private AppointmentUnavailability buildUnavailability(String startDate, String startTime, String endDate, String endTime) {
        AppointmentUnavailability unavailability = new AppointmentUnavailability();
        unavailability.setStartDate(Date.valueOf(startDate));
        unavailability.setStartTime(Time.valueOf(startTime));
        unavailability.setEndDate(Date.valueOf(endDate));
        unavailability.setEndTime(Time.valueOf(endTime));
        return unavailability;
    }

    private Appointment buildAppointment() {
        Appointment appointment = new Appointment();
        appointment.setStartDateTime(getDate(2026, 5, 28, 19, 0, 0));
        appointment.setEndDateTime(getDate(2026, 5, 28, 20, 0, 0));
        return appointment;
    }

    @Test
    public void shouldReturnConflictWhenAppointmentOverlapsUnavailability() {
        AppointmentUnavailability unavailability = buildUnavailability("2026-06-28", "18:00:00", "2026-06-28", "21:00:00");
        when(appointmentUnavailabilityService.getAll(any(AppointmentUnavailabilitySearchParams.class)))
                .thenReturn(Collections.singletonList(unavailability));

        Appointment appointment = buildAppointment();
        List<Appointment> conflicts = appointmentUnavailabilityConflict.getConflicts(Collections.singletonList(appointment));

        assertNotNull(conflicts);
        assertEquals(1, conflicts.size());
        assertEquals(appointment, conflicts.get(0));
    }

    @Test
    public void shouldNotReturnConflictWhenAppointmentIsOutsideUnavailabilityTime() {
        AppointmentUnavailability unavailability = buildUnavailability("2026-06-28", "10:00:00", "2026-06-28", "11:00:00");
        when(appointmentUnavailabilityService.getAll(any(AppointmentUnavailabilitySearchParams.class)))
                .thenReturn(Collections.singletonList(unavailability));

        Appointment appointment = buildAppointment();
        List<Appointment> conflicts = appointmentUnavailabilityConflict.getConflicts(Collections.singletonList(appointment));

        assertNotNull(conflicts);
        assertEquals(0, conflicts.size());
    }

    @Test
    public void shouldNotReturnConflictWhenLocationDoesNotMatch() {
        Location appointmentLocation = mock(Location.class);
        Location unavailabilityLocation = mock(Location.class);

        AppointmentUnavailability unavailability = buildUnavailability("2026-06-28", "18:00:00", "2026-06-28", "21:00:00");
        unavailability.setLocation(unavailabilityLocation);
        when(appointmentUnavailabilityService.getAll(any(AppointmentUnavailabilitySearchParams.class)))
                .thenReturn(Collections.singletonList(unavailability));

        Appointment appointment = buildAppointment();
        appointment.setLocation(appointmentLocation);

        List<Appointment> conflicts = appointmentUnavailabilityConflict.getConflicts(Collections.singletonList(appointment));

        assertNotNull(conflicts);
        assertEquals(0, conflicts.size());
    }

    @Test
    public void shouldNotReturnConflictWhenServiceDoesNotMatch() {
        AppointmentServiceDefinition appointmentService = new AppointmentServiceDefinition();
        AppointmentServiceDefinition unavailabilityService = new AppointmentServiceDefinition();

        AppointmentUnavailability unavailability = buildUnavailability("2026-06-28", "18:00:00", "2026-06-28", "21:00:00");
        unavailability.setService(unavailabilityService);
        when(appointmentUnavailabilityService.getAll(any(AppointmentUnavailabilitySearchParams.class)))
                .thenReturn(Collections.singletonList(unavailability));

        Appointment appointment = buildAppointment();
        appointment.setService(appointmentService);

        List<Appointment> conflicts = appointmentUnavailabilityConflict.getConflicts(Collections.singletonList(appointment));

        assertNotNull(conflicts);
        assertEquals(0, conflicts.size());
    }

    @Test
    public void shouldNotReturnConflictWhenProviderDoesNotMatch() {
        Provider appointmentProvider = mock(Provider.class);
        Provider unavailabilityProvider = mock(Provider.class);

        AppointmentProvider apptProvider = new AppointmentProvider();
        apptProvider.setProvider(appointmentProvider);

        AppointmentUnavailability unavailability = buildUnavailability("2026-06-28", "18:00:00", "2026-06-28", "21:00:00");
        unavailability.setProvider(unavailabilityProvider);
        when(appointmentUnavailabilityService.getAll(any(AppointmentUnavailabilitySearchParams.class)))
                .thenReturn(Collections.singletonList(unavailability));

        Appointment appointment = buildAppointment();
        appointment.setProviders(new HashSet<>(Collections.singletonList(apptProvider)));

        List<Appointment> conflicts = appointmentUnavailabilityConflict.getConflicts(Collections.singletonList(appointment));

        assertNotNull(conflicts);
        assertEquals(0, conflicts.size());
    }

    @Test
    public void shouldReturnConflictWhenProviderIsNullAndLocationServiceAndTimeMatch() {
        Location location = mock(Location.class);
        AppointmentServiceDefinition service = new AppointmentServiceDefinition();

        AppointmentUnavailability unavailability = buildUnavailability("2026-06-28", "18:00:00", "2026-06-28", "21:00:00");
        unavailability.setLocation(location);
        unavailability.setService(service);
        when(appointmentUnavailabilityService.getAll(any(AppointmentUnavailabilitySearchParams.class)))
                .thenReturn(Collections.singletonList(unavailability));

        Appointment appointment = buildAppointment();
        appointment.setLocation(location);
        appointment.setService(service);

        List<Appointment> conflicts = appointmentUnavailabilityConflict.getConflicts(Collections.singletonList(appointment));

        assertNotNull(conflicts);
        assertEquals(1, conflicts.size());
        assertEquals(appointment, conflicts.get(0));
    }
}
