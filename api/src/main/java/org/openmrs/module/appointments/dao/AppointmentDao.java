package org.openmrs.module.appointments.dao;

import java.util.Date;


import org.openmrs.module.appointments.model.Appointment;
import org.openmrs.module.appointments.model.AppointmentSearchRequestModel;
import org.openmrs.module.appointments.model.AppointmentServiceDefinition;
import org.openmrs.module.appointments.model.AppointmentServiceType;
import org.openmrs.module.appointments.model.AppointmentStatus;
import org.openmrs.module.appointments.model.AppointmentSearchRequest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface AppointmentDao {
    List<Appointment> getAllAppointments(Date forDate);
    List<Appointment> getAllAppointmentsReminder(String afterTime);

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
