package org.openmrs.module.appointments.web.mapper;

import org.apache.commons.lang.StringUtils;
import org.openmrs.Location;
import org.openmrs.Provider;
import org.openmrs.api.LocationService;
import org.openmrs.api.ProviderService;
import org.openmrs.api.context.Context;
import org.openmrs.module.appointments.model.AppointmentServiceDefinition;
import org.openmrs.module.appointments.model.AppointmentUnavailability;
import org.openmrs.module.appointments.service.AppointmentServiceDefinitionService;
import org.openmrs.module.appointments.web.contract.AppointmentUnavailabilityRequest;
import org.openmrs.module.appointments.web.contract.AppointmentUnavailabilityResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.sql.Time;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class AppointmentUnavailabilityMapper {

    @Autowired
    private LocationService locationService;

    @Autowired
    private ProviderService providerService;

    @Autowired
    private AppointmentServiceDefinitionService appointmentServiceDefinitionService;

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
    private static final SimpleDateFormat TIME_FORMAT = new SimpleDateFormat("HH:mm");
    private static final SimpleDateFormat DATETIME_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm");

    public AppointmentUnavailability fromRequest(AppointmentUnavailabilityRequest request) {
        AppointmentUnavailability unavailability = new AppointmentUnavailability();

        // Set location (required)
        Location location = locationService.getLocationByUuid(request.getLocationUuid());
        unavailability.setLocation(location);

        // Set service (optional)
        if (StringUtils.isNotBlank(request.getAppointmentServiceUuid())) {
            AppointmentServiceDefinition service = appointmentServiceDefinitionService
                    .getAppointmentServiceByUuid(request.getAppointmentServiceUuid());
            unavailability.setService(service);
        }

        // Set provider (optional)
        if (StringUtils.isNotBlank(request.getProviderUuid())) {
            Provider provider = providerService.getProviderByUuid(request.getProviderUuid());
            unavailability.setProvider(provider);
        }

        // Parse and set dates and times
        try {
            Date startDate = DATE_FORMAT.parse(request.getStartDate());
            unavailability.setStartDate(new java.sql.Date(startDate.getTime()));

            Date startTime = TIME_FORMAT.parse(request.getStartTime());
            unavailability.setStartTime(new Time(startTime.getTime()));

            Date endDate = DATE_FORMAT.parse(request.getEndDate());
            unavailability.setEndDate(new java.sql.Date(endDate.getTime()));

            Date endTime = TIME_FORMAT.parse(request.getEndTime());
            unavailability.setEndTime(new Time(endTime.getTime()));
        } catch (ParseException e) {
            throw new RuntimeException("Invalid date or time format: " + e.getMessage(), e);
        }

        return unavailability;
    }

    public List<AppointmentUnavailability> fromRequest(List<AppointmentUnavailabilityRequest> requests) {
        if (requests == null) {
            return new ArrayList<>();
        }
        return requests.stream()
                .map(this::fromRequest)
                .collect(Collectors.toList());
    }

    public AppointmentUnavailabilityResponse constructResponse(AppointmentUnavailability unavailability) {
        AppointmentUnavailabilityResponse response = new AppointmentUnavailabilityResponse();

        response.setUuid(unavailability.getUuid());

        // Location details
        if (unavailability.getLocation() != null) {
            response.setLocationUuid(unavailability.getLocation().getUuid());
            response.setLocationName(unavailability.getLocation().getName());
        }

        // Service details (optional)
        if (unavailability.getService() != null) {
            response.setAppointmentServiceUuid(unavailability.getService().getUuid());
            response.setAppointmentServiceName(unavailability.getService().getName());
        }

        // Provider details (optional)
        if (unavailability.getProvider() != null) {
            response.setProviderUuid(unavailability.getProvider().getUuid());
            response.setProviderName(unavailability.getProvider().getName());
        }

        // Date and time fields
        response.setStartDate(formatDate(unavailability.getStartDate()));
        response.setStartTime(formatTime(unavailability.getStartTime()));
        response.setEndDate(formatDate(unavailability.getEndDate()));
        response.setEndTime(formatTime(unavailability.getEndTime()));

        // Metadata
        response.setVoided(unavailability.getVoided());
        if (unavailability.getDateCreated() != null) {
            response.setDateCreated(DATETIME_FORMAT.format(unavailability.getDateCreated()));
        }
        if (unavailability.getCreator() != null) {
            response.setCreatorName(unavailability.getCreator().getUsername());
        }

        return response;
    }

    public List<AppointmentUnavailabilityResponse> constructResponse(List<AppointmentUnavailability> unavailabilities) {
        if (unavailabilities == null) {
            return new ArrayList<>();
        }
        return unavailabilities.stream()
                .map(this::constructResponse)
                .collect(Collectors.toList());
    }

    private String formatDate(java.sql.Date date) {
        return date != null ? DATE_FORMAT.format(date) : null;
    }

    private String formatTime(Time time) {
        return time != null ? TIME_FORMAT.format(time) : null;
    }
}
