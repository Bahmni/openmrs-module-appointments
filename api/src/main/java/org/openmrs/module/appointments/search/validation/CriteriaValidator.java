package org.openmrs.module.appointments.search.validation;

import org.bahmni.search.exceptions.InvalidSearchCriteriaException;
import org.bahmni.search.exceptions.SearchResponseErrorStatus;
import org.bahmni.search.model.SearchCondition;
import org.bahmni.search.model.SearchRequest;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CriteriaValidator {

    public void validateRequest(SearchRequest request) {
        if (request.getCriteria() == null) {
            throw new InvalidSearchCriteriaException("Request must include 'criteria'", SearchResponseErrorStatus.BAD_REQUEST);
        }
        List<String> errors = validateCondition(request.getCriteria());
        if (!errors.isEmpty()) {
            throw new InvalidSearchCriteriaException(errors, SearchResponseErrorStatus.BAD_REQUEST);
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
            errors.add("Leaf condition for field '" + leaf.getField() + "' is missing 'comparator'. Supported: eq, gt, lt");
        }
        if (leaf.getValue() == null || leaf.getValue().isEmpty()) {
            errors.add("Leaf condition for field '" + leaf.getField() + "' is missing 'value'");
        }
        return errors;
    }

    private List<String> validateGroup(SearchCondition group) {
        if (group.getConditions() == null || group.getConditions().isEmpty()) {
            return Collections.singletonList("A group condition must have at least one condition in 'conditions'");
        }
        List<String> errors = new ArrayList<>();
        for (SearchCondition child : group.getConditions()) {
            errors.addAll(validateCondition(child));
        }
        return errors;
    }
}
