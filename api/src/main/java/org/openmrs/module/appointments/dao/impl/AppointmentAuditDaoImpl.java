package org.openmrs.module.appointments.dao.impl;

import org.hibernate.Criteria;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;
import org.openmrs.module.appointments.dao.AppointmentAuditDao;
import org.openmrs.module.appointments.model.Appointment;
import org.openmrs.module.appointments.model.AppointmentAudit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
public class AppointmentAuditDaoImpl implements AppointmentAuditDao{

	@Autowired
	private SessionFactory sessionFactory;

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

}
