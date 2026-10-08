package org.openmrs.module.appointments.dao.impl;

import java.util.List;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.openmrs.api.db.hibernate.HibernateUtil;
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
        List<Speciality> list = sessionFactory.getCurrentSession()
                .createQuery("from Speciality where uuid = :uuid", Speciality.class)
                .setParameter("uuid", uuid).list();
        return list.size() > 0? list.get(0) : null;
    }

    public List<Speciality> getAllSpecialities() {
        return sessionFactory.getCurrentSession().createQuery("from Speciality", Speciality.class).list();
    }
    
    @Transactional
	@Override
	public Speciality save(Speciality speciality) {
		 Session currentSession = sessionFactory.getCurrentSession();
	     return HibernateUtil.saveOrUpdate(currentSession, speciality);
	}
}
