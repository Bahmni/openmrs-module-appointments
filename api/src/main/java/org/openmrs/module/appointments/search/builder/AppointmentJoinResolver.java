package org.openmrs.module.appointments.search.builder;

import javax.persistence.criteria.Fetch;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;

public class AppointmentJoinResolver {

    private static final String VOIDED = "voided";

    From<?, ?> joinLocation(QueryContext queryContext) {
        return queryContext.joinCache.computeIfAbsent("location",
                key -> findExistingFetchOrJoin(queryContext.appointmentRoot, "location", JoinType.INNER));
    }

    From<?, ?> joinService(QueryContext queryContext) {
        return queryContext.joinCache.computeIfAbsent("service",
                key -> findExistingFetchOrJoin(queryContext.appointmentRoot, "service", JoinType.INNER));
    }

    From<?, ?> joinPatient(QueryContext queryContext) {
        return queryContext.joinCache.computeIfAbsent("patient",
                key -> findExistingFetchOrJoin(queryContext.appointmentRoot, "patient", JoinType.INNER));
    }

    /**
     * Join: patient_appointment -> appointment_service -> appointment_service_attribute
     * Filters out voided attributes.
     */
    From<?, ?> joinServiceAttributes(QueryContext queryContext) {
        return queryContext.joinCache.computeIfAbsent("serviceAttributes", key -> {
            Join<?, ?> attributesJoin = joinService(queryContext).join("attributes", JoinType.INNER);
            queryContext.predicates.add(queryContext.criteriaBuilder.isFalse(attributesJoin.get(VOIDED)));
            return attributesJoin;
        });
    }

    /**
     * Join: appointment_service_attribute -> appointment_service_attribute_type
     */
    From<?, ?> joinServiceAttributeType(QueryContext queryContext) {
        return queryContext.joinCache.computeIfAbsent("serviceAttributeType",
                key -> joinServiceAttributes(queryContext).join("attributeType", JoinType.INNER));
    }

    @SuppressWarnings("unchecked")
    private From<?, ?> findExistingFetchOrJoin(From<?, ?> parent, String attributeName, JoinType joinType) {
        for (Fetch<?, ?> fetch : parent.getFetches()) {
            if (attributeName.equals(fetch.getAttribute().getName())) {
                return (From<?, ?>) fetch;
            }
        }
        return parent.join(attributeName, joinType);
    }
}
