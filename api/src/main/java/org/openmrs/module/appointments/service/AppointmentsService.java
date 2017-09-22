package org.openmrs.module.appointments.service;


import org.openmrs.annotation.Authorized;
import org.openmrs.module.appointments.model.Appointment;
import org.openmrs.module.appointments.model.AppointmentService;
import org.openmrs.module.appointments.model.AppointmentServiceType;
import org.openmrs.module.appointments.model.AppointmentStatus;

import java.util.Date;
import java.util.List;

public interface AppointmentsService {

    @Authorized({"Manage Appointments"})
    Appointment validateAndSave(Appointment appointment);

    @Authorized({"View Appointments"})
    List<Appointment> getAllAppointments(Date forDate);

    @Authorized({"View Appointments"})
    List<Appointment> search(Appointment appointment);

    @Authorized({"View Appointments"})
    List<Appointment> getAllFutureAppointmentsForService(AppointmentService appointmentService);

    @Authorized({"View Appointments"})
    List<Appointment> getAllFutureAppointmentsForServiceType(AppointmentServiceType appointmentServiceType);

    @Authorized({"View Appointments"})
    List<Appointment> getAppointmentsForService(AppointmentService appointmentService, Date startDate, Date endDate, List<AppointmentStatus> appointmentStatusList);

    @Authorized({"View Appointments"})
    Appointment getAppointmentByUuid(String uuid);

    @Authorized({"Manage Appointments"})
	void changeStatus(Appointment appointment, String status, Date onDate);

    @Authorized({"View Appointments"})
    List<Appointment> getAllAppointmentsInDateRange(Date startDate, Date endDate);

    @Authorized({"Manage Appointments"})
	void undoStatusChange(Appointment appointment);
}

