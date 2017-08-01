package org.openmrs.module.appointments.service;


import org.openmrs.module.appointments.model.Appointment;
import org.openmrs.module.appointments.model.AppointmentService;

import java.util.List;

public interface AppointmentsService {


    Appointment save(Appointment appointment);

    List<Appointment> getAllAppointments();

    List<Appointment> Search(Appointment appointment);

    List<Appointment> getAllFutureAppointmentsForService(AppointmentService appointmentService);
}

