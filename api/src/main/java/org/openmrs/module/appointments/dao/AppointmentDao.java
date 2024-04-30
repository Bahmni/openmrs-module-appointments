package org.openmrs.module.appointments.dao;

import java.util.Date;

import org.openmrs.module.appointments.model.*;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface AppointmentDao {
    List<Appointment> getAllAppointments(Date forDate);

    @Transactional
    void save(Appointment appointment);

    List<Appointment> search(Appointment appointment);

    List<Appointment> search(AppointmentSearchRequestModel searchQuery);

    List<Appointment> getAllFutureAppointmentsForService(AppointmentServiceDefinition appointmentServiceDefinition);

    List<Appointment> getAllFutureAppointmentsForServiceType(AppointmentServiceType appointmentServiceType);

    List<Appointment> getAppointmentsForService(AppointmentServiceDefinition appointmentServiceDefinition, Date startDate, Date endDate, List<AppointmentStatus> appointmentStatusFilterList);

	Appointment getAppointmentByUuid(String uuid);

    List<Appointment> getAllAppointmentsInDateRange(Date startDate, Date endDate);

    List<Appointment> search(AppointmentSearchRequest appointmentSearchRequest);

    List<Appointment> getAppointmentsForPatient(Integer patientId);

    List<Appointment> getDatelessAppointments(AppointmentSearchRequestModel searchQuery);
}
