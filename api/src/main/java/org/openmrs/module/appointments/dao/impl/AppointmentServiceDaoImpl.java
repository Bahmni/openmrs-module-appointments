package org.openmrs.module.appointments.dao.impl;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;
import org.hibernate.query.Query;
import org.openmrs.module.appointments.dao.AppointmentServiceDao;
import org.openmrs.module.appointments.model.AppointmentServiceDefinition;
import org.openmrs.module.appointments.model.AppointmentServiceSearchRequest;
import org.openmrs.module.appointments.model.AppointmentServiceType;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public class AppointmentServiceDaoImpl implements AppointmentServiceDao{

    private static final String HQL_SEARCH_BASE = "SELECT asd FROM AppointmentServiceDefinition asd";
    private static final String HQL_LEFT_JOIN_LOCATION = " LEFT JOIN asd.location loc";
    private static final String HQL_LEFT_JOIN_SPECIALITY = " LEFT JOIN asd.speciality spec";
    private static final String HQL_WHERE_LOCATION = " loc.uuid = :locationUuid";
    private static final String HQL_WHERE_SPECIALITY = " spec.uuid = :specialityUuid";
    private static final String HQL_WHERE_VOIDED = " asd.voided = :voided";
    private static final String HQL_ORDER_BY = " ORDER BY asd.appointmentServiceId ASC";

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
    public List<AppointmentServiceDefinition> search(AppointmentServiceSearchRequest searchRequest) {
        StringBuilder hql = new StringBuilder(HQL_SEARCH_BASE);

        boolean hasLocationFilter = StringUtils.isNotBlank(searchRequest.getLocationUuid());
        boolean hasSpecialityFilter = StringUtils.isNotBlank(searchRequest.getSpecialityUuid());

        if (hasLocationFilter) {
            hql.append(HQL_LEFT_JOIN_LOCATION);
        }
        if (hasSpecialityFilter) {
            hql.append(HQL_LEFT_JOIN_SPECIALITY);
        }

        boolean hasConditions = false;
        if (hasLocationFilter) {
            hql.append(" WHERE").append(HQL_WHERE_LOCATION);
            hasConditions = true;
        }

        if (hasSpecialityFilter) {
            hql.append(hasConditions ? " AND" : " WHERE").append(HQL_WHERE_SPECIALITY);
            hasConditions = true;
        }

        if (searchRequest.getIncludeVoided() == null || !searchRequest.getIncludeVoided()) {
            hql.append(hasConditions ? " AND" : " WHERE").append(HQL_WHERE_VOIDED);
        }

        hql.append(HQL_ORDER_BY);

        Query<AppointmentServiceDefinition> query = sessionFactory.getCurrentSession()
                .createQuery(hql.toString(), AppointmentServiceDefinition.class);

        if (hasLocationFilter) {
            query.setParameter("locationUuid", searchRequest.getLocationUuid());
        }

        if (hasSpecialityFilter) {
            query.setParameter("specialityUuid", searchRequest.getSpecialityUuid());
        }

        if (searchRequest.getIncludeVoided() == null || !searchRequest.getIncludeVoided()) {
            query.setParameter("voided", false);
        }

        if (searchRequest.getLimit() > 0) {
            query.setMaxResults(searchRequest.getLimit());
        }

        return query.list();
    }

    private void evictObjectFromSession(Session currentSession, AppointmentServiceDefinition appointmentServiceDefinition) {
        if (appointmentServiceDefinition != null) {
            currentSession.evict(appointmentServiceDefinition);
        }
    }
}
