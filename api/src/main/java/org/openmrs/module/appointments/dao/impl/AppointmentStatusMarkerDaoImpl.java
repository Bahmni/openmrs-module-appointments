package org.openmrs.module.appointments.dao.impl;

import org.hibernate.Query;
import org.hibernate.SessionFactory;
import org.openmrs.Patient;
import org.openmrs.module.appointments.dao.AppointmentStatusMarkerDao;
import org.openmrs.module.appointments.model.AppointmentServiceDefinition;
import org.openmrs.module.appointments.service.AppointmentsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

//@Transactional
@Repository
public class AppointmentStatusMarkerDaoImpl implements AppointmentStatusMarkerDao {

    private SessionFactory sessionFactory;

    public SessionFactory getSessionFactory() {
        return sessionFactory;
    }

    public void setSessionFactory(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    @Override
    public List<String> getAllAppointmentsNotConsultedUuidList(Date startDateTime, Date endDateTime) {
        Query query = sessionFactory.getCurrentSession().
                createSQLQuery("SELECT uuid FROM patient_appointment WHERE status IN ('Scheduled', 'CheckedIn') AND start_date_time>=:startDateTime AND start_date_time<=:endDateTime");
        query.setDate("startDateTime", startDateTime);
        query.setDate("endDateTime", endDateTime);
        List<String> appointmentsUuidList = (List<String>) query.list();
        return appointmentsUuidList;
    }

    @Override
    public List<String> getPatientFutureAppointmentUuidList(Patient patient, AppointmentServiceDefinition appointmentServiceDefinition, Date startDateTime, Date endDateTime) {
        Query query = sessionFactory.getCurrentSession()
                .createQuery("SELECT a.uuid FROM Appointment a WHERE a.service=:appointmentService AND a.patient=:patient " +
                        "AND a.startDateTime>=:startDateTime AND a.endDateTime<:endDateTime");
        query.setParameter("appointmentService", appointmentServiceDefinition);
        query.setParameter("patient", patient);
        query.setDate("startDateTime", startDateTime);
        query.setDate("endDateTime", endDateTime);

        List<String> futureAppoinmentsUuidList = (List<String>) query.list();
        return futureAppoinmentsUuidList;
    }

    @Override
    public void markAppoinmentAsComplete(String uuid) {
        Query query = sessionFactory.getCurrentSession().
                createSQLQuery("UPDATE patient_appointment SET status='Completed' WHERE uuid=:uuid");
        query.setString("uuid", uuid);
        query.executeUpdate();

    }

    @Override
    public void markAllMissedAppointments(String concatenatedUuidList) {
        Query query = sessionFactory.getCurrentSession().
                  createSQLQuery("UPDATE patient_appointment SET status='Missed' WHERE uuid IN (" + concatenatedUuidList + ")");
        query.executeUpdate();
    }

}
