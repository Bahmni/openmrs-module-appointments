package org.openmrs.module.appointments.dao.impl;

import org.hibernate.Criteria;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Example;
import org.hibernate.criterion.Restrictions;
import org.openmrs.module.appointments.dao.AppointmentDao;
import org.openmrs.module.appointments.model.Appointment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
public class AppointmentDaoImpl implements AppointmentDao {

    @Autowired
    private SessionFactory sessionFactory;

    @Override
    public List<Appointment> getAllAppointments() {
        Criteria criteria = sessionFactory.getCurrentSession().createCriteria(Appointment.class);
        return criteria.list();
    }

    @Transactional
    @Override
    public void save(Appointment appointment) {
        sessionFactory.getCurrentSession().saveOrUpdate(appointment);
    }

    @Override
    public List<Appointment> search(Appointment appointment) {
        Criteria criteria = sessionFactory.getCurrentSession().createCriteria(Appointment.class).add(
                Example.create(appointment).excludeProperty("uuid"));

        if(appointment.getPatient()!=null) criteria.createCriteria("patient").add(
                Example.create(appointment.getPatient()));

        if(appointment.getLocation()!=null) criteria.createCriteria("location").add(
                Example.create(appointment.getLocation()));

        if(appointment.getService()!=null) criteria.createCriteria("service").add(
                Example.create(appointment.getService()));

        if(appointment.getProvider()!=null) criteria.createCriteria("provider").add(
                Example.create(appointment.getProvider()));

        return criteria.list();
    }
}
