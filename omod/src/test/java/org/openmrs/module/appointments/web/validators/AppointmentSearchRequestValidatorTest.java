package org.openmrs.module.appointments.web.validators;


import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.openmrs.module.appointments.model.AppointmentSearchRequest;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Date;

public class AppointmentSearchRequestValidatorTest {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();
    private AppointmentSearchValidator appointmentSearchValidator = new AppointmentSearchValidator();
    private Errors errors;
    private AppointmentSearchRequest appointmentSearchRequest;

    private AppointmentSearchRequest getAppointmentSearchRequest() {
        AppointmentSearchRequest appointmentSearchRequest = new AppointmentSearchRequest();
        appointmentSearchRequest.setEndDate(new Date());
        appointmentSearchRequest.setStartDate(new Date());
        return appointmentSearchRequest;
    }

    @Before
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
