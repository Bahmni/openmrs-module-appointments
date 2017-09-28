package org.openmrs.module.appointments.dao.impl;

import org.hibernate.Criteria;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.openmrs.module.appointments.dao.AppointmentAuditDao;
import org.openmrs.module.appointments.model.Appointment;
import org.openmrs.module.appointments.model.AppointmentAudit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public class AppointmentAuditDaoImpl implements AppointmentAuditDao{

	private SessionFactory sessionFactory;

	public void setSessionFactory(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}

	@Transactional
	@Override
	public void save(AppointmentAudit appointmentAuditEvent) {
		sessionFactory.getCurrentSession().saveOrUpdate(appointmentAuditEvent);
	}

	@Override
	public List<AppointmentAudit> getAppointmentHistoryForAppointment(Appointment appointment) {
		Criteria criteria = sessionFactory.getCurrentSession().createCriteria(AppointmentAudit.class, "appointmentAudit");
		criteria.add(Restrictions.eq("appointment", appointment));
		return criteria.list();
	}

	@Override
	public AppointmentAudit getPriorStatusChangeEvent(Appointment appointment) {
		Criteria criteria = sessionFactory.getCurrentSession().createCriteria(AppointmentAudit.class, "appointmentAudit");
		criteria.add(Restrictions.eq("appointment", appointment));
		criteria.add(Restrictions.ne("status", appointment.getStatus()));
		criteria.addOrder(Order.desc("dateCreated"));
		criteria.setMaxResults(1);
		return (AppointmentAudit) criteria.uniqueResult();
	}

}
