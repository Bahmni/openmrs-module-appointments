package org.openmrs.module.appointments.dao.impl;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;
import org.openmrs.module.appointments.dao.AppointmentServiceDao;
import org.openmrs.module.appointments.model.AppointmentServiceDefinition;
import org.openmrs.module.appointments.model.AppointmentServiceSearchParams;
import org.openmrs.module.appointments.model.AppointmentServiceType;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.List;

public class AppointmentServiceDaoImpl implements AppointmentServiceDao{

    private SessionFactory sessionFactory;

    public void setSessionFactory(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    @Override
    public List<AppointmentServiceDefinition> getAllAppointmentServices(boolean includeVoided) {
        Criteria criteria = sessionFactory.getCurrentSession().createCriteria(AppointmentServiceDefinition.class, "appointmentService");
        if(!includeVoided) {
            criteria.add(Restrictions.eq("voided", includeVoided));
        }
        return criteria.list();
    }

    @Transactional
    @Override
    public AppointmentServiceDefinition save(AppointmentServiceDefinition appointmentServiceDefinition) {
        Session currentSession = sessionFactory.getCurrentSession();
        currentSession.saveOrUpdate(appointmentServiceDefinition);
        return appointmentServiceDefinition;
    }

    @Override
    public AppointmentServiceDefinition getAppointmentServiceByUuid(String uuid) {
        Session currentSession = sessionFactory.getCurrentSession();
        Criteria criteria = currentSession.createCriteria(AppointmentServiceDefinition.class, "appointmentServiceDefinition");
        criteria.add(Restrictions.eq("uuid", uuid));
        AppointmentServiceDefinition appointmentServiceDefinition = (AppointmentServiceDefinition) criteria.uniqueResult();
        evictObjectFromSession(currentSession, appointmentServiceDefinition);
        return appointmentServiceDefinition;
    }

    @Override
    public AppointmentServiceDefinition getNonVoidedAppointmentServiceByName(String serviceName) {
        Session currentSession = sessionFactory.getCurrentSession();
        Criteria criteria = currentSession.createCriteria(AppointmentServiceDefinition.class, "appointmentServiceDefinition");
        criteria.add(Restrictions.eq("name", serviceName));
        criteria.add(Restrictions.eq("voided", false));
        AppointmentServiceDefinition appointmentServiceDefinition = (AppointmentServiceDefinition) criteria.uniqueResult();
        evictObjectFromSession(currentSession, appointmentServiceDefinition);
        return appointmentServiceDefinition;
    }

    @Override
    public AppointmentServiceType getAppointmentServiceTypeByUuid(String uuid) {
        Criteria criteria = sessionFactory.getCurrentSession().createCriteria(AppointmentServiceType.class, "appointmentServiceType");
        criteria.add(Restrictions.eq("uuid", uuid));
        return (AppointmentServiceType) criteria.uniqueResult();
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
