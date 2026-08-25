package org.openmrs.module.appointments.conflicts.impl;

import org.junit.Before;
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
import org.openmrs.module.appointments.model.ServiceWeeklyAvailability;
import org.openmrs.module.appointments.dao.AppointmentUnavailabilityDao;
import org.openmrs.module.appointments.search.param.AppointmentUnavailabilitySearchParams;

import java.sql.Date;
import java.sql.Time;
import java.time.DayOfWeek;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.openmrs.module.appointments.helper.DateHelper.getDate;

@RunWith(MockitoJUnitRunner.class)
public class AppointmentServiceUnavailabilityConflictTest {

    @InjectMocks
    private AppointmentServiceUnavailabilityConflict appointmentServiceUnavailabilityConflict;

    @Mock
    private AppointmentUnavailabilityDao appointmentUnavailabilityDao;

    @Before
    public void setUp() {
        when(appointmentUnavailabilityDao.getAll(any(AppointmentUnavailabilitySearchParams.class)))
                .thenReturn(Collections.emptyList());
    }

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
    public void shouldReturnServiceUnavailableDayConflicts() {
        AppointmentServiceDefinition appointmentServiceDefinition = new AppointmentServiceDefinition();
        Appointment appointment = new Appointment();
        appointment.setService(appointmentServiceDefinition);
        appointment.setStartDateTime(getDate(2019, 8, 24, 11, 0, 0));
        appointment.setEndDateTime(getDate(2019, 8, 24, 12, 0, 0));
        appointment.setAppointmentId(1);
        ServiceWeeklyAvailability day1 = new ServiceWeeklyAvailability();
        day1.setDayOfWeek(DayOfWeek.MONDAY);
        ServiceWeeklyAvailability day2 = new ServiceWeeklyAvailability();
        day2.setDayOfWeek(DayOfWeek.WEDNESDAY);
        Set<ServiceWeeklyAvailability> availabilities = new HashSet<>(Arrays.asList(day1, day2));
        appointmentServiceDefinition.setWeeklyAvailability(availabilities);
        List<Appointment> conflictingAppointments = appointmentServiceUnavailabilityConflict.getConflicts(Collections.singletonList(appointment));
        assertNotNull(conflictingAppointments);
        assertEquals(appointment, conflictingAppointments.get(0));
    }

    @Test
    public void shouldNotHaveAnyServiceUnavailableConflicts() {
        AppointmentServiceDefinition appointmentServiceDefinition = new AppointmentServiceDefinition();
        Appointment appointment = new Appointment();
        appointment.setService(appointmentServiceDefinition);
        //Tuesday Appointment
        appointment.setStartDateTime(getDate(2019, 8, 24, 11, 30, 0));
        appointment.setEndDateTime(getDate(2019, 8, 24, 12, 0, 0));
        appointment.setAppointmentId(2);
        ServiceWeeklyAvailability day1 = new ServiceWeeklyAvailability();
        day1.setStartTime(Time.valueOf("08:30:00"));
        day1.setEndTime(Time.valueOf("17:30:00"));
        day1.setDayOfWeek(DayOfWeek.MONDAY);
        ServiceWeeklyAvailability day2 = new ServiceWeeklyAvailability();
        day2.setStartTime(Time.valueOf("08:30:00"));
        day2.setEndTime(Time.valueOf("17:30:00"));
        day2.setDayOfWeek(DayOfWeek.TUESDAY);
        Set<ServiceWeeklyAvailability> availabilities = new HashSet<>(Arrays.asList(day1, day2));
        appointmentServiceDefinition.setWeeklyAvailability(availabilities);

        List<Appointment> conflicts = appointmentServiceUnavailabilityConflict.getConflicts(Collections.singletonList(appointment));

        assertNotNull(conflicts);
        assertEquals(0, conflicts.size());
    }

