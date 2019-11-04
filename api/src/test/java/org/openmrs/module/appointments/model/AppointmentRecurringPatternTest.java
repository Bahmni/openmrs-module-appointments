package org.openmrs.module.appointments.model;

import org.junit.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.*;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.when;

public class AppointmentRecurringPatternTest {

    @Test
    public void shouldReturnAllAppointmentsWhenThereIsNoVoidedAppointment() {
        AppointmentRecurringPattern appointmentRecurringPattern = new AppointmentRecurringPattern();
        Appointment appointment1 = mock(Appointment.class);
        Appointment appointment2 = mock(Appointment.class);
        Appointment appointment3 = mock(Appointment.class);

        when(appointment1.getVoided()).thenReturn(false);
        when(appointment2.getVoided()).thenReturn(false);
        when(appointment3.getVoided()).thenReturn(false);

        Set<Appointment> appointments = new HashSet<>();
        appointments.add(appointment1);
        appointments.add(appointment2);
        appointments.add(appointment3);
        appointmentRecurringPattern.setAppointments(appointments);

        Set<Appointment> activeAppointments = appointmentRecurringPattern.getActiveAppointments();

        assertEquals(3,activeAppointments.size());
        assertEquals(appointments, activeAppointments);
    }

    @Test
    public void shouldReturnOnlyNonVoidedAppointmentsAsActiveAppointmentsButNotVoidedAppointments() {
        AppointmentRecurringPattern appointmentRecurringPattern = new AppointmentRecurringPattern();
        Appointment appointment1 = mock(Appointment.class);
        Appointment appointment2 = mock(Appointment.class);
        Appointment appointment3 = mock(Appointment.class);

        when(appointment1.getVoided()).thenReturn(true);
        when(appointment2.getVoided()).thenReturn(false);
        when(appointment3.getVoided()).thenReturn(false);

        Set<Appointment> appointments = new HashSet<>();
        appointments.add(appointment1);
        appointments.add(appointment2);
        appointments.add(appointment3);
        appointmentRecurringPattern.setAppointments(appointments);

        Set<Appointment> activeAppointments = appointmentRecurringPattern.getActiveAppointments();

        assertEquals(3, appointments.size());
        assertEquals(2,activeAppointments.size());
        assertTrue(activeAppointments.contains(appointment2));
        assertTrue(activeAppointments.contains(appointment3));
        assertFalse(activeAppointments.contains(appointment1));
    }

    @Test
    public void shouldNotReturnAnyAppointmentWhenAllAppointmentsAreVoided() {
        AppointmentRecurringPattern appointmentRecurringPattern = new AppointmentRecurringPattern();
        Appointment appointment1 = mock(Appointment.class);
        Appointment appointment2 = mock(Appointment.class);
        Appointment appointment3 = mock(Appointment.class);

        when(appointment1.getVoided()).thenReturn(true);
        when(appointment2.getVoided()).thenReturn(true);
        when(appointment3.getVoided()).thenReturn(true);

        Set<Appointment> appointments = new HashSet<>();
        appointments.add(appointment1);
        appointments.add(appointment2);
        appointments.add(appointment3);
        appointmentRecurringPattern.setAppointments(appointments);

        Set<Appointment> activeAppointments = appointmentRecurringPattern.getActiveAppointments();

        assertEquals(3, appointments.size());
        assertEquals(0,activeAppointments.size());
        assertFalse(activeAppointments.contains(appointment2));
        assertFalse(activeAppointments.contains(appointment3));
        assertFalse(activeAppointments.contains(appointment1));
    }
}