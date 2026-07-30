package org.openmrs.module.appointments.search.builder;

import org.bahmni.search.builder.AbstractCriteriaBuilder;
import org.bahmni.search.builder.FieldConfig;
import org.bahmni.search.exceptions.InvalidSearchCriteriaException;
import org.bahmni.search.exceptions.SearchResponseErrorStatus;
import org.bahmni.search.model.FieldType;
import org.bahmni.search.model.SearchCondition;
import org.openmrs.module.appointments.model.Appointment;
import org.openmrs.module.appointments.search.AppointmentSearchFields;
import org.openmrs.module.appointments.search.AppointmentSearchConstants;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AppointmentCriteriaBuilder extends AbstractCriteriaBuilder<Appointment> {

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

    @Override
    @SuppressWarnings("unchecked")
    protected Predicate buildLeafCriterion(CriteriaBuilder cb, Root<Appointment> root,
                                            SearchCondition leaf, Map<String, ?> joinCache) {
        String fieldName = leaf.getField();

        FieldConfig config = fieldRegistry.get(fieldName);
        if (config == null) {
            throw new InvalidSearchCriteriaException(
                    "Unknown search field: '" + fieldName + "'",
                    SearchResponseErrorStatus.BAD_REQUEST);
        }

        validateComparator(fieldName, leaf.getComparator(), config.getFieldType());

        From<?, ?> from = resolveFrom(root, config.getJoinPath(), (Map<String, Join<?, ?>>) joinCache);
        Path<?> fieldPath = from.get(config.getPropertyName());
        return buildPredicate(cb, fieldPath, leaf.getComparator(), leaf.getValue());
    }

    private Map<String, FieldConfig> buildFieldRegistry() {
        Map<String, FieldConfig> registry = new HashMap<>();

        registry.put(AppointmentSearchFields.APPOINTMENT_DATE,
                new FieldConfig(null, AppointmentSearchConstants.START_DATE_TIME, FieldType.DATE));
        registry.put(AppointmentSearchFields.LOCATION,
                new FieldConfig(AppointmentSearchConstants.LOCATION, AppointmentSearchConstants.UUID, FieldType.STRING));
        registry.put(AppointmentSearchFields.SERVICE_TYPE,
                new FieldConfig(AppointmentSearchConstants.SERVICE, AppointmentSearchConstants.UUID, FieldType.STRING));
        registry.put(AppointmentSearchFields.APPOINTMENT_NUMBER,
                new FieldConfig(null, AppointmentSearchConstants.APPOINTMENT_NUMBER, FieldType.STRING));

        return registry;
    }

    private From<?, ?> resolveFrom(Root<Appointment> root, String joinPath,
                                    Map<String, Join<?, ?>> joinCache) {
        if (joinPath == null) {
            return root;
        }
        return joinCache.computeIfAbsent(joinPath, k ->
                root.join(joinPath, JoinType.INNER));
    }
}
