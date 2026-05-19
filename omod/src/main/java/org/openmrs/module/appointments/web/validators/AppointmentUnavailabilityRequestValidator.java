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
    private static final String ERROR_CODE_INVALID = "invalid";

    @Override
    public boolean supports(Class<?> aClass) {
        return List.class.equals(aClass);
    }

    @Override
    public void validate(Object o, Errors errors) {
        List<AppointmentUnavailabilityRequest> requests = (List<AppointmentUnavailabilityRequest>) o;

        if (requests == null || requests.isEmpty()) {
            errors.reject(ERROR_CODE_INVALID, "Request must contain at least one unavailability block");
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
            errors.reject(ERROR_CODE_INVALID, "locationUuid is required");
            return;
        }

        if (StringUtils.isBlank(request.getStartDate())) {
            errors.reject(ERROR_CODE_INVALID, "startDate is required");
            return;
        }

        if (StringUtils.isBlank(request.getStartTime())) {
            errors.reject(ERROR_CODE_INVALID, "startTime is required");
            return;
        }

        if (StringUtils.isBlank(request.getEndDate())) {
            errors.reject(ERROR_CODE_INVALID, "endDate is required");
            return;
        }

        if (StringUtils.isBlank(request.getEndTime())) {
            errors.reject(ERROR_CODE_INVALID, "endTime is required");
        }
    }

    private void validateDateTimeFormats(AppointmentUnavailabilityRequest request, Errors errors) {
        try {
            LocalDate.parse(request.getStartDate(), DATE_FORMATTER);
        } catch (DateTimeParseException e) {
            errors.reject(ERROR_CODE_INVALID, "startDate must be in yyyy-MM-dd format");
            return;
        }

        try {
            LocalDate.parse(request.getEndDate(), DATE_FORMATTER);
        } catch (DateTimeParseException e) {
            errors.reject(ERROR_CODE_INVALID, "endDate must be in yyyy-MM-dd format");
            return;
        }

        try {
            LocalTime.parse(request.getStartTime(), TIME_FORMATTER);
        } catch (DateTimeParseException e) {
            errors.reject(ERROR_CODE_INVALID, "startTime must be in HH:mm format");
            return;
        }

        try {
            LocalTime.parse(request.getEndTime(), TIME_FORMATTER);
        } catch (DateTimeParseException e) {
            errors.reject(ERROR_CODE_INVALID, "endTime must be in HH:mm format");
        }
    }

    private void validateBusinessLogic(AppointmentUnavailabilityRequest request, Errors errors) {
        try {
            LocalDate startDate = LocalDate.parse(request.getStartDate(), DATE_FORMATTER);
            LocalDate endDate = LocalDate.parse(request.getEndDate(), DATE_FORMATTER);

            if (endDate.isBefore(startDate)) {
                errors.reject(ERROR_CODE_INVALID, "endDate cannot be before startDate");
                return;
            }

            if (startDate.equals(endDate)) {
                LocalTime startTime = LocalTime.parse(request.getStartTime(), TIME_FORMATTER);
                LocalTime endTime = LocalTime.parse(request.getEndTime(), TIME_FORMATTER);

                if (!endTime.isAfter(startTime)) {
                    errors.reject(ERROR_CODE_INVALID, "endTime must be after startTime when dates are the same");
                }
            }
        } catch (DateTimeParseException e) {
            errors.reject(ERROR_CODE_INVALID, "Invalid date or time format");
        }
    }
}
