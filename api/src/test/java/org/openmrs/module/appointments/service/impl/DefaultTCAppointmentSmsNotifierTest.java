package org.openmrs.module.appointments.service.impl;

import org.databene.commons.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.AdditionalMatchers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.openmrs.Patient;
import org.openmrs.PersonAttribute;
import org.openmrs.PersonAttributeType;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.context.Context;
import org.openmrs.messagesource.MessageSourceService;
import org.openmrs.module.appointments.model.Appointment;
import org.openmrs.module.appointments.notification.AppointmentEventNotifier;
import org.openmrs.module.appointments.notification.NotificationException;
import org.openmrs.module.appointments.notification.SmsSender;
import org.openmrs.module.appointments.notification.impl.DefaultTCAppointmentPatientEmailNotifier;
import org.openmrs.module.appointments.notification.impl.DefaultTCAppointmentSmsNotifier;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest(Context.class)
public class DefaultTCAppointmentSmsNotifierTest {
    private static final String BAHMNI_APPOINTMENT_TELE_CONSULTATION_SMS_NOTIFICATION_TEMPLATE = "bahmni.appointment.teleConsultation.patientSmsNotificationTemplate";
    private static final String BAHMNI_APPOINTMENT_TELE_CONSULTATION_CONTACT_ATTRIBUTE = "bahmni.appointment.teleConsultation.contactAttribute";
    private AppointmentEventNotifier tcAppointmentEventNotifier;

    @Mock
    private AdministrationService administrationService;

    @Mock
    private SmsSender smsSender;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);
        mockStatic(Context.class);
        tcAppointmentEventNotifier = new DefaultTCAppointmentSmsNotifier(smsSender);
        PowerMockito.when(Context.getAdministrationService()).thenReturn(administrationService);
        when(administrationService.getGlobalProperty(BAHMNI_APPOINTMENT_TELE_CONSULTATION_SMS_NOTIFICATION_TEMPLATE)).thenReturn("Email body");
        when(administrationService.getGlobalProperty(BAHMNI_APPOINTMENT_TELE_CONSULTATION_CONTACT_ATTRIBUTE)).thenReturn("primaryContact");
    }

    @Test
    public void shouldSendTeleconsultationAppointmentLinkSms() throws Exception {
        Appointment appointment = buildAppointment();
        tcAppointmentEventNotifier.sendNotification(appointment);
        List<String> contacts = new ArrayList<>();
        contacts.add("+61411111111");
        verify(smsSender).send(
                eq("Email body"),
                eq(contacts));
    }

    @Test
    public void shouldThrowExceptionIfSendingFails() throws NotificationException {
        Appointment appointment = buildAppointment();
        doThrow(new RuntimeException()).when(smsSender).send(any(), any());
        expectedException.expect(NotificationException.class);
        tcAppointmentEventNotifier.sendNotification(appointment);
    }

    private Appointment buildAppointment() {
        Appointment appointment = new Appointment();
        Patient patient = new Patient();
        patient.setUuid("patientUuid");
        PersonAttributeType personAttributeType = new PersonAttributeType();
        personAttributeType.setName("primaryContact");
        patient.addAttribute(new PersonAttribute(personAttributeType, "+61411111111"));
        appointment.setPatient(patient);
        return appointment;
    }
}
