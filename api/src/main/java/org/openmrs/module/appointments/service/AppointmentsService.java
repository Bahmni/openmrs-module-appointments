package org.openmrs.module.appointments.service;


import java.util.Date;
import org.openmrs.module.appointments.model.Appointment;
import org.openmrs.module.appointments.model.AppointmentService;
import org.openmrs.module.appointments.model.AppointmentServiceType;

import java.util.List;

public interface AppointmentsService {


    Appointment save(Appointment appointment);

    List<Appointment> getAllAppointments(Date forDate);

    List<Appointment> Search(Appointment appointment);

    List<Appointment> getAllFutureAppointmentsForService(AppointmentService appointmentService);

    List<Appointment> getAllFutureAppointmentsForServiceType(AppointmentServiceType appointmentServiceType);
}

