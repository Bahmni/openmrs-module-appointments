package org.openmrs.module.appointments.service.impl;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.openmrs.*;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.LocationService;
import org.openmrs.api.context.Context;
import org.openmrs.messagesource.MessageSourceService;
import org.openmrs.module.appointments.model.*;
import org.openmrs.module.appointments.properties.AppointmentProperties;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.*;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@PowerMockIgnore("javax.management.*")
@PrepareForTest(Context.class)
@RunWith(PowerMockRunner.class)
public class SMSServiceTest {
    public static final String PATIENT_IDENTIFIER = "GAN230901";
    @Mock
    private AdministrationService administrationService;

    @Mock
    private MessageSourceService messageSourceService;

    @Mock
    private LocationService locationService;

    @Mock
    private AppointmentVisitLocation appointmentVisitLocation;

    @Mock
    private Provider provider;

    private GlobalProperty globalPropertyHelpdesk;
    @Mock
    private Patient patient;
    @InjectMocks
    private SMSService smsService;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        PowerMockito.mockStatic(Context.class);
        when(Context.getAdministrationService()).thenReturn(administrationService);
        when(Context.getMessageSourceService()).thenReturn(messageSourceService);
        when(Context.getLocationService()).thenReturn(locationService);


        when(administrationService.getGlobalProperty("sms.appointmentBookingSMSTemplate")).thenReturn("Appointment for {patientName}, {identifier} on {date} for {service} is booked at {facilityname}.");
        when(administrationService.getGlobalProperty("sms.teleconsultationLinkTemplate")).thenReturn("As you have chosen to book a teleconsultation-appointment click on the link {teleconsultationLink} on {date} to join the consultation.");
        when(administrationService.getGlobalProperty("sms.helpdeskTemplate")).thenReturn("For any queries call us on {helpdeskNumber}.");

        String globalPropertyNameHelpdesk = "clinic.helpDeskNumber";
        globalPropertyHelpdesk = new GlobalProperty(globalPropertyNameHelpdesk, "+919999999999");
        when(administrationService.getGlobalPropertyObject(globalPropertyNameHelpdesk)).thenReturn(globalPropertyHelpdesk);

        when(administrationService.getGlobalProperty("bahmni.sms.timezone")).thenReturn("ITC");
        when(administrationService.getGlobalProperty("bahmni.sms.dateformat")).thenReturn("yyyy-MM-dd HH:mm:ss");

        when(locationService.getLocationTagByName("Visit Location")).thenReturn(new LocationTag());

        when(appointmentVisitLocation.getFacilityName(anyString())).thenReturn("Hospital");

        Properties properties = new Properties();
        properties.setProperty("sms.uri", "http://example.com/sendSMS");
        AppointmentProperties.setProperties(properties);

        smsService.setAppointmentVisitLocation(appointmentVisitLocation);
    }

    @Test
    public void testGetAppointmentMessageWithProviders() throws Exception {
        Appointment appointment = new Appointment();
        appointment.setStartDateTime(new Date());
        appointment.setService(new AppointmentServiceDefinition());

        PatientIdentifier identifier = new PatientIdentifier();
        identifier.setIdentifier(PATIENT_IDENTIFIER);
        PatientIdentifierType patientIdentifierType = new PatientIdentifierType();
        patientIdentifierType.setName("Patient Identifier");
        identifier.setIdentifierType(patientIdentifierType);

        Patient patient = new Patient();
        patient.setUuid("patientUuid");
        PersonName name = new PersonName();
        name.setGivenName("John");
        name.setFamilyName("Doe");
        Set<PersonName> personNames = new HashSet<>();
        personNames.add(name);
        patient.setNames(personNames);
        patient.setIdentifiers(new HashSet<>(Arrays.asList(identifier)));

        appointment.setPatient(patient);
        appointment.setAppointmentKind(AppointmentKind.Virtual);

        String message = smsService.getAppointmentBookingMessage(appointment);

        assertEquals("Appointment for John Doe, GAN230901 on null for null is booked at xxxxx.As you have chosen to book a teleconsultation-appointment click on the link null on null to join the consultation.For any queries call us on +919999999999.", message);

        verify(appointmentVisitLocation, never()).getFacilityName(anyString());
    }
}