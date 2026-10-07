/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at https://www.bahmni.org/license/mplv2hd.
 * Copyright (C) 2026 OpenMRS Inc.
 */

package org.openmrs.module.appointments.search.validation;

import org.bahmni.search.exceptions.InvalidSearchCriteriaException;
import org.bahmni.search.exceptions.SearchResponseErrorStatus;
import org.bahmni.search.model.SearchCondition;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.module.appointments.search.dto.AppointmentSearchRequest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;


import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class CriteriaValidatorTest {

    private CriteriaValidator criteriaValidator;
    private static final List<String> SUPPORTED_COMPARATORS_UNDER_TEST = Arrays.asList("eq", "gt", "lt", "ge", "le");

    @Before
    public void setUp() {
        criteriaValidator = new CriteriaValidator();
    }

    private SearchCondition leaf(String field, String comparator, String value) {
        SearchCondition condition = new SearchCondition();
        condition.setField(field);
        if (comparator != null) {
            condition.setComparator(comparator);
        }
        condition.setValue(value);
        return condition;
    }

    private SearchCondition group(String operator, SearchCondition... children) {
        SearchCondition condition = new SearchCondition();
        if (operator != null) {
            condition.setOperator(operator);
        }
        condition.setConditions(new ArrayList<>(Arrays.asList(children)));
        return condition;
    }

    @Test
    public void shouldNotThrowForValidLeafConditionForEachSupportedComparator() {
        for (String comparator : SUPPORTED_COMPARATORS_UNDER_TEST) {
            AppointmentSearchRequest request = new AppointmentSearchRequest();
            request.setCriteria(leaf("startDateTime", comparator, "2024-01-01T00:00:00Z"));

            try {
                criteriaValidator.validateRequest(request);
            } catch (InvalidSearchCriteriaException e) {
                fail("Did not expect an exception for supported comparator '" + comparator
                        + "' but got: " + e.getMessages());
            }
        }
    }

    @Test
    public void shouldThrowWhenCriteriaIsNull() {

        AppointmentSearchRequest request = new AppointmentSearchRequest();
        request.setCriteria(null);

        try {
            criteriaValidator.validateRequest(request);
            fail("Expected InvalidSearchCriteriaException");
        } catch (InvalidSearchCriteriaException e) {
            assertEquals(SearchResponseErrorStatus.BAD_REQUEST, e.getStatus());
            assertThat(e.getMessages(), hasItem("Request must include 'criteria'"));
        }
    }

    @Test
    public void shouldNotThrowForValidLeafCondition() {
        AppointmentSearchRequest request = new AppointmentSearchRequest();
        request.setCriteria(leaf("status", "eq", "Scheduled"));

        criteriaValidator.validateRequest(request);
        // no exception expected
    }

    @Test
    public void shouldThrowWhenLeafIsMissingComparator() {
        AppointmentSearchRequest request = new AppointmentSearchRequest();
        request.setCriteria(leaf("status", null, "Scheduled"));

        try {
            criteriaValidator.validateRequest(request);
            fail("Expected InvalidSearchCriteriaException");
        } catch (InvalidSearchCriteriaException e) {
            assertThat(e.getMessages(), hasItem(
                    "Leaf condition for field 'status' is missing 'comparator'. Supported: eq, gt, lt, ge, le"));
        }
    }


    @Test
    public void shouldThrowWhenLeafIsMissingValue() {
        AppointmentSearchRequest request = new AppointmentSearchRequest();
        request.setCriteria(leaf("status", "eq", null));

        try {
            criteriaValidator.validateRequest(request);
            fail("Expected InvalidSearchCriteriaException");
        } catch (InvalidSearchCriteriaException e) {
            assertThat(e.getMessages(), hasItem("Leaf condition for field 'status' is missing 'value'"));
        }
    }

    @Test
    public void shouldThrowWhenLeafValueIsEmpty() {
        AppointmentSearchRequest request = new AppointmentSearchRequest();
        request.setCriteria(leaf("status", "eq", ""));

        try {
            criteriaValidator.validateRequest(request);
            fail("Expected InvalidSearchCriteriaException");
        } catch (InvalidSearchCriteriaException e) {
            assertThat(e.getMessages(), hasItem("Leaf condition for field 'status' is missing 'value'"));
        }
    }


    @Test
    public void shouldReportAllErrorsWhenLeafIsMissingComparatorAndValue() {
        AppointmentSearchRequest request = new AppointmentSearchRequest();
        request.setCriteria(leaf("status", null, null));

        try {
            criteriaValidator.validateRequest(request);
            fail("Expected InvalidSearchCriteriaException");
        } catch (InvalidSearchCriteriaException e) {
            assertEquals(2, e.getMessages().size());
            assertThat(e.getMessages(), hasItem(
                    "Leaf condition for field 'status' is missing 'comparator'. Supported: eq, gt, lt, ge, le"));
            assertThat(e.getMessages(), hasItem("Leaf condition for field 'status' is missing 'value'"));
        }
    }

    @Test
    public void shouldThrowForUnsupportedComparatorViaSetComparatorString() {
        try {
            leaf("status", "invalidComparator", "Scheduled");
            fail("Expected InvalidSearchCriteriaException when resolving unsupported comparator");
        } catch (InvalidSearchCriteriaException e) {
            assertEquals(SearchResponseErrorStatus.BAD_REQUEST, e.getStatus());
        }
    }

    @Test
    public void shouldThrowWhenGroupIsMissingOperator() {
        AppointmentSearchRequest request = new AppointmentSearchRequest();
        request.setCriteria(group(null, leaf("status", "eq", "Scheduled")));

        try {
            criteriaValidator.validateRequest(request);
            fail("Expected InvalidSearchCriteriaException");
        } catch (InvalidSearchCriteriaException e) {
            assertThat(e.getMessages(), hasItem("Group condition is missing 'operator'. Supported: AND, OR"));
        }
    }

    @Test
    public void shouldThrowWhenGroupHasEmptyConditionsList() {
        SearchCondition emptyGroup = new SearchCondition();
        emptyGroup.setOperator("AND");
        emptyGroup.setConditions(Collections.emptyList());

        AppointmentSearchRequest request = new AppointmentSearchRequest();
        request.setCriteria(emptyGroup);

        try {
            criteriaValidator.validateRequest(request);
            fail("Expected InvalidSearchCriteriaException");
        } catch (InvalidSearchCriteriaException e) {
            assertThat(e.getMessages(), hasItem(
                    "Each condition must be either a leaf {field, comparator, value} or a group {operator, conditions}"));
        }
    }

    @Test
    public void shouldNotThrowForValidGroupWithAndOperator() {
        AppointmentSearchRequest request = new AppointmentSearchRequest();
        request.setCriteria(group("AND",
                leaf("status", "eq", "Scheduled"),
                leaf("location", "eq", "OPD")));

        criteriaValidator.validateRequest(request);
    }

    @Test
    public void shouldNotThrowForValidGroupWithOrOperator() {
        AppointmentSearchRequest request = new AppointmentSearchRequest();
        request.setCriteria(group("OR",
                leaf("status", "eq", "Scheduled"),
                leaf("status", "eq", "Completed")));

        criteriaValidator.validateRequest(request);
    }

    @Test
    public void shouldAggregateErrorsFromNestedGroupsAndLeaves() {
        SearchCondition invalidLeaf1 = leaf("status", null, null);
        SearchCondition invalidLeaf2 = leaf("location", "eq", null);
        SearchCondition nestedGroup = group("OR", invalidLeaf2);
        SearchCondition topGroup = group("AND", invalidLeaf1, nestedGroup);

        AppointmentSearchRequest request = new AppointmentSearchRequest();
        request.setCriteria(topGroup);

        try {
            criteriaValidator.validateRequest(request);
            fail("Expected InvalidSearchCriteriaException");
        } catch (InvalidSearchCriteriaException e) {
            assertThat(e.getMessages(), hasItem(
                    "Leaf condition for field 'status' is missing 'comparator'. Supported: eq, gt, lt, ge, le"));
            assertThat(e.getMessages(), hasItem("Leaf condition for field 'status' is missing 'value'"));
            assertThat(e.getMessages(), hasItem("Leaf condition for field 'location' is missing 'value'"));
        }
    }

    @Test
    public void shouldThrowWhenEntityIsNull() {
        try {
            criteriaValidator.validateEntity(null, "appointment");
            fail("Expected InvalidSearchCriteriaException");
        } catch (InvalidSearchCriteriaException e) {
            assertEquals(SearchResponseErrorStatus.BAD_REQUEST, e.getStatus());
            assertThat(e.getMessages(), hasItem("Request must include 'entity'"));
        }
    }

    @Test
    public void shouldThrowWhenEntityIsEmpty() {
        try {
            criteriaValidator.validateEntity("", "appointment");
            fail("Expected InvalidSearchCriteriaException");
        } catch (InvalidSearchCriteriaException e) {
            assertThat(e.getMessages(), hasItem("Request must include 'entity'"));
        }
    }

    @Test
    public void shouldThrowWhenEntityIsNotSupported() {
        try {
            criteriaValidator.validateEntity("patient", "appointment");
            fail("Expected InvalidSearchCriteriaException");
        } catch (InvalidSearchCriteriaException e) {
            assertThat(e.getMessages(), hasItem(
                    "Entity 'patient' is not supported. Supported entities: [appointment]"));
        }
    }

    @Test
    public void shouldNotThrowWhenEntityMatchesSupportedEntity() {
        criteriaValidator.validateEntity("appointment", "appointment");
    }

    @Test
    public void shouldNotThrowWhenEntityMatchesSupportedEntityIgnoringCase() {
        criteriaValidator.validateEntity("APPOINTMENT", "appointment");
    }
}
