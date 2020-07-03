package org.openmrs.module.appointments.conflicts.impl;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.runners.MockitoJUnitRunner;
import org.openmrs.Patient;
import org.openmrs.module.appointments.dao.AppointmentDao;
import org.openmrs.module.appointments.helper.DateHelper;
import org.openmrs.module.appointments.model.Appointment;
import org.openmrs.module.appointments.model.AppointmentStatus;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class PatientDoubleBookingConflictTest {

    @InjectMocks
    private PatientDoubleBookingConflict patientDoubleBookingConflict;

    @Mock
    private AppointmentDao appointmentDao;

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void shouldReturnDoubleBookingConflictWithSameTime() {
        Patient patient = new Patient();
        patient.setId(1);
        Appointment patientAppointment = new Appointment();
        patientAppointment.setStartDateTime(DateHelper.getDate(2119,8,1,11,0,0));
        patientAppointment.setEndDateTime(DateHelper.getDate(2119,8,1,12,0,0));
        Appointment appointment = new Appointment();
        appointment.setPatient(patient);
        appointment.setStartDateTime(DateHelper.getDate(2119,8,1,11,0,0));
        appointment.setEndDateTime(DateHelper.getDate(2119,8,1,12,0,0));
        when(appointmentDao.getAppointmentsForPatient(1)).thenReturn(Collections.singletonList(patientAppointment));

        List<Appointment> appointments = patientDoubleBookingConflict.getConflicts(Collections.singletonList(appointment));

        assertNotNull(appointments);
        assertEquals(1,appointments.size());
        assertEquals(patientAppointment,appointments.get(0));
    }

    @Test
    public void shouldNotConflictWithCancelledAppointmentOnSameTime() {
        Patient patient = new Patient();
        patient.setId(1);
        Appointment patientAppointment = new Appointment();
        patientAppointment.setStartDateTime(DateHelper.getDate(2119,8,1,11,0,0));
        patientAppointment.setEndDateTime(DateHelper.getDate(2119,8,1,12,0,0));
        patientAppointment.setStatus(AppointmentStatus.Cancelled);

        Appointment appointment = new Appointment();
        appointment.setPatient(patient);
        appointment.setStartDateTime(DateHelper.getDate(2119,8,1,11,0,0));
        appointment.setEndDateTime(DateHelper.getDate(2119,8,1,12,0,0));
        when(appointmentDao.getAppointmentsForPatient(1)).thenReturn(Collections.singletonList(patientAppointment));

        List<Appointment> appointments = patientDoubleBookingConflict.getConflicts(Collections.singletonList(appointment));

        assertNotNull(appointments);
        assertEquals(0,appointments.size());
    }

    @Test
    public void shouldReturnAppointmentWithOverlapping() {
        Patient patient = new Patient();
        patient.setId(1);
        Appointment patientAppointment = new Appointment();
        patientAppointment.setStartDateTime(DateHelper.getDate(2119,8,1,11,0,0));
        patientAppointment.setEndDateTime(DateHelper.getDate(2119,8,1,11,30,0));
        Appointment patientAppointmentTwo = new Appointment();
        patientAppointmentTwo.setStartDateTime(DateHelper.getDate(2119,8,1,11,0,0));
        patientAppointmentTwo.setEndDateTime(DateHelper.getDate(2119,8,1,11,15,0));
        Appointment appointment = new Appointment();
        appointment.setPatient(patient);
        appointment.setStartDateTime(DateHelper.getDate(2119,8,1,10,30,0));
        appointment.setEndDateTime(DateHelper.getDate(2119,8,1,12,30,0));
        when(appointmentDao.getAppointmentsForPatient(1)).thenReturn(Arrays.asList(patientAppointment,patientAppointmentTwo));

        List<Appointment> appointments = patientDoubleBookingConflict.getConflicts(Collections.singletonList(appointment));

        assertNotNull(appointments);
        assertEquals(2,appointments.size());
        assertEquals(patientAppointmentTwo,appointments.get(1));
        assertEquals(patientAppointment,appointments.get(0));
    }

    @Test
    public void shouldReturnAppointmentWithOverlappingPreviousAppDate() {
        Patient patient = new Patient();
        patient.setId(1);
        Appointment patientAppointment = new Appointment();
        patientAppointment.setStartDateTime(DateHelper.getDate(2119,8,1,11,0,0));
        patientAppointment.setEndDateTime(DateHelper.getDate(2119,8,1,12,0,0));
        Appointment patientAppointmentTwo = new Appointment();
        patientAppointmentTwo.setStartDateTime(DateHelper.getDate(2119,8,1,11,30,0));
        patientAppointmentTwo.setEndDateTime(DateHelper.getDate(2119,8,1,13,0,0));
        Appointment patientAppointmentThree = new Appointment();
        patientAppointmentThree.setStartDateTime(DateHelper.getDate(2119,8,1,11,0,0));
        patientAppointmentThree.setEndDateTime(DateHelper.getDate(2119,8,1,11,45,0));
        Appointment patientAppointmentFour = new Appointment();
        patientAppointmentFour.setStartDateTime(DateHelper.getDate(2119,8,1,11,45,0));
        patientAppointmentFour.setEndDateTime(DateHelper.getDate(2119,8,1,12,0,0));
        Appointment appointment = new Appointment();
        appointment.setPatient(patient);
        appointment.setStartDateTime(DateHelper.getDate(2119,8,1,11,30,0));
        appointment.setEndDateTime(DateHelper.getDate(2119,8,1,12,30,0));
        when(appointmentDao.getAppointmentsForPatient(1)).thenReturn(Arrays.asList(patientAppointment,patientAppointmentThree,
                patientAppointmentTwo,patientAppointmentFour));

        List<Appointment> appointments = patientDoubleBookingConflict.getConflicts(Collections.singletonList(appointment));

        assertNotNull(appointments);
        assertEquals(4,appointments.size());
        assertEquals(patientAppointment,appointments.get(0));
        assertEquals(patientAppointmentThree,appointments.get(1));
        assertEquals(patientAppointmentTwo,appointments.get(2));
        assertEquals(patientAppointmentFour,appointments.get(3));
    }

    @Test
    public void shouldNotReturnAppointmentWithOutOverlapping() {
        Patient patient = new Patient();
        patient.setId(1);
        Appointment patientAppointment = new Appointment();
        patientAppointment.setStartDateTime(DateHelper.getDate(2119,8,1,11,0,0));
        patientAppointment.setEndDateTime(DateHelper.getDate(2119,8,1,12,0,0));
        Appointment patientAppointmentTwo = new Appointment();
        patientAppointmentTwo.setStartDateTime(DateHelper.getDate(2119,8,1,12,30,0));
        patientAppointmentTwo.setEndDateTime(DateHelper.getDate(2119,8,1,13,0,0));
        Appointment patientAppointmentThree = new Appointment();
        patientAppointmentThree.setStartDateTime(DateHelper.getDate(2119,8,1,7,30,0));
        patientAppointmentThree.setEndDateTime(DateHelper.getDate(2119,8,1,8,0,0));
        Appointment appointment = new Appointment();
        appointment.setPatient(patient);
        appointment.setStartDateTime(DateHelper.getDate(2119,8,1,12,0,0));
        appointment.setEndDateTime(DateHelper.getDate(2119,8,1,12,30,0));
        when(appointmentDao.getAppointmentsForPatient(1)).thenReturn(Arrays.asList(patientAppointment,patientAppointmentThree,patientAppointmentTwo));

        List<Appointment> appointments = patientDoubleBookingConflict.getConflicts(Collections.singletonList(appointment));

        assertNotNull(appointments);
        assertEquals(0,appointments.size());
    }


}
