package org.openmrs.module.appointments.web.mapper;

import org.apache.commons.lang.StringUtils;
import org.openmrs.Location;
import org.openmrs.api.LocationService;
import org.openmrs.module.appointments.model.AppointmentService;
import org.openmrs.module.appointments.model.Speciality;
import org.openmrs.module.appointments.service.SpecialityService;
import org.openmrs.module.appointments.web.contract.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.sql.Time;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class AppointmentServiceMapper {
    @Autowired
    LocationService locationService;

    @Autowired
    SpecialityService specialityService;

    public AppointmentService getAppointmentServiceFromPayload(AppointmentServicePayload appointmentServicePayload) {
        AppointmentService appointmentService = new AppointmentService();
        if (!StringUtils.isBlank(appointmentServicePayload.getUuid())) {
            appointmentService.setUuid(appointmentServicePayload.getUuid());
        }
        appointmentService.setName(appointmentServicePayload.getName());
        appointmentService.setDescription(appointmentServicePayload.getDescription());
        appointmentService.setDurationMins(appointmentServicePayload.getDurationMins());
        appointmentService.setStartTime(appointmentServicePayload.getStartTime());
        appointmentService.setEndTime(appointmentServicePayload.getEndTime());
        appointmentService.setMaxAppointmentsLimit(appointmentServicePayload.getMaxAppointmentsLimit());

        String locationUuid = appointmentServicePayload.getLocationUuid();
        Location location = locationService.getLocationByUuid(locationUuid);
        appointmentService.setLocation(location);

        String specialityUuid = appointmentServicePayload.getSpecialityUuid();
        Speciality speciality = specialityService.getSpecialityByUuid(specialityUuid);
        appointmentService.setSpeciality(speciality);

        return appointmentService;
    }

    public List<AppointmentServiceResponse> constructResponse(List<AppointmentService> appointmentServices) {
        return appointmentServices.stream().map(as -> this.constructResponse(as)).collect(Collectors.toList());
    }

    public AppointmentServiceResponse constructResponse(AppointmentService as) {
        AppointmentServiceResponse asResponse = new AppointmentServiceResponse();
        asResponse.setUuid(as.getUuid());
        asResponse.setName(as.getName());
        asResponse.setStartTime(as.getStartTime().toString());
        asResponse.setEndTime(as.getEndTime().toString());
        asResponse.setDescription(as.getDescription());
        asResponse.setDurationMins(as.getDurationMins());
        asResponse.setMaxAppointmentsLimit(as.getMaxAppointmentsLimit());

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

}
