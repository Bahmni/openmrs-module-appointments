package org.openmrs.module.appointments.web.mapper;

import org.apache.commons.lang.StringUtils;
import org.openmrs.Location;
import org.openmrs.Provider;
import org.openmrs.api.LocationService;
import org.openmrs.api.ProviderService;
import org.openmrs.module.appointments.model.AppointmentServiceDefinition;
import org.openmrs.module.appointments.model.AppointmentUnavailability;
import org.openmrs.module.appointments.service.AppointmentServiceDefinitionService;
import org.openmrs.module.appointments.web.contract.AppointmentUnavailabilityRequest;
import org.openmrs.module.appointments.web.contract.AppointmentUnavailabilityResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.sql.Time;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
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

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public AppointmentUnavailability fromRequest(AppointmentUnavailabilityRequest request) {
        AppointmentUnavailability unavailability = new AppointmentUnavailability();

        Location location = locationService.getLocationByUuid(request.getLocationUuid());
        if (location == null) {
            throw new RuntimeException("Invalid location or location not found");
        }
        unavailability.setLocation(location);

        if (StringUtils.isNotBlank(request.getAppointmentServiceUuid())) {
            AppointmentServiceDefinition service = appointmentServiceDefinitionService
                    .getAppointmentServiceByUuid(request.getAppointmentServiceUuid());
            if (service == null) {
                throw new RuntimeException("Invalid service or service not found");
            }
            unavailability.setService(service);
        }

        if (StringUtils.isNotBlank(request.getProviderUuid())) {
            Provider provider = providerService.getProviderByUuid(request.getProviderUuid());
            unavailability.setProvider(provider);
        }

        try {
            LocalDate startDate = LocalDate.parse(request.getStartDate(), DATE_FORMATTER);
            unavailability.setStartDate(java.sql.Date.valueOf(startDate));

            LocalTime startTime = LocalTime.parse(request.getStartTime(), TIME_FORMATTER);
            unavailability.setStartTime(Time.valueOf(startTime));

            LocalDate endDate = LocalDate.parse(request.getEndDate(), DATE_FORMATTER);
            unavailability.setEndDate(java.sql.Date.valueOf(endDate));

            LocalTime endTime = LocalTime.parse(request.getEndTime(), TIME_FORMATTER);
            unavailability.setEndTime(Time.valueOf(endTime));
        } catch (Exception e) {
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

        if (unavailability.getLocation() != null) {
            response.setLocationUuid(unavailability.getLocation().getUuid());
            response.setLocationName(unavailability.getLocation().getName());
        }

        if (unavailability.getService() != null) {
            response.setAppointmentServiceUuid(unavailability.getService().getUuid());
            response.setAppointmentServiceName(unavailability.getService().getName());
        }

        if (unavailability.getProvider() != null) {
            response.setProviderUuid(unavailability.getProvider().getUuid());
            response.setProviderName(unavailability.getProvider().getName());
        }

        response.setStartDate(formatDate(unavailability.getStartDate()));
        response.setStartTime(formatTime(unavailability.getStartTime()));
        response.setEndDate(formatDate(unavailability.getEndDate()));
        response.setEndTime(formatTime(unavailability.getEndTime()));

        response.setVoided(unavailability.getVoided());
        if (unavailability.getDateCreated() != null) {
            response.setDateCreated(DATETIME_FORMATTER.format(
                    unavailability.getDateCreated().toInstant().atZone(ZoneOffset.UTC)));
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
        return date != null ? date.toLocalDate().format(DATE_FORMATTER) : null;
    }

    private String formatTime(Time time) {
        return time != null ? time.toLocalTime().format(TIME_FORMATTER) : null;
    }
}
