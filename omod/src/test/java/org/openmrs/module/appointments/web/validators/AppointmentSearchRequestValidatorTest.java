package org.openmrs.module.appointments.web.validators;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openmrs.module.appointments.model.AppointmentSearchRequest;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.Date;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class AppointmentSearchRequestValidatorTest {
    private AppointmentSearchValidator appointmentSearchValidator = new AppointmentSearchValidator();
    private Errors errors;
    private AppointmentSearchRequest appointmentSearchRequest;

    private AppointmentSearchRequest getAppointmentSearchRequest() {
        AppointmentSearchRequest appointmentSearchRequest = new AppointmentSearchRequest();
        appointmentSearchRequest.setEndDate(new Date());
        appointmentSearchRequest.setStartDate(new Date());
        return appointmentSearchRequest;
    }

    @BeforeEach
    public void setup() throws Exception {
        appointmentSearchRequest = getAppointmentSearchRequest();
        errors = new BeanPropertyBindingResult(appointmentSearchRequest, "appointmentSearchRequest");
    }

    @Test
    public void shouldNotThrowAnExceptionForValidData() {
        appointmentSearchValidator.validate(appointmentSearchRequest, errors);
        assertEquals(errors.getAllErrors().size(), 0);
    }

    @Test
    public void shouldNotThrowErrorsWhenEndDateIsNull() {
        appointmentSearchRequest = getAppointmentSearchRequest();
        appointmentSearchRequest.setEndDate(null);
        appointmentSearchValidator.validate(appointmentSearchRequest, errors);
        assertEquals(errors.getAllErrors().size(), 0);
    }

    @Test
    public void shouldAddToErrorsWhenStartDateIsNull() {
        appointmentSearchRequest = getAppointmentSearchRequest();
        appointmentSearchRequest.setStartDate(null);
        appointmentSearchValidator.validate(appointmentSearchRequest, errors);
        assertEquals(errors.getAllErrors().size(), 1);
        assertNotNull(errors.getAllErrors().get(0).getCodes()[1]);
    }

}
