package org.openmrs.module.appointments.web.mapper;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.util.JSONPObject;
import org.openmrs.Location;
import org.openmrs.Patient;
import org.openmrs.Provider;
import org.openmrs.api.LocationService;
import org.openmrs.api.PatientService;
import org.openmrs.api.ProviderService;
import org.openmrs.module.appointments.dao.AppointmentAuditDao;
import org.openmrs.module.appointments.model.*;
import org.openmrs.module.appointments.service.AppointmentServiceService;
import org.openmrs.module.appointments.service.AppointmentsService;
import org.openmrs.module.appointments.web.contract.AppointmentDefaultResponse;
import org.openmrs.module.appointments.web.contract.AppointmentPayload;
import org.openmrs.module.appointments.web.contract.AppointmentQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

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
    AppointmentServiceMapper appointmentServiceMapper;

    @Autowired
    AppointmentsService appointmentsService;

    @Autowired
    AppointmentAuditDao appointmentAuditDao;

    public List<AppointmentDefaultResponse> constructResponse(List<Appointment> appointments) {
        return appointments.stream().map(as -> this.mapToDefaultResponse(as, new AppointmentDefaultResponse())).collect(Collectors.toList());
    }

    public Appointment getAppointmentFromPayload(AppointmentPayload appointmentPayload) {
        Appointment appointment;
        if (!StringUtils.isBlank(appointmentPayload.getUuid())) {
            appointment = appointmentsService.getAppointmentByUuid(appointmentPayload.getUuid());
            createEditEventInAppointmentAudit(appointment, appointmentPayload);
        } else {
            appointment = new Appointment();
            appointment.setPatient(patientService.getPatientByUuid(appointmentPayload.getPatientUuid()));
        }
        appointment.setAppointmentNumber(appointmentPayload.getAppointmentNumber());
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
        appointment.setComments(appointmentPayload.getComments());

        return appointment;
    }

    private void createEditEventInAppointmentAudit(Appointment existingAppointment, AppointmentPayload payload) {
        Map  fromObject = new HashMap<String,String>();
        Map  toObject = new HashMap<String,String>();

        String serviceUuid = existingAppointment.getService().getUuid();
        if(!payload.getServiceUuid().equals(serviceUuid)) {
            fromObject.put("serviceUuid", serviceUuid);
            toObject.put("serviceUuid", payload.getServiceUuid());
        }
        String serviceTypeUuid = existingAppointment.getServiceType() != null ? existingAppointment.getServiceType().getUuid() : null;
        if(!Objects.equals(payload.getServiceTypeUuid(), serviceTypeUuid)) {
            fromObject.put("serviceTypeUuid", serviceTypeUuid);
            toObject.put("serviceTypeUuid", payload.getServiceTypeUuid());
        }
        String providerUuid = existingAppointment.getProvider() != null ? existingAppointment.getProvider().getUuid() : null;
        if(!Objects.equals(payload.getProviderUuid(), providerUuid)){
            fromObject.put("providerUuid", providerUuid);
            toObject.put("providerUuid", payload.getProviderUuid());
        }
        String locationUuid = existingAppointment.getLocation() != null ? existingAppointment.getLocation().getUuid() : null;
        if(!Objects.equals(payload.getLocationUuid(), locationUuid)){
            fromObject.put("locationUuid", serviceUuid);
            toObject.put("locationUuid", payload.getLocationUuid());
        }
        if(!Objects.equals(payload.getStartDateTime().toInstant().toString(),
                existingAppointment.getStartDateTime().toInstant().toString())){
            fromObject.put("startDateTime", existingAppointment.getStartDateTime().toInstant().toString());
            toObject.put("startDateTime", payload.getStartDateTime().toInstant().toString());
        }
        if(!Objects.equals(payload.getEndDateTime().toInstant().toString(),
                existingAppointment.getEndDateTime().toInstant().toString())){
            fromObject.put("endDateTime", existingAppointment.getEndDateTime().toInstant().toString());
            toObject.put("endDateTime", payload.getEndDateTime().toInstant().toString());
        }
        if(!Objects.equals(AppointmentKind.valueOf(payload.getAppointmentKind()),
                existingAppointment.getAppointmentKind())){
            fromObject.put("appointmentKind", existingAppointment.getAppointmentKind().name());
            toObject.put("appointmentKind", payload.getAppointmentKind());
        }
        if(!Objects.equals(payload.getComments(),
                existingAppointment.getComments())){
            fromObject.put("appointmentNotes", existingAppointment.getComments());
            toObject.put("appointmentNotes", payload.getComments());
        }
        if(!fromObject.isEmpty()) {
	        ObjectMapper mapperObj = new ObjectMapper();
	        String auditNotes = null;
	        try {
		        auditNotes = String
				        .format("%s to %s", mapperObj.writeValueAsString(fromObject), mapperObj.writeValueAsString(toObject));
	        }
	        catch (IOException e) {
		        new RuntimeException(e);
	        }
	        AppointmentAudit auditEvent = new AppointmentAudit();
	        auditEvent.setStatus(existingAppointment.getStatus());
	        auditEvent.setNotes(auditNotes);
	        auditEvent.setAppointment(existingAppointment);
	        appointmentAuditDao.save(auditEvent);
        }
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
        response.setService(appointmentServiceMapper.constructDefaultResponse(a.getService()));
        response.setServiceType(createServiceTypeMap(a.getServiceType()));
        response.setProvider(createProviderMap(a.getProvider()));
        response.setLocation(createLocationMap(a.getLocation()));
        response.setStartDateTime(a.getStartDateTime());
        response.setEndDateTime(a.getEndDateTime());
        response.setAppointmentKind(a.getAppointmentKind().name());
        response.setStatus(a.getStatus().name());
        response.setComments(a.getComments());

        return response;
    }
    
    private Map createServiceTypeMap(AppointmentServiceType s) {
        Map serviceTypeMap = null;
        if (s != null) {
            serviceTypeMap = new HashMap();
            serviceTypeMap.put("name", s.getName());
            serviceTypeMap.put("uuid", s.getUuid());
            serviceTypeMap.put("duration", s.getDuration());
        }
        return serviceTypeMap;
    }
    
    private Map createProviderMap(Provider p) {
        Map providerMap = null;
        if (p != null) {
            providerMap = new HashMap();
            providerMap.put("name", p.getName());
            providerMap.put("uuid", p.getUuid());
        }
        return providerMap;
    }

    private Map createLocationMap(Location l) {
        Map locationMap = null;
        if (l != null) {
            locationMap = new HashMap();
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
}
