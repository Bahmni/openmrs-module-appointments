package org.openmrs.module.appointments.dao.impl;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;
import org.openmrs.module.appointments.dao.AppointmentServiceDao;
import org.openmrs.module.appointments.model.AppointmentService;
import org.openmrs.module.appointments.model.AppointmentServiceType;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public class AppointmentServiceDaoImpl implements AppointmentServiceDao{

    private SessionFactory sessionFactory;

    public void setSessionFactory(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    @Override
    public List<AppointmentService> getAllAppointmentServices(boolean includeVoided) {
        Criteria criteria = sessionFactory.getCurrentSession().createCriteria(AppointmentService.class, "appointmentService");
        if(!includeVoided) {
            criteria.add(Restrictions.eq("voided", includeVoided));
        }
        return criteria.list();
    }

    @Transactional
    @Override
    public AppointmentService save(AppointmentService appointmentService) {
        Session currentSession = sessionFactory.getCurrentSession();
        currentSession.saveOrUpdate(appointmentService);
        return appointmentService;
    }

    @Override
    public AppointmentService getAppointmentServiceByUuid(String uuid) {
        Session currentSession = sessionFactory.getCurrentSession();
        Criteria criteria = currentSession.createCriteria(AppointmentService.class, "appointmentService");
        criteria.add(Restrictions.eq("uuid", uuid));
        AppointmentService appointmentService = (AppointmentService) criteria.uniqueResult();
        evictObjectFromSession(currentSession, appointmentService);
        return appointmentService;
    }

    @Override
    public AppointmentService getNonVoidedAppointmentServiceByName(String serviceName) {
        Session currentSession = sessionFactory.getCurrentSession();
        Criteria criteria = currentSession.createCriteria(AppointmentService.class, "appointmentService");
        criteria.add(Restrictions.eq("name", serviceName));
        criteria.add(Restrictions.eq("voided", false));
        AppointmentService appointmentService = (AppointmentService) criteria.uniqueResult();
        evictObjectFromSession(currentSession, appointmentService);
        return appointmentService;
    }

    @Override
    public AppointmentServiceType getAppointmentServiceTypeByUuid(String uuid) {
        Criteria criteria = sessionFactory.getCurrentSession().createCriteria(AppointmentServiceType.class, "appointmentServiceType");
        criteria.add(Restrictions.eq("uuid", uuid));
        return (AppointmentServiceType) criteria.uniqueResult();
    }

    private void evictObjectFromSession(Session currentSession, AppointmentService appointmentService) {
        if (appointmentService != null) {
            currentSession.evict(appointmentService);
        }
    }
}
