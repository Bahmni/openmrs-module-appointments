package org.openmrs.module.appointments.search.builder;

import org.bahmni.search.builder.QueryContext;
import org.bahmni.search.exceptions.InvalidSearchCriteriaException;
import org.bahmni.search.exceptions.SearchResponseErrorStatus;
import org.bahmni.search.model.SearchCondition;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openmrs.module.appointments.model.Appointment;
import org.openmrs.module.appointments.search.AppointmentSearchConstants;
import org.openmrs.module.appointments.search.AppointmentSearchFields;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link AppointmentCriteriaBuilder} (which in turn exercises
 * {@link AppointmentJoinResolver} since it is used internally to build joins).
 */
@RunWith(MockitoJUnitRunner.Silent.class)
public class AppointmentCriteriaBuilderTest {

    @Mock
    private CriteriaBuilder criteriaBuilder;

    @Mock
    private Root<Appointment> root;

    @Mock
    private Join<Object, Object> locationJoin;

    @Mock
    private Join<Object, Object> serviceJoin;

    @Mock
    private Join<Object, Object> attributesJoin;

    @Mock
    private Join<Object, Object> attributeTypeJoin;

    @Mock
    private Path<Date> startDateTimePath;

    @Mock
    private Path<String> appointmentNumberPath;

    @Mock
    private Path<String> locationUuidPath;

    @Mock
    private Path<String> serviceUuidPath;

    @Mock
    private Path<String> attributeTypeNamePath;

    @Mock
    private Path<String> attributeValueReferencePath;

    @Mock
    private Path<Boolean> voidedPath;

    @Mock
    private Predicate voidedPredicate;

    @Mock
    private Predicate dateGtPredicate;

    @Mock
    private Predicate dateLtPredicate;

    @Mock
    private Predicate appointmentNumberPredicate;

    @Mock
    private Predicate locationPredicate;

    @Mock
    private Predicate servicePredicate;

    @Mock
    private Predicate kindPredicate;

    @Mock
    private Predicate valuePredicate;

    @Mock
    private Predicate combinedAndPredicate;

    @Mock
    private Predicate combinedOrPredicate;

    private AppointmentCriteriaBuilder appointmentCriteriaBuilder;
    private List<Predicate> predicates;
    private QueryContext<Appointment> queryContext;

    private static final String LOCATION_UUID = "loc-uuid-1234";
    private static final String SERVICE_TYPE_UUID = "svc-uuid-5678";
    private static final String APPOINTMENT_NUMBER_VALUE = "APT-001";
    private static final String KIND_VALUE = "Servicing Country";
    private static final String VALUE_REFERENCE_VALUE = "India";
    private static final String VALID_DATE = "2024-01-01T10:30:00.000+0530";

