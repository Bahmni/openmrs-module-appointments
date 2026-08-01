package org.openmrs.module.appointments.search.builder;

import org.openmrs.Concept;
import org.openmrs.Location;
import org.openmrs.Patient;
import org.openmrs.PatientIdentifier;
import org.openmrs.PatientIdentifierType;
import org.openmrs.PersonName;
import org.openmrs.api.context.Context;
import org.openmrs.module.appointments.model.Appointment;
import org.openmrs.module.appointments.model.AppointmentReason;
import org.openmrs.module.appointments.model.AppointmentServiceAttribute;
import org.openmrs.module.appointments.model.AppointmentServiceAttributeType;
import org.openmrs.module.appointments.model.AppointmentServiceDefinition;
import org.openmrs.module.appointments.search.AppointmentSearchConstants;


import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;


public class AppointmentResponseBuilder {

    private static final DateTimeFormatter ISO_DATETIME =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'").withZone(ZoneOffset.UTC);

    public Map<String, Object> mapAppointment(Appointment appointment) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put(AppointmentSearchConstants.UUID, appointment.getUuid());
        map.put(AppointmentSearchConstants.APPOINTMENT_NUMBER, appointment.getAppointmentNumber());
        map.put(AppointmentSearchConstants.START_DATE_TIME, formatIsoDateTime(appointment.getStartDateTime()));
        map.put(AppointmentSearchConstants.END_DATE_TIME, formatIsoDateTime(appointment.getEndDateTime()));
        map.put(AppointmentSearchConstants.STATUS, appointment.getStatus() != null ? appointment.getStatus().name() : null);
        map.put(AppointmentSearchConstants.REASONS, buildReasonsList(appointment.getReasons()));
        map.put(AppointmentSearchConstants.SERVICE, buildServiceMap(appointment.getService()));
        map.put(AppointmentSearchConstants.LOCATION, buildLocationMap(appointment.getLocation()));
        map.put(AppointmentSearchConstants.PATIENT, buildPatientMap(appointment.getPatient()));
        return map;
    }

    private List<Map<String, Object>> buildReasonsList(Set<AppointmentReason> reasons) {
        List<Map<String, Object>> reasonList = new ArrayList<>();
        if (reasons == null || reasons.isEmpty()) {
            return reasonList;
        }
        for (AppointmentReason appointmentReason : reasons) {
            if (Boolean.TRUE.equals(appointmentReason.getVoided()) || appointmentReason.getConcept() == null) {
                continue;
            }
            Concept concept = appointmentReason.getConcept();
            Map<String, Object> reasonMap = new LinkedHashMap<>();
            reasonMap.put(AppointmentSearchConstants.CONCEPT_UUID, concept.getUuid());
            reasonMap.put(AppointmentSearchConstants.NAME, resolveConceptName(concept));
            reasonList.add(reasonMap);
        }
        return reasonList;
    }

    private String resolveConceptName(Concept concept) {
        if (concept.getName() != null) {
            return concept.getName().getName();
        } else if (concept.getFullySpecifiedName(Context.getLocale()) != null) {
            return concept.getFullySpecifiedName(Context.getLocale()).getName();
        }
        return null;
    }


    private Map<String, Object> buildServiceMap(AppointmentServiceDefinition service) {
        if (service == null) return null;
        Map<String, Object> map = new LinkedHashMap<>();
        map.put(AppointmentSearchConstants.UUID, service.getUuid());
        map.put(AppointmentSearchConstants.NAME, service.getName());
        map.put(AppointmentSearchConstants.DESCRIPTION, service.getDescription());
        Map<String, Object> destinationCountryAttribute = buildDestinationCountryAttribute(service);
        if (destinationCountryAttribute != null) {
            map.put(AppointmentSearchConstants.ATTRIBUTES, Collections.singletonList(destinationCountryAttribute));
        }
        return map;
    }

    private Map<String, Object> buildDestinationCountryAttribute(AppointmentServiceDefinition service) {
        if (service.getActiveAttributes() == null) return null;
        for (AppointmentServiceAttribute attribute : service.getActiveAttributes()) {
            if (attribute.getAttributeType() != null
                    && AppointmentSearchConstants.DESTINATION_COUNTRY_ATTRIBUTE_TYPE_NAME
                            .equals(attribute.getAttributeType().getName())) {
                Map<String, Object> attributeMap = new LinkedHashMap<>();
                attributeMap.put(AppointmentSearchConstants.TYPE, buildAttributeTypeMap(attribute.getAttributeType()));
                attributeMap.put(AppointmentSearchConstants.VALUE, attribute.getValueReference());
                return attributeMap;
            }
        }
        return null;
    }

    private Map<String, Object> buildAttributeTypeMap(AppointmentServiceAttributeType attributeType) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put(AppointmentSearchConstants.UUID, attributeType.getUuid());
        map.put(AppointmentSearchConstants.NAME, attributeType.getName());
        return map;
    }


    private Map<String, Object> buildLocationMap(Location location) {
        if (location == null) return null;
        Map<String, Object> map = new LinkedHashMap<>();
        map.put(AppointmentSearchConstants.UUID, location.getUuid());
        map.put(AppointmentSearchConstants.NAME, location.getName());
        return map;
    }

    private Map<String, Object> buildPatientMap(Patient patient) {
        if (patient == null) return null;
        Map<String, Object> map = new LinkedHashMap<>();
        map.put(AppointmentSearchConstants.UUID, patient.getUuid());
        map.put(AppointmentSearchConstants.GENDER, patient.getGender());
        map.put(AppointmentSearchConstants.BIRTHDATE, formatIsoDateTime(patient.getBirthdate()));
        map.put(AppointmentSearchConstants.VOIDED, Boolean.TRUE.equals(patient.getVoided()));
        map.put(AppointmentSearchConstants.NAME, buildNameMap(patient.getPersonName()));
        map.put(AppointmentSearchConstants.IDENTIFIERS, buildIdentifiersList(patient));
        return map;
    }

    private List<Map<String, Object>> buildIdentifiersList(Patient patient) {
        List<Map<String, Object>> identifiersList = new ArrayList<>();
        List<PatientIdentifier> identifiers = patient.getActiveIdentifiers();
        if (identifiers == null || identifiers.isEmpty()) {
            return identifiersList;
        }
        for (PatientIdentifier patientIdentifier : identifiers) {
            Map<String, Object> identifierMap = new LinkedHashMap<>();
            identifierMap.put(AppointmentSearchConstants.TYPE, buildIdentifierTypeMap(patientIdentifier.getIdentifierType()));
            identifierMap.put(AppointmentSearchConstants.IDENTIFIER, patientIdentifier.getIdentifier());
            identifierMap.put(AppointmentSearchConstants.PREFERRED, Boolean.TRUE.equals(patientIdentifier.getPreferred()));
            identifiersList.add(identifierMap);
        }
        return identifiersList;
    }

    private Map<String, Object> buildIdentifierTypeMap(PatientIdentifierType identifierType) {
        if (identifierType == null) return null;
        Map<String, Object> map = new LinkedHashMap<>();
        map.put(AppointmentSearchConstants.UUID, identifierType.getUuid());
        map.put(AppointmentSearchConstants.NAME, identifierType.getName());
        return map;
    }

    private Map<String, Object> buildNameMap(PersonName name) {
        if (name == null) return null;
        Map<String, Object> map = new LinkedHashMap<>();
        map.put(AppointmentSearchConstants.GIVEN_NAME, name.getGivenName());
        map.put(AppointmentSearchConstants.MIDDLE_NAME, name.getMiddleName());
        map.put(AppointmentSearchConstants.FAMILY_NAME, name.getFamilyName());
        map.put(AppointmentSearchConstants.FAMILY_NAME_2, name.getFamilyName2());
        map.put(AppointmentSearchConstants.VOIDED, Boolean.TRUE.equals(name.getVoided()));
        return map;
    }

    private String formatIsoDateTime(Date date) {
        if (date == null) return null;
        return ISO_DATETIME.format(date.toInstant());
    }
}
