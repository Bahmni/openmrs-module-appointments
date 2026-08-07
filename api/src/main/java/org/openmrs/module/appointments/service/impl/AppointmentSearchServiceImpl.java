package org.openmrs.module.appointments.service.impl;

import org.openmrs.module.appointments.search.dto.AppointmentSearchResponse;
import org.openmrs.module.appointments.search.dto.AppointmentSearchRequest;

import org.openmrs.module.appointments.search.validation.CriteriaValidator;
import org.openmrs.module.appointments.dao.AppointmentSearchDao;
import org.openmrs.module.appointments.model.Appointment;
import org.openmrs.module.appointments.search.AppointmentSearchConstants;
import org.openmrs.module.appointments.search.builder.AppointmentResponseBuilder;
import org.openmrs.module.appointments.service.AppointmentSearchService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AppointmentSearchServiceImpl implements AppointmentSearchService {

    private static final Logger log = LoggerFactory.getLogger(AppointmentSearchServiceImpl.class);

    private static final String ENTITY = AppointmentSearchConstants.ENTITY_APPOINTMENT;

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
    public AppointmentSearchResponse search(AppointmentSearchRequest request) {
        log.debug("Searching appointments for entity '{}'", request.getEntity());
        validator.validateRequest(request);

        List<Appointment> appointments = appointmentSearchDao.search(request.getCriteria());
        if (appointments.isEmpty()) {
            log.debug("No appointments found for the given criteria");
            return AppointmentSearchResponse.success(ENTITY, new ArrayList<>());
        }

        List<Map<String, Object>> results = new ArrayList<>();
        for (Appointment appointment : appointments) {
            results.add(responseBuilder.mapAppointment(appointment));
        }

        log.debug("Returning {} appointment results", results.size());
        return AppointmentSearchResponse.success(ENTITY, results);
    }
}
