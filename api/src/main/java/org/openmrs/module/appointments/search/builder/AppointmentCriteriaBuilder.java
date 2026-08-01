package org.openmrs.module.appointments.search.builder;

import org.bahmni.search.builder.QueryContext;
import org.bahmni.search.builder.SearchFieldPredicate;
import org.bahmni.search.exceptions.InvalidSearchCriteriaException;
import org.bahmni.search.exceptions.SearchResponseErrorStatus;
import org.bahmni.search.model.ConditionOperator;
import org.bahmni.search.model.FieldComparator;
import org.bahmni.search.model.FieldType;
import org.bahmni.search.model.SearchCondition;
import org.openmrs.module.appointments.model.Appointment;
import org.openmrs.module.appointments.search.AppointmentSearchConstants;
import org.openmrs.module.appointments.search.AppointmentSearchFields;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class AppointmentCriteriaBuilder {

    private static final DateTimeFormatter ISO_DATETIME_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ");

    private final AppointmentJoinResolver joinResolver = new AppointmentJoinResolver();
    private final Map<String, SearchFieldPredicate> fieldRegistry;

    public AppointmentCriteriaBuilder() {
        this.fieldRegistry = Collections.unmodifiableMap(buildFieldRegistry());
    }

    public void apply(QueryContext<Appointment> queryContext, SearchCondition criteria) {
        Predicate predicate = buildCriterion(queryContext, criteria);
        if (predicate != null) {
            queryContext.predicates.add(predicate);
        }
    }

    private Map<String, SearchFieldPredicate> buildFieldRegistry() {
        Map<String, SearchFieldPredicate> registry = new HashMap<>();

        registry.put(AppointmentSearchFields.APPOINTMENT_DATE,
                createFieldPredicate(queryContext -> queryContext.root,
                        AppointmentSearchConstants.START_DATE_TIME, FieldType.DATE));

        registry.put(AppointmentSearchFields.APPOINTMENT_NUMBER,
                createFieldPredicate(queryContext -> queryContext.root,
                        AppointmentSearchConstants.APPOINTMENT_NUMBER, FieldType.STRING));

        registry.put(AppointmentSearchFields.LOCATION,
                createFieldPredicate(joinResolver::joinLocation,
                        AppointmentSearchConstants.UUID, FieldType.STRING));

        registry.put(AppointmentSearchFields.SERVICE_TYPE,
                createFieldPredicate(joinResolver::joinService,
                        AppointmentSearchConstants.UUID, FieldType.STRING));

        registry.put(AppointmentSearchFields.SERVICE_ATTRIBUTE_KIND,
                createFieldPredicate(joinResolver::joinServiceAttributeType,
                        AppointmentSearchConstants.NAME, FieldType.STRING));
        registry.put(AppointmentSearchFields.SERVICE_ATTRIBUTE_VALUE,
                createFieldPredicate(joinResolver::joinServiceAttributes,
                        AppointmentSearchConstants.VALUE_REFERENCE, FieldType.STRING));

        return registry;
    }

    private SearchFieldPredicate createFieldPredicate(Function<QueryContext<Appointment>, From<?, ?>> joinFunction,
                                                     String propertyName, FieldType fieldType) {
        return (queryContext, fieldName, comparator, value, operator) -> {
            validateComparator(fieldName, comparator, fieldType);
            @SuppressWarnings("unchecked")
            Path<?> fieldPath = joinFunction.apply((QueryContext<Appointment>) queryContext).get(propertyName);
            return buildPredicate(queryContext.criteriaBuilder, fieldPath, comparator, value);
        };
    }

    private Predicate buildCriterion(QueryContext<Appointment> queryContext, SearchCondition criteria) {
        if (criteria == null) {
            return null;
        }
        if (criteria.isLeaf()) {
            return buildLeafCriterion(queryContext, criteria);
        }
        return combineChildPredicates(queryContext, criteria);
    }

    private Predicate buildLeafCriterion(QueryContext<Appointment> queryContext, SearchCondition leafCriteria) {
        String fieldName = leafCriteria.getField();
        FieldComparator comparator = leafCriteria.getComparator();

        SearchFieldPredicate fieldPredicate = fieldRegistry.get(fieldName);
        if (fieldPredicate == null) {
            throw new InvalidSearchCriteriaException(
                    "Unknown search field: '" + fieldName + "'",
                    SearchResponseErrorStatus.BAD_REQUEST);
        }

        return fieldPredicate.build(queryContext, fieldName, comparator,
                leafCriteria.getValue(), leafCriteria.getOperator());
    }

    private Predicate combineChildPredicates(QueryContext<Appointment> queryContext, SearchCondition parentCriteria) {
        List<Predicate> childPredicates = new ArrayList<>();
        if (parentCriteria.getConditions() != null) {
            for (SearchCondition childCriteria : parentCriteria.getConditions()) {
                Predicate resolvedPredicate = buildCriterion(queryContext, childCriteria);
                if (resolvedPredicate != null) {
                    childPredicates.add(resolvedPredicate);
                }
            }
        }

        if (childPredicates.isEmpty()) {
            return null;
        }
        if (childPredicates.size() == 1) {
            return childPredicates.get(0);
        }

        Predicate[] predicateArray = childPredicates.toArray(new Predicate[0]);
        return parentCriteria.getOperator() == ConditionOperator.OR
                ? queryContext.criteriaBuilder.or(predicateArray)
                : queryContext.criteriaBuilder.and(predicateArray);
    }

    @SuppressWarnings("unchecked")
    private Predicate buildPredicate(CriteriaBuilder criteriaBuilder, Path<?> fieldPath,
                                     FieldComparator comparator, String value) {
        switch (comparator) {
            case EQ: return criteriaBuilder.equal(fieldPath, value);
            case GT: return criteriaBuilder.greaterThan((Path<Date>) fieldPath, parseDate(value));
            case LT: return criteriaBuilder.lessThan((Path<Date>) fieldPath, parseDate(value));
            default:
                throw new InvalidSearchCriteriaException(
                        "Unsupported comparator: " + comparator,
                        SearchResponseErrorStatus.BAD_REQUEST);
        }
    }

    private void validateComparator(String fieldName, FieldComparator comparator, FieldType fieldType) {
        if (!fieldType.supports(comparator)) {
            throw new InvalidSearchCriteriaException(
                    "Comparator '" + comparator.name().toLowerCase()
                            + "' is not supported for field '" + fieldName
                            + "'. Supported: " + fieldType.getSupportedComparators().toString().toLowerCase(),
                    SearchResponseErrorStatus.BAD_REQUEST);
        }
    }

    private Date parseDate(String dateValue) {
        try {
            return Date.from(OffsetDateTime.parse(dateValue, ISO_DATETIME_FORMAT).toInstant());
        } catch (DateTimeParseException exception) {
            throw new InvalidSearchCriteriaException(
                    "Invalid date format: '" + dateValue
                            + "'. Expected yyyy-MM-dd'T'HH:mm:ss.SSSZ (e.g. 2024-01-01T10:30:00.000+0530)",
                    SearchResponseErrorStatus.BAD_REQUEST);
        }
    }
}
