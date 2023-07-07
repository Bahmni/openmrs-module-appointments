package org.openmrs.module.appointments.service.impl;


import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.openmrs.GlobalProperty;
import org.openmrs.LocationTag;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.LocationService;
import org.openmrs.api.context.Context;
import org.openmrs.messagesource.MessageSourceService;
import org.openmrs.module.appointments.connection.OpenmrsLogin;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.Arrays;
import java.util.Date;
import java.util.Locale;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.powermock.api.mockito.PowerMockito.when;

@PowerMockIgnore("javax.management.*")
@PrepareForTest(Context.class)
@RunWith(PowerMockRunner.class)
public class SMSServiceTest {

    @Mock
    private OpenmrsLogin openmrsLogin;
    @Mock
    private AdministrationService administrationService;

    @Mock
    private MessageSourceService messageSourceService;
    @Mock
    private AppointmentVisitLocation appointmentVisitLocation;

    @Mock
    private LocationService locationService;
    private SMSService smsService;

    private GlobalProperty globalProperty;

    @Before
    public void setup() {
        PowerMockito.mockStatic(Context.class);
        when(Context.getAdministrationService()).thenReturn(administrationService);
        when(Context.getMessageSourceService()).thenReturn(messageSourceService);
        when(Context.getLocationService()).thenReturn(locationService);
        LocationTag visitLocationTag = new LocationTag();
        visitLocationTag.setName("Visit Location");
        when(locationService.getLocationTagByName("Visit Location")).thenReturn(visitLocationTag);
        smsService =new SMSService();
    }

    @Test
    public void testGetAppointmentMessageWithProviders() throws Exception {
        String smsTemplate = "Reminder: Appointment for {0} ({1}) on {2} with providers: {3} at {5}. Contact: {6}.";
        String smsDateFormat = "yyyy-MM-dd HH:mm:ss";
        String smsTimeZone = "Asia/Kolkata";

        when(administrationService.getGlobalProperty("sms.appointmentReminderSMSTemplate")).thenReturn(smsTemplate);
        when(administrationService.getGlobalProperty("bahmni.sms.timezone")).thenReturn("Asia/Kolkata");
        when(administrationService.getGlobalProperty("bahmni.sms.dateformat")).thenReturn(smsDateFormat);
        String globalPropertyName = "clinic.helpDeskNumber";
        globalProperty = new GlobalProperty(globalPropertyName, "+919999999999");
        when(administrationService.getGlobalPropertyObject(globalPropertyName)).thenReturn(globalProperty);
        when(messageSourceService.getMessage(smsTemplate, new Object[]{"John Doe", "12345", "2023-07-06 10:00:00", "Dr. Smith, Dr. Johnson", "Cardiology", "Hospital", "1234567890"}, Locale.ENGLISH))
                .thenReturn("Reminder: Appointment for John Doe (12345) on 2023-07-06 10:00:00 with providers: Dr. Smith, Dr. Johnson at Hospital. Contact: 1234567890.");

        Date appointmentDate = new Date();
        String message = smsService.getAppointmentMessage("John", "Doe", "12345", appointmentDate, "Cardiology",
                Arrays.asList("Dr. Smith", "Dr. Johnson"), null);

        assertEquals("Reminder: Appointment for John Doe (12345) on null with providers: Dr. Smith,Dr. Johnson at xxxxx. Contact: +919999999999.", message);

        verify(appointmentVisitLocation, never()).getFacilityName(anyString());
    }

}