package org.openmrs.module.appointments.dao.impl;

import org.bahmni.search.builder.QueryContext;
import org.bahmni.search.model.SearchCondition;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openmrs.module.appointments.model.Appointment;
import org.openmrs.module.appointments.search.AppointmentSearchConstants;
import org.openmrs.module.appointments.search.builder.AppointmentCriteriaBuilder;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Fetch;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.Silent.class)
public class AppointmentSearchDaoImplTest {

    @Mock
    private SessionFactory sessionFactory;

    @Mock
    private Session session;

    @Mock
    private CriteriaBuilder criteriaBuilder;

    @Mock
    private CriteriaQuery<Appointment> criteriaQuery;

    @Mock
    private Root<Appointment> root;

    @Mock
    private AppointmentCriteriaBuilder appointmentCriteriaBuilder;

    @Mock
    private Fetch patientFetch;

    @Mock
    private Fetch serviceFetch;

    @Mock
    private Fetch locationFetch;

    @Mock
    private Fetch reasonsFetch;

    @Mock
    private Fetch serviceAttributesFetch;

    @Mock
    private Fetch patientIdentifiersFetch;

    @Mock
    private Path<Boolean> voidedPath;

    @Mock
    private Path<Integer> appointmentIdPath;

    @Mock
    private Predicate voidedPredicate;

    @Mock
    private Order order;

    @Mock
    private Query<Appointment> hibernateQuery;

    private AppointmentSearchDaoImpl appointmentSearchDao;

    @Before
    public void setUp() {
        appointmentSearchDao = new AppointmentSearchDaoImpl(sessionFactory, appointmentCriteriaBuilder);

        when(sessionFactory.getCurrentSession()).thenReturn(session);
        when(session.getCriteriaBuilder()).thenReturn(criteriaBuilder);
        when(criteriaBuilder.createQuery(Appointment.class)).thenReturn(criteriaQuery);
        when(criteriaQuery.from(Appointment.class)).thenReturn(root);

        doReturn(patientFetch).when(root).fetch(eq(AppointmentSearchConstants.PATIENT), any(JoinType.class));
        doReturn(serviceFetch).when(root).fetch(eq(AppointmentSearchConstants.SERVICE), any(JoinType.class));
        doReturn(locationFetch).when(root).fetch(eq(AppointmentSearchConstants.LOCATION), any(JoinType.class));
        doReturn(reasonsFetch).when(root).fetch(eq(AppointmentSearchConstants.REASONS), any(JoinType.class));
        doReturn(serviceAttributesFetch).when(serviceFetch).fetch(eq(AppointmentSearchConstants.ATTRIBUTES), any(JoinType.class));
        doReturn(patientIdentifiersFetch).when(patientFetch).fetch(eq(AppointmentSearchConstants.IDENTIFIERS), any(JoinType.class));

        doReturn(voidedPath).when(root).get(AppointmentSearchConstants.VOIDED);
        doReturn(appointmentIdPath).when(root).get("appointmentId");
        when(criteriaBuilder.isFalse(voidedPath)).thenReturn(voidedPredicate);
        when(criteriaBuilder.desc(appointmentIdPath)).thenReturn(order);
        when(criteriaBuilder.asc(appointmentIdPath)).thenReturn(order);

        when(criteriaQuery.select(root)).thenReturn(criteriaQuery);
        when(criteriaQuery.distinct(true)).thenReturn(criteriaQuery);
        doReturn(criteriaQuery).when(criteriaQuery).where(any(Predicate[].class));
        doReturn(criteriaQuery).when(criteriaQuery).orderBy(any(Order.class));

        when(session.createQuery(criteriaQuery)).thenReturn(hibernateQuery);
        when(hibernateQuery.setHint(anyString(), any())).thenReturn(hibernateQuery);
        when(hibernateQuery.setMaxResults(anyInt())).thenReturn(hibernateQuery);
    }

    @Test
    public void shouldFetchPatientServiceAndLocationToAvoidNPlusOne() {
        when(hibernateQuery.getResultList()).thenReturn(Collections.emptyList());

        appointmentSearchDao.search(searchCondition(), null, "desc", "next", 101);

        verify(root, times(1)).fetch(eq(AppointmentSearchConstants.PATIENT), eq(JoinType.INNER));
        verify(root, times(1)).fetch(eq(AppointmentSearchConstants.SERVICE), eq(JoinType.LEFT));
        verify(root, times(1)).fetch(eq(AppointmentSearchConstants.LOCATION), eq(JoinType.LEFT));
    }

    @Test
    public void shouldFetchReasonsServiceAttributesAndPatientIdentifiersToAvoidNPlusOne() {
        when(hibernateQuery.getResultList()).thenReturn(Collections.emptyList());

        appointmentSearchDao.search(searchCondition(), null, "desc", "next", 101);

        verify(root, times(1)).fetch(eq(AppointmentSearchConstants.REASONS), eq(JoinType.LEFT));
        verify(serviceFetch, times(1)).fetch(eq(AppointmentSearchConstants.ATTRIBUTES), eq(JoinType.LEFT));
        verify(patientFetch, times(1)).fetch(eq(AppointmentSearchConstants.IDENTIFIERS), eq(JoinType.LEFT));
    }

    @Test
    public void shouldDelegateCriteriaToAppointmentCriteriaBuilder() {
        when(hibernateQuery.getResultList()).thenReturn(Collections.emptyList());
        SearchCondition condition = searchCondition();

        appointmentSearchDao.search(condition, null, "desc", "next", 101);

        verify(appointmentCriteriaBuilder, times(1)).apply(any(QueryContext.class), eq(condition));
    }

    @Test
    public void shouldApplyDistinctAndPassDistinctThroughFalseHintToAvoidDuplicateRows() {
        when(hibernateQuery.getResultList()).thenReturn(Collections.emptyList());

        appointmentSearchDao.search(searchCondition(), null, "desc", "next", 101);

        verify(criteriaQuery, times(1)).distinct(true);
        verify(hibernateQuery, times(1)).setHint("hibernate.query.passDistinctThrough", false);
    }

    @Test
    public void shouldReturnAppointmentsReturnedByHibernateQuery() {
        Appointment appointment = new Appointment();
        List<Appointment> expected = Arrays.asList(appointment);
        when(hibernateQuery.getResultList()).thenReturn(expected);

        List<Appointment> actual = appointmentSearchDao.search(searchCondition(), null, "desc", "next", 101);

        assertThat(actual, is(expected));
    }

    private SearchCondition searchCondition() {
        SearchCondition condition = new SearchCondition();
        condition.setField("appointment.startDate");
        condition.setComparator("gt");
        condition.setValue("2024-01-01T00:00:00.000+0000");
        return condition;
    }
}
