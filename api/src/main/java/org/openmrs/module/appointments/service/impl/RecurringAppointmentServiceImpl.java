package org.openmrs.module.appointments.service.impl;

import org.openmrs.api.APIException;
import org.openmrs.module.appointments.dao.AppointmentRecurringPatternDao;
import org.openmrs.module.appointments.model.Appointment;
import org.openmrs.module.appointments.model.AppointmentRecurringPattern;
import org.openmrs.module.appointments.service.AppointmentsService;
import org.openmrs.module.appointments.service.RecurringAppointmentService;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;

@Transactional
public class RecurringAppointmentServiceImpl implements RecurringAppointmentService {


    AppointmentRecurringPatternDao appointmentRecurringPatternDao;

    AppointmentsService appointmentsService;

    public void setAppointmentRecurringPatternDao(AppointmentRecurringPatternDao appointmentRecurringPatternDao) {
        this.appointmentRecurringPatternDao = appointmentRecurringPatternDao;
    }

    public void setAppointmentsService(AppointmentsService appointmentsService) {
        this.appointmentsService = appointmentsService;
    }

    @Override
    public List<Appointment> saveRecurringAppointments(AppointmentRecurringPattern appointmentRecurringPattern,
                                                       List<Appointment> appointments) {
        appointments.forEach(appointmentsService::validateAndSave);
        appointmentRecurringPattern.setAppointments(new HashSet<>(appointments));
        appointmentRecurringPatternDao.save(appointmentRecurringPattern);
        return appointments;
    }

}
