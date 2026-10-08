package org.openmrs.module.appointments.dao.impl;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.openmrs.api.db.hibernate.HibernateUtil;
import org.openmrs.module.appointments.dao.AppointmentServiceDao;
import org.openmrs.module.appointments.model.AppointmentServiceDefinition;
import org.openmrs.module.appointments.model.AppointmentServiceSearchParams;
import org.openmrs.module.appointments.model.AppointmentServiceType;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.List;

public class AppointmentServiceDaoImpl implements AppointmentServiceDao{

    private SessionFactory sessionFactory;

    public void setSessionFactory(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    @Override
    public List<AppointmentServiceDefinition> getAllAppointmentServices(boolean includeVoided) {
        String hql = "from AppointmentServiceDefinition appointmentService" + (includeVoided ? "" : " where appointmentService.voided = false");
        return sessionFactory.getCurrentSession().createQuery(hql, AppointmentServiceDefinition.class).list();
    }

    @Transactional
    @Override
    public AppointmentServiceDefinition save(AppointmentServiceDefinition appointmentServiceDefinition) {
        Session currentSession = sessionFactory.getCurrentSession();
        return HibernateUtil.saveOrUpdate(currentSession, appointmentServiceDefinition);
    }

    @Override
    public AppointmentServiceDefinition getAppointmentServiceByUuid(String uuid) {
        Session currentSession = sessionFactory.getCurrentSession();
        AppointmentServiceDefinition appointmentServiceDefinition = currentSession
                .createQuery("from AppointmentServiceDefinition where uuid = :uuid", AppointmentServiceDefinition.class)
                .setParameter("uuid", uuid).uniqueResult();
        evictObjectFromSession(currentSession, appointmentServiceDefinition);
        return appointmentServiceDefinition;
    }

    @Override
    public AppointmentServiceDefinition getNonVoidedAppointmentServiceByName(String serviceName) {
        Session currentSession = sessionFactory.getCurrentSession();
        AppointmentServiceDefinition appointmentServiceDefinition = currentSession
                .createQuery("from AppointmentServiceDefinition where name = :name and voided = false", AppointmentServiceDefinition.class)
                .setParameter("name", serviceName).uniqueResult();
        evictObjectFromSession(currentSession, appointmentServiceDefinition);
        return appointmentServiceDefinition;
    }

    @Override
    public AppointmentServiceType getAppointmentServiceTypeByUuid(String uuid) {
        return sessionFactory.getCurrentSession()
                .createQuery("from AppointmentServiceType where uuid = :uuid", AppointmentServiceType.class)
                .setParameter("uuid", uuid).uniqueResult();
    }

    @Override
    public List<AppointmentServiceDefinition> search(AppointmentServiceSearchParams searchParams) {
        CriteriaBuilder criteriaBuilder = sessionFactory.getCurrentSession().getCriteriaBuilder();
        CriteriaQuery<AppointmentServiceDefinition> criteriaQuery = criteriaBuilder.createQuery(AppointmentServiceDefinition.class);
        Root<AppointmentServiceDefinition> root = criteriaQuery.from(AppointmentServiceDefinition.class);

        List<Predicate> predicates = new ArrayList<>();

        if (StringUtils.isNotBlank(searchParams.getLocationUuid())) {
            Join<Object, Object> locationJoin = root.join("location", JoinType.LEFT);
            predicates.add(criteriaBuilder.equal(locationJoin.get("uuid"), searchParams.getLocationUuid()));
        }

        if (StringUtils.isNotBlank(searchParams.getSpecialityUuid())) {
            Join<Object, Object> specialityJoin = root.join("speciality", JoinType.LEFT);
            predicates.add(criteriaBuilder.equal(specialityJoin.get("uuid"), searchParams.getSpecialityUuid()));
        }

        if (searchParams.getIncludeVoided() == null || !searchParams.getIncludeVoided()) {
            predicates.add(criteriaBuilder.equal(root.get("voided"), false));
        }

        if (!predicates.isEmpty()) {
            criteriaQuery.where(criteriaBuilder.and(predicates.toArray(new Predicate[0])));
        }

        criteriaQuery.orderBy(criteriaBuilder.asc(root.get("appointmentServiceId")));
        TypedQuery<AppointmentServiceDefinition> query = sessionFactory.getCurrentSession().createQuery(criteriaQuery);

        if (searchParams.getLimit() != null && searchParams.getLimit() > 0) {
            query.setMaxResults(searchParams.getLimit());
        }

        return query.getResultList();
    }

    private void evictObjectFromSession(Session currentSession, AppointmentServiceDefinition appointmentServiceDefinition) {
        if (appointmentServiceDefinition != null) {
            currentSession.evict(appointmentServiceDefinition);
        }
    }
}
