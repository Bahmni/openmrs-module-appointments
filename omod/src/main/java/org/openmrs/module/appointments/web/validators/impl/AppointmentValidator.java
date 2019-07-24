package org.openmrs.module.appointments.web.validators.impl;

import org.openmrs.module.appointments.model.Appointment;
import org.openmrs.module.appointments.web.validators.Validator;
import org.springframework.stereotype.Component;

@Component("appointmentValidator")
public class AppointmentValidator implements Validator<Appointment> {

    private String error;
    @Override
    public boolean validate(Appointment appointment) {

        if (appointment == null) {
            error= "Invalid appointment";
            return false;
        }

        return true;
    }

    @Override
    public String getError() {
        return error;
    }
}
