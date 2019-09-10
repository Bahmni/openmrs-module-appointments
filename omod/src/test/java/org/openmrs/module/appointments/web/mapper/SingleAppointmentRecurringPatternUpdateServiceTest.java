package org.openmrs.module.appointments.web.mapper;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.openmrs.module.appointments.helper.AppointmentServiceHelper;
import org.openmrs.module.appointments.model.Appointment;
import org.openmrs.module.appointments.model.AppointmentAudit;
import org.openmrs.module.appointments.model.AppointmentRecurringPattern;
import org.openmrs.module.appointments.service.AppointmentsService;
import org.openmrs.module.appointments.web.contract.AppointmentRequest;
import org.openmrs.module.appointments.web.contract.RecurringAppointmentRequest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith(PowerMockRunner.class)
public class SingleAppointmentRecurringPatternUpdateServiceTest {

    @Mock
    private AppointmentsService appointmentsService;

    @Mock
    private AppointmentMapper appointmentMapper;

    @Mock
    private AppointmentServiceHelper appointmentServiceHelper;

    @InjectMocks
    SingleAppointmentRecurringPatternUpdateService singleAppointmentRecurringPatternUpdateService;
    @Rule
    public ExpectedException expectedException = ExpectedException.none();


    @Test
    public void shouldReturnAppointmentRecurringPatternWithAnAppointmentReferringToTheAppointmentInAppointmentRequest() throws IOException {
        RecurringAppointmentRequest recurringAppointmentRequest = mock(RecurringAppointmentRequest.class);
        Appointment appointment = mock(Appointment.class);
        AppointmentRecurringPattern appointmentRecurringPattern = mock(AppointmentRecurringPattern.class);
        AppointmentRequest appointmentRequest = mock(AppointmentRequest.class);

        when(recurringAppointmentRequest.getAppointmentRequest()).thenReturn(appointmentRequest);
        when(recurringAppointmentRequest.getAppointmentRequest().getUuid()).thenReturn("uuid");
        when(appointmentsService.getAppointmentByUuid("uuid")).thenReturn(appointment);
        doNothing().when(appointmentMapper).mapAppointmentRequestToAppointment(eq(appointmentRequest), any(Appointment.class));
        when(appointment.getAppointmentRecurringPattern()).thenReturn(appointmentRecurringPattern);
        when(appointmentRecurringPattern.getAppointments()).thenReturn(new HashSet<>(Arrays.asList(appointment)));

        final AppointmentRecurringPattern returnedAppointmentRecurringPattern = singleAppointmentRecurringPatternUpdateService.getUpdatedRecurringPattern(recurringAppointmentRequest);

        verify(recurringAppointmentRequest.getAppointmentRequest(), times(1)).getUuid();
        verify(appointmentsService, times(1)).getAppointmentByUuid("uuid");
        verify(appointment, times(1)).getAppointmentRecurringPattern();
        verify(appointmentRecurringPattern, times(1)).getAppointments();
        verify(appointmentMapper, times(1)).mapAppointmentRequestToAppointment(eq(recurringAppointmentRequest.getAppointmentRequest()), any());

        final ArrayList<Appointment> finalAppointments = new ArrayList<>(returnedAppointmentRecurringPattern.getAppointments());
        final Appointment appointment1 = finalAppointments.get(0);
        final Appointment appointment2 = finalAppointments.get(1);

        final Appointment newAppointment = appointment1.getId() == null ? appointment1 : appointment2;
        final Appointment oldAppointment = appointment1.getId() != null ? appointment1 : appointment2;

        assertEquals(2, returnedAppointmentRecurringPattern.getAppointments().size());
        assertEquals(appointment, newAppointment.getRelatedAppointment());
        assertNull(newAppointment.getId());
        assertEquals(null, newAppointment.getAppointmentNumber());
        assertEquals(appointment, oldAppointment);

    }

    @Test
    public void shouldVoidTheOldAppointmentWhenNewAppointmentIsCreatedFromAppointmentRequest() {
        RecurringAppointmentRequest recurringAppointmentRequest = mock(RecurringAppointmentRequest.class);
        Appointment appointment = new Appointment();
        AppointmentRequest appointmentRequest = mock(AppointmentRequest.class);
        AppointmentRecurringPattern appointmentRecurringPattern = mock(AppointmentRecurringPattern.class);
        appointment.setAppointmentRecurringPattern(appointmentRecurringPattern);
        appointment.setVoided(false);

        when(recurringAppointmentRequest.getAppointmentRequest()).thenReturn(appointmentRequest);
        when(recurringAppointmentRequest.getAppointmentRequest().getUuid()).thenReturn("uuid");
        when(appointmentsService.getAppointmentByUuid("uuid")).thenReturn(appointment);
        doNothing().when(appointmentMapper).mapAppointmentRequestToAppointment(eq(appointmentRequest), any(Appointment.class));
        when(appointmentRecurringPattern.getAppointments()).thenReturn(new HashSet<>(Arrays.asList(appointment)));

        singleAppointmentRecurringPatternUpdateService.getUpdatedRecurringPattern(recurringAppointmentRequest);

        assertEquals(true, appointment.getVoided());
    }

