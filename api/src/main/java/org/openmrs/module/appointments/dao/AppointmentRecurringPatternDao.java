package org.openmrs.module.appointments.dao;

import org.openmrs.module.appointments.model.AppointmentRecurringPattern;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


public interface AppointmentRecurringPatternDao {
    @Transactional
    void save(AppointmentRecurringPattern appointmentRecurringPattern);

    List<AppointmentRecurringPattern> getAllAppointmentRecurringPatterns();

}