    @Before
    public void setUp() {
        appointmentCriteriaBuilder = new AppointmentCriteriaBuilder();
        predicates = new ArrayList<>();
        queryContext = new QueryContext<>(criteriaBuilder, root, predicates);

        // appointment date field
        doReturn(startDateTimePath).when(root).get(AppointmentSearchConstants.START_DATE_TIME);

        // appointment number field
        doReturn(appointmentNumberPath).when(root).get(AppointmentSearchConstants.APPOINTMENT_NUMBER);
        when(criteriaBuilder.equal(appointmentNumberPath, APPOINTMENT_NUMBER_VALUE)).thenReturn(appointmentNumberPredicate);

        // location join
        doReturn(locationJoin).when(root).join(eq(AppointmentSearchConstants.LOCATION), any(JoinType.class));
        doReturn(locationUuidPath).when(locationJoin).get(AppointmentSearchConstants.UUID);
        when(criteriaBuilder.equal(locationUuidPath, LOCATION_UUID)).thenReturn(locationPredicate);

        // service join
        doReturn(serviceJoin).when(root).join(eq(AppointmentSearchConstants.SERVICE), any(JoinType.class));
        doReturn(serviceUuidPath).when(serviceJoin).get(AppointmentSearchConstants.UUID);
        when(criteriaBuilder.equal(serviceUuidPath, SERVICE_TYPE_UUID)).thenReturn(servicePredicate);

        // service -> attributes join (voided excluded on creation)
        doReturn(attributesJoin).when(serviceJoin).join(eq(AppointmentSearchConstants.ATTRIBUTES), any(JoinType.class));
        doReturn(voidedPath).when(attributesJoin).get(AppointmentSearchConstants.VOIDED);
        when(criteriaBuilder.isFalse(voidedPath)).thenReturn(voidedPredicate);

        // attributes -> attributeType join
        doReturn(attributeTypeJoin).when(attributesJoin).join(eq(AppointmentSearchConstants.ATTRIBUTE_TYPE), any(JoinType.class));
        doReturn(attributeTypeNamePath).when(attributeTypeJoin).get(AppointmentSearchConstants.NAME);
        when(criteriaBuilder.equal(attributeTypeNamePath, KIND_VALUE)).thenReturn(kindPredicate);

        // attributes valueReference
        doReturn(attributeValueReferencePath).when(attributesJoin).get(AppointmentSearchConstants.VALUE_REFERENCE);
        when(criteriaBuilder.equal(attributeValueReferencePath, VALUE_REFERENCE_VALUE)).thenReturn(valuePredicate);

        // Production combines these two leaf predicates via the Predicate[] varargs
        // overload of and(...), not the 2-arg Expression<Boolean> overload.
        when(criteriaBuilder.and(new Predicate[] { kindPredicate, valuePredicate })).thenReturn(combinedAndPredicate);
    }

    // ---------- Leaf criterion: unknown field / unsupported comparator ----------

    @Test
    public void shouldThrowExceptionForUnknownField() {
        SearchCondition condition = leaf("unknown.field", "eq", "someValue");

        try {
            appointmentCriteriaBuilder.apply(queryContext, condition);
            fail("Expected InvalidSearchCriteriaException to be thrown");
        } catch (InvalidSearchCriteriaException e) {
            assertThat(e.getStatus(), is(SearchResponseErrorStatus.BAD_REQUEST));
            assertThat(e.getMessage(), is("Unknown search field: 'unknown.field'"));
        }
    }

    @Test
    public void shouldThrowExceptionWhenComparatorNotSupportedForStringField() {
        SearchCondition condition = leaf(AppointmentSearchFields.LOCATION, "gt", LOCATION_UUID);

        try {
            appointmentCriteriaBuilder.apply(queryContext, condition);
            fail("Expected InvalidSearchCriteriaException to be thrown");
        } catch (InvalidSearchCriteriaException e) {
            assertThat(e.getStatus(), is(SearchResponseErrorStatus.BAD_REQUEST));
        }
    }

    @Test
    public void shouldThrowExceptionWhenComparatorNotSupportedForDateField() {
        SearchCondition condition = leaf(AppointmentSearchFields.APPOINTMENT_DATE, "eq", VALID_DATE);

        try {
            appointmentCriteriaBuilder.apply(queryContext, condition);
            fail("Expected InvalidSearchCriteriaException to be thrown");
        } catch (InvalidSearchCriteriaException e) {
            assertThat(e.getStatus(), is(SearchResponseErrorStatus.BAD_REQUEST));
        }
    }

    // ---------- Appointment date (GT / LT) ----------

    @Test
    public void shouldBuildGreaterThanPredicateForAppointmentDate() {
        when(criteriaBuilder.greaterThan(eq(startDateTimePath), any(Date.class))).thenReturn(dateGtPredicate);
        SearchCondition condition = leaf(AppointmentSearchFields.APPOINTMENT_DATE, "gt", VALID_DATE);

        appointmentCriteriaBuilder.apply(queryContext, condition);

        assertThat(predicates, hasItem(dateGtPredicate));
    }

