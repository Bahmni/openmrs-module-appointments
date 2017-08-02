package org.openmrs.module.appointments.web.mapper;

import org.apache.commons.lang.StringUtils;
import org.openmrs.Location;
import org.openmrs.Patient;
import org.openmrs.Provider;
import org.openmrs.api.LocationService;
import org.openmrs.api.PatientService;
import org.openmrs.api.ProviderService;
import org.openmrs.module.appointments.model.Appointment;
import org.openmrs.module.appointments.model.AppointmentKind;
import org.openmrs.module.appointments.model.AppointmentStatus;
import org.openmrs.module.appointments.service.AppointmentServiceService;
import org.openmrs.module.appointments.service.AppointmentsService;
import org.openmrs.module.appointments.web.contract.AppointmentDefaultResponse;
import org.openmrs.module.appointments.web.contract.AppointmentPayload;
import org.openmrs.module.appointments.web.contract.AppointmentQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class AppointmentMapper {
    @Autowired
    LocationService locationService;

    @Autowired
    ProviderService providerService;

    @Autowired
    PatientService patientService;

    @Autowired
    AppointmentServiceService appointmentServiceService;

    @Autowired
    AppointmentsService appointmentsService;

    public List<AppointmentDefaultResponse> constructResponse(List<Appointment> appointments) {
        return appointments.stream().map(as -> this.mapToDefaultResponse(as, new AppointmentDefaultResponse())).collect(Collectors.toList());
    }

    public Appointment getAppointmentFromPayload(AppointmentPayload appointmentPayload) {
        Appointment appointment = new Appointment();
        if (!StringUtils.isBlank(appointmentPayload.getUuid())) {
            appointment.setUuid(appointmentPayload.getUuid());
        }
        appointment.setAppointmentsKind(AppointmentKind.valueOf(appointmentPayload.getAppointmentsKind()));
        appointment.setComments(appointmentPayload.getComments());
        appointment.setEndDateTime(appointmentPayload.getEndDateTime());
        appointment.setStartDateTime(appointmentPayload.getStartDateTime());
        appointment.setStatus(AppointmentStatus.valueOf(appointmentPayload.getStatus()));
        appointment.setAppointmentNumber(appointmentPayload.getAppointmentNumber());

        appointment.setService(
                appointmentServiceService.getAppointmentServiceByUuid(appointmentPayload.getServiceUuid()));
        //appointment.setServiceType((AppointmentServiceType) appointment.getService().getServiceTypes().toArray()[0]);
        appointment.setPatient(patientService.getPatientByUuid(appointmentPayload.getPatientUuid()));
        appointment.setLocation(locationService.getLocationByUuid(appointmentPayload.getLocationUuid()));
        appointment.setProvider(providerService.getProviderByUuid(appointmentPayload.getProviderUuid()));

        return appointment;
    }

    public Appointment mapQueryToAppointment(AppointmentQuery searchQuery) {
        Appointment appointment = new Appointment();
        appointment.setService(
                appointmentServiceService.getAppointmentServiceByUuid(searchQuery.getServiceUuid()));
        appointment.setPatient(patientService.getPatientByUuid(searchQuery.getPatientUuid()));
        appointment.setLocation(locationService.getLocationByUuid(searchQuery.getLocationUuid()));
        appointment.setProvider(providerService.getProviderByUuid(searchQuery.getProviderUuid()));
        if (searchQuery.getStatus() != null) {
            appointment.setStatus(AppointmentStatus.valueOf(searchQuery.getStatus()));
        }
        return appointment;
    }

    private AppointmentDefaultResponse mapToDefaultResponse(Appointment a, AppointmentDefaultResponse response) {
        response.setUuid(a.getUuid());
        response.setStartDateTime(convertTimeToString(a.getStartDateTime()));
        response.setEndDateTime(convertTimeToString(a.getEndDateTime()));
        response.setAppointmentNumber(a.getAppointmentNumber());
        response.setAppointmentsKind(a.getAppointmentsKind().name());
        response.setComments(a.getComments());
        response.setPatient(createPatientMap(a.getPatient()));
        response.setLocation(createLocationMap(a.getLocation()));
        response.setProvider(createProviderMap(a.getProvider()));
        response.setStatus(a.getStatus().name());
        response.setUuid(a.getUuid());

        return response;
    }

    private Map createProviderMap(Provider p) {
        Map map = new HashMap();
        if (p != null) {
            map.put("name", p.getName());
            map.put("uuid", p.getUuid());
        }
        return map;
    }

    private Map createLocationMap(Location l) {
        Map locationMap = new HashMap();
        if (l != null) {
            locationMap.put("name", l.getName());
            locationMap.put("uuid", l.getUuid());
        }
        return locationMap;
    }

    private Map createPatientMap(Patient p) {
        Map map = new HashMap();
        map.put("name", p.getPersonName().getFullName());
        map.put("uuid", p.getUuid());
        return map;
    }

    private String convertTimeToString(Date time) {
        return time != null ? time.toString() : new String();
    }

}