    @Test
    public void shouldReturnServiceUnavailableTimeSlotConflict() {
        AppointmentServiceDefinition appointmentServiceDefinition = new AppointmentServiceDefinition();
        //All Appointments are on Tuesday
        Appointment appointmentOne = new Appointment();
        appointmentOne.setService(appointmentServiceDefinition);
        appointmentOne.setStartDateTime(getDate(2019, 8, 23, 6, 30, 0));
        appointmentOne.setEndDateTime(getDate(2019, 8, 23, 7, 0, 0));
        appointmentOne.setAppointmentId(2);
        Appointment appointmentTwo = new Appointment();
        appointmentTwo.setService(appointmentServiceDefinition);
        appointmentTwo.setStartDateTime(getDate(2019, 8, 23, 17, 30, 0));
        appointmentTwo.setEndDateTime(getDate(2019, 8, 23, 17, 0, 0));
        appointmentTwo.setAppointmentId(3);
        Appointment appointmentThree = new Appointment();
        appointmentThree.setService(appointmentServiceDefinition);
        appointmentThree.setStartDateTime(getDate(2019, 8, 23, 16, 30, 0));
        appointmentThree.setEndDateTime(getDate(2019, 8, 23, 17, 1, 0));
        appointmentThree.setAppointmentId(4);
        ServiceWeeklyAvailability day1 = new ServiceWeeklyAvailability();
        day1.setStartTime(Time.valueOf("08:30:00"));
        day1.setEndTime(Time.valueOf("17:00:00"));
        day1.setDayOfWeek(DayOfWeek.MONDAY);
        ServiceWeeklyAvailability day2 = new ServiceWeeklyAvailability();
        day2.setStartTime(Time.valueOf("08:30:00"));
        day2.setEndTime(Time.valueOf("17:00:00"));
        day2.setDayOfWeek(DayOfWeek.TUESDAY);
        Set<ServiceWeeklyAvailability> availabilities = new HashSet<>(Arrays.asList(day1, day2));
        appointmentServiceDefinition.setWeeklyAvailability(availabilities);

        List<Appointment> conflictingAppointments = new ArrayList<>();
        conflictingAppointments.addAll(appointmentServiceUnavailabilityConflict.getConflicts(Arrays.asList(appointmentOne, appointmentTwo, appointmentThree)));

        assertNotNull(conflictingAppointments);
        assertEquals(3, conflictingAppointments.size());
        assertEquals(appointmentOne, conflictingAppointments.get(0));
        assertEquals(appointmentTwo, conflictingAppointments.get(1));
        assertEquals(appointmentThree, conflictingAppointments.get(2));
    }

    @Test
    public void shouldNotReturnServiceUnavailableConflictsForMoreSlotsInSingleDay() {
        AppointmentServiceDefinition appointmentServiceDefinition = new AppointmentServiceDefinition();
        // All Appointments are on Monday
        Appointment appointmentOne = new Appointment();
        appointmentOne.setService(appointmentServiceDefinition);
        appointmentOne.setStartDateTime(getDate(2019, 8, 23, 6, 30, 0));
        appointmentOne.setEndDateTime(getDate(2019, 8, 23, 7, 0, 0));
        appointmentOne.setAppointmentId(2);
        Appointment appointmentTwo = new Appointment();
        appointmentTwo.setService(appointmentServiceDefinition);
        appointmentTwo.setStartDateTime(getDate(2019, 8, 23, 16, 30, 0));
        appointmentTwo.setEndDateTime(getDate(2019, 8, 23, 17, 30, 0));
        appointmentTwo.setAppointmentId(3);
        Appointment appointmentThree = new Appointment();
        appointmentThree.setService(appointmentServiceDefinition);
        appointmentThree.setStartDateTime(getDate(2019, 8, 23, 16, 30, 0));
        appointmentThree.setEndDateTime(getDate(2019, 8, 23, 17, 0, 0));
        appointmentThree.setAppointmentId(4);
        ServiceWeeklyAvailability day1 = new ServiceWeeklyAvailability();
        day1.setStartTime(Time.valueOf("06:30:00"));
        day1.setEndTime(Time.valueOf("14:00:00"));
        day1.setDayOfWeek(DayOfWeek.MONDAY);
        ServiceWeeklyAvailability day2 = new ServiceWeeklyAvailability();
        day2.setStartTime(Time.valueOf("16:30:00"));
        day2.setEndTime(Time.valueOf("19:00:00"));
        day2.setDayOfWeek(DayOfWeek.MONDAY);
        Set<ServiceWeeklyAvailability> availabilities = new HashSet<>(Arrays.asList(day1, day2));
        appointmentServiceDefinition.setWeeklyAvailability(availabilities);

        List<Appointment> conflictingAppointments = new ArrayList<>();
        conflictingAppointments.addAll(appointmentServiceUnavailabilityConflict.getConflicts(Arrays.asList(appointmentOne, appointmentTwo, appointmentThree)));

        assertNotNull(conflictingAppointments);
        assertEquals(0, conflictingAppointments.size());
    }

