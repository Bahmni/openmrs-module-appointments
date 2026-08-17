package org.openmrs.module.appointments.service.impl;

import org.openmrs.module.appointments.search.dto.AppointmentSearchResponse;
import org.openmrs.module.appointments.search.dto.AppointmentSearchRequest;
import org.openmrs.module.appointments.search.dto.SearchResponseMeta;

import org.openmrs.module.appointments.search.validation.CriteriaValidator;
import org.openmrs.module.appointments.dao.AppointmentSearchDao;
import org.openmrs.module.appointments.model.Appointment;
import org.openmrs.module.appointments.search.AppointmentSearchConstants;
import org.openmrs.module.appointments.search.builder.AppointmentResponseBuilder;
import org.openmrs.module.appointments.service.AppointmentSearchService;
import org.bahmni.search.model.PaginationRequest;
import org.bahmni.search.model.PaginationResponse;
import org.bahmni.search.model.SearchRequestMeta;
import org.bahmni.search.pagination.PaginationHelper;
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

        SearchRequestMeta meta = request.getMeta();
        PaginationRequest pagination = PaginationHelper.resolvePagination(meta);
        int effectiveLimit = PaginationHelper.resolveEffectiveLimit(pagination.getLimit());
        String sortOrder = PaginationHelper.resolveSortOrder(pagination.getSortOrder());
        String direction = pagination.getDirection();
        Long cursorId = PaginationHelper.decodeCursor(pagination.getCursor());
        boolean isPrev = PaginationHelper.isPrevDirection(direction);

        int fetchSize = effectiveLimit + 1;
        List<Appointment> rawAppointments = appointmentSearchDao.search(
                request.getCriteria(), cursorId, sortOrder, direction, fetchSize);

        boolean hasMore = PaginationHelper.hasMore(rawAppointments.size(), effectiveLimit);
        List<Appointment> appointments = PaginationHelper.trimAndOrient(rawAppointments, effectiveLimit, isPrev);

        List<Map<String, Object>> results = new ArrayList<>();
        for (Appointment appointment : appointments) {
            results.add(responseBuilder.mapAppointment(appointment));
        }

        PaginationResponse paginationResponse = appointments.isEmpty()
                ? PaginationHelper.emptyPaginationResponse()
                : PaginationHelper.buildPaginationResponse(
                        appointments.get(0).getAppointmentId(),
                        appointments.get(appointments.size() - 1).getAppointmentId(),
                        hasMore, cursorId, isPrev);

        Long totalCount = PaginationHelper.resolveTotalCount(meta,
                () -> appointmentSearchDao.count(request.getCriteria()));

        SearchResponseMeta responseMeta = new SearchResponseMeta(paginationResponse, totalCount);
        log.debug("Returning {} appointment results", results.size());
        return AppointmentSearchResponse.success(ENTITY, results, responseMeta);
    }
}
