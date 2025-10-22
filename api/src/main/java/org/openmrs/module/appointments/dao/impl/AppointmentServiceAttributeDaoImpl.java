package org.openmrs.module.appointments.dao.impl;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;
import org.openmrs.module.appointments.dao.AppointmentServiceAttributeDao;
import org.openmrs.module.appointments.model.AppointmentServiceAttribute;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public class AppointmentServiceAttributeDaoImpl implements AppointmentServiceAttributeDao {
    private SessionFactory sessionFactory;

    public void setSessionFactory(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    @Override
    public List<AppointmentServiceAttribute> getAttributesByService(Integer serviceId, boolean includeVoided) {
        Criteria criteria = sessionFactory.getCurrentSession().createCriteria(AppointmentServiceAttribute.class);
        criteria.createAlias("appointmentService", "service");
        criteria.add(Restrictions.eq("service.appointmentServiceId", serviceId));
        if (!includeVoided) {
            criteria.add(Restrictions.eq("voided", false));
        }
        return criteria.list();
    }

    @Override
    public AppointmentServiceAttribute getAttributeByUuid(String uuid) {
        Criteria criteria = sessionFactory.getCurrentSession().createCriteria(AppointmentServiceAttribute.class);
        criteria.add(Restrictions.eq("uuid", uuid));
        List list = criteria.list();
        return list.size() > 0 ? (AppointmentServiceAttribute) list.get(0) : null;
    }

    @Transactional
    @Override
    public AppointmentServiceAttribute save(AppointmentServiceAttribute attribute) {
        Session currentSession = sessionFactory.getCurrentSession();
        currentSession.saveOrUpdate(attribute);
        return attribute;
    }
}
