package org.openmrs.module.appointments.web.validators;

import org.junit.Before;
import org.junit.Test;
import org.openmrs.module.appointments.web.contract.AppointmentUnavailabilityRequest;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;

import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class AppointmentUnavailabilityRequestValidatorTest {

    private AppointmentUnavailabilityRequestValidator validator;

    @Before
    public void setUp() {
        validator = new AppointmentUnavailabilityRequestValidator();
    }

    @Test
    public void shouldNotAddErrorsForValidRequest() {
        AppointmentUnavailabilityRequest request = createValidRequest();
        List<AppointmentUnavailabilityRequest> requests = Collections.singletonList(request);

        Errors errors = new BeanPropertyBindingResult(requests, "requests");
        validator.validate(requests, errors);

        assertFalse(errors.hasErrors());
    }

    @Test
    public void shouldAddErrorWhenLocationUuidIsMissing() {
        AppointmentUnavailabilityRequest request = createValidRequest();
        request.setLocationUuid(null);
        List<AppointmentUnavailabilityRequest> requests = Collections.singletonList(request);

        Errors errors = new BeanPropertyBindingResult(requests, "requests");
        validator.validate(requests, errors);

        assertTrue(errors.hasErrors());
        assertEquals("locationUuid is required", errors.getAllErrors().get(0).getDefaultMessage());
    }

    @Test
    public void shouldAddErrorWhenStartDateIsMissing() {
        AppointmentUnavailabilityRequest request = createValidRequest();
        request.setStartDate(null);
        List<AppointmentUnavailabilityRequest> requests = Collections.singletonList(request);

        Errors errors = new BeanPropertyBindingResult(requests, "requests");
        validator.validate(requests, errors);

        assertTrue(errors.hasErrors());
        assertEquals("startDate is required", errors.getAllErrors().get(0).getDefaultMessage());
    }

    @Test
    public void shouldAddErrorWhenStartTimeIsMissing() {
        AppointmentUnavailabilityRequest request = createValidRequest();
        request.setStartTime(null);
        List<AppointmentUnavailabilityRequest> requests = Collections.singletonList(request);

        Errors errors = new BeanPropertyBindingResult(requests, "requests");
        validator.validate(requests, errors);

        assertTrue(errors.hasErrors());
        assertEquals("startTime is required", errors.getAllErrors().get(0).getDefaultMessage());
    }

    @Test
    public void shouldAddErrorWhenEndDateIsMissing() {
        AppointmentUnavailabilityRequest request = createValidRequest();
        request.setEndDate(null);
        List<AppointmentUnavailabilityRequest> requests = Collections.singletonList(request);

        Errors errors = new BeanPropertyBindingResult(requests, "requests");
        validator.validate(requests, errors);

        assertTrue(errors.hasErrors());
        assertEquals("endDate is required", errors.getAllErrors().get(0).getDefaultMessage());
    }

    @Test
    public void shouldAddErrorWhenEndTimeIsMissing() {
        AppointmentUnavailabilityRequest request = createValidRequest();
        request.setEndTime(null);
        List<AppointmentUnavailabilityRequest> requests = Collections.singletonList(request);

        Errors errors = new BeanPropertyBindingResult(requests, "requests");
        validator.validate(requests, errors);

        assertTrue(errors.hasErrors());
        assertEquals("endTime is required", errors.getAllErrors().get(0).getDefaultMessage());
    }

    @Test
    public void shouldAddErrorWhenStartDateHasInvalidFormat() {
        AppointmentUnavailabilityRequest request = createValidRequest();
        request.setStartDate("invalid-date");
        List<AppointmentUnavailabilityRequest> requests = Collections.singletonList(request);

        Errors errors = new BeanPropertyBindingResult(requests, "requests");
        validator.validate(requests, errors);

        assertTrue(errors.hasErrors());
        assertEquals("startDate must be in yyyy-MM-dd format", errors.getAllErrors().get(0).getDefaultMessage());
    }

    @Test
    public void shouldAddErrorWhenRequestListIsEmpty() {
        List<AppointmentUnavailabilityRequest> requests = Collections.emptyList();

        Errors errors = new BeanPropertyBindingResult(requests, "requests");
        validator.validate(requests, errors);

        assertTrue(errors.hasErrors());
        assertEquals("Request must contain at least one unavailability block", errors.getAllErrors().get(0).getDefaultMessage());
    }

    private AppointmentUnavailabilityRequest createValidRequest() {
        AppointmentUnavailabilityRequest request = new AppointmentUnavailabilityRequest();
        request.setLocationUuid("aaa006e5-9fbb-4f20-866b-0ece245615a1");
        request.setStartDate("2026-08-10");
        request.setStartTime("09:00");
        request.setEndDate("2026-08-10");
        request.setEndTime("17:00");
        return request;
    }
}
