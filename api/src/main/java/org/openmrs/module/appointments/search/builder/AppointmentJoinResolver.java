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

    /**
     * Join: patient_appointment -> appointment_service -> appointment_service_attribute
     * Filters out voided attributes.
     */
    From<?, ?> joinServiceAttributes(QueryContext<Appointment> queryContext) {
        return queryContext.joinCache.computeIfAbsent(JOIN_KEY_SERVICE_ATTRIBUTES, key -> {
            Join<?, ?> attributesJoin = joinService(queryContext).join(AppointmentSearchConstants.ATTRIBUTES, JoinType.INNER);
            queryContext.predicates.add(queryContext.criteriaBuilder.isFalse(attributesJoin.get(AppointmentSearchConstants.VOIDED)));
            return attributesJoin;
        });
    }

    /**
     * Join: appointment_service_attribute -> appointment_service_attribute_type
     */
    From<?, ?> joinServiceAttributeType(QueryContext<Appointment> queryContext) {
        return queryContext.joinCache.computeIfAbsent(JOIN_KEY_SERVICE_ATTRIBUTE_TYPE,
                key -> joinServiceAttributes(queryContext).join(AppointmentSearchConstants.ATTRIBUTE_TYPE, JoinType.INNER));
    }
}
