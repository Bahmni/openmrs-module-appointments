package org.openmrs.module.appointments.validator.impl;

import org.openmrs.module.appointments.dao.AppointmentDao;
import org.openmrs.module.appointments.model.Appointment;
import org.openmrs.module.appointments.validator.AppointmentValidator;

import java.util.List;

public class DefaultEditAppointmentValidator implements AppointmentValidator {

    private AppointmentDao appointmentDao;

    public void setAppointmentDao(AppointmentDao appointmentDao) {
        this.appointmentDao = appointmentDao;
    }

    @Override
    public void validate(Appointment requestAppointment, List<String> errors) {
        Appointment savedAppointment = appointmentDao.getAppointmentByUuid(requestAppointment.getUuid());
        if (savedAppointment == null) {
            errors.add("Invalid appointment for edit");
        } else {
            if (!savedAppointment.getPatient().getUuid().equals(requestAppointment.getPatient().getUuid())) {
                errors.add("Appointment cannot be updated with that patient");
            }
        }
        if (requestAppointment.getService() == null)
            errors.add("Appointment cannot be updated without Service");
    }
}
