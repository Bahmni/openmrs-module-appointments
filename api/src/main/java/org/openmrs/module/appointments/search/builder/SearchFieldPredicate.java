package org.openmrs.module.appointments.search.builder;

import org.bahmni.search.model.ConditionOperator;
import org.bahmni.search.model.FieldComparator;

import javax.persistence.criteria.Predicate;

@FunctionalInterface
public interface SearchFieldPredicate {
    Predicate build(QueryContext queryContext, String fieldName,
                    FieldComparator comparator, String value,
                    ConditionOperator operator);
}
