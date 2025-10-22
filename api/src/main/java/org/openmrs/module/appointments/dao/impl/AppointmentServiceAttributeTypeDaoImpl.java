package org.openmrs.module.appointments.dao.impl;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;
import org.openmrs.module.appointments.dao.AppointmentServiceAttributeTypeDao;
import org.openmrs.module.appointments.model.AppointmentServiceAttributeType;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public class AppointmentServiceAttributeTypeDaoImpl implements AppointmentServiceAttributeTypeDao {
    private SessionFactory sessionFactory;

    public void setSessionFactory(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    @Override
    public List<AppointmentServiceAttributeType> getAllAttributeTypes(boolean includeRetired) {
        Criteria criteria = sessionFactory.getCurrentSession().createCriteria(AppointmentServiceAttributeType.class);
        if (!includeRetired) {
            criteria.add(Restrictions.eq("retired", false));
        }
        return criteria.list();
    }

    @Override
    public AppointmentServiceAttributeType getAttributeTypeByUuid(String uuid) {
        Criteria criteria = sessionFactory.getCurrentSession().createCriteria(AppointmentServiceAttributeType.class);
        criteria.add(Restrictions.eq("uuid", uuid));
        List list = criteria.list();
        return list.size() > 0 ? (AppointmentServiceAttributeType) list.get(0) : null;
    }

    @Override
    public AppointmentServiceAttributeType getAttributeTypeById(Integer id) {
        return (AppointmentServiceAttributeType) sessionFactory.getCurrentSession().get(AppointmentServiceAttributeType.class, id);
    }

    @Transactional
    @Override
    public AppointmentServiceAttributeType save(AppointmentServiceAttributeType attributeType) {
        Session currentSession = sessionFactory.getCurrentSession();
        currentSession.saveOrUpdate(attributeType);
        return attributeType;
    }
}