    @Test
    public void shouldNotReturnServiceUnavailableConflictsForServicesWithNoAvailabilityInformation() {
        AppointmentServiceDefinition appointmentServiceDefinition = new AppointmentServiceDefinition();
        Appointment appointmentOne = new Appointment();
        appointmentOne.setService(appointmentServiceDefinition);
        appointmentOne.setStartDateTime(getDate(2019, 8, 23, 6, 30, 0));
        appointmentOne.setEndDateTime(getDate(2019, 8, 23, 7, 0, 0));
        appointmentOne.setAppointmentId(2);
        Appointment appointmentTwo = new Appointment();
        appointmentTwo.setService(appointmentServiceDefinition);
        appointmentTwo.setStartDateTime(getDate(2019, 8, 23, 16, 30, 0));
        appointmentTwo.setEndDateTime(getDate(2019, 8, 23, 17, 30, 0));
        appointmentTwo.setAppointmentId(3);
        Appointment appointmentThree = new Appointment();
        appointmentThree.setService(appointmentServiceDefinition);
        appointmentThree.setStartDateTime(getDate(2019, 8, 23, 16, 30, 0));
        appointmentThree.setEndDateTime(getDate(2019, 8, 23, 17, 0, 0));
        appointmentThree.setAppointmentId(4);
        Set<ServiceWeeklyAvailability> availabilities = new HashSet<>();
        appointmentServiceDefinition.setWeeklyAvailability(availabilities);

        List<Appointment> conflictingAppointments = new ArrayList<>();
        conflictingAppointments.addAll(appointmentServiceUnavailabilityConflict.getConflicts(Arrays.asList(appointmentOne, appointmentTwo, appointmentThree)));

        assertNotNull(conflictingAppointments);
        assertEquals(0, conflictingAppointments.size());
    }

    @Test
    public void shouldReturnConflictWhenAppointmentStartTimeAfterEndTime() {
        AppointmentServiceDefinition appointmentServiceDefinition = new AppointmentServiceDefinition();
        Appointment appointment = new Appointment();
        appointment.setStartDateTime(getDate(2019, 8, 23, 11, 30, 0));
        appointment.setEndDateTime(getDate(2019, 8, 23, 11, 0, 0));
        appointment.setService(appointmentServiceDefinition);
        appointment.setAppointmentId(1);
        ServiceWeeklyAvailability day1 = new ServiceWeeklyAvailability();
        day1.setStartTime(Time.valueOf("08:30:00"));
        day1.setEndTime(Time.valueOf("17:00:00"));
        day1.setDayOfWeek(DayOfWeek.MONDAY);
        ServiceWeeklyAvailability day2 = new ServiceWeeklyAvailability();
        day2.setStartTime(Time.valueOf("08:30:00"));
        day2.setEndTime(Time.valueOf("17:00:00"));
        day2.setDayOfWeek(DayOfWeek.TUESDAY);
        Set<ServiceWeeklyAvailability> availabilities = new HashSet<>(Arrays.asList(day1, day2));
        appointmentServiceDefinition.setWeeklyAvailability(availabilities);
        List<Appointment> appointments = appointmentServiceUnavailabilityConflict.getConflicts(Collections.singletonList(appointment));

        assertNotNull(appointments);
        assertEquals(appointment, appointments.get(0));
    }

    @Test
    public void shouldReturnConflictWhenWeeklyAvailabilityIsNotAvailable() {
        AppointmentServiceDefinition appointmentServiceDefinition = new AppointmentServiceDefinition();
        Appointment appointment = new Appointment();
        appointment.setStartDateTime(getDate(2019, 8, 23, 11, 0, 0));
        appointment.setEndDateTime(getDate(2019, 8, 23, 11, 30, 0));
        appointment.setService(appointmentServiceDefinition);
        appointment.setAppointmentId(1);
        appointmentServiceDefinition.setStartTime(Time.valueOf("11:30:00"));
        appointmentServiceDefinition.setEndTime(Time.valueOf("17:00:00"));

        List<Appointment> appointments = appointmentServiceUnavailabilityConflict.getConflicts(Collections.singletonList(appointment));

        assertNotNull(appointments);
        assertEquals(appointment, appointments.get(0));
    }

    @Test
    public void shouldUseSystemTimezoneWhenLocationHasNoTimezoneAttribute() {
        Location location = mock(Location.class);
        when(location.getActiveAttributes()).thenReturn(Collections.emptyList());

        AppointmentServiceDefinition service = new AppointmentServiceDefinition();
        service.setStartTime(Time.valueOf("08:30:00"));
        service.setEndTime(Time.valueOf("17:00:00"));

        Appointment appointment = new Appointment();
        appointment.setService(service);
        appointment.setLocation(location);
        appointment.setStartDateTime(getDate(2019, 8, 24, 11, 30, 0));
        appointment.setEndDateTime(getDate(2019, 8, 24, 12, 0, 0));

        List<Appointment> conflicts = appointmentServiceUnavailabilityConflict.getConflicts(Collections.singletonList(appointment));

        assertNotNull(conflicts);
        assertEquals(0, conflicts.size());
    }

