package org.openmrs.module.appointments.search.builder;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openmrs.Concept;
import org.openmrs.ConceptName;
import org.openmrs.Location;
import org.openmrs.Patient;
import org.openmrs.PatientIdentifier;
import org.openmrs.PatientIdentifierType;
import org.openmrs.PersonName;
import org.openmrs.module.appointments.model.Appointment;
import org.openmrs.module.appointments.model.AppointmentReason;
import org.openmrs.module.appointments.model.AppointmentServiceAttribute;
import org.openmrs.module.appointments.model.AppointmentServiceAttributeType;
import org.openmrs.module.appointments.model.AppointmentServiceDefinition;
import org.openmrs.module.appointments.model.AppointmentStatus;
import org.openmrs.module.appointments.search.AppointmentSearchConstants;

import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.Silent.class)
public class AppointmentResponseBuilderTest {

    private AppointmentResponseBuilder appointmentResponseBuilder;

    @Mock
    private Patient patient;

    @Mock
    private PersonName personName;

    @Mock
    private PatientIdentifier patientIdentifier;

    @Mock
    private PatientIdentifierType patientIdentifierType;

    @Mock
    private Location location;

    @Mock
    private AppointmentServiceDefinition service;

    @Mock
    private Concept concept;

    @Mock
    private ConceptName conceptName;

    @Before
    public void setUp() {
        appointmentResponseBuilder = new AppointmentResponseBuilder();
    }

    private Appointment createAppointment() {
        Appointment appointment = new Appointment();
        appointment.setUuid("appointment-uuid");
        appointment.setAppointmentNumber("APT-1");
        appointment.setStartDateTime(new Date(1700000000000L));
        appointment.setEndDateTime(new Date(1700003600000L));
        appointment.setStatus(AppointmentStatus.Scheduled);
        return appointment;
    }

    @Test
    public void shouldMapBasicAppointmentFields() {
        Appointment appointment = createAppointment();

        Map<String, Object> result = appointmentResponseBuilder.mapAppointment(appointment);

        assertEquals("appointment-uuid", result.get(AppointmentSearchConstants.UUID));
        assertEquals("APT-1", result.get(AppointmentSearchConstants.APPOINTMENT_NUMBER));
        assertEquals("Scheduled", result.get(AppointmentSearchConstants.STATUS));
        assertThat(result.get(AppointmentSearchConstants.START_DATE_TIME), notNullValue());
        assertThat(result.get(AppointmentSearchConstants.END_DATE_TIME), notNullValue());
    }

    @Test
    public void shouldReturnNullStatusWhenAppointmentStatusIsNull() {
        Appointment appointment = createAppointment();
        appointment.setStatus(null);

        Map<String, Object> result = appointmentResponseBuilder.mapAppointment(appointment);

        assertThat(result.get(AppointmentSearchConstants.STATUS), nullValue());
    }

    @Test
    public void shouldReturnNullForDateFieldsWhenDatesAreNull() {
        Appointment appointment = createAppointment();
        appointment.setStartDateTime(null);
        appointment.setEndDateTime(null);

        Map<String, Object> result = appointmentResponseBuilder.mapAppointment(appointment);

        assertThat(result.get(AppointmentSearchConstants.START_DATE_TIME), nullValue());
        assertThat(result.get(AppointmentSearchConstants.END_DATE_TIME), nullValue());
    }

    @Test
    public void shouldReturnEmptyMapForServiceWhenServiceIsNull() {
        Appointment appointment = createAppointment();
        appointment.setService(null);

        Map<String, Object> result = appointmentResponseBuilder.mapAppointment(appointment);

        Object serviceMap = result.get(AppointmentSearchConstants.SERVICE);
        assertThat(serviceMap, is(Collections.emptyMap()));
    }

    @Test
    public void shouldMapServiceFieldsWithoutAttributesWhenNoDestinationCountryAttributePresent() {
        Appointment appointment = createAppointment();
        when(service.getUuid()).thenReturn("service-uuid");
        when(service.getName()).thenReturn("Consultation");
        when(service.getDescription()).thenReturn("General consultation");
        when(service.getActiveAttributes()).thenReturn(Collections.emptySet());
        appointment.setService(service);

        Map<String, Object> result = appointmentResponseBuilder.mapAppointment(appointment);

        @SuppressWarnings("unchecked")
        Map<String, Object> serviceMap = (Map<String, Object>) result.get(AppointmentSearchConstants.SERVICE);
        assertEquals("service-uuid", serviceMap.get(AppointmentSearchConstants.UUID));
        assertEquals("Consultation", serviceMap.get(AppointmentSearchConstants.NAME));
        assertEquals("General consultation", serviceMap.get(AppointmentSearchConstants.DESCRIPTION));
        assertTrue(!serviceMap.containsKey(AppointmentSearchConstants.ATTRIBUTES));
    }