    @Test
    public void shouldBuildLessThanPredicateForAppointmentDate() {
        when(criteriaBuilder.lessThan(eq(startDateTimePath), any(Date.class))).thenReturn(dateLtPredicate);
        SearchCondition condition = leaf(AppointmentSearchFields.APPOINTMENT_DATE, "lt", VALID_DATE);

        appointmentCriteriaBuilder.apply(queryContext, condition);

        assertThat(predicates, hasItem(dateLtPredicate));
    }

    @Test
    public void shouldThrowExceptionForInvalidDateFormat() {
        SearchCondition condition = leaf(AppointmentSearchFields.APPOINTMENT_DATE, "gt", "not-a-date");

        try {
            appointmentCriteriaBuilder.apply(queryContext, condition);
            fail("Expected InvalidSearchCriteriaException to be thrown");
        } catch (InvalidSearchCriteriaException e) {
            assertThat(e.getStatus(), is(SearchResponseErrorStatus.BAD_REQUEST));
        }
    }

    // ---------- Appointment number (EQ) ----------

    @Test
    public void shouldBuildEqualPredicateForAppointmentNumber() {
        SearchCondition condition = leaf(AppointmentSearchFields.APPOINTMENT_NUMBER, "eq", APPOINTMENT_NUMBER_VALUE);

        appointmentCriteriaBuilder.apply(queryContext, condition);

        assertThat(predicates, hasItem(appointmentNumberPredicate));
    }

    // ---------- Location (join + EQ) ----------

    @Test
    public void shouldBuildEqualPredicateForLocationUsingJoin() {
        SearchCondition condition = leaf(AppointmentSearchFields.LOCATION, "eq", LOCATION_UUID);

        appointmentCriteriaBuilder.apply(queryContext, condition);

        verify(root, times(1)).join(eq(AppointmentSearchConstants.LOCATION), any(JoinType.class));
        assertThat(predicates, hasItem(locationPredicate));
    }

    // ---------- Service type (join + EQ) ----------

    @Test
    public void shouldBuildEqualPredicateForServiceTypeUsingJoin() {
        SearchCondition condition = leaf(AppointmentSearchFields.SERVICE_TYPE, "eq", SERVICE_TYPE_UUID);

        appointmentCriteriaBuilder.apply(queryContext, condition);

        verify(root, times(1)).join(eq(AppointmentSearchConstants.SERVICE), any(JoinType.class));
        assertThat(predicates, hasItem(servicePredicate));
    }

    // ---------- Service attribute kind/value: join reuse within a group ----------

    @Test
    public void shouldReuseSameServiceJoinAndServiceAttributesJoinForKindAndValueConditions() {
        SearchCondition group = kindAndValueGroup();

        appointmentCriteriaBuilder.apply(queryContext, group);

        // service join created once, reused by both the attributeType and attributes chains
        verify(root, times(1)).join(eq(AppointmentSearchConstants.SERVICE), any(JoinType.class));
        verify(serviceJoin, times(1)).join(eq(AppointmentSearchConstants.ATTRIBUTES), any(JoinType.class));
        verify(attributesJoin, times(1)).join(eq(AppointmentSearchConstants.ATTRIBUTE_TYPE), any(JoinType.class));
    }

    @Test
    public void shouldCombineKindAndValuePredicatesWithAnd() {
        SearchCondition group = kindAndValueGroup();

        appointmentCriteriaBuilder.apply(queryContext, group);

        verify(criteriaBuilder, times(1)).and(new Predicate[] { kindPredicate, valuePredicate });
        assertThat(predicates, hasItem(combinedAndPredicate));
    }

    @Test
    public void shouldExcludeVoidedServiceAttributesOnlyOnceEvenWithMultipleFieldsOnSameJoin() {
        SearchCondition group = kindAndValueGroup();

        appointmentCriteriaBuilder.apply(queryContext, group);

        long voidedPredicateCount = predicates.stream().filter(p -> p == voidedPredicate).count();
        assertThat(voidedPredicateCount, is(1L));
    }

    // ---------- Group combination (AND / OR) semantics using generic conditions ----------

