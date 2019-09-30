package org.openmrs.module.appointments.dao.impl;

import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;
import org.openmrs.module.appointments.dao.SpecialityDao;
import org.openmrs.module.appointments.model.Speciality;
import org.springframework.transaction.annotation.Transactional;

public class SpecialityDaoImpl implements SpecialityDao{
    private SessionFactory sessionFactory;

    public void setSessionFactory(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    @Override
    public Speciality getSpecialityByUuid(String uuid) {
        Criteria criteria = sessionFactory.getCurrentSession().createCriteria(Speciality.class, "Speciality");
        criteria.add(Restrictions.eq("uuid", uuid));
        List list = criteria.list();
        return list.size() > 0? (Speciality) list.get(0) : null;
    }

    public List<Speciality> getAllSpecialities() {
        Criteria criteria = sessionFactory.getCurrentSession().createCriteria(Speciality.class, "Speciality");
        return criteria.list();
    }
    
    @Transactional
	@Override
	public Speciality save(Speciality speciality) {
		 Session currentSession = sessionFactory.getCurrentSession();
		 currentSession.saveOrUpdate(speciality);
	     return speciality;
	}
}
