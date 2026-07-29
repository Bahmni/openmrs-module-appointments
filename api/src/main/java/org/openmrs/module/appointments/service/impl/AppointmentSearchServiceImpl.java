package org.openmrs.module.appointments.service.impl;

import org.bahmni.search.model.ContextSearchResponse;
import org.bahmni.search.model.DefaultSearchResponse;
import org.bahmni.search.model.SearchRequest;
import org.bahmni.search.service.SearchService;
import org.bahmni.search.validation.CriteriaValidator;
import org.openmrs.module.appointments.dao.AppointmentSearchDao;
import org.openmrs.module.appointments.model.Appointment;
import org.openmrs.module.appointments.search.builder.AppointmentResponseBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AppointmentSearchServiceImpl implements SearchService {

    private static final Logger log = LoggerFactory.getLogger(AppointmentSearchServiceImpl.class);

    private static final String ENTITY = "appointment";

    private final AppointmentSearchDao appointmentSearchDao;
    private final CriteriaValidator validator;
    private final AppointmentResponseBuilder responseBuilder;

    public AppointmentSearchServiceImpl(AppointmentSearchDao appointmentSearchDao,
                                        CriteriaValidator validator,
                                        AppointmentResponseBuilder responseBuilder) {
        this.appointmentSearchDao = appointmentSearchDao;
        this.validator = validator;
        this.responseBuilder = responseBuilder;
    }

    @Override
    public String getEntity() {
        return ENTITY;
    }

    @Override
    public ContextSearchResponse search(SearchRequest request) {
        log.debug("Searching appointments for entity '{}'", request.getEntity());
        validator.validateRequest(request);

        List<Appointment> appointments = appointmentSearchDao.search(request.getCriteria());
        if (appointments.isEmpty()) {
            log.debug("No appointments found for the given criteria");
            return new DefaultSearchResponse(ENTITY, new ArrayList<>());
        }

        List<Map<String, Object>> results = new ArrayList<>();
        for (Appointment appointment : appointments) {
            results.add(responseBuilder.mapAppointment(appointment));
        }

        log.debug("Returning {} appointment results", results.size());
        return new DefaultSearchResponse(ENTITY, results);
    }
}
