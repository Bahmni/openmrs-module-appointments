package org.openmrs.module.appointments.web.mapper;

import java.util.Set;
import org.apache.commons.lang.StringUtils;
import org.openmrs.Location;
import org.openmrs.Patient;
import org.openmrs.Provider;
import org.openmrs.api.LocationService;
import org.openmrs.api.PatientService;
import org.openmrs.api.ProviderService;
import org.openmrs.module.appointments.model.Appointment;
import org.openmrs.module.appointments.model.AppointmentKind;
import org.openmrs.module.appointments.model.AppointmentService;
import org.openmrs.module.appointments.model.AppointmentServiceType;
import org.openmrs.module.appointments.model.AppointmentStatus;
import org.openmrs.module.appointments.service.AppointmentServiceService;
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

    public List<AppointmentDefaultResponse> constructResponse(List<Appointment> appointments) {
        return appointments.stream().map(as -> this.mapToDefaultResponse(as, new AppointmentDefaultResponse())).collect(Collectors.toList());
    }

    public Appointment getAppointmentFromPayload(AppointmentPayload appointmentPayload) {
        Appointment appointment = new Appointment();
        if (!StringUtils.isBlank(appointmentPayload.getUuid())) {
            appointment.setUuid(appointmentPayload.getUuid());
        }
        appointment.setAppointmentNumber(appointmentPayload.getAppointmentNumber());
        appointment.setPatient(patientService.getPatientByUuid(appointmentPayload.getPatientUuid()));
        AppointmentService appointmentService = appointmentServiceService.getAppointmentServiceByUuid(appointmentPayload.getServiceUuid());
        if(appointmentPayload.getServiceTypeUuid() != null) {
            AppointmentServiceType appointmentServiceType = getServiceTypeByUuid(appointmentService.getServiceTypes(), appointmentPayload.getServiceTypeUuid());
            appointment.setServiceType(appointmentServiceType);
        }
        appointment.setService(appointmentService);
        appointment.setProvider(providerService.getProviderByUuid(appointmentPayload.getProviderUuid()));
        appointment.setLocation(locationService.getLocationByUuid(appointmentPayload.getLocationUuid()));
        appointment.setStartDateTime(appointmentPayload.getStartDateTime());
        appointment.setEndDateTime(appointmentPayload.getEndDateTime());
        appointment.setAppointmentKind(AppointmentKind.valueOf(appointmentPayload.getAppointmentKind()));
        appointment.setStatus(AppointmentStatus.valueOf(appointmentPayload.getStatus()));
        appointment.setComments(appointmentPayload.getComments());

        return appointment;
    }
    
    private AppointmentServiceType getServiceTypeByUuid(Set<AppointmentServiceType> serviceTypes, String serviceTypeUuid) {
        return serviceTypes.stream()
                .filter(avb -> avb.getUuid().equals(serviceTypeUuid)).findAny().get();
    }

    public Appointment mapQueryToAppointment(AppointmentQuery searchQuery) {
        Appointment appointment = new Appointment();
        appointment.setService(
                appointmentServiceService.getAppointmentServiceByUuid(searchQuery.getServiceUuid()));
        appointment.setPatient(patientService.getPatientByUuid(searchQuery.getPatientUuid()));
        appointment.setProvider(providerService.getProviderByUuid(searchQuery.getProviderUuid()));
        appointment.setLocation(locationService.getLocationByUuid(searchQuery.getLocationUuid()));
        if (searchQuery.getStatus() != null) {
            appointment.setStatus(AppointmentStatus.valueOf(searchQuery.getStatus()));
        }
        return appointment;
    }

    private AppointmentDefaultResponse mapToDefaultResponse(Appointment a, AppointmentDefaultResponse response) {
        response.setUuid(a.getUuid());
        response.setAppointmentNumber(a.getAppointmentNumber());
        response.setPatient(createPatientMap(a.getPatient()));
        response.setService(createServiceMap(a.getService()));
        response.setServiceType(createServiceTypeMap(a.getServiceType()));
        response.setProvider(createProviderMap(a.getProvider()));
        response.setLocation(createLocationMap(a.getLocation()));
        response.setStartDateTime(convertDateToString(a.getStartDateTime()));
        response.setEndDateTime(convertDateToString(a.getEndDateTime()));
        response.setAppointmentKind(a.getAppointmentKind().name());
        response.setStatus(a.getStatus().name());
        response.setComments(a.getComments());

        return response;
    }
    
    private Map createServiceTypeMap(AppointmentServiceType s) {
        Map serviceTypeMap = new HashMap();
        if (s != null) {
            serviceTypeMap.put("name", s.getName());
            serviceTypeMap.put("uuid", s.getUuid());
        }
        return serviceTypeMap;
    }
    
    private Map createServiceMap(AppointmentService s) {
        Map serviceMap = new HashMap();
        if (s != null) {
            serviceMap.put("name", s.getName());
            serviceMap.put("uuid", s.getUuid());
        }
        return serviceMap;
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
        map.put("identifier", p.getPatientIdentifier().getIdentifier());
        return map;
    }

    private String convertDateToString(Date dateTime) {
        return dateTime != null ? dateTime.toString() : new String();
    }

}
