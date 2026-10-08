package org.openmrs.module.appointments.validator.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.openmrs.Patient;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.context.Context;
import org.openmrs.module.appointments.model.Appointment;
import org.openmrs.module.appointments.model.AppointmentServiceDefinition;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.junit.jupiter.api.AfterEach;

public class DefaultAppointmentValidatorTest {

    private MockedStatic<Context> contextMockedStatic;

    @AfterEach
    public void closeStaticMocks() {
        if (contextMockedStatic != null) {
            contextMockedStatic.close();
        }
    }

    @Mock
    private AdministrationService administrationService;

    @InjectMocks
    private DefaultAppointmentValidator defaultAppointmentValidator;

    @BeforeEach
    public void init() {
        MockitoAnnotations.openMocks(this);
        contextMockedStatic = Mockito.mockStatic(Context.class);

        when(Context.getAdministrationService()).thenReturn(administrationService);
    }

    @Test
    public void shouldAddErrorIfThereIsNoPatientForAnAppointment() throws Exception {
        Appointment appointment = new Appointment();
        appointment.setService(new AppointmentServiceDefinition());
        List<String> errors = new ArrayList<>();
        defaultAppointmentValidator.validate(appointment, errors);
        assertEquals(1,errors.size());
        assertEquals("Appointment cannot be created without Patient", errors.get(0));
    }

    @Test
    public void shouldAddErrorIfThereIsNoServiceForAnAppointment() throws Exception {
        Appointment appointment = new Appointment();
        appointment.setPatient(new Patient());
        List<String> errors = new ArrayList<>();
        defaultAppointmentValidator.validate(appointment, errors);
        assertEquals(1,errors.size());
        assertEquals("Appointment cannot be created without Service", errors.get(0));
    }
}
