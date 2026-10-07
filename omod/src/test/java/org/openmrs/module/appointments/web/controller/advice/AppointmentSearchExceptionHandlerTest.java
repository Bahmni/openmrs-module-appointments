package org.openmrs.module.appointments.web.controller.advice;

import org.bahmni.search.exceptions.InvalidSearchCriteriaException;
import org.bahmni.search.exceptions.SearchResponseErrorStatus;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.api.APIAuthenticationException;
import org.openmrs.api.context.ContextAuthenticationException;
import org.openmrs.module.appointments.search.dto.AppointmentSearchResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class AppointmentSearchExceptionHandlerTest {

    private static final String SUPPORTED_ENTITY = "appointment";

    private AppointmentSearchExceptionHandler exceptionHandler;

    private WebRequest webRequest;

    @Before
    public void setUp() {
        exceptionHandler = new AppointmentSearchExceptionHandler();
        MockHttpServletRequest mockHttpServletRequest = new MockHttpServletRequest();
        webRequest = new ServletWebRequest(mockHttpServletRequest);
        webRequest.setAttribute(org.openmrs.module.appointments.web.controller.AppointmentSearchController.CURRENT_ENTITY_ATTRIBUTE,
                SUPPORTED_ENTITY, WebRequest.SCOPE_REQUEST);
    }

    @Test
    public void shouldHandleInvalidSearchCriteriaExceptionAndReturnBadRequestResponse() {
        InvalidSearchCriteriaException exception = new InvalidSearchCriteriaException(
                "Leaf condition for field 'appointment.startDateTime' has an invalid date value",
                SearchResponseErrorStatus.BAD_REQUEST);

        ResponseEntity<AppointmentSearchResponse> response =
                exceptionHandler.handleInvalidSearchCriteria(exception, webRequest);

        assertNotNull(response);
        assertEquals(400, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        assertNotNull(response.getBody().getError());
        assertEquals(400, response.getBody().getError().getStatus());
        assertEquals(exception.getMessages(), response.getBody().getError().getMessages());
    }

    @Test
    public void shouldHandleContextAuthenticationExceptionAndReturnUnauthorizedResponse() {
        ContextAuthenticationException exception = new ContextAuthenticationException("Authentication required");

        ResponseEntity<AppointmentSearchResponse> response =
                exceptionHandler.handleAuthenticationRequired(exception, webRequest);

        assertNotNull(response);
        assertEquals(401, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        assertNotNull(response.getBody().getError());
        assertEquals(401, response.getBody().getError().getStatus());
    }

    @Test
    public void shouldHandleAPIAuthenticationExceptionAndReturnForbiddenResponse() {
        APIAuthenticationException exception = new APIAuthenticationException("Access denied");

        ResponseEntity<AppointmentSearchResponse> response =
                exceptionHandler.handleAccessDenied(exception, webRequest);

        assertNotNull(response);
        assertEquals(403, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        assertNotNull(response.getBody().getError());
        assertEquals(403, response.getBody().getError().getStatus());
    }

    @Test
    public void shouldHandleMethodNotSupportedExceptionAndReturnMethodNotAllowedResponse() throws Exception {
        HttpRequestMethodNotSupportedException exception =
                new HttpRequestMethodNotSupportedException("GET");

        ResponseEntity<AppointmentSearchResponse> response =
                exceptionHandler.handleMethodNotSupported(exception, webRequest);

        assertNotNull(response);
        assertEquals(405, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        assertNotNull(response.getBody().getError());
        assertEquals(405, response.getBody().getError().getStatus());
    }

    @Test
    public void shouldHandleUnexpectedRuntimeExceptionAndReturnInternalServerErrorResponse() {
        RuntimeException exception = new RuntimeException("Something went wrong");

        ResponseEntity<AppointmentSearchResponse> response =
                exceptionHandler.handleUnexpectedError(exception, webRequest);

        assertNotNull(response);
        assertEquals(500, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        assertNotNull(response.getBody().getError());
        assertEquals(500, response.getBody().getError().getStatus());
    }

    @Test
    public void shouldHandleUnexpectedCheckedExceptionAndReturnInternalServerErrorResponse() {
        Exception exception = new Exception("Something went wrong");

        ResponseEntity<AppointmentSearchResponse> response =
                exceptionHandler.handleUnexpectedCheckedError(exception, webRequest);

        assertNotNull(response);
        assertEquals(500, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        assertNotNull(response.getBody().getError());
        assertEquals(500, response.getBody().getError().getStatus());
    }
}
