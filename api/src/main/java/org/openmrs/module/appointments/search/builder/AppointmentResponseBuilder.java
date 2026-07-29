package org.openmrs.module.appointments.search.builder;

import org.openmrs.Location;
import org.openmrs.Patient;
import org.openmrs.PatientIdentifier;
import org.openmrs.PersonAttribute;
import org.openmrs.PersonName;
import org.openmrs.module.appointments.model.Appointment;
import org.openmrs.module.appointments.model.AppointmentServiceDefinition;

import java.text.SimpleDateFormat;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class AppointmentResponseBuilder {

    private static final DateTimeFormatter ISO_DATETIME =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'").withZone(ZoneOffset.UTC);
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");

    public Map<String, Object> mapAppointment(Appointment appointment) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("uuid", appointment.getUuid());
        map.put("appointmentNumber", appointment.getAppointmentNumber());
        map.put("startDateTime", formatIsoDateTime(appointment.getStartDateTime()));
        map.put("endDateTime", formatIsoDateTime(appointment.getEndDateTime()));
        map.put("status", appointment.getStatus() != null ? appointment.getStatus().name() : null);
        map.put("service", buildServiceMap(appointment.getService()));
        map.put("location", buildLocationMap(appointment.getLocation()));
        map.put("patient", buildPatientMap(appointment.getPatient()));
        return map;
    }

    private Map<String, Object> buildServiceMap(AppointmentServiceDefinition service) {
        if (service == null) return null;
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("uuid", service.getUuid());
        map.put("name", service.getName());
        map.put("description", service.getDescription());
        return map;
    }

    private Map<String, Object> buildLocationMap(Location location) {
        if (location == null) return null;
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("uuid", location.getUuid());
        map.put("name", location.getName());
        return map;
    }

    private Map<String, Object> buildPatientMap(Patient patient) {
        if (patient == null) return null;
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("uuid", patient.getUuid());
        map.put("person", buildPersonMap(patient));
        map.put("identifiers", buildIdentifiersList(patient));
        map.put("attributes", buildAttributesList(patient));
        return map;
    }

    private Map<String, Object> buildPersonMap(Patient patient) {
        Map<String, Object> map = new LinkedHashMap<>();
        PersonName name = patient.getPersonName();
        if (name != null) {
            map.put("givenName", name.getGivenName());
            map.put("familyName", name.getFamilyName());
            map.put("fullName", name.getFullName());
        }
        map.put("gender", patient.getGender());
        map.put("birthdate", formatDate(patient.getBirthdate()));
        return map;
    }

    private List<Map<String, Object>> buildIdentifiersList(Patient patient) {
        List<Map<String, Object>> list = new ArrayList<>();
        for (PatientIdentifier identifier : patient.getActiveIdentifiers()) {
            Map<String, Object> idMap = new LinkedHashMap<>();
            if (identifier.getIdentifierType() != null) {
                Map<String, Object> typeMap = new LinkedHashMap<>();
                typeMap.put("uuid", identifier.getIdentifierType().getUuid());
                typeMap.put("name", identifier.getIdentifierType().getName());
                idMap.put("type", typeMap);
            }
            idMap.put("identifier", identifier.getIdentifier());
            idMap.put("preferred", Boolean.TRUE.equals(identifier.getPreferred()));
            list.add(idMap);
        }
        return list;
    }

    private List<Map<String, Object>> buildAttributesList(Patient patient) {
        List<Map<String, Object>> list = new ArrayList<>();
        for (PersonAttribute attribute : patient.getActiveAttributes()) {
            Map<String, Object> attrMap = new LinkedHashMap<>();
            if (attribute.getAttributeType() != null) {
                Map<String, Object> typeMap = new LinkedHashMap<>();
                typeMap.put("uuid", attribute.getAttributeType().getUuid());
                typeMap.put("name", attribute.getAttributeType().getName());
                attrMap.put("type", typeMap);
            }
            attrMap.put("value", attribute.getValue());
            list.add(attrMap);
        }
        return list;
    }

    private String formatDate(Date date) {
        if (date == null) return null;
        return DATE_FORMAT.format(date);
    }

    private String formatIsoDateTime(Date date) {
        if (date == null) return null;
        return ISO_DATETIME.format(date.toInstant());
    }
}
