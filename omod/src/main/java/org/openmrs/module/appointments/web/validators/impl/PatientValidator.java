package org.openmrs.module.appointments.web.validators.impl;

import org.openmrs.Patient;
import org.openmrs.module.appointments.web.validators.Validator;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
@Qualifier("patientValidator")
public class PatientValidator implements Validator<Patient> {

    private String error;
    @Override
    public boolean validate(Patient patient) {
    if (patient ==null || patient.getUuid() == null) {
            error = "Appointment patient details missing.";
            return false;
        }
        return true;
    }


    @Override
    public String getError() {
        return error;
    }
}
