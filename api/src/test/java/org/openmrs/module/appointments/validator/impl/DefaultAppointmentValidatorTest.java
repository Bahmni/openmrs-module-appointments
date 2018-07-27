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
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest({Context.class})
public class DefaultAppointmentValidatorTest {

    @Mock
    private AdministrationService administrationService;

    @InjectMocks
    private DefaultAppointmentValidator defaultAppointmentValidator;

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);
        PowerMockito.mockStatic(Context.class);

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
