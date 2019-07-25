package org.openmrs.module.appointments.dao.impl;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.openmrs.module.appointments.dao.AppointmentRecurringPatternDao;
import org.openmrs.module.appointments.model.AppointmentRecurringPattern;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public class AppointmentRecurringPatternDaoImpl implements AppointmentRecurringPatternDao {

    private SessionFactory sessionFactory;

    public void setSessionFactory(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    @Transactional
    @Override
    public void save(AppointmentRecurringPattern appointmentRecurringPattern) {
        session().saveOrUpdate(appointmentRecurringPattern);
    }

    @Override
    public List<AppointmentRecurringPattern> getAllAppointmentRecurringPatterns() {
        return session().createCriteria(AppointmentRecurringPattern.class).list();
    }

    private Session session() {
        return sessionFactory.getCurrentSession();
    }
}