    @Test
    public void shouldMapServiceFieldsWhenActiveAttributesIsNull() {
        Appointment appointment = createAppointment();
        when(service.getUuid()).thenReturn("service-uuid");
        when(service.getName()).thenReturn("Consultation");
        when(service.getDescription()).thenReturn("General consultation");
        when(service.getActiveAttributes()).thenReturn(null);
        appointment.setService(service);

        Map<String, Object> result = appointmentResponseBuilder.mapAppointment(appointment);

        @SuppressWarnings("unchecked")
        Map<String, Object> serviceMap = (Map<String, Object>) result.get(AppointmentSearchConstants.SERVICE);
        assertTrue(!serviceMap.containsKey(AppointmentSearchConstants.ATTRIBUTES));
    }

    @Test
    public void shouldIncludeDestinationCountryAttributeWhenPresent() {
        Appointment appointment = createAppointment();
        AppointmentServiceAttribute attribute = mock(AppointmentServiceAttribute.class);
        AppointmentServiceAttributeType attributeType = mock(AppointmentServiceAttributeType.class);
        when(attributeType.getName()).thenReturn(AppointmentSearchConstants.DESTINATION_COUNTRY_ATTRIBUTE_TYPE_NAME);
        when(attributeType.getUuid()).thenReturn("attr-type-uuid");
        when(attribute.getAttributeType()).thenReturn(attributeType);
        when(attribute.getValueReference()).thenReturn("India");

        Set<AppointmentServiceAttribute> attributes = new HashSet<>();
        attributes.add(attribute);

        when(service.getUuid()).thenReturn("service-uuid");
        when(service.getName()).thenReturn("Consultation");
        when(service.getDescription()).thenReturn("General consultation");
        when(service.getActiveAttributes()).thenReturn(attributes);
        appointment.setService(service);

        Map<String, Object> result = appointmentResponseBuilder.mapAppointment(appointment);

        @SuppressWarnings("unchecked")
        Map<String, Object> serviceMap = (Map<String, Object>) result.get(AppointmentSearchConstants.SERVICE);
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> attributesList = (List<Map<String, Object>>) serviceMap.get(AppointmentSearchConstants.ATTRIBUTES);
        assertEquals(1, attributesList.size());
        Map<String, Object> attributeMap = attributesList.get(0);
        assertEquals("India", attributeMap.get(AppointmentSearchConstants.VALUE));

        @SuppressWarnings("unchecked")
        Map<String, Object> typeMap = (Map<String, Object>) attributeMap.get(AppointmentSearchConstants.TYPE);
        assertEquals("attr-type-uuid", typeMap.get(AppointmentSearchConstants.UUID));
    }

    @Test
    public void shouldNotIncludeAttributeWhenAttributeTypeNameDoesNotMatch() {
        Appointment appointment = createAppointment();
        AppointmentServiceAttribute attribute = mock(AppointmentServiceAttribute.class);
        AppointmentServiceAttributeType attributeType = mock(AppointmentServiceAttributeType.class);
        when(attributeType.getName()).thenReturn("SomeOtherAttribute");
        when(attribute.getAttributeType()).thenReturn(attributeType);

        Set<AppointmentServiceAttribute> attributes = new HashSet<>();
        attributes.add(attribute);

        when(service.getActiveAttributes()).thenReturn(attributes);
        appointment.setService(service);

        Map<String, Object> result = appointmentResponseBuilder.mapAppointment(appointment);

        @SuppressWarnings("unchecked")
        Map<String, Object> serviceMap = (Map<String, Object>) result.get(AppointmentSearchConstants.SERVICE);
        assertTrue(!serviceMap.containsKey(AppointmentSearchConstants.ATTRIBUTES));
    }

    @Test
    public void shouldReturnEmptyMapForLocationWhenLocationIsNull() {
        Appointment appointment = createAppointment();
        appointment.setLocation(null);

        Map<String, Object> result = appointmentResponseBuilder.mapAppointment(appointment);

        assertThat(result.get(AppointmentSearchConstants.LOCATION), is(Collections.emptyMap()));
    }

