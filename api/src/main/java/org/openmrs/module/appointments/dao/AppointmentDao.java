package org.openmrs.module.appointments.dao;

import java.util.Date;
import org.openmrs.module.appointments.model.Appointment;
import org.openmrs.module.appointments.model.AppointmentSearchRequest;
import org.openmrs.module.appointments.model.AppointmentServiceDefinition;
import org.openmrs.module.appointments.model.AppointmentServiceType;
import org.openmrs.module.appointments.model.AppointmentStatus;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface AppointmentDao {
    List<Appointment> getAllAppointments(Date forDate);

    List<Appointment> getAllAppointments(Date forDate, String status);

    List<Appointment> getPendingAppointments(Date forDate);

    List<Appointment> getHonouredAppointments(Date forDate);

    List<Appointment> getCameEarlyAppointments(Date forDate);

    List<Appointment> getRescheduledAppointments(Date forDate);

    @Transactional
    void save(Appointment appointment);

    List<Appointment> search(Appointment appointment);

    List<Appointment> getAllFutureAppointmentsForService(AppointmentServiceDefinition appointmentServiceDefinition);

    List<Appointment> getAllFutureAppointmentsForServiceType(AppointmentServiceType appointmentServiceType);

    List<Appointment> getAppointmentsForService(AppointmentServiceDefinition appointmentServiceDefinition, Date startDate, Date endDate, List<AppointmentStatus> appointmentStatusFilterList);

	Appointment getAppointmentByUuid(String uuid);

    Appointment getAppointmentById(Integer id);

    List<Appointment> getAllAppointmentsInDateRange(Date startDate, Date endDate);

    List<Appointment> search(AppointmentSearchRequest appointmentSearchRequest);

    List<Appointment> getAppointmentsForPatient(Integer patientId);

    List<Appointment> getAllCameEarlyAppointments(Date forDate);

    List<Appointment> getCompletedAppointments(Date forDate);

}
