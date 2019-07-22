package org.openmrs.module.appointments.web.mapper;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.openmrs.api.APIException;
import org.openmrs.module.appointments.model.Appointment;
import org.openmrs.module.appointments.model.AppointmentRecurringPattern;
import org.openmrs.module.appointments.service.AppointmentsService;
import org.openmrs.module.appointments.web.contract.AppointmentRequest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.*;

@RunWith(PowerMockRunner.class)
public class SingleAppointmentRecurringPatternMapperTest {

    @Mock
    private AppointmentsService appointmentsService;

    @Mock
    private AppointmentMapper appointmentMapper;

    @InjectMocks
    SingleAppointmentRecurringPatternMapper singleAppointmentRecurringPatternMapper;
    @Rule
    public ExpectedException expectedException = ExpectedException.none();


    @Test
    public void shouldReturnAppointmentRecurringPatternWithAnAppointmentReferringToTheAppointmentInAppointmentRequest() {
        AppointmentRequest appointmentRequest = mock(AppointmentRequest.class);
        Appointment appointment = mock(Appointment.class);
        AppointmentRecurringPattern appointmentRecurringPattern = mock(AppointmentRecurringPattern.class);

        when(appointmentRequest.getUuid()).thenReturn("uuid");

        when(appointmentsService.getAppointmentByUuid("uuid")).thenReturn(appointment);
        doNothing().when(appointmentMapper).mapAppointmentRequestToAppointment(eq(appointmentRequest), any(Appointment.class));


        when(appointment.getAppointmentRecurringPattern()).thenReturn(appointmentRecurringPattern);
        when(appointmentRecurringPattern.getAppointments()).thenReturn(new HashSet<>(Arrays.asList(appointment)));

        final AppointmentRecurringPattern returnedAppointmentRecurringPattern = singleAppointmentRecurringPatternMapper.fromRequest(appointmentRequest);

        verify(appointmentRequest, times(1)).getUuid();
        verify(appointmentsService, times(1)).getAppointmentByUuid("uuid");

        verify(appointment, times(1)).getAppointmentRecurringPattern();
        verify(appointmentRecurringPattern, times(1)).getAppointments();
        verify(appointmentMapper, times(1)).mapAppointmentRequestToAppointment(eq(appointmentRequest), any());

        final ArrayList<Appointment> finalAppointments = new ArrayList<>(returnedAppointmentRecurringPattern.getAppointments());
        final Appointment appointment1 = finalAppointments.get(0);
        final Appointment appointment2 = finalAppointments.get(1);

        final Appointment newAppointment = appointment1.getId() == null ? appointment1 : appointment2;
        final Appointment oldAppointment = appointment1.getId() != null ? appointment1 : appointment2;

        assertEquals(2, returnedAppointmentRecurringPattern.getAppointments().size());
        assertEquals(appointment, newAppointment.getRelatedAppointment());
        assertNull(newAppointment.getId());
        assertEquals(0, newAppointment.getAppointmentAudits().size());
        assertEquals(null, newAppointment.getAppointmentNumber());
        assertEquals(appointment, oldAppointment);

    }

    @Test

    public void shouldThrowAnExpectionIfAppointmentInAppointmentRequestNotFound(){
        AppointmentRequest appointmentRequest = mock(AppointmentRequest.class);
        when(appointmentRequest.getUuid()).thenReturn("uuid");
        when(appointmentsService.getAppointmentByUuid("uuid")).thenReturn(null);
        expectedException.expect(APIException.class);
        expectedException.expectMessage("Appointment does not exist.");

        singleAppointmentRecurringPatternMapper.fromRequest(appointmentRequest);
    }

}
