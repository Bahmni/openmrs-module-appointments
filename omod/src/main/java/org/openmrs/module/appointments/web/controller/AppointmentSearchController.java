package org.openmrs.module.appointments.web.controller;

import org.openmrs.module.appointments.search.dto.AppointmentSearchResponse;
import org.openmrs.module.appointments.search.dto.AppointmentSearchRequest;
import org.openmrs.module.appointments.search.validation.CriteriaValidator;
import org.openmrs.module.appointments.service.AppointmentSearchService;

import org.openmrs.module.webservices.rest.web.RestConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.WebRequest;

@Controller
@RequestMapping(value = "/rest/" + RestConstants.VERSION_1 + "/appointmentSearch")
public class AppointmentSearchController {

    private static final String SUPPORTED_ENTITY = "appointment";

    public static final String CURRENT_ENTITY_ATTRIBUTE = "appointmentSearch.currentEntity";

    private final AppointmentSearchService appointmentSearchService;
    private final CriteriaValidator criteriaValidator;

    @Autowired
    public AppointmentSearchController(AppointmentSearchService appointmentSearchService,
                                        CriteriaValidator criteriaValidator) {
        this.appointmentSearchService = appointmentSearchService;
        this.criteriaValidator = criteriaValidator;
    }

    @PostMapping(
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ResponseBody
    public ResponseEntity<AppointmentSearchResponse> search(@RequestBody AppointmentSearchRequest request,
                                                             WebRequest webRequest) {
        String entity = request.getEntity();
        criteriaValidator.validateEntity(entity, SUPPORTED_ENTITY);
        webRequest.setAttribute(CURRENT_ENTITY_ATTRIBUTE, entity, WebRequest.SCOPE_REQUEST);
        criteriaValidator.validateRequest(request);
        AppointmentSearchResponse response = appointmentSearchService.search(request);
        return ResponseEntity.ok(response);
    }
}
