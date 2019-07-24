package org.openmrs.module.appointments.web.validators.impl;

import org.junit.Test;
import org.mockito.InjectMocks;
import org.openmrs.module.appointments.model.Appointment;
import org.openmrs.module.appointments.web.validators.Validator;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;

public class AppointmentValidatorTest {
    @InjectMocks
    Validator<Appointment> appointmentValidator = new AppointmentValidator();

    @Test
    public void shouldReturnTrueWhenAppointmentIsNotNull() {
        Appointment appointment = mock(Appointment.class);
        final boolean isValid = appointmentValidator.validate(appointment);
        assertTrue(isValid);
    }

    @Test
    public void shouldReturnFalseWhenAppointmentIsNull() {
        final boolean isValid = appointmentValidator.validate(null);
        assertFalse(isValid);
    }

    @Test
    public void shouldSetErrorWhenAppointmentIsNull() {
        appointmentValidator.validate(null);
        assertNotNull(appointmentValidator.getError());
    }

    @Test
    public void shouldNotSetErrorWhenAppointmentIsNotNull() {
        Appointment appointment = mock(Appointment.class);
        appointmentValidator.validate(appointment);
        assertNull(appointmentValidator.getError());
    }
}
