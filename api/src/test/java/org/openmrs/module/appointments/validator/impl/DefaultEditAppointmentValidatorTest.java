package org.openmrs.module.appointments.validator.impl;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.openmrs.Patient;
import org.openmrs.module.appointments.dao.AppointmentDao;
import org.openmrs.module.appointments.model.Appointment;
import org.openmrs.module.appointments.model.AppointmentServiceDefinition;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

public class DefaultEditAppointmentValidatorTest {

    @Mock
    private AppointmentDao appointmentDao;

    @InjectMocks
    private DefaultEditAppointmentValidator defaultEditAppointmentValidator;

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);
    }

    private Appointment createAppointment(String uuid, Patient patient) {
        Appointment appointment = new Appointment();
        appointment.setPatient(patient);
        appointment.setUuid(uuid);
        appointment.setService(new AppointmentServiceDefinition());
        return appointment;
    }

    @Test
    public void shouldAddErrorIfPatientUuidsAreDifferentForSavedAppointmentAndRequestedAppointment() {
        Patient requestAppointmentPatient = new Patient();
        requestAppointmentPatient.setUuid("requestAppointmentPatient");
        Patient savedAppointmentPatient = new Patient();
        savedAppointmentPatient.setUuid("savedAppointmentPatient");
        String appointmentUuid = "appointmentUuid";
        Appointment requestAppointment = createAppointment(appointmentUuid, requestAppointmentPatient);
        Appointment savedAppointment = createAppointment(appointmentUuid, savedAppointmentPatient);
        List<String> errors = new ArrayList<>();
        when(appointmentDao.getAppointmentByUuid(appointmentUuid)).thenReturn(savedAppointment);

        defaultEditAppointmentValidator.validate(requestAppointment, errors);

        assertEquals(1, errors.size());
        assertEquals("Appointment cannot be updated with that patient", errors.get(0));
    }

    @Test
    public void shouldNotAddErrorIfPatientUuidsAreSameForSavedAppointmentAndRequestedAppointment() {
        Patient patient = new Patient();
        patient.setUuid("uuid");
        String appointmentUuid = "appointmentUuid";
        Appointment requestAppointment = createAppointment(appointmentUuid, patient);
        Appointment savedAppointment = createAppointment(appointmentUuid, patient);
        List<String> errors = new ArrayList<>();
        when(appointmentDao.getAppointmentByUuid(appointmentUuid)).thenReturn(savedAppointment);

        defaultEditAppointmentValidator.validate(requestAppointment, errors);

        assertEquals(0, errors.size());
    }

    @Test
    public void shouldAddErrorWhenSavedAppointmentIsNull() {
        String uuid = "uuid";
        Appointment appointment = createAppointment(uuid, null);
        List<String> errors = new ArrayList<>();
        when(appointmentDao.getAppointmentByUuid(uuid)).thenReturn(null);

        defaultEditAppointmentValidator.validate(appointment, errors);

        assertEquals(1, errors.size());
        assertEquals("Invalid appointment for edit", errors.get(0));
    }

    @Test
    public void shouldThrowErrorWhenServiceIsNullInAppointment() {
        Patient patient = new Patient();
        patient.setUuid("patient");
        String appointmentUuid = "uuid";
        Appointment requestAppointment = createAppointment(appointmentUuid, patient);
        requestAppointment.setService(null);
        Appointment savedAppointment = createAppointment(appointmentUuid, patient);
        List<String> errors = new ArrayList<>();
        when(appointmentDao.getAppointmentByUuid(appointmentUuid)).thenReturn(savedAppointment);

        defaultEditAppointmentValidator.validate(requestAppointment, errors);

        assertEquals(1, errors.size());
        assertEquals("Appointment cannot be updated without Service", errors.get(0));
    }
}
