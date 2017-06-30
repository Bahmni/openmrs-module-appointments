package org.openmrs.module.appointments.dao.impl;

import org.hibernate.Criteria;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;
import org.openmrs.module.appointments.dao.SpecialityDao;
import org.openmrs.module.appointments.model.AppointmentService;
import org.openmrs.module.appointments.model.Speciality;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
public class SpecialityDaoImpl implements SpecialityDao{
    @Autowired
    private SessionFactory sessionFactory;

    @Override
    public Speciality getSpecialityByUuid(String uuid) {
        Criteria criteria = sessionFactory.getCurrentSession().createCriteria(Speciality.class, "Speciality");
        criteria.add(Restrictions.eq("uuid", uuid));
        List list = criteria.list();
        return list.size() > 0? (Speciality) list.get(0) : null;
    }
}
