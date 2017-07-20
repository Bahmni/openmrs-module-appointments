package org.openmrs.module.appointments.web.mapper;

import org.apache.commons.lang.StringUtils;
import org.openmrs.Location;
import org.openmrs.Patient;
import org.openmrs.api.LocationService;
import org.openmrs.api.PatientService;
import org.openmrs.api.ProviderService;
import org.openmrs.module.appointments.model.Appointment;
import org.openmrs.module.appointments.model.AppointmentService;
import org.openmrs.module.appointments.model.ServiceWeeklyAvailability;
import org.openmrs.module.appointments.model.Speciality;
import org.openmrs.module.appointments.service.SpecialityService;
import org.openmrs.module.appointments.web.contract.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.sql.Time;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class AppointmentMapper {
    @Autowired
    LocationService locationService;

    @Autowired
    ProviderService providerService;

    @Autowired
    PatientService patientService;


    public List<AppointmentDefaultResponse> constructResponse(List<Appointment> appointments) {
        return appointments.stream().map(as -> this.mapToDefaultResponse(as, new AppointmentDefaultResponse())).collect(Collectors.toList());
    }

    private AppointmentDefaultResponse mapToDefaultResponse(Appointment a, AppointmentDefaultResponse response) {
        response.setUuid(a.getUuid());
        response.setStartDateTime(convertTimeToString(a.getStartDateTime()));
        response.setEndDateTime(convertTimeToString(a.getStartDateTime()));
        response.setAppointmentNumber(a.getAppointmentNumber());
        response.setAppointmentsKind(a.getAppointmentsKind());
        response.setComments(a.getComments());
        response.setPatient(a.getPatient());
        if(a.getLocation()!=null){
            response.setLocationUuid(a.getLocation().getUuid());
        }
        if(a.getProvider() != null){
            response.setProviderUuid(a.getProvider().getUuid());
        }
        response.setStatus(a.getStatus());
        response.setUuid(a.getUuid());

        return response;
    }

    private String convertTimeToString(Date time) {
       return time != null ? time.toString() : new String();
    }

    public Appointment getAppointmentFromPayload(AppointmentPayload appointmentPayload) {
        Appointment appointment = new Appointment();
        if (!StringUtils.isBlank(appointmentPayload.getUuid())) {
            appointment.setUuid(appointmentPayload.getUuid());
        }
        appointment.setAppointmentsKind(appointmentPayload.getAppointmentNumber());
        appointment.setComments(appointmentPayload.getComments());
        appointment.setEndDateTime(appointmentPayload.getEndDateTime());
        appointment.setStartDateTime(appointmentPayload.getStartDateTime());
        appointment.setStatus(appointmentPayload.getStatus());

        appointment.setPatient(patientService.getPatientByUuid(appointmentPayload.getPatientUuid()));
        appointment.setLocation(locationService.getLocationByUuid(appointmentPayload.getLocationUuid()));
        appointment.setProvider(providerService.getProviderByUuid(appointmentPayload.getProviderUuid()));

        return appointment;
    }
}
