package org.openmrs.module.appointments.service.impl;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.openmrs.GlobalProperty;
import org.openmrs.Location;
import org.openmrs.LocationTag;
import org.openmrs.Patient;
import org.openmrs.PatientIdentifier;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.LocationService;
import org.openmrs.api.PatientService;
import org.openmrs.api.context.Context;
import org.openmrs.messagesource.MessageSourceService;
import org.openmrs.module.appointments.model.Appointment;
import org.openmrs.module.appointments.model.AppointmentKind;
import org.openmrs.module.appointments.model.AppointmentServiceDefinition;
import org.openmrs.util.PrivilegeConstants;

import java.util.Date;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class AppointmentArgumentsMapperImplTest {

    @Mock
    private PatientService patientService;

    @Mock
    private AdministrationService administrationService;

    @Mock
    private MessageSourceService messageSourceService;

    @Mock
    private LocationService locationService;

    private MockedStatic<Context> contextMock;

    private final AppointmentArgumentsMapperImpl mapper = new AppointmentArgumentsMapperImpl();

    @Before
    public void setUp() {
        contextMock = Mockito.mockStatic(Context.class);
        contextMock.when(Context::getPatientService).thenReturn(patientService);
        contextMock.when(Context::getAdministrationService).thenReturn(administrationService);
        contextMock.when(Context::getMessageSourceService).thenReturn(messageSourceService);
        contextMock.when(Context::getLocationService).thenReturn(locationService);
    }

    @After
    public void tearDown() {
        contextMock.close();
    }

    @Test
    public void shouldCreateArgumentsMapForAppointmentBookingUnderProxiedGlobalPropertyPrivilege() {
        Patient patient = Mockito.mock(Patient.class);
        PatientIdentifier identifier = Mockito.mock(PatientIdentifier.class);
        when(patient.getUuid()).thenReturn("patient-uuid");
        when(patient.getGivenName()).thenReturn("John");
        when(patient.getFamilyName()).thenReturn("Doe");
        when(patient.getGender()).thenReturn("M");
        when(patient.getPatientIdentifier()).thenReturn(identifier);
        when(identifier.getIdentifier()).thenReturn("GAN12345");
        when(patientService.getPatientByUuid("patient-uuid")).thenReturn(patient);

        AppointmentServiceDefinition service = new AppointmentServiceDefinition();
        service.setName("Consultation");

        LocationTag visitLocationTag = new LocationTag("Visit Location", "marks a location as a visit location");
        Location location = Mockito.mock(Location.class);
        when(location.getName()).thenReturn("Room 1");
        when(location.getTags()).thenReturn(java.util.Collections.singleton(visitLocationTag));
        when(location.getParentLocation()).thenReturn(null);

        Appointment appointment = new Appointment();
        appointment.setPatient(patient);
        appointment.setService(service);
        appointment.setLocation(location);
        appointment.setStartDateTime(new Date());
        appointment.setAppointmentKind(AppointmentKind.Scheduled);
        appointment.setTeleHealthVideoLink("https://meet.example.com/abc");

        when(administrationService.getGlobalProperty("sms.timezone")).thenReturn("UTC");
        when(administrationService.getGlobalProperty("sms.dateformat")).thenReturn("dd-MM-yyyy");
        when(messageSourceService.getMessage(eq("UTC"), isNull(), any())).thenReturn("UTC");
        when(messageSourceService.getMessage(eq("dd-MM-yyyy"), isNull(), any())).thenReturn("dd-MM-yyyy");
        GlobalProperty helpDeskProperty = new GlobalProperty("clinic.helpDeskNumber", "1800-123-456");
        when(administrationService.getGlobalPropertyObject("clinic.helpDeskNumber")).thenReturn(helpDeskProperty);

        Map<String, String> arguments = mapper.createArgumentsMapForAppointmentBooking(appointment);

        assertEquals("John Doe", arguments.get("patientname"));
        assertEquals("GAN12345", arguments.get("identifier"));
        assertEquals("M", arguments.get("gender"));
        assertEquals("Consultation", arguments.get("service"));
        assertEquals("Room 1", arguments.get("facilityname"));
        assertEquals("https://meet.example.com/abc", arguments.get("teleconsultationlink"));
        assertEquals("1800-123-456", arguments.get("helpdesknumber"));
        assertEquals("Scheduled", arguments.get("appointmentKind"));

        contextMock.verify(() -> Context.addProxyPrivilege(PrivilegeConstants.GET_GLOBAL_PROPERTIES), Mockito.atLeastOnce());
        contextMock.verify(() -> Context.removeProxyPrivilege(PrivilegeConstants.GET_GLOBAL_PROPERTIES), Mockito.atLeastOnce());
    }
}
