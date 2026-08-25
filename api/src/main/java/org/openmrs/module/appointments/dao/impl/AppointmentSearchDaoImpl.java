package org.openmrs.module.appointments.dao.impl;

import org.bahmni.search.model.SearchCondition;
import org.bahmni.search.pagination.PaginationHelper;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.openmrs.module.appointments.dao.AppointmentSearchDao;
import org.openmrs.module.appointments.model.Appointment;
import org.openmrs.module.appointments.search.AppointmentSearchConstants;
import org.bahmni.search.builder.QueryContext;
import org.openmrs.module.appointments.search.builder.AppointmentCriteriaBuilder;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Fetch;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.List;

public class AppointmentSearchDaoImpl implements AppointmentSearchDao {

    private static final String FIELD_APPOINTMENT_ID = "appointmentId";

    private final SessionFactory sessionFactory;
    private final AppointmentCriteriaBuilder criteriaBuilder;

    public AppointmentSearchDaoImpl(SessionFactory sessionFactory,
                                    AppointmentCriteriaBuilder criteriaBuilder) {
        this.sessionFactory = sessionFactory;
        this.criteriaBuilder = criteriaBuilder;
    }

    @Override
    public List<Integer> findMatchingIds(SearchCondition searchCriteria, Long cursorId,
                                          String sortOrder, String direction, int limit) {
        Session session = sessionFactory.getCurrentSession();
        CriteriaBuilder cb = session.getCriteriaBuilder();

        CriteriaQuery<Integer> query = cb.createQuery(Integer.class);
        Root<Appointment> root = query.from(Appointment.class);

        List<Predicate> predicates = buildPredicates(cb, root, searchCriteria);

        boolean queryDescending = PaginationHelper.shouldSortQueryDescending(sortOrder, direction);

        if (cursorId != null) {
            if (queryDescending) {
                predicates.add(cb.lessThan(root.get(FIELD_APPOINTMENT_ID), cursorId));
            } else {
                predicates.add(cb.greaterThan(root.get(FIELD_APPOINTMENT_ID), cursorId));
            }
        }

        query.select(root.get(FIELD_APPOINTMENT_ID)).distinct(true);
        query.where(predicates.toArray(new Predicate[0]));
        query.orderBy(queryDescending
                ? cb.desc(root.get(FIELD_APPOINTMENT_ID))
                : cb.asc(root.get(FIELD_APPOINTMENT_ID)));

        return session.createQuery(query)
                .setMaxResults(limit)
                .getResultList();
    }

    @Override
    public List<Appointment> findByIds(List<Integer> appointmentIds) {
        if (appointmentIds == null || appointmentIds.isEmpty()) {
            return new ArrayList<>();
        }

        Session session = sessionFactory.getCurrentSession();
        CriteriaBuilder cb = session.getCriteriaBuilder();

        CriteriaQuery<Appointment> query = cb.createQuery(Appointment.class);
        Root<Appointment> root = query.from(Appointment.class);

        addFetchJoins(root);

        query.select(root).distinct(true);
        query.where(
                root.get(FIELD_APPOINTMENT_ID).in(appointmentIds),
                cb.isFalse(root.get(AppointmentSearchConstants.VOIDED)));

        List<Appointment> appointments = session.createQuery(query)
                .setHint(PaginationHelper.HINT_PASS_DISTINCT_THROUGH, false)
                .getResultList();

        return PaginationHelper.reorderByIds(appointments, appointmentIds, Appointment::getAppointmentId);
    }

    @Override
    public long count(SearchCondition searchCriteria) {
        Session session = sessionFactory.getCurrentSession();
        CriteriaBuilder cb = session.getCriteriaBuilder();

        CriteriaQuery<Long> query = cb.createQuery(Long.class);
        Root<Appointment> root = query.from(Appointment.class);

        List<Predicate> predicates = buildPredicates(cb, root, searchCriteria);

        query.select(cb.countDistinct(root))
                .where(predicates.toArray(new Predicate[0]));

        return session.createQuery(query).getSingleResult();
    }

    private List<Predicate> buildPredicates(CriteriaBuilder cb, Root<Appointment> root, SearchCondition searchCriteria) {
        List<Predicate> predicates = new ArrayList<>();
        predicates.add(cb.isFalse(root.get(AppointmentSearchConstants.VOIDED)));

        QueryContext<Appointment> context = new QueryContext<>(cb, root, predicates);
        criteriaBuilder.apply(context, searchCriteria);

        return predicates;
    }


    private void addFetchJoins(Root<Appointment> root) {
        Fetch<Appointment, ?> patientFetch = root.fetch(AppointmentSearchConstants.PATIENT, JoinType.INNER);
        Fetch<Appointment, ?> serviceFetch = root.fetch(AppointmentSearchConstants.SERVICE, JoinType.LEFT);
        root.fetch(AppointmentSearchConstants.LOCATION, JoinType.LEFT);
        root.fetch(AppointmentSearchConstants.REASONS, JoinType.LEFT);
        serviceFetch.fetch(AppointmentSearchConstants.ATTRIBUTES, JoinType.LEFT);
        patientFetch.fetch(AppointmentSearchConstants.IDENTIFIERS, JoinType.LEFT);
    }
}
