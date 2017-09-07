package org.openmrs.module.appointments.service;


import org.openmrs.module.appointments.model.Appointment;
import org.openmrs.module.appointments.model.AppointmentService;
import org.openmrs.module.appointments.model.AppointmentServiceType;
import org.openmrs.module.appointments.model.AppointmentStatus;

import java.util.Date;
import java.util.List;

public interface AppointmentsService {

    Appointment save(Appointment appointment);

    List<Appointment> getAllAppointments(Date forDate);

    List<Appointment> search(Appointment appointment);

    List<Appointment> getAllFutureAppointmentsForService(AppointmentService appointmentService);

    List<Appointment> getAllFutureAppointmentsForServiceType(AppointmentServiceType appointmentServiceType);

    List<Appointment> getAppointmentsForService(AppointmentService appointmentService, Date startDate, Date endDate, List<AppointmentStatus> appointmentStatusList);

    Appointment getAppointmentByUuid(String uuid);

	void changeStatus(Appointment appointment, String status, Date onDate);
}

