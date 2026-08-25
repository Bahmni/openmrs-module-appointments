package org.openmrs.module.appointments.search.builder;

import org.bahmni.search.builder.JoinResolvers;
import org.bahmni.search.builder.QueryContext;
import org.openmrs.module.appointments.model.Appointment;
import org.openmrs.module.appointments.search.AppointmentSearchConstants;

import javax.persistence.criteria.From;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;

public class AppointmentJoinResolver {

    private static final String JOIN_KEY_SERVICE_ATTRIBUTES = "serviceAttributes";
    private static final String JOIN_KEY_SERVICE_ATTRIBUTE_TYPE = "serviceAttributeType";

    From<?, ?> joinLocation(QueryContext<Appointment> queryContext) {
        return queryContext.joinCache.computeIfAbsent(AppointmentSearchConstants.LOCATION,
                key -> JoinResolvers.findExistingFetchOrJoin(queryContext.root, AppointmentSearchConstants.LOCATION, JoinType.INNER));
    }

    From<?, ?> joinService(QueryContext<Appointment> queryContext) {
        return queryContext.joinCache.computeIfAbsent(AppointmentSearchConstants.SERVICE,
                key -> JoinResolvers.findExistingFetchOrJoin(queryContext.root, AppointmentSearchConstants.SERVICE, JoinType.INNER));
    }

    From<?, ?> joinServiceAttributes(QueryContext<Appointment> queryContext) {
        From<?, ?> serviceJoin = joinService(queryContext);
        return queryContext.joinCache.computeIfAbsent(JOIN_KEY_SERVICE_ATTRIBUTES, key -> {
            Join<?, ?> attributesJoin = serviceJoin.join(AppointmentSearchConstants.ATTRIBUTES, JoinType.INNER);
            queryContext.predicates.add(queryContext.criteriaBuilder.isFalse(attributesJoin.get(AppointmentSearchConstants.VOIDED)));
            return attributesJoin;
        });
    }

    From<?, ?> joinServiceAttributeType(QueryContext<Appointment> queryContext) {
        From<?, ?> attributesJoin = joinServiceAttributes(queryContext);
        return queryContext.joinCache.computeIfAbsent(JOIN_KEY_SERVICE_ATTRIBUTE_TYPE,
                key -> attributesJoin.join(AppointmentSearchConstants.ATTRIBUTE_TYPE, JoinType.INNER));
    }
}
