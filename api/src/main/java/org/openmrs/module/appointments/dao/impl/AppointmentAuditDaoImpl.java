package org.openmrs.module.appointments.dao.impl;

import org.hibernate.SessionFactory;
import org.openmrs.api.db.hibernate.HibernateUtil;
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
		HibernateUtil.saveOrUpdate(sessionFactory.getCurrentSession(), appointmentAuditEvent);
	}

	@Override
	public List<AppointmentAudit> getAppointmentHistoryForAppointment(Appointment appointment) {
		return sessionFactory.getCurrentSession()
				.createQuery("from AppointmentAudit appointmentAudit where appointmentAudit.appointment = :appointment",
						AppointmentAudit.class)
				.setParameter("appointment", appointment)
				.list();
	}

	@Override
	public AppointmentAudit getPriorStatusChangeEvent(Appointment appointment) {
		return sessionFactory.getCurrentSession()
				.createQuery("from AppointmentAudit appointmentAudit where appointmentAudit.appointment = :appointment"
						+ " and appointmentAudit.status <> :status order by appointmentAudit.dateCreated desc",
						AppointmentAudit.class)
				.setParameter("appointment", appointment)
				.setParameter("status", appointment.getStatus())
				.setMaxResults(1)
				.uniqueResult();
	}

}
