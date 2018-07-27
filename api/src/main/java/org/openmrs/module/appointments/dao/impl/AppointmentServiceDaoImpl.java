package org.openmrs.module.appointments.dao.impl;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;
import org.openmrs.module.appointments.dao.AppointmentServiceDao;
import org.openmrs.module.appointments.model.AppointmentServiceDefinition;
import org.openmrs.module.appointments.model.AppointmentServiceType;
import org.springframework.transaction.annotation.Transactional;

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

    private void evictObjectFromSession(Session currentSession, AppointmentServiceDefinition appointmentServiceDefinition) {
        if (appointmentServiceDefinition != null) {
            currentSession.evict(appointmentServiceDefinition);
        }
    }
}
