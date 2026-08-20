package org.openmrs.module.appointments.service.impl;

import org.openmrs.module.appointments.search.dto.AppointmentSearchResponse;
import org.openmrs.module.appointments.search.dto.AppointmentSearchRequest;

import org.openmrs.module.appointments.search.validation.CriteriaValidator;
import org.openmrs.module.appointments.dao.AppointmentSearchDao;
import org.openmrs.module.appointments.model.Appointment;
import org.openmrs.module.appointments.search.AppointmentSearchConstants;
import org.openmrs.module.appointments.search.builder.AppointmentResponseBuilder;
import org.openmrs.module.appointments.service.AppointmentSearchService;
import org.bahmni.search.model.SearchRequestMeta;
import org.bahmni.search.model.SearchResponseMeta;
import org.bahmni.search.pagination.PageResult;
import org.bahmni.search.pagination.PaginationHelper;
import org.bahmni.search.pagination.ResolvedPagination;
import org.openmrs.api.context.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AppointmentSearchServiceImpl implements AppointmentSearchService {

    private static final Logger log = LoggerFactory.getLogger(AppointmentSearchServiceImpl.class);

    private static final String ENTITY = AppointmentSearchConstants.ENTITY_APPOINTMENT;

    private static final String GP_PAGINATION_DEFAULT_LIMIT = "bahmni.appointmentSearch.pagination.defaultLimit";
    private static final String GP_PAGINATION_MAX_LIMIT = "bahmni.appointmentSearch.pagination.maxLimit";
    private static final int FALLBACK_DEFAULT_LIMIT = 100;
    private static final int FALLBACK_MAX_LIMIT = 500;


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
        int defaultLimit = PaginationHelper.resolveGlobalProperty(
                Context.getAdministrationService().getGlobalProperty(GP_PAGINATION_DEFAULT_LIMIT), FALLBACK_DEFAULT_LIMIT, GP_PAGINATION_DEFAULT_LIMIT);
        int maxLimit = PaginationHelper.resolveGlobalProperty(
                Context.getAdministrationService().getGlobalProperty(GP_PAGINATION_MAX_LIMIT), FALLBACK_MAX_LIMIT, GP_PAGINATION_MAX_LIMIT);

        ResolvedPagination resolved = PaginationHelper.resolvePaginationContext(meta, ENTITY, defaultLimit, maxLimit);

        List<Integer> matchingIds = appointmentSearchDao.findMatchingIds(
                request.getCriteria(), resolved.getCursorId(), resolved.getSortOrder(),
                resolved.getDirection(), resolved.getFetchSize());

        List<Appointment> rawAppointments = appointmentSearchDao.findByIds(matchingIds);

        PageResult<Appointment> pageResult = PaginationHelper.paginate(
                ENTITY, rawAppointments, appointment -> appointment.getAppointmentId().longValue(), resolved);

        List<Map<String, Object>> results = new ArrayList<>();

        for (Appointment appointment : pageResult.getItems()) {
            results.add(responseBuilder.mapAppointment(appointment));
        }

        Long totalCount = PaginationHelper.resolveTotalCount(meta,
                () -> appointmentSearchDao.count(request.getCriteria()));

        SearchResponseMeta responseMeta = new SearchResponseMeta(pageResult.getPaginationResponse(), totalCount);
        log.debug("Returning {} appointment results", results.size());
        return AppointmentSearchResponse.success(ENTITY, results, responseMeta);
    }

}


