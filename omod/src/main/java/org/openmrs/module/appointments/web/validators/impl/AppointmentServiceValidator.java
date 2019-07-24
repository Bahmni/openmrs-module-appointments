package org.openmrs.module.appointments.web.validators.impl;

import org.openmrs.module.appointments.model.AppointmentServiceDefinition;
import org.openmrs.module.appointments.web.validators.Validator;
import org.springframework.stereotype.Component;

@Component("appointmentServiceValidator")
public class AppointmentServiceValidator implements Validator<AppointmentServiceDefinition> {
    private String error;
    @Override
    public boolean validate(AppointmentServiceDefinition appointmentServiceDefinition) {
       if(appointmentServiceDefinition==null || appointmentServiceDefinition.getUuid() == null){
           error ="Appointment service details missing.";
           return false;
       }
       return true;
    }

    @Override
    public String getError() {
        return error;
    }
}
