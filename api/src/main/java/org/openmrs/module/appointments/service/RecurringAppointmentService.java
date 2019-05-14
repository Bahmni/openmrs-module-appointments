package org.openmrs.module.appointments.service;

import org.openmrs.module.appointments.model.Appointment;
import org.openmrs.module.appointments.model.AppointmentRecurringPattern;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

public interface RecurringAppointmentService {

    @Transactional
    List<Appointment> saveRecurringAppointments(AppointmentRecurringPattern appointmentRecurringPattern,
                                                List<Appointment> appointments);

    List<Date> getRecurringDates(Date appointmentStartDateTime,
                                 AppointmentRecurringPattern appointmentRecurringPattern) throws Exception;
}
