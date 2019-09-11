package org.openmrs.module.appointments.web.service.impl;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.openmrs.Patient;
import org.openmrs.module.appointments.model.Appointment;
import org.openmrs.module.appointments.model.AppointmentRecurringPattern;
import org.openmrs.module.appointments.service.AppointmentsService;
import org.openmrs.module.appointments.web.contract.AppointmentRequest;
import org.openmrs.module.appointments.web.contract.RecurringAppointmentRequest;
import org.openmrs.module.appointments.web.mapper.AppointmentMapper;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.Collections;
import java.util.HashSet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(PowerMockRunner.class)
public class SingleAppointmentRecurringPatternUpdateServiceTest {

    @Mock
    private AppointmentsService appointmentsService;

    @Mock
    private AppointmentMapper appointmentMapper;

    @InjectMocks
    SingleAppointmentRecurringPatternUpdateService singleAppointmentRecurringPatternUpdateService;

    @Test
    public void shouldReturnNewAppointmentByAddingOldAppointmentAsRelatedAppointment() {
        RecurringAppointmentRequest recurringAppointmentRequest = mock(RecurringAppointmentRequest.class);
        AppointmentRequest appointmentRequest = mock(AppointmentRequest.class);
        when(recurringAppointmentRequest.getAppointmentRequest()).thenReturn(appointmentRequest);
        String appointmentRequestUUID = "appointmentRequestUUID";
        when(appointmentRequest.getUuid()).thenReturn(appointmentRequestUUID);

        Appointment appointment = new Appointment();
        Patient patient = mock(Patient.class);
        AppointmentRecurringPattern appointmentRecurringPattern = mock(AppointmentRecurringPattern.class);
        appointment.setPatient(patient);
        appointment.setAppointmentRecurringPattern(appointmentRecurringPattern);

        when(appointmentsService.getAppointmentByUuid(appointmentRequestUUID)).thenReturn(appointment);
        doNothing().when(appointmentMapper).mapAppointmentRequestToAppointment(eq(appointmentRequest), any(Appointment.class));
        when(appointmentRecurringPattern.getAppointments()).thenReturn(new HashSet<>(Collections.singletonList(appointment)));

        Appointment newAppointment = singleAppointmentRecurringPatternUpdateService.getUpdatedAppointment(recurringAppointmentRequest);

        assertEquals(newAppointment.getRelatedAppointment(), appointment);
        assertEquals(newAppointment.getPatient(), patient);
        assertTrue(appointment.getVoided());
        assertEquals(newAppointment.getAppointmentRecurringPattern(), appointment.getAppointmentRecurringPattern());
        assertEquals(2, newAppointment.getAppointmentRecurringPattern().getAppointments().size());
        verify(appointmentMapper).mapAppointmentRequestToAppointment(appointmentRequest, newAppointment);
    }

    @Test
    public void shouldUpdateAppointmentWithRelatedAppointment() {
        RecurringAppointmentRequest recurringAppointmentRequest = mock(RecurringAppointmentRequest.class);
        AppointmentRequest appointmentRequest = mock(AppointmentRequest.class);
        when(recurringAppointmentRequest.getAppointmentRequest()).thenReturn(appointmentRequest);
        String appointmentRequestUUID = "appointmentRequestUUID";
        when(appointmentRequest.getUuid()).thenReturn(appointmentRequestUUID);

        Appointment appointment = new Appointment();
        AppointmentRecurringPattern appointmentRecurringPattern = mock(AppointmentRecurringPattern.class);
        appointment.setAppointmentRecurringPattern(appointmentRecurringPattern);
        appointment.setRelatedAppointment(mock(Appointment.class));

        when(appointmentsService.getAppointmentByUuid(appointmentRequestUUID)).thenReturn(appointment);
        doNothing().when(appointmentMapper).mapAppointmentRequestToAppointment(eq(appointmentRequest), any(Appointment.class));

        Appointment newAppointment = singleAppointmentRecurringPatternUpdateService.getUpdatedAppointment(recurringAppointmentRequest);

        verify(appointmentMapper).mapAppointmentRequestToAppointment(appointmentRequest, newAppointment);
        assertEquals(appointment, newAppointment);
    }
}