    @Test
    public void shouldCombineChildPredicatesWithOrWhenOperatorIsOr() {
        SearchCondition locationLeaf = leaf(AppointmentSearchFields.LOCATION, "eq", LOCATION_UUID);
        SearchCondition serviceLeaf = leaf(AppointmentSearchFields.SERVICE_TYPE, "eq", SERVICE_TYPE_UUID);
        // Production code combines predicates via the Predicate[] varargs overload
        // (CriteriaBuilder#or(Predicate...)), not the 2-arg Expression<Boolean> overload,
        // so the stub/verify must match on the array form.
        when(criteriaBuilder.or(new Predicate[] { locationPredicate, servicePredicate })).thenReturn(combinedOrPredicate);

        SearchCondition group = new SearchCondition();
        group.setOperator("OR");
        group.setConditions(Arrays.asList(locationLeaf, serviceLeaf));

        appointmentCriteriaBuilder.apply(queryContext, group);

        verify(criteriaBuilder, times(1)).or(new Predicate[] { locationPredicate, servicePredicate });
        assertThat(predicates, hasItem(combinedOrPredicate));
    }

    @Test
    public void shouldCombineChildPredicatesWithAndByDefaultOperator() {
        SearchCondition locationLeaf = leaf(AppointmentSearchFields.LOCATION, "eq", LOCATION_UUID);
        SearchCondition serviceLeaf = leaf(AppointmentSearchFields.SERVICE_TYPE, "eq", SERVICE_TYPE_UUID);
        // See note above: production uses the Predicate[] varargs overload of and(...).
        when(criteriaBuilder.and(new Predicate[] { locationPredicate, servicePredicate })).thenReturn(combinedAndPredicate);

        SearchCondition group = new SearchCondition();
        group.setOperator("AND");
        group.setConditions(Arrays.asList(locationLeaf, serviceLeaf));

        appointmentCriteriaBuilder.apply(queryContext, group);

        verify(criteriaBuilder, times(1)).and(new Predicate[] { locationPredicate, servicePredicate });
        assertThat(predicates, hasItem(combinedAndPredicate));
    }

    @Test
    public void shouldNotWrapSingleChildPredicateInAndOr() {
        SearchCondition locationLeaf = leaf(AppointmentSearchFields.LOCATION, "eq", LOCATION_UUID);

        SearchCondition group = new SearchCondition();
        group.setOperator("AND");
        group.setConditions(Collections.singletonList(locationLeaf));

        appointmentCriteriaBuilder.apply(queryContext, group);

        verify(criteriaBuilder, never()).and(any(Predicate.class), any(Predicate.class));
        verify(criteriaBuilder, never()).or(any(Predicate.class), any(Predicate.class));
        assertThat(predicates, hasItem(locationPredicate));
    }

    @Test
    public void shouldAddNoPredicateWhenGroupHasNoConditions() {
        SearchCondition group = new SearchCondition();
        group.setOperator("AND");
        group.setConditions(Collections.emptyList());

        appointmentCriteriaBuilder.apply(queryContext, group);

        assertThat(predicates.isEmpty(), is(true));
    }

    @Test
    public void shouldAddNoPredicateWhenCriteriaIsNull() {
        appointmentCriteriaBuilder.apply(queryContext, null);

        assertThat(predicates.isEmpty(), is(true));
    }

    // ---------- helpers ----------

    private SearchCondition leaf(String field, String comparator, String value) {
        SearchCondition condition = new SearchCondition();
        condition.setField(field);
        condition.setComparator(comparator);
        condition.setValue(value);
        return condition;
    }

    private SearchCondition kindAndValueGroup() {
        SearchCondition kindLeaf = leaf(AppointmentSearchFields.SERVICE_ATTRIBUTE_KIND, "eq", KIND_VALUE);
        SearchCondition valueLeaf = leaf(AppointmentSearchFields.SERVICE_ATTRIBUTE_VALUE, "eq", VALUE_REFERENCE_VALUE);

        SearchCondition group = new SearchCondition();
        group.setOperator("AND");
        group.setConditions(Arrays.asList(kindLeaf, valueLeaf));
        return group;
    }
}