    @Test
    public void shouldReturnConflictWhenAppointmentOverlapsUnavailability() {
        AppointmentUnavailability unavailability = buildUnavailability("2026-06-28", "18:00:00", "2026-06-28", "21:00:00");
        when(appointmentUnavailabilityDao.getAll(any(AppointmentUnavailabilitySearchParams.class)))
                .thenReturn(Collections.singletonList(unavailability));

        Appointment appointment = buildAppointment();
        List<Appointment> conflicts = appointmentServiceUnavailabilityConflict.getConflicts(Collections.singletonList(appointment));

        assertNotNull(conflicts);
        assertEquals(1, conflicts.size());
        assertEquals(appointment, conflicts.get(0));
    }

    @Test
    public void shouldNotReturnConflictWhenAppointmentIsOutsideUnavailabilityTime() {
        AppointmentUnavailability unavailability = buildUnavailability("2026-06-28", "10:00:00", "2026-06-28", "11:00:00");
        when(appointmentUnavailabilityDao.getAll(any(AppointmentUnavailabilitySearchParams.class)))
                .thenReturn(Collections.singletonList(unavailability));

        Appointment appointment = buildAppointment();
        List<Appointment> conflicts = appointmentServiceUnavailabilityConflict.getConflicts(Collections.singletonList(appointment));

        assertNotNull(conflicts);
        assertEquals(0, conflicts.size());
    }

    @Test
    public void shouldNotReturnConflictWhenLocationDoesNotMatch() {
        Location appointmentLocation = mock(Location.class);
        Location unavailabilityLocation = mock(Location.class);

        AppointmentUnavailability unavailability = buildUnavailability("2026-06-28", "18:00:00", "2026-06-28", "21:00:00");
        unavailability.setLocation(unavailabilityLocation);
        when(appointmentUnavailabilityDao.getAll(any(AppointmentUnavailabilitySearchParams.class)))
                .thenReturn(Collections.singletonList(unavailability));

        Appointment appointment = buildAppointment();
        appointment.setLocation(appointmentLocation);

        List<Appointment> conflicts = appointmentServiceUnavailabilityConflict.getConflicts(Collections.singletonList(appointment));

        assertNotNull(conflicts);
        assertEquals(0, conflicts.size());
    }

    @Test
    public void shouldNotReturnConflictWhenServiceDoesNotMatch() {
        AppointmentServiceDefinition appointmentService = new AppointmentServiceDefinition();
        AppointmentServiceDefinition unavailabilityService = new AppointmentServiceDefinition();

        AppointmentUnavailability unavailability = buildUnavailability("2026-06-28", "18:00:00", "2026-06-28", "21:00:00");
        unavailability.setService(unavailabilityService);
        when(appointmentUnavailabilityDao.getAll(any(AppointmentUnavailabilitySearchParams.class)))
                .thenReturn(Collections.singletonList(unavailability));

        Appointment appointment = buildAppointment();
        appointment.setService(appointmentService);

        List<Appointment> conflicts = appointmentServiceUnavailabilityConflict.getConflicts(Collections.singletonList(appointment));

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
        when(appointmentUnavailabilityDao.getAll(any(AppointmentUnavailabilitySearchParams.class)))
                .thenReturn(Collections.singletonList(unavailability));

        Appointment appointment = buildAppointment();
        appointment.setProviders(new HashSet<>(Collections.singletonList(apptProvider)));

        List<Appointment> conflicts = appointmentServiceUnavailabilityConflict.getConflicts(Collections.singletonList(appointment));

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
        when(appointmentUnavailabilityDao.getAll(any(AppointmentUnavailabilitySearchParams.class)))
                .thenReturn(Collections.singletonList(unavailability));

        Appointment appointment = buildAppointment();
        appointment.setLocation(location);
        appointment.setService(service);

        List<Appointment> conflicts = appointmentServiceUnavailabilityConflict.getConflicts(Collections.singletonList(appointment));

        assertNotNull(conflicts);
        assertEquals(1, conflicts.size());
        assertEquals(appointment, conflicts.get(0));
    }

    @Test
    public void shouldReturnConflictWhenAppointmentFallsWithinOvernightUnavailability() {
        AppointmentUnavailability unavailability = buildUnavailability("2026-06-28", "18:00:00", "2026-06-29", "09:00:00");
        when(appointmentUnavailabilityDao.getAll(any(AppointmentUnavailabilitySearchParams.class)))
                .thenReturn(Collections.singletonList(unavailability));

        Appointment appointment = new Appointment();
        appointment.setStartDateTime(getDate(2026, 5, 29, 8, 0, 0));
        appointment.setEndDateTime(getDate(2026, 5, 29, 8, 30, 0));

        List<Appointment> conflicts = appointmentServiceUnavailabilityConflict.getConflicts(Collections.singletonList(appointment));

        assertNotNull(conflicts);
        assertEquals(1, conflicts.size());
        assertEquals(appointment, conflicts.get(0));
    }
}
