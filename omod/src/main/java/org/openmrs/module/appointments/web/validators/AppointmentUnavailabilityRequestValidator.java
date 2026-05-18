package org.openmrs.module.appointments.web.validators;

import org.apache.commons.lang.StringUtils;
import org.openmrs.module.appointments.web.contract.AppointmentUnavailabilityRequest;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@Component
public class AppointmentUnavailabilityRequestValidator implements Validator {

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
    private static final SimpleDateFormat TIME_FORMAT = new SimpleDateFormat("HH:mm");

    static {
        DATE_FORMAT.setLenient(false);
        TIME_FORMAT.setLenient(false);
    }

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
            DATE_FORMAT.parse(request.getStartDate());
        } catch (ParseException e) {
            errors.reject("invalid", "startDate must be in yyyy-MM-dd format");
            return;
        }

        try {
            DATE_FORMAT.parse(request.getEndDate());
        } catch (ParseException e) {
            errors.reject("invalid", "endDate must be in yyyy-MM-dd format");
            return;
        }

        try {
            TIME_FORMAT.parse(request.getStartTime());
        } catch (ParseException e) {
            errors.reject("invalid", "startTime must be in HH:mm format");
            return;
        }

        try {
            TIME_FORMAT.parse(request.getEndTime());
        } catch (ParseException e) {
            errors.reject("invalid", "endTime must be in HH:mm format");
        }
    }

    private void validateBusinessLogic(AppointmentUnavailabilityRequest request, Errors errors) {
        try {
            Date startDate = DATE_FORMAT.parse(request.getStartDate());
            Date endDate = DATE_FORMAT.parse(request.getEndDate());

            if (endDate.before(startDate)) {
                errors.reject("invalid", "endDate cannot be before startDate");
                return;
            }

            if (startDate.equals(endDate)) {
                Date startTime = TIME_FORMAT.parse(request.getStartTime());
                Date endTime = TIME_FORMAT.parse(request.getEndTime());

                if (!endTime.after(startTime)) {
                    errors.reject("invalid", "endTime must be after startTime when dates are the same");
                }
            }
        } catch (ParseException e) {
            errors.reject("invalid", "Invalid date or time format");
        }
    }
}
