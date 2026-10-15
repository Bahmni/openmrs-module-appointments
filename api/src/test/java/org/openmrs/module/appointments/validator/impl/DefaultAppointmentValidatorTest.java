package org.openmrs.module.appointments.validator.impl;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.openmrs.Patient;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.context.Context;
import org.openmrs.module.appointments.model.Appointment;
import org.openmrs.module.appointments.model.AppointmentServiceDefinition;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.MockedStatic;
import org.junit.After;
import org.mockito.Mockito;

@RunWith(MockitoJUnitRunner.class)
public class DefaultAppointmentValidatorTest {
    private MockedStatic<Context> contextMock;


    @Mock
    private AdministrationService administrationService;

    @InjectMocks
    private DefaultAppointmentValidator defaultAppointmentValidator;

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);
        contextMock = Mockito.mockStatic(Context.class);

        contextMock.when(Context::getAdministrationService).thenReturn(administrationService);
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

    @After
    public void tearDownContextMock() {
        if (contextMock != null) {
            contextMock.close();
            contextMock = null;
        }
    }
}
