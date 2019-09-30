package org.openmrs.module.appointments.web.validators;
import org.openmrs.module.appointments.model.AppointmentSearchRequest;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import java.util.Date;

import static java.util.Objects.isNull;

@Component
public class AppointmentSearchValidator  implements Validator {
    @Override
    public boolean supports(Class<?> aClass) {
        return AppointmentSearchRequest.class.equals(aClass);
    }

    @Override
    public void validate(Object o, Errors errors) {
        AppointmentSearchRequest appointmentSearchRequest = (AppointmentSearchRequest) o;
        Date endDate = appointmentSearchRequest.getEndDate();
        Date startDate = appointmentSearchRequest.getStartDate();
        if(isNull(endDate) || isNull(startDate))
            errors.reject("invalid","Either StartDate or EndDate not provided");
    }
}
