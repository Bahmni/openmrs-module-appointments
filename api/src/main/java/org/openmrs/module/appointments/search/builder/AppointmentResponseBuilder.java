package org.openmrs.module.appointments.search.builder;

import org.openmrs.Concept;
import org.openmrs.Location;
import org.openmrs.Patient;
import org.openmrs.PersonName;
import org.openmrs.api.context.Context;
import org.openmrs.module.appointments.model.Appointment;
import org.openmrs.module.appointments.model.AppointmentReason;
import org.openmrs.module.appointments.model.AppointmentServiceDefinition;
import org.openmrs.module.appointments.search.AppointmentSearchConstants;

import java.text.SimpleDateFormat;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;


public class AppointmentResponseBuilder {

    private static final DateTimeFormatter ISO_DATETIME =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'").withZone(ZoneOffset.UTC);
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
    private static final SimpleDateFormat DATE_TIME_WITH_OFFSET_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");

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
        map.put(AppointmentSearchConstants.BIRTHDATE, formatDateTimeWithOffset(patient.getBirthdate()));
        map.put(AppointmentSearchConstants.VOIDED, Boolean.TRUE.equals(patient.getVoided()));
        map.put(AppointmentSearchConstants.NAME, buildNameMap(patient.getPersonName()));
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

    private String formatDate(Date date) {
        if (date == null) return null;
        return DATE_FORMAT.format(date);
    }

    private String formatDateTimeWithOffset(Date date) {
        if (date == null) return null;
        return DATE_TIME_WITH_OFFSET_FORMAT.format(date);
    }

    private String formatIsoDateTime(Date date) {
        if (date == null) return null;
        return ISO_DATETIME.format(date.toInstant());
    }
}
