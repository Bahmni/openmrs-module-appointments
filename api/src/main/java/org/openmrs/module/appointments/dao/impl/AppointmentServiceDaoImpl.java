package org.openmrs.module.appointments.dao.impl;

import java.util.List;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;
import org.openmrs.module.appointments.dao.AppointmentServiceDao;
import org.openmrs.module.appointments.model.AppointmentService;
import org.openmrs.module.appointments.model.AppointmentServiceType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class AppointmentServiceDaoImpl implements AppointmentServiceDao{

    @Autowired
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
        Criteria criteria = sessionFactory.getCurrentSession().createCriteria(AppointmentService.class, "appointmentService");
        criteria.add(Restrictions.eq("uuid", uuid));
        return (AppointmentService) criteria.uniqueResult();
    }

    @Override
    public AppointmentService getNonVoidedAppointmentServiceByName(String serviceName) {
        Criteria criteria = sessionFactory.getCurrentSession().createCriteria(AppointmentService.class, "appointmentService");
        criteria.add(Restrictions.eq("name", serviceName));
        criteria.add(Restrictions.eq("voided", false));
        return (AppointmentService) criteria.uniqueResult();
    }

    @Override
    public AppointmentServiceType getAppointmentServiceTypeByUuid(String uuid) {
        Criteria criteria = sessionFactory.getCurrentSession().createCriteria(AppointmentServiceType.class, "appointmentServiceType");
        criteria.add(Restrictions.eq("uuid", uuid));
        return (AppointmentServiceType) criteria.uniqueResult();
    }
}
