package org.openmrs.module.appointments.web.validators;

import org.apache.commons.lang.StringUtils;
import org.openmrs.module.appointments.web.contract.AppointmentUnavailabilityRequest;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

@Component
public class AppointmentUnavailabilityRequestValidator implements Validator {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    @Override
    public boolean supports(Class<?> aClass) {
        return List.class.equals(aClass);
    }

    @Override
    public void validate(Object o, Errors errors) {
        List<AppointmentUnavailabilityRequest> requests = (List<AppointmentUnavailabilityRequest>) o;

        if (requests == null || requests.isEmpty()) {
            errors.reject("invalid", "Request must contain at least one unavailability block");
            return;
        }

        for (AppointmentUnavailabilityRequest request : requests) {
            validateRequest(request, errors);
            if (errors.hasErrors()) {
                return;
            }
        }
    }

    private void validateRequest(AppointmentUnavailabilityRequest request, Errors errors) {
        validateRequiredFields(request, errors);

        if (!errors.hasErrors()) {
            validateDateTimeFormats(request, errors);
        }

        if (!errors.hasErrors()) {
            validateBusinessLogic(request, errors);
        }
    }

    private void validateRequiredFields(AppointmentUnavailabilityRequest request, Errors errors) {
        if (StringUtils.isBlank(request.getLocationUuid())) {
            errors.reject("invalid", "locationUuid is required");
            return;
        }

        if (StringUtils.isBlank(request.getStartDate())) {
            errors.reject("invalid", "startDate is required");
            return;
        }

        if (StringUtils.isBlank(request.getStartTime())) {
            errors.reject("invalid", "startTime is required");
            return;
        }

        if (StringUtils.isBlank(request.getEndDate())) {
            errors.reject("invalid", "endDate is required");
            return;
        }

        if (StringUtils.isBlank(request.getEndTime())) {
            errors.reject("invalid", "endTime is required");
        }
    }

    private void validateDateTimeFormats(AppointmentUnavailabilityRequest request, Errors errors) {
        try {
            LocalDate.parse(request.getStartDate(), DATE_FORMATTER);
        } catch (DateTimeParseException e) {
            errors.reject("invalid", "startDate must be in yyyy-MM-dd format");
            return;
        }

        try {
            LocalDate.parse(request.getEndDate(), DATE_FORMATTER);
        } catch (DateTimeParseException e) {
            errors.reject("invalid", "endDate must be in yyyy-MM-dd format");
            return;
        }

        try {
            LocalTime.parse(request.getStartTime(), TIME_FORMATTER);
        } catch (DateTimeParseException e) {
            errors.reject("invalid", "startTime must be in HH:mm format");
            return;
        }

        try {
            LocalTime.parse(request.getEndTime(), TIME_FORMATTER);
        } catch (DateTimeParseException e) {
            errors.reject("invalid", "endTime must be in HH:mm format");
        }
    }

    private void validateBusinessLogic(AppointmentUnavailabilityRequest request, Errors errors) {
        try {
            LocalDate startDate = LocalDate.parse(request.getStartDate(), DATE_FORMATTER);
            LocalDate endDate = LocalDate.parse(request.getEndDate(), DATE_FORMATTER);

            if (endDate.isBefore(startDate)) {
                errors.reject("invalid", "endDate cannot be before startDate");
                return;
            }

            if (startDate.equals(endDate)) {
                LocalTime startTime = LocalTime.parse(request.getStartTime(), TIME_FORMATTER);
                LocalTime endTime = LocalTime.parse(request.getEndTime(), TIME_FORMATTER);

                if (!endTime.isAfter(startTime)) {
                    errors.reject("invalid", "endTime must be after startTime when dates are the same");
                }
            }
        } catch (DateTimeParseException e) {
            errors.reject("invalid", "Invalid date or time format");
        }
    }
}
