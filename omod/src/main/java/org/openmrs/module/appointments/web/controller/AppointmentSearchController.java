package org.openmrs.module.appointments.web.controller;

import org.bahmni.search.exceptions.InvalidSearchCriteriaException;
import org.bahmni.search.exceptions.SearchException;
import org.bahmni.search.exceptions.SearchResponseErrorStatus;
import org.bahmni.search.model.ContextSearchResponse;
import org.bahmni.search.model.ErrorSearchResponse;
import org.bahmni.search.model.SearchRequest;
import org.openmrs.api.APIAuthenticationException;
import org.openmrs.api.context.ContextAuthenticationException;
import org.openmrs.module.appointments.service.AppointmentSearchService;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.v1_0.controller.BaseRestController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping(value = "/rest/" + RestConstants.VERSION_1 + "/appointmentSearch")
public class AppointmentSearchController extends BaseRestController {

    private static final Logger log = LoggerFactory.getLogger(AppointmentSearchController.class);

    private static final String SUPPORTED_ENTITY = "appointment";

    private final AppointmentSearchService appointmentSearchService;

    @Autowired
    public AppointmentSearchController(AppointmentSearchService appointmentSearchService) {
        this.appointmentSearchService = appointmentSearchService;
    }

    @PostMapping(
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ResponseBody
    public ResponseEntity<ContextSearchResponse> search(@RequestBody SearchRequest request) {
        String entity = request.getEntity();
        try {
            if (entity == null || entity.isEmpty()) {
                throw new InvalidSearchCriteriaException(
                        "Request must include 'entity'", SearchResponseErrorStatus.BAD_REQUEST);
            }
            if (!SUPPORTED_ENTITY.equalsIgnoreCase(entity)) {
                throw new InvalidSearchCriteriaException(
                        "Entity '" + entity + "' is not supported. Supported entities: ["
                                + SUPPORTED_ENTITY + "]",
                        SearchResponseErrorStatus.BAD_REQUEST);
            }
            ContextSearchResponse response = appointmentSearchService.search(request);
            return ResponseEntity.ok(response);
        } catch (InvalidSearchCriteriaException e) {
            return ResponseEntity.status(e.getStatus().getCode())
                    .body(new ErrorSearchResponse(entity, e.getStatus().getCode(), e.getMessages()));
        } catch (ContextAuthenticationException e) {
            String message = e.getMessage() != null ? e.getMessage() : "Authentication required";
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ErrorSearchResponse(entity, HttpStatus.UNAUTHORIZED.value(), message));
        } catch (APIAuthenticationException e) {
            String message = e.getMessage() != null ? e.getMessage() : "Access denied";
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ErrorSearchResponse(entity, HttpStatus.FORBIDDEN.value(), message));
        } catch (RuntimeException e) {
            log.error("Unexpected error during appointment search. Root cause: {} - {}",
                    e.getClass().getName(), e.getMessage(), e);
            SearchException searchException =
                    new SearchException("Unexpected error during appointment search", e);
            int statusCode = searchException.getStatus().getCode();
            return ResponseEntity.status(statusCode)
                    .body(new ErrorSearchResponse(entity, statusCode,
                            "An unexpected error occurred while processing the search request"));
        }
    }
}
