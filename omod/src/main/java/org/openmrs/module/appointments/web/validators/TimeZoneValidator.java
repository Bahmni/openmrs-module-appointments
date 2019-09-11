package org.openmrs.module.appointments.web.validators;

import org.openmrs.api.APIException;
import org.openmrs.module.appointments.web.contract.RecurringPattern;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Component
public class TimeZoneValidator implements Validator {

    @Override
    public boolean supports(Class<?> aClass) {
        return String.class.equals(aClass);
    }

    @Override
    public void validate(Object o, Errors errors) {
        if (!StringUtils.hasText((String) o)) {
            errors.reject("Time Zone is missing");
        }
    }
}
