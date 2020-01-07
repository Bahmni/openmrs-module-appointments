package org.openmrs.module.appointments.conflicts.impl;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;
import org.openmrs.module.appointments.model.Appointment;
import org.openmrs.module.appointments.model.AppointmentServiceDefinition;
import org.openmrs.module.appointments.model.ServiceWeeklyAvailability;

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
import static org.openmrs.module.appointments.helper.DateHelper.getDate;

@RunWith(MockitoJUnitRunner.class)
public class AppointmentServiceUnavailabilityConflictTest {

    @InjectMocks
    private AppointmentServiceUnavailabilityConflict appointmentServiceUnavailabilityConflict;

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
        day1.setStartTime(new Time(8, 30, 0));
        day1.setEndTime(new Time(17, 30, 0));
        day1.setDayOfWeek(DayOfWeek.MONDAY);
        ServiceWeeklyAvailability day2 = new ServiceWeeklyAvailability();
        day2.setStartTime(new Time(8, 30, 0));
        day2.setEndTime(new Time(17, 30, 0));
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
        day1.setStartTime(new Time(8, 30, 0));
        day1.setEndTime(new Time(17, 0, 0));
        day1.setDayOfWeek(DayOfWeek.MONDAY);
        ServiceWeeklyAvailability day2 = new ServiceWeeklyAvailability();
        day2.setStartTime(new Time(8, 30, 0));
        day2.setEndTime(new Time(17, 0, 0));
        day2.setDayOfWeek(DayOfWeek.TUESDAY);
        Set<ServiceWeeklyAvailability> availabilities = new HashSet<>(Arrays.asList(day1, day2));
        appointmentServiceDefinition.setWeeklyAvailability(availabilities);

        List<Appointment> conflictingAppointments= new ArrayList<>();
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
        day1.setStartTime(new Time(6, 30, 0));
        day1.setEndTime(new Time(14, 0, 0));
        day1.setDayOfWeek(DayOfWeek.MONDAY);
        ServiceWeeklyAvailability day2 = new ServiceWeeklyAvailability();
        day2.setStartTime(new Time(16, 30, 0));
        day2.setEndTime(new Time(19, 0, 0));
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
        day1.setStartTime(new Time(8, 30, 0));
        day1.setEndTime(new Time(17, 0, 0));
        day1.setDayOfWeek(DayOfWeek.MONDAY);
        ServiceWeeklyAvailability day2 = new ServiceWeeklyAvailability();
        day2.setStartTime(new Time(8, 30, 0));
        day2.setEndTime(new Time(17, 0, 0));
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
        appointmentServiceDefinition.setStartTime(new Time(11, 30, 0));
        appointmentServiceDefinition.setEndTime(new Time(17, 0, 0));

        List<Appointment> appointments = appointmentServiceUnavailabilityConflict.getConflicts(Collections.singletonList(appointment));

        assertNotNull(appointments);
        assertEquals(appointment, appointments.get(0));
    }
}