    @Test
    public void shouldMapLocationFields() {
        Appointment appointment = createAppointment();
        when(location.getUuid()).thenReturn("location-uuid");
        when(location.getName()).thenReturn("OPD");
        appointment.setLocation(location);

        Map<String, Object> result = appointmentResponseBuilder.mapAppointment(appointment);

        @SuppressWarnings("unchecked")
        Map<String, Object> locationMap = (Map<String, Object>) result.get(AppointmentSearchConstants.LOCATION);
        assertEquals("location-uuid", locationMap.get(AppointmentSearchConstants.UUID));
        assertEquals("OPD", locationMap.get(AppointmentSearchConstants.NAME));
    }

    @Test
    public void shouldReturnEmptyMapForPatientWhenPatientIsNull() {
        Appointment appointment = createAppointment();
        appointment.setPatient(null);

        Map<String, Object> result = appointmentResponseBuilder.mapAppointment(appointment);

        assertThat(result.get(AppointmentSearchConstants.PATIENT), is(Collections.emptyMap()));
    }

    @Test
    public void shouldMapPatientFieldsIncludingNameAndIdentifiers() {
        Appointment appointment = createAppointment();

        when(patient.getUuid()).thenReturn("patient-uuid");
        when(patient.getGender()).thenReturn("M");
        when(patient.getBirthdate()).thenReturn(new Date(900000000000L));
        when(patient.getVoided()).thenReturn(false);

        when(personName.getGivenName()).thenReturn("John");
        when(personName.getMiddleName()).thenReturn(null);
        when(personName.getFamilyName()).thenReturn("Doe");
        when(personName.getFamilyName2()).thenReturn(null);
        when(personName.getVoided()).thenReturn(false);
        when(patient.getPersonName()).thenReturn(personName);

        when(patientIdentifierType.getUuid()).thenReturn("id-type-uuid");
        when(patientIdentifierType.getName()).thenReturn("National ID");
        when(patientIdentifier.getIdentifierType()).thenReturn(patientIdentifierType);
        when(patientIdentifier.getIdentifier()).thenReturn("123456");
        when(patientIdentifier.getPreferred()).thenReturn(true);

        List<PatientIdentifier> identifiers = Collections.singletonList(patientIdentifier);
        when(patient.getActiveIdentifiers()).thenReturn(identifiers);

        appointment.setPatient(patient);

        Map<String, Object> result = appointmentResponseBuilder.mapAppointment(appointment);

        @SuppressWarnings("unchecked")
        Map<String, Object> patientMap = (Map<String, Object>) result.get(AppointmentSearchConstants.PATIENT);
        assertEquals("patient-uuid", patientMap.get(AppointmentSearchConstants.UUID));
        assertEquals("M", patientMap.get(AppointmentSearchConstants.GENDER));
        assertEquals(false, patientMap.get(AppointmentSearchConstants.VOIDED));

        @SuppressWarnings("unchecked")
        Map<String, Object> nameMap = (Map<String, Object>) patientMap.get(AppointmentSearchConstants.NAME);
        assertEquals("John", nameMap.get(AppointmentSearchConstants.GIVEN_NAME));
        assertEquals("Doe", nameMap.get(AppointmentSearchConstants.FAMILY_NAME));

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> identifiersList = (List<Map<String, Object>>) patientMap.get(AppointmentSearchConstants.IDENTIFIERS);
        assertEquals(1, identifiersList.size());
        Map<String, Object> identifierMap = identifiersList.get(0);
        assertEquals("123456", identifierMap.get(AppointmentSearchConstants.IDENTIFIER));
        assertEquals(true, identifierMap.get(AppointmentSearchConstants.PREFERRED));

        @SuppressWarnings("unchecked")
        Map<String, Object> idTypeMap = (Map<String, Object>) identifierMap.get(AppointmentSearchConstants.TYPE);
        assertEquals("id-type-uuid", idTypeMap.get(AppointmentSearchConstants.UUID));
        assertEquals("National ID", idTypeMap.get(AppointmentSearchConstants.NAME));
    }

