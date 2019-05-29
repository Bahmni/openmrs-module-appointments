package org.openmrs.module.appointments.web.service;

import org.openmrs.module.appointments.model.Appointment;

import java.util.List;

public interface RecurringAppointmentsGenerationService {

    List<Appointment> getAppointments();

}