    @Test
    public void shouldNotCreateANewRelatedAppointmentWhenTheAppointmentAlreadyHasARelatedAppointment(){
        RecurringAppointmentRequest recurringAppointmentRequest = mock(RecurringAppointmentRequest.class);
        Appointment appointment = mock(Appointment.class);
        Appointment relatedAppointment = mock(Appointment.class);
        AppointmentRequest appointmentRequest = mock(AppointmentRequest.class);
        AppointmentRecurringPattern appointmentRecurringPattern = mock(AppointmentRecurringPattern.class);

        when(recurringAppointmentRequest.getAppointmentRequest()).thenReturn(appointmentRequest);
        when(recurringAppointmentRequest.getAppointmentRequest().getUuid()).thenReturn("uuid");
        when(appointmentsService.getAppointmentByUuid("uuid")).thenReturn(appointment);
        when(appointment.getRelatedAppointment()).thenReturn(relatedAppointment);
        when(appointment.getAppointmentRecurringPattern()).thenReturn(appointmentRecurringPattern);
        when(appointmentRecurringPattern.getAppointments()).thenReturn(new HashSet<>(Arrays.asList(appointment, relatedAppointment)));

        assertEquals(2, appointmentRecurringPattern.getAppointments().size());

        final AppointmentRecurringPattern returnedAppointmentRecurringPattern = singleAppointmentRecurringPatternUpdateService.getUpdatedRecurringPattern(recurringAppointmentRequest);

        verify(appointment, times(1)).getRelatedAppointment();
        verify(appointmentMapper, times(1))
                .mapAppointmentRequestToAppointment(recurringAppointmentRequest.getAppointmentRequest(),appointment);
        assertEquals(2, returnedAppointmentRecurringPattern.getAppointments().size());

    }

    @Test
    public void shouldAddTheAuditLogInTheNewAppointmentCreatedFromSingleAppointmentRequest() throws IOException {
        RecurringAppointmentRequest recurringAppointmentRequest = mock(RecurringAppointmentRequest.class);
        AppointmentRequest appointmentRequest = mock(AppointmentRequest.class);
        Appointment appointment = mock(Appointment.class);
        AppointmentRecurringPattern appointmentRecurringPattern = mock(AppointmentRecurringPattern.class);
        String notes = "";
        AppointmentAudit appointmentAuditMock = mock(AppointmentAudit.class);

        when(recurringAppointmentRequest.getAppointmentRequest()).thenReturn(appointmentRequest);
        when(recurringAppointmentRequest.getAppointmentRequest().getUuid()).thenReturn("uuid");
        when(appointmentsService.getAppointmentByUuid("uuid")).thenReturn(appointment);
        doNothing().when(appointmentMapper).mapAppointmentRequestToAppointment(eq(appointmentRequest), any(Appointment.class));

        when(appointmentServiceHelper.getAppointmentAsJsonString(appointment)).thenReturn(notes);
        when(appointmentServiceHelper.getAppointmentAuditEvent(appointment, notes))
                .thenReturn(appointmentAuditMock);
        when(appointment.getAppointmentRecurringPattern()).thenReturn(appointmentRecurringPattern);
        when(appointmentRecurringPattern.getAppointments()).thenReturn(new HashSet<>(Arrays.asList(appointment)));

        final AppointmentRecurringPattern returnedAppointmentRecurringPattern = singleAppointmentRecurringPatternUpdateService.getUpdatedRecurringPattern(recurringAppointmentRequest);
        final ArrayList<Appointment> finalAppointments = new ArrayList<>(returnedAppointmentRecurringPattern.getAppointments());
        final Appointment appointment1 = finalAppointments.get(0);
        final Appointment appointment2 = finalAppointments.get(1);

        final Appointment newAppointment = appointment1.getId() == null ? appointment1 : appointment2;
        final Appointment oldAppointment = appointment1.getId() != null ? appointment1 : appointment2;
        assertNotNull(newAppointment.getAppointmentAudits());

    }

    @Test
    public void shouldAddTheAuditLogInTheExistingAppointmentCreatedFromSingleAppointmentRequest() throws IOException {
        RecurringAppointmentRequest recurringAppointmentRequest = mock(RecurringAppointmentRequest.class);
        AppointmentRequest appointmentRequest = mock(AppointmentRequest.class);
        Appointment appointment = mock(Appointment.class);
        Appointment relatedAppointment = mock(Appointment.class);
        AppointmentRecurringPattern appointmentRecurringPattern = mock(AppointmentRecurringPattern.class);

        when(recurringAppointmentRequest.getAppointmentRequest()).thenReturn(appointmentRequest);
        when(recurringAppointmentRequest.getAppointmentRequest().getUuid()).thenReturn("uuid");
        when(appointmentsService.getAppointmentByUuid("uuid")).thenReturn(appointment);
        when(appointment.getRelatedAppointment()).thenReturn(relatedAppointment);
        when(appointment.getAppointmentRecurringPattern()).thenReturn(appointmentRecurringPattern);
        when(appointmentRecurringPattern.getAppointments()).thenReturn(new HashSet<>(Arrays.asList(appointment, relatedAppointment)));
        assertEquals(2, appointmentRecurringPattern.getAppointments().size());

        singleAppointmentRecurringPatternUpdateService.getUpdatedRecurringPattern(recurringAppointmentRequest);
        assertNotNull(appointment.getAppointmentAudits());

    }
}
