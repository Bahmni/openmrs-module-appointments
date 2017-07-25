package org.openmrs.module.appointments.dao;

import org.openmrs.module.appointments.model.Appointment;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface AppointmentDao {
    List<Appointment> getAllAppointments();

    @Transactional
    void save(Appointment appointmentService);

    List<Appointment> search(Appointment appointment);
}
