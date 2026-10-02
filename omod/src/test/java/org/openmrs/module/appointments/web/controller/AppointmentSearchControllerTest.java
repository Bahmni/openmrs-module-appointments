package org.openmrs.module.appointments.web.controller;

import org.bahmni.search.exceptions.InvalidSearchCriteriaException;
import org.bahmni.search.exceptions.SearchResponseErrorStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.openmrs.module.appointments.search.dto.AppointmentSearchRequest;
import org.openmrs.module.appointments.search.dto.AppointmentSearchResponse;
import org.openmrs.module.appointments.search.validation.CriteriaValidator;
import org.openmrs.module.appointments.service.AppointmentSearchService;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;
import org.mockito.Mockito;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class AppointmentSearchControllerTest {

    private static final String SUPPORTED_ENTITY = "appointment";

    @Mock
    private AppointmentSearchService appointmentSearchService;

    @Mock
    private CriteriaValidator criteriaValidator;

    @InjectMocks
    private AppointmentSearchController appointmentSearchController;

    private WebRequest webRequest;

    @BeforeEach
    public void setUp() {
        initMocks(this);
        webRequest = new ServletWebRequest(new MockHttpServletRequest());
    }

    @Test
    public void shouldValidateEntityAndReturnSuccessResponseOnSearch() {
        AppointmentSearchRequest request = new AppointmentSearchRequest();
        request.setEntity(SUPPORTED_ENTITY);
        AppointmentSearchResponse expectedResponse =
                AppointmentSearchResponse.success(SUPPORTED_ENTITY, Collections.emptyList());
        when(appointmentSearchService.search(request)).thenReturn(expectedResponse);

        ResponseEntity<AppointmentSearchResponse> response = appointmentSearchController.search(request, webRequest);

        verify(criteriaValidator, times(1)).validateEntity(SUPPORTED_ENTITY, SUPPORTED_ENTITY);
        verify(appointmentSearchService, times(1)).search(request);
        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        assertEquals(expectedResponse, response.getBody());
        assertEquals(SUPPORTED_ENTITY, webRequest.getAttribute(
                AppointmentSearchController.CURRENT_ENTITY_ATTRIBUTE, WebRequest.SCOPE_REQUEST));
    }

    @Test
    public void shouldPropagateExceptionAndNotInvokeServiceWhenEntityValidationFails() {
        assertThrows(InvalidSearchCriteriaException.class, () -> {
            AppointmentSearchRequest request = new AppointmentSearchRequest();
            request.setEntity("invalidEntity");
            doThrowOnValidate("invalidEntity");

            try {
                appointmentSearchController.search(request, webRequest);
            } finally {
                verify(appointmentSearchService, never()).search(any(AppointmentSearchRequest.class));
            }
        });
    }

    private void doThrowOnValidate(String entity) {
        org.mockito.Mockito.doThrow(new InvalidSearchCriteriaException(
                        "Entity '" + entity + "' is not supported. Supported entities: [" + SUPPORTED_ENTITY + "]",
                        SearchResponseErrorStatus.BAD_REQUEST))
                .when(criteriaValidator).validateEntity(entity, SUPPORTED_ENTITY);
    }
}
