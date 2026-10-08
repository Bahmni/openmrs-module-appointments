package org.openmrs.module.appointments.dao.impl;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.openmrs.api.db.hibernate.HibernateUtil;
import org.openmrs.module.appointments.dao.AppointmentUnavailabilityDao;
import org.openmrs.module.appointments.model.AppointmentUnavailability;
import org.openmrs.module.appointments.search.param.AppointmentUnavailabilitySearchParams;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
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
        return HibernateUtil.saveOrUpdate(currentSession, appointmentUnavailability);
    }

    @Override
    public AppointmentUnavailability getByUuid(String uuid) {
        Session currentSession = sessionFactory.getCurrentSession();
        AppointmentUnavailability appointmentUnavailability = currentSession
                .createQuery("from AppointmentUnavailability where uuid = :uuid", AppointmentUnavailability.class)
                .setParameter("uuid", uuid).uniqueResult();
        evictObjectFromSession(currentSession, appointmentUnavailability);
        return appointmentUnavailability;
    }

    @Override
    public List<AppointmentUnavailability> getAll(AppointmentUnavailabilitySearchParams searchParams) {
        CriteriaBuilder criteriaBuilder = sessionFactory.getCurrentSession().getCriteriaBuilder();
        CriteriaQuery<AppointmentUnavailability> criteriaQuery = criteriaBuilder.createQuery(AppointmentUnavailability.class);
        Root<AppointmentUnavailability> root = criteriaQuery.from(AppointmentUnavailability.class);

        List<Predicate> predicates = new ArrayList<>();

        if (StringUtils.isNotBlank(searchParams.getLocationUuid())) {
            predicates.add(criteriaBuilder.equal(root.get("location").get("uuid"), searchParams.getLocationUuid()));
        }

        if (StringUtils.isNotBlank(searchParams.getServiceUuid())) {
            predicates.add(criteriaBuilder.equal(root.get("service").get("uuid"), searchParams.getServiceUuid()));
        }

        if (StringUtils.isNotBlank(searchParams.getProviderUuid())) {
            predicates.add(criteriaBuilder.equal(root.get("provider").get("uuid"), searchParams.getProviderUuid()));
        }

        if (StringUtils.isNotBlank(searchParams.getStartDate())) {
            Date startDate = java.sql.Date.valueOf(LocalDate.parse(searchParams.getStartDate(), DateTimeFormatter.ISO_LOCAL_DATE));
            predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("endDate"), startDate));
        }

        if (StringUtils.isNotBlank(searchParams.getEndDate())) {
            Date endDate = java.sql.Date.valueOf(LocalDate.parse(searchParams.getEndDate(), DateTimeFormatter.ISO_LOCAL_DATE));
            predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("startDate"), endDate));
        }

        if (!searchParams.isIncludeVoided()) {
            predicates.add(criteriaBuilder.equal(root.get("voided"), false));
        }

        if (!predicates.isEmpty()) {
            criteriaQuery.where(criteriaBuilder.and(predicates.toArray(new Predicate[0])));
        }

        criteriaQuery.orderBy(criteriaBuilder.asc(root.get("startDate")), criteriaBuilder.asc(root.get("startTime")));
        TypedQuery<AppointmentUnavailability> query = sessionFactory.getCurrentSession().createQuery(criteriaQuery);

        if (searchParams.getLimit() != null && searchParams.getLimit() > 0) {
            query.setMaxResults(searchParams.getLimit());
        }

        return query.getResultList();
    }

    private void evictObjectFromSession(Session currentSession, AppointmentUnavailability appointmentUnavailability) {
        if (appointmentUnavailability != null) {
            currentSession.evict(appointmentUnavailability);
        }
    }
}
