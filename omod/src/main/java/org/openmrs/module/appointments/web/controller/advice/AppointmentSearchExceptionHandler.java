package org.openmrs.module.appointments.web.controller.advice;

import org.bahmni.search.exceptions.InvalidSearchCriteriaException;
import org.bahmni.search.exceptions.SearchException;
import org.openmrs.api.APIAuthenticationException;
import org.openmrs.api.context.ContextAuthenticationException;
import org.openmrs.module.appointments.search.dto.AppointmentSearchResponse;
import org.openmrs.module.appointments.web.controller.AppointmentSearchController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.WebRequest;

import java.util.Collections;
import java.util.List;

@ControllerAdvice(assignableTypes = AppointmentSearchController.class)
public class AppointmentSearchExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(AppointmentSearchExceptionHandler.class);

    @ExceptionHandler(InvalidSearchCriteriaException.class)
    @ResponseBody
    public ResponseEntity<AppointmentSearchResponse> handleInvalidSearchCriteria(
            InvalidSearchCriteriaException e, WebRequest webRequest) {
        return errorResponse(currentEntity(webRequest), e.getStatus().getCode(), e.getMessages());
    }

    @ExceptionHandler(ContextAuthenticationException.class)
    @ResponseBody
    public ResponseEntity<AppointmentSearchResponse> handleAuthenticationRequired(
            ContextAuthenticationException e, WebRequest webRequest) {
        String message = e.getMessage() != null ? e.getMessage() : "Authentication required";
        return errorResponse(currentEntity(webRequest), HttpStatus.UNAUTHORIZED.value(), message);
    }

    @ExceptionHandler(APIAuthenticationException.class)
    @ResponseBody
    public ResponseEntity<AppointmentSearchResponse> handleAccessDenied(
            APIAuthenticationException e, WebRequest webRequest) {
        String message = e.getMessage() != null ? e.getMessage() : "Access denied";
        return errorResponse(currentEntity(webRequest), HttpStatus.FORBIDDEN.value(), message);
    }

    @ExceptionHandler(RuntimeException.class)
    @ResponseBody
    public ResponseEntity<AppointmentSearchResponse> handleUnexpectedError(
            RuntimeException e, WebRequest webRequest) {
        SearchException searchException =
                new SearchException("Unexpected error during appointment search", e);
        log.error(searchException.getMessage(), searchException);
        int statusCode = searchException.getStatus().getCode();
        return errorResponse(currentEntity(webRequest), statusCode,
                "An unexpected error occurred while processing the search request");
    }

    private ResponseEntity<AppointmentSearchResponse> errorResponse(String entity, int status, List<String> messages) {
        return ResponseEntity.status(status).body(AppointmentSearchResponse.error(entity, status, messages));
    }

    private ResponseEntity<AppointmentSearchResponse> errorResponse(String entity, int status, String message) {
        return errorResponse(entity, status, Collections.singletonList(message));
    }

    private String currentEntity(WebRequest webRequest) {
        Object entity = webRequest.getAttribute(
                AppointmentSearchController.CURRENT_ENTITY_ATTRIBUTE, WebRequest.SCOPE_REQUEST);
        return entity != null ? entity.toString() : null;
    }
}
