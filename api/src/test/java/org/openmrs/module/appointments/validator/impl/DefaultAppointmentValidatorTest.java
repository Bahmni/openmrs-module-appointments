package org.openmrs.module.appointments.validator.impl;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;
import org.openmrs.Patient;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.context.Context;
import org.openmrs.module.appointments.model.Appointment;
import org.openmrs.module.appointments.model.AppointmentServiceDefinition;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

public class DefaultAppointmentValidatorTest {

    @Mock
    private AdministrationService administrationService;

    @InjectMocks
    private DefaultAppointmentValidator defaultAppointmentValidator;

    private MockedStatic<Context> contextMockedStatic;
    private AutoCloseable mocks;

    @Before
    public void init() {
        mocks = MockitoAnnotations.openMocks(this);
        contextMockedStatic = mockStatic(Context.class);
        when(Context.getAdministrationService()).thenReturn(administrationService);
    }

    @After
    public void tearDown() throws Exception {
        if (contextMockedStatic != null) contextMockedStatic.close();
        if (mocks != null) mocks.close();
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
