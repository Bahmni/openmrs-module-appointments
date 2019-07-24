package org.openmrs.module.appointments.web.validators.impl;

import org.junit.Test;
import org.mockito.InjectMocks;
import org.openmrs.Patient;
import org.openmrs.module.appointments.web.contract.AppointmentRequest;
import org.openmrs.module.appointments.web.validators.Validator;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class PatientValidatorTest {

    @InjectMocks
    Validator<Patient> patientValidator = new PatientValidator();

    @Test
    public void shouldReturnTrueWhenPatientUuidIsNotNull() {
        Patient patient = mock(Patient.class);
        when(patient.getUuid()).thenReturn("patientUuid");
        final boolean isValid = patientValidator.validate(patient);
        assertTrue(isValid);
    }

    @Test
    public void shouldReturnFalseWhenPatientIsNull() {
        final boolean isValid = patientValidator.validate(null);
        assertFalse(isValid);
    }

    @Test
    public void shouldReturnFalseWhenPatientUuidIsNull() {
        Patient patient = mock(Patient.class);
        when(patient.getUuid()).thenReturn(null);
        final boolean isValid = patientValidator.validate(patient);
        assertFalse(isValid);
    }

    @Test
    public void shouldNotSetErrorWhenPatientUuidIsNotNull() {
        Patient patient = mock(Patient.class);
        when(patient.getUuid()).thenReturn("patientUuid");
        patientValidator.validate(patient);
        assertNull(patientValidator.getError());
    }

    @Test
    public void shouldSetErrorWhenPatientIsNull() {
        patientValidator.validate(null);
        assertNotNull(patientValidator.getError());

    }

    @Test
    public void shouldSetErrorWhenPatientUuidIsNull() {
        Patient patient = mock(Patient.class);
        when(patient.getUuid()).thenReturn(null);
        patientValidator.validate(patient);
        assertNotNull(patientValidator.getError());
    }
}
