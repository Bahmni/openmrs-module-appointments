package org.openmrs.module.appointments.service;


import org.openmrs.annotation.Authorized;
import org.openmrs.module.appointments.model.Appointment;
import org.openmrs.module.appointments.model.AppointmentService;
import org.openmrs.module.appointments.model.AppointmentServiceType;
import org.openmrs.module.appointments.model.AppointmentStatus;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

public interface AppointmentsService {

    @Transactional
	@Authorized({"Manage Appointments"})
    Appointment validateAndSave(Appointment appointment);

    @Transactional
	@Authorized({"View Appointments"})
    List<Appointment> getAllAppointments(Date forDate);

    @Transactional
	@Authorized({"View Appointments"})
    List<Appointment> search(Appointment appointment);

    @Transactional
	@Authorized({"View Appointments"})
    List<Appointment> getAllFutureAppointmentsForService(AppointmentService appointmentService);

    @Transactional
	@Authorized({"View Appointments"})
    List<Appointment> getAllFutureAppointmentsForServiceType(AppointmentServiceType appointmentServiceType);

    @Transactional
	@Authorized({"View Appointments"})
    List<Appointment> getAppointmentsForService(AppointmentService appointmentService, Date startDate, Date endDate, List<AppointmentStatus> appointmentStatusList);

    @Transactional
	@Authorized({"View Appointments"})
    Appointment getAppointmentByUuid(String uuid);

    @Transactional
	@Authorized({"Manage Appointments"})
	void changeStatus(Appointment appointment, String status, Date onDate);

    @Transactional
	@Authorized({"View Appointments"})
    List<Appointment> getAllAppointmentsInDateRange(Date startDate, Date endDate);

    @Transactional
	@Authorized({"Manage Appointments"})
	void undoStatusChange(Appointment appointment);
}

