package org.openmrs.module.appointments.dao.impl;

import org.bahmni.search.model.SearchCondition;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.openmrs.module.appointments.dao.AppointmentSearchDao;
import org.openmrs.module.appointments.model.Appointment;
import org.openmrs.module.appointments.search.builder.AppointmentCriteriaBuilder;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

        root.fetch("patient", JoinType.INNER);
        root.fetch("service", JoinType.LEFT);
        root.fetch("location", JoinType.LEFT);

        List<Predicate> predicates = new ArrayList<>();
        predicates.add(cb.isFalse(root.get("voided")));

        Map<String, Join<?, ?>> joinCache = new HashMap<>();
        criteriaBuilder.apply(cb, root, searchCriteria, predicates, joinCache);

        query.select(root).distinct(true)
                .where(predicates.toArray(new Predicate[0]));

        return session.createQuery(query)
                .setHint("hibernate.query.passDistinctThrough", false)
                .getResultList();
    }
}