    @Test
    public void shouldReturnEmptyMapForNameWhenPersonNameIsNull() {
        Appointment appointment = createAppointment();
        when(patient.getPersonName()).thenReturn(null);
        when(patient.getActiveIdentifiers()).thenReturn(Collections.emptyList());
        appointment.setPatient(patient);

        Map<String, Object> result = appointmentResponseBuilder.mapAppointment(appointment);

        @SuppressWarnings("unchecked")
        Map<String, Object> patientMap = (Map<String, Object>) result.get(AppointmentSearchConstants.PATIENT);
        assertThat(patientMap.get(AppointmentSearchConstants.NAME), is(Collections.emptyMap()));
    }

    @Test
    public void shouldReturnEmptyListForIdentifiersWhenActiveIdentifiersIsNullOrEmpty() {
        Appointment appointment = createAppointment();
        when(patient.getPersonName()).thenReturn(null);
        when(patient.getActiveIdentifiers()).thenReturn(null);
        appointment.setPatient(patient);

        Map<String, Object> result = appointmentResponseBuilder.mapAppointment(appointment);

        @SuppressWarnings("unchecked")
        Map<String, Object> patientMap = (Map<String, Object>) result.get(AppointmentSearchConstants.PATIENT);
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> identifiersList = (List<Map<String, Object>>) patientMap.get(AppointmentSearchConstants.IDENTIFIERS);
        assertTrue(identifiersList.isEmpty());
    }

    @Test
    public void shouldReturnEmptyMapForIdentifierTypeWhenIdentifierTypeIsNull() {
        Appointment appointment = createAppointment();
        when(patient.getPersonName()).thenReturn(null);
        when(patientIdentifier.getIdentifierType()).thenReturn(null);
        when(patientIdentifier.getIdentifier()).thenReturn("123456");
        when(patientIdentifier.getPreferred()).thenReturn(false);
        when(patient.getActiveIdentifiers()).thenReturn(Collections.singletonList(patientIdentifier));
        appointment.setPatient(patient);

        Map<String, Object> result = appointmentResponseBuilder.mapAppointment(appointment);

        @SuppressWarnings("unchecked")
        Map<String, Object> patientMap = (Map<String, Object>) result.get(AppointmentSearchConstants.PATIENT);
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> identifiersList = (List<Map<String, Object>>) patientMap.get(AppointmentSearchConstants.IDENTIFIERS);
        Map<String, Object> identifierMap = identifiersList.get(0);
        assertThat(identifierMap.get(AppointmentSearchConstants.TYPE), is(Collections.emptyMap()));
    }

    @Test
    public void shouldReturnEmptyListForReasonsWhenReasonsIsNullOrEmpty() {
        Appointment appointment = createAppointment();
        appointment.setReasons(null);

        Map<String, Object> result = appointmentResponseBuilder.mapAppointment(appointment);

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> reasons = (List<Map<String, Object>>) result.get(AppointmentSearchConstants.REASONS);
        assertTrue(reasons.isEmpty());
    }

    @Test
    public void shouldSkipVoidedReasonsAndReasonsWithoutConcept() {
        Appointment appointment = createAppointment();

        AppointmentReason voidedReason = mock(AppointmentReason.class);
        when(voidedReason.getVoided()).thenReturn(true);

        AppointmentReason reasonWithoutConcept = mock(AppointmentReason.class);
        when(reasonWithoutConcept.getVoided()).thenReturn(false);
        when(reasonWithoutConcept.getConcept()).thenReturn(null);

        AppointmentReason validReason = mock(AppointmentReason.class);
        when(validReason.getVoided()).thenReturn(false);
        when(validReason.getConcept()).thenReturn(concept);
        when(concept.getUuid()).thenReturn("concept-uuid");
        when(concept.getName()).thenReturn(conceptName);
        when(conceptName.getName()).thenReturn("Fever");

        Set<AppointmentReason> reasons = new HashSet<>();
        reasons.add(voidedReason);
        reasons.add(reasonWithoutConcept);
        reasons.add(validReason);
        appointment.setReasons(reasons);

        Map<String, Object> result = appointmentResponseBuilder.mapAppointment(appointment);

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> reasonsList = (List<Map<String, Object>>) result.get(AppointmentSearchConstants.REASONS);
        assertEquals(1, reasonsList.size());
        Map<String, Object> reasonMap = reasonsList.get(0);
        assertEquals("concept-uuid", reasonMap.get(AppointmentSearchConstants.CONCEPT_UUID));
        assertEquals("Fever", reasonMap.get(AppointmentSearchConstants.NAME));
    }
}
