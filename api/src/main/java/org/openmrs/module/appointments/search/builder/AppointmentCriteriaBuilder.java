package org.openmrs.module.appointments.search.builder;

import org.bahmni.search.exceptions.InvalidSearchCriteriaException;
import org.bahmni.search.exceptions.SearchResponseErrorStatus;
import org.bahmni.search.model.ConditionOperator;
import org.bahmni.search.model.FieldComparator;
import org.bahmni.search.model.FieldType;
import org.bahmni.search.model.SearchCondition;
import org.openmrs.module.appointments.model.Appointment;
import org.openmrs.module.appointments.search.AppointmentSearchFields;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AppointmentCriteriaBuilder {

    private static final DateTimeFormatter ISO_DATETIME_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ");

    private final Map<String, FieldConfig> fieldRegistry;

    public AppointmentCriteriaBuilder() {
        this.fieldRegistry = Collections.unmodifiableMap(buildFieldRegistry());
    }

    public void apply(CriteriaBuilder cb, Root<Appointment> root,
                      SearchCondition criteria, List<Predicate> predicates,
                      Map<String, Join<?, ?>> joinCache) {
        Predicate predicate = buildCriterion(cb, root, criteria, joinCache);
        if (predicate != null) {
            predicates.add(predicate);
        }
    }

    private Map<String, FieldConfig> buildFieldRegistry() {
        Map<String, FieldConfig> registry = new HashMap<>();

        registry.put(AppointmentSearchFields.APPOINTMENT_DATE,
                new FieldConfig(null, "startDateTime", FieldType.DATE));
        registry.put(AppointmentSearchFields.LOCATION,
                new FieldConfig("location", "uuid", FieldType.STRING));
        registry.put(AppointmentSearchFields.SERVICE_TYPE,
                new FieldConfig("service", "uuid", FieldType.STRING));
        registry.put(AppointmentSearchFields.APPOINTMENT_NUMBER,
                new FieldConfig(null, "appointmentNumber", FieldType.STRING));

        return registry;
    }

    private Predicate buildCriterion(CriteriaBuilder cb, Root<Appointment> root,
                                     SearchCondition condition, Map<String, Join<?, ?>> joinCache) {
        if (condition == null) {
            return null;
        }
        if (condition.isLeaf()) {
            return buildLeafCriterion(cb, root, condition, joinCache);
        }
        return combineChildPredicates(cb, root, condition, joinCache);
    }

    private Predicate buildLeafCriterion(CriteriaBuilder cb, Root<Appointment> root,
                                          SearchCondition leaf, Map<String, Join<?, ?>> joinCache) {
        String fieldName = leaf.getField();
        FieldComparator comparator = leaf.getComparator();

        FieldConfig config = fieldRegistry.get(fieldName);
        if (config == null) {
            throw new InvalidSearchCriteriaException(
                    "Unknown search field: '" + fieldName + "'",
                    SearchResponseErrorStatus.BAD_REQUEST);
        }

        validateComparator(fieldName, comparator, config.fieldType);

        From<?, ?> from = resolveFrom(root, config.joinPath, joinCache);
        Path<?> fieldPath = from.get(config.propertyName);
        return buildPredicate(cb, fieldPath, comparator, leaf.getValue());
    }

    private From<?, ?> resolveFrom(Root<Appointment> root, String joinPath,
                                    Map<String, Join<?, ?>> joinCache) {
        if (joinPath == null) {
            return root;
        }
        return joinCache.computeIfAbsent(joinPath, k ->
                root.join(joinPath, JoinType.INNER));
    }

    private Predicate combineChildPredicates(CriteriaBuilder cb, Root<Appointment> root,
                                              SearchCondition parent, Map<String, Join<?, ?>> joinCache) {
        List<Predicate> childPredicates = new ArrayList<>();
        if (parent.getConditions() != null) {
            for (SearchCondition child : parent.getConditions()) {
                Predicate p = buildCriterion(cb, root, child, joinCache);
                if (p != null) {
                    childPredicates.add(p);
                }
            }
        }

        if (childPredicates.isEmpty()) {
            return null;
        }
        if (childPredicates.size() == 1) {
            return childPredicates.get(0);
        }

        Predicate[] array = childPredicates.toArray(new Predicate[0]);
        return parent.getOperator() == ConditionOperator.OR
                ? cb.or(array)
                : cb.and(array);
    }

    @SuppressWarnings("unchecked")
    private Predicate buildPredicate(CriteriaBuilder cb, Path<?> fieldPath,
                                     FieldComparator comparator, String value) {
        switch (comparator) {
            case EQ:
                return cb.equal(fieldPath, value);
            case GT:
                return cb.greaterThan((Path<Date>) fieldPath, parseDate(value));
            case LT:
                return cb.lessThan((Path<Date>) fieldPath, parseDate(value));
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

    private static class FieldConfig {
        final String joinPath;
        final String propertyName;
        final FieldType fieldType;

        FieldConfig(String joinPath, String propertyName, FieldType fieldType) {
            this.joinPath = joinPath;
            this.propertyName = propertyName;
            this.fieldType = fieldType;
        }
    }
}
