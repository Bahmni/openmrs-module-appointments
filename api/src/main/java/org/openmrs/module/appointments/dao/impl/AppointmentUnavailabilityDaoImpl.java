package org.openmrs.module.appointments.dao.impl;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;
import org.openmrs.Location;
import org.openmrs.Provider;
import org.openmrs.module.appointments.dao.AppointmentUnavailabilityDao;
import org.openmrs.module.appointments.model.AppointmentServiceDefinition;
import org.openmrs.module.appointments.model.AppointmentUnavailability;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class AppointmentUnavailabilityDaoImpl implements AppointmentUnavailabilityDao {

    private SessionFactory sessionFactory;

    public void setSessionFactory(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    @Transactional
    @Override
    public AppointmentUnavailability save(AppointmentUnavailability appointmentUnavailability) {
        Session currentSession = sessionFactory.getCurrentSession();
        currentSession.saveOrUpdate(appointmentUnavailability);
        return appointmentUnavailability;
    }

    @Override
    public AppointmentUnavailability getByUuid(String uuid) {
        Session currentSession = sessionFactory.getCurrentSession();
        Criteria criteria = currentSession.createCriteria(AppointmentUnavailability.class, "appointmentUnavailability");
        criteria.add(Restrictions.eq("uuid", uuid));
        AppointmentUnavailability appointmentUnavailability = (AppointmentUnavailability) criteria.uniqueResult();
        evictObjectFromSession(currentSession, appointmentUnavailability);
        return appointmentUnavailability;
    }

    @Override
    public List<AppointmentUnavailability> getAll(Location location, AppointmentServiceDefinition service,
                                                   Provider provider, Date startDate, Date endDate,
                                                   boolean includeVoided, Integer limit) {
        CriteriaBuilder criteriaBuilder = sessionFactory.getCurrentSession().getCriteriaBuilder();
        CriteriaQuery<AppointmentUnavailability> criteriaQuery = criteriaBuilder.createQuery(AppointmentUnavailability.class);
        Root<AppointmentUnavailability> root = criteriaQuery.from(AppointmentUnavailability.class);

        List<Predicate> predicates = new ArrayList<>();

        if (location != null) {
            predicates.add(criteriaBuilder.equal(root.get("location"), location));
        }

        if (service != null) {
            predicates.add(criteriaBuilder.equal(root.get("service"), service));
        }

        if (provider != null) {
            predicates.add(criteriaBuilder.equal(root.get("provider"), provider));
        }

        if (startDate != null) {
            predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("endDate"), startDate));
        }

        if (endDate != null) {
            predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("startDate"), endDate));
        }

        if (!includeVoided) {
            predicates.add(criteriaBuilder.equal(root.get("voided"), false));
        }

        if (!predicates.isEmpty()) {
            criteriaQuery.where(criteriaBuilder.and(predicates.toArray(new Predicate[0])));
        }

        criteriaQuery.orderBy(criteriaBuilder.asc(root.get("startDate")), criteriaBuilder.asc(root.get("startTime")));
        TypedQuery<AppointmentUnavailability> query = sessionFactory.getCurrentSession().createQuery(criteriaQuery);

        if (limit != null && limit > 0) {
            query.setMaxResults(limit);
        }

        return query.getResultList();
    }

    private void evictObjectFromSession(Session currentSession, AppointmentUnavailability appointmentUnavailability) {
        if (appointmentUnavailability != null) {
            currentSession.evict(appointmentUnavailability);
        }
    }
}
