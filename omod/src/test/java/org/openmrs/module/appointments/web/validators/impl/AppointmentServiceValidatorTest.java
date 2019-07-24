package org.openmrs.module.appointments.web.validators.impl;

import org.junit.Test;
import org.mockito.InjectMocks;
import org.openmrs.module.appointments.model.AppointmentServiceDefinition;
import org.openmrs.module.appointments.web.validators.Validator;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class AppointmentServiceValidatorTest {

    @InjectMocks
    Validator<AppointmentServiceDefinition> appointmentServiceValidator = new AppointmentServiceValidator();

    @Test
    public void shouldReturnTrueWhenPatientUuidIsNotNull() {
        AppointmentServiceDefinition appointmentServiceDefinition = mock(AppointmentServiceDefinition.class);
        when(appointmentServiceDefinition.getUuid()).thenReturn("serviceUuid");
        final boolean isValid = appointmentServiceValidator.validate(appointmentServiceDefinition);
        assertTrue(isValid);
    }

    @Test
    public void shouldReturnFalseWhenAppointmentServiceDefinitionIsNull() {
        final boolean isValid = appointmentServiceValidator.validate(null);
        assertFalse(isValid);
    }

    @Test
    public void shouldReturnFalseWhenAppointmentServiceDefinitionUuidIsNull() {
        AppointmentServiceDefinition appointmentServiceDefinition = mock(AppointmentServiceDefinition.class);
        when(appointmentServiceDefinition.getUuid()).thenReturn(null);
        final boolean isValid = appointmentServiceValidator.validate(appointmentServiceDefinition);
        assertFalse(isValid);
    }

    @Test
    public void shouldNotSetErrorWhenAppointmentServiceDefinitionUuidIsNotNull() {
        AppointmentServiceDefinition appointmentServiceDefinition = mock(AppointmentServiceDefinition.class);
        when(appointmentServiceDefinition.getUuid()).thenReturn("serviceUuid");
        appointmentServiceValidator.validate(appointmentServiceDefinition);
        assertNull(appointmentServiceValidator.getError());
    }

    @Test
    public void shouldSetErrorWhenAppointmentServiceDefinitionIsNull() {
        appointmentServiceValidator.validate(null);
        assertNotNull(appointmentServiceValidator.getError());

    }

    @Test
    public void shouldSetErrorWhenAppointmentServiceDefinitionUuidIsNull() {
        AppointmentServiceDefinition appointmentServiceDefinition = mock(AppointmentServiceDefinition.class);
        when(appointmentServiceDefinition.getUuid()).thenReturn(null);
        appointmentServiceValidator.validate(appointmentServiceDefinition);
        assertNotNull(appointmentServiceValidator.getError());
    }

}
