package org.openmrs.module.appointments.search.validation;

import org.bahmni.search.exceptions.InvalidSearchCriteriaException;
import org.bahmni.search.exceptions.SearchResponseErrorStatus;
import org.bahmni.search.model.ConditionOperator;
import org.bahmni.search.model.FieldComparator;
import org.bahmni.search.model.SearchCondition;
import org.openmrs.module.appointments.search.dto.AppointmentSearchRequest;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

public class CriteriaValidator {

    private static final String LEAF_CONDITION_FIELD_PREFIX = "Leaf condition for field '";

    private static final Set<FieldComparator> SUPPORTED_COMPARATORS =
            EnumSet.of(FieldComparator.EQ, FieldComparator.GT, FieldComparator.LT);

    private static final Set<ConditionOperator> SUPPORTED_OPERATORS =
            EnumSet.of(ConditionOperator.AND, ConditionOperator.OR);


    public void validateRequest(AppointmentSearchRequest request) {
        if (request.getCriteria() == null) {
            throw new InvalidSearchCriteriaException("Request must include 'criteria'",
                    SearchResponseErrorStatus.BAD_REQUEST);
        }
        List<String> errors = validateCondition(request.getCriteria());
        if (!errors.isEmpty()) {
            throw new InvalidSearchCriteriaException(errors, SearchResponseErrorStatus.BAD_REQUEST);
        }
    }

    public void validateEntity(String entity, String supportedEntity) {
        if (entity == null || entity.isEmpty()) {
            throw new InvalidSearchCriteriaException(
                    "Request must include 'entity'", SearchResponseErrorStatus.BAD_REQUEST);
        }
        if (!supportedEntity.equalsIgnoreCase(entity)) {
            throw new InvalidSearchCriteriaException(
                    "Entity '" + entity + "' is not supported. Supported entities: ["
                            + supportedEntity + "]",
                    SearchResponseErrorStatus.BAD_REQUEST);
        }
    }


    private List<String> validateCondition(SearchCondition condition) {
        if (condition.isLeaf()) {
            return validateLeaf(condition);
        } else if (condition.isGroup()) {
            return validateGroup(condition);
        }
        return Collections.singletonList(
                "Each condition must be either a leaf {field, comparator, value} or a group {operator, conditions}");
    }

    private List<String> validateLeaf(SearchCondition leaf) {
        List<String> errors = new ArrayList<>();
        if (leaf.getComparator() == null) {
            errors.add(LEAF_CONDITION_FIELD_PREFIX + leaf.getField()
                    + "' is missing 'comparator'. Supported: eq, gt, lt");
        } else if (!SUPPORTED_COMPARATORS.contains(leaf.getComparator())) {
            errors.add(LEAF_CONDITION_FIELD_PREFIX + leaf.getField()
                    + "' has unsupported 'comparator': '" + leaf.getComparator()
                    + "'. Supported: eq, gt, lt");
        }
        if (leaf.getValue() == null || leaf.getValue().isEmpty()) {
            errors.add(LEAF_CONDITION_FIELD_PREFIX + leaf.getField() + "' is missing 'value'");
        }
        return errors;
    }

    private List<String> validateGroup(SearchCondition group) {
        if (group.getConditions() == null || group.getConditions().isEmpty()) {
            return Collections.singletonList(
                    "A group condition must have at least one condition in 'conditions'");
        }
        List<String> errors = new ArrayList<>();
        if (group.getOperator() == null) {
            errors.add("Group condition is missing 'operator'. Supported: AND, OR");
        } else if (!SUPPORTED_OPERATORS.contains(group.getOperator())) {
            errors.add("Group condition has unsupported 'operator': '" + group.getOperator()
                    + "'. Supported: AND, OR");
        }
        for (SearchCondition child : group.getConditions()) {
            errors.addAll(validateCondition(child));
        }
        return errors;
    }

}
