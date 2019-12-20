package org.openmrs.module.appointments.validator.impl;

import org.openmrs.api.context.Context;
import org.openmrs.module.appointments.model.Appointment;
import org.openmrs.module.appointments.model.AppointmentStatus;
import org.openmrs.module.appointments.validator.AppointmentStatusChangeValidator;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DefaultAppointmentStatusChangeValidator implements AppointmentStatusChangeValidator {

    @Override
    public void validate(Appointment appointment, AppointmentStatus toStatus, List<String> errors) {
        String disableDefaultValidationValue = Context.getAdministrationService().getGlobalProperty("disableDefaultAppointmentValidations");
        boolean disableValidation = disableDefaultValidationValue != null ? Boolean.valueOf(disableDefaultValidationValue) : false;
        if (!disableValidation) {
            AppointmentStatus currentStatus = appointment.getStatus();
            if (toStatus.getSequence() <= currentStatus.getSequence() && toStatus != AppointmentStatus.Scheduled) {
                errors.add("Appointment status can not be changed from " + appointment.getStatus() + " to " + toStatus);
            }
        }
    }

}
