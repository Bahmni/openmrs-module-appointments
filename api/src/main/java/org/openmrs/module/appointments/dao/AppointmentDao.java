package org.openmrs.module.appointments.dao;

import java.util.Date;
import org.openmrs.module.appointments.model.Appointment;
import org.openmrs.module.appointments.model.AppointmentService;
import org.openmrs.module.appointments.model.AppointmentServiceType;
import org.openmrs.module.appointments.model.AppointmentStatus;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface AppointmentDao {
    List<Appointment> getAllAppointments(Date forDate);

    @Transactional
    void save(Appointment appointmentService);

    List<Appointment> search(Appointment appointment);

    List<Appointment> getAllFutureAppointmentsForService(AppointmentService appointmentService);

    List<Appointment> getAllFutureAppointmentsForServiceType(AppointmentServiceType appointmentServiceType);

    List<Appointment> getAppointmentsForService(AppointmentService appointmentService, Date startDate, Date endDate, List<AppointmentStatus> appointmentStatusFilterList);

	Appointment getAppointmentByUuid(String uuid);

    List<Appointment> getAllAppointmentsInDateRange(Date startDate, Date endDate);
}
