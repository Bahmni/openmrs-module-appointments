package org.openmrs.module.appointments.dao.impl;

import org.bahmni.search.model.SearchCondition;
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

    private final SessionFactory sessionFactory;
    private final AppointmentCriteriaBuilder criteriaBuilder;

    public AppointmentSearchDaoImpl(SessionFactory sessionFactory,
                                    AppointmentCriteriaBuilder criteriaBuilder) {
        this.sessionFactory = sessionFactory;
        this.criteriaBuilder = criteriaBuilder;
    }

    @Override
    public List<Appointment> search(SearchCondition searchCriteria) {
        Session session = sessionFactory.getCurrentSession();
        CriteriaBuilder cb = session.getCriteriaBuilder();

        CriteriaQuery<Appointment> query = cb.createQuery(Appointment.class);
        Root<Appointment> root = query.from(Appointment.class);

        // Fetch joins to solve N+1 for response building
        Fetch<Appointment, ?> patientFetch = root.fetch(AppointmentSearchConstants.PATIENT, JoinType.INNER);
        Fetch<Appointment, ?> serviceFetch = root.fetch(AppointmentSearchConstants.SERVICE, JoinType.LEFT);
        root.fetch(AppointmentSearchConstants.LOCATION, JoinType.LEFT);
        root.fetch(AppointmentSearchConstants.REASONS, JoinType.LEFT);
        serviceFetch.fetch(AppointmentSearchConstants.ATTRIBUTES, JoinType.LEFT);
        patientFetch.fetch(AppointmentSearchConstants.IDENTIFIERS, JoinType.LEFT);

        List<Predicate> predicates = new ArrayList<>();
        predicates.add(cb.isFalse(root.get(AppointmentSearchConstants.VOIDED)));

        QueryContext<Appointment> context = new QueryContext<>(cb, root, predicates);
        criteriaBuilder.apply(context, searchCriteria);

        query.select(root).distinct(true)
                .where(predicates.toArray(new Predicate[0]));

        return session.createQuery(query)
                .setHint("hibernate.query.passDistinctThrough", false)
                .getResultList();
    }
}
