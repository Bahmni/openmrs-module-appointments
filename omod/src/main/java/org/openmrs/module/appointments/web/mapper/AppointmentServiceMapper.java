package org.openmrs.module.appointments.web.mapper;

import org.apache.commons.lang.StringUtils;
import org.openmrs.Location;
import org.openmrs.api.LocationService;
import org.openmrs.api.context.Context;
import org.openmrs.module.appointments.model.AppointmentService;
import org.openmrs.module.appointments.model.AppointmentServiceType;
import org.openmrs.module.appointments.model.ServiceWeeklyAvailability;
import org.openmrs.module.appointments.model.Speciality;
import org.openmrs.module.appointments.service.AppointmentServiceService;
import org.openmrs.module.appointments.service.SpecialityService;
import org.openmrs.module.appointments.web.contract.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.sql.Time;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class AppointmentServiceMapper {
    @Autowired
    LocationService locationService;

    @Autowired
    SpecialityService specialityService;

    @Autowired
    AppointmentServiceService appointmentServiceService;

    public AppointmentService getAppointmentServiceFromPayload(AppointmentServicePayload appointmentServicePayload) {
        AppointmentService appointmentService;
        if (!StringUtils.isBlank(appointmentServicePayload.getUuid())) {
            appointmentService = appointmentServiceService.getAppointmentServiceByUuid(appointmentServicePayload.getUuid());
        }else{
            appointmentService = new AppointmentService();
        }
        appointmentService.setName(appointmentServicePayload.getName());
        appointmentService.setDescription(appointmentServicePayload.getDescription());
        appointmentService.setDurationMins(appointmentServicePayload.getDurationMins());
        appointmentService.setStartTime(appointmentServicePayload.getStartTime());
        appointmentService.setEndTime(appointmentServicePayload.getEndTime());
        appointmentService.setMaxAppointmentsLimit(appointmentServicePayload.getMaxAppointmentsLimit());
        appointmentService.setColor(appointmentServicePayload.getColor());

        String locationUuid = appointmentServicePayload.getLocationUuid();
        Location location = locationService.getLocationByUuid(locationUuid);
        appointmentService.setLocation(location);

        String specialityUuid = appointmentServicePayload.getSpecialityUuid();
        Speciality speciality = specialityService.getSpecialityByUuid(specialityUuid);
        appointmentService.setSpeciality(speciality);

        List<ServiceWeeklyAvailabilityPayload> availabilityPayload = appointmentServicePayload.getWeeklyAvailability();

        if(availabilityPayload != null) {
            Set<ServiceWeeklyAvailability> availabilityList = availabilityPayload.stream()
                    .map(avb -> constructServiceWeeklyAvailability(avb, appointmentService)).collect(Collectors.toSet());
            appointmentService.setWeeklyAvailability(availabilityList);
        }
        
        if(appointmentServicePayload.getServiceTypes() != null) {
            appointmentServicePayload.getServiceTypes()
                    .forEach(serviceType -> constructAppointmentServiceTypes(serviceType, appointmentService));
        }
        return appointmentService;
    }

    private void constructAppointmentServiceTypes(AppointmentServiceTypePayload ast, AppointmentService appointmentService) {
        AppointmentServiceType serviceType;
        Set<AppointmentServiceType> existingServiceTypes = appointmentService.getServiceTypes(true);
        if(ast.getUuid() != null)
            serviceType = getServiceTypeByUuid(existingServiceTypes, ast.getUuid());
        else
            serviceType = new AppointmentServiceType();
        serviceType.setName(ast.getName());
        serviceType.setDuration(ast.getDuration());
        serviceType.setAppointmentService(appointmentService);
        if (ast.getVoided() != null) {
            setVoidedInfo(serviceType, ast.getVoidedReason());
        }
        existingServiceTypes.add(serviceType);
    }

    private void setVoidedInfo(AppointmentServiceType serviceType, String voidReason) {
        serviceType.setVoided(true);
        serviceType.setVoidReason(voidReason);
        serviceType.setDateVoided(new Date());
        serviceType.setVoidedBy(Context.getAuthenticatedUser());
    }

    private ServiceWeeklyAvailability constructServiceWeeklyAvailability(ServiceWeeklyAvailabilityPayload avb, AppointmentService appointmentService) {
        ServiceWeeklyAvailability availability;
        if(avb.getUuid() != null)
            availability = getAvailabilityByUuid(appointmentService.getWeeklyAvailability(), avb.getUuid());
        else
            availability = new ServiceWeeklyAvailability();
        availability.setDayOfWeek(avb.getDayOfWeek());
        availability.setStartTime(avb.getStartTime());
        availability.setEndTime(avb.getEndTime());
        availability.setMaxAppointmentsLimit(avb.getMaxAppointmentsLimit());
        availability.setService(appointmentService);
        availability.setVoided(avb.isVoided());

        return availability;
    }

    private ServiceWeeklyAvailability getAvailabilityByUuid(Set<ServiceWeeklyAvailability> weeklyAvailabilities, String availabilityUuid) {
           return weeklyAvailabilities.stream()
                    .filter(avb -> avb.getUuid().equals(availabilityUuid)).findAny().get();
    }

    private AppointmentServiceType getServiceTypeByUuid(Set<AppointmentServiceType> serviceTypes, String serviceTypeUuid) {
        return serviceTypes.stream()
                .filter(avb -> avb.getUuid().equals(serviceTypeUuid)).findAny().get();
    }

    public List<AppointmentServiceDefaultResponse> constructDefaultResponseForServiceList(List<AppointmentService> appointmentServices) {
        return appointmentServices.stream().map(as -> this.mapToDefaultResponse(as, new AppointmentServiceDefaultResponse())).collect(Collectors.toList());
    }

    private Map constructServiceTypeResponse(AppointmentServiceType serviceType) {
        Map serviceTypeMap = new HashMap();
        serviceTypeMap.put("name", serviceType.getName());
        serviceTypeMap.put("duration",serviceType.getDuration());
        serviceTypeMap.put("uuid", serviceType.getUuid());
        return serviceTypeMap;
    }

    public List<AppointmentServiceFullResponse> constructFullResponseForServiceList(List<AppointmentService> appointmentServices) {
        return appointmentServices.stream().map(as -> this.constructResponse(as)).collect(Collectors.toList());
    }

    public AppointmentServiceFullResponse constructResponse(AppointmentService service) {
        AppointmentServiceFullResponse response = new AppointmentServiceFullResponse();
        mapToDefaultResponse(service, response);
        Set<ServiceWeeklyAvailability> serviceWeeklyAvailability = service.getWeeklyAvailability();
        if(serviceWeeklyAvailability != null) {
            response.setWeeklyAvailability(serviceWeeklyAvailability.stream().map(availability -> this.constructAvailabilityResponse(availability)).collect(Collectors.toList()));
        }
        Set<AppointmentServiceType> serviceTypes = service.getServiceTypes();
        if(serviceTypes != null) {
            response.setServiceTypes(serviceTypes.stream().map(serviceType -> this.constructServiceTypeResponse(serviceType)).collect(Collectors.toList()));
        }
        return response;
    }

    public AppointmentServiceDefaultResponse constructDefaultResponse(AppointmentService appointmentService){
        return mapToDefaultResponse(appointmentService, new AppointmentServiceDefaultResponse());
    }
    
    private AppointmentServiceDefaultResponse mapToDefaultResponse(AppointmentService as, AppointmentServiceDefaultResponse asResponse) {
        asResponse.setUuid(as.getUuid());
        asResponse.setAppointmentServiceId(as.getAppointmentServiceId());
        asResponse.setName(as.getName());
        asResponse.setStartTime(convertTimeToString(as.getStartTime()));
        asResponse.setEndTime(convertTimeToString(as.getEndTime()));
        asResponse.setDescription(as.getDescription());
        asResponse.setDurationMins(as.getDurationMins());
        asResponse.setMaxAppointmentsLimit(as.getMaxAppointmentsLimit());
        asResponse.setColor(as.getColor());

        Map specialityMap = new HashMap();
        Speciality speciality = as.getSpeciality();
        if(speciality != null) {
            specialityMap.put("name", speciality.getName());
            specialityMap.put("uuid", speciality.getUuid());
        }
        asResponse.setSpeciality(specialityMap);

        Map locationMap = new HashMap();
        Location location = as.getLocation();
        if(location != null) {
            locationMap.put("name", location.getName());
            locationMap.put("uuid", location.getUuid());
        }
        asResponse.setLocation(locationMap);

        return asResponse;
    }

    private Map constructAvailabilityResponse(ServiceWeeklyAvailability availability) {
        Map availabilityMap = new HashMap();
        availabilityMap.put("dayOfWeek",availability.getDayOfWeek());
        availabilityMap.put("startTime", convertTimeToString(availability.getStartTime()));
        availabilityMap.put("endTime", convertTimeToString(availability.getEndTime()));
        availabilityMap.put("maxAppointmentsLimit", availability.getMaxAppointmentsLimit());
        availabilityMap.put("uuid", availability.getUuid());
        return availabilityMap;
    }

    private String convertTimeToString(Time time) {
       return time != null ? time.toString() : new String();
    }
}
