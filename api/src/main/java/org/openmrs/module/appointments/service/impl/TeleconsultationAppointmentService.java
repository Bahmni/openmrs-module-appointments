package org.openmrs.module.appointments.service.impl;

import org.openmrs.module.appointments.model.Appointment;

public class TeleconsultationAppointmentService {

    private final static String BASE_URL = "https://meet.jit.si/";

    public String getTeleconsultationURL(Appointment appointment) {
        return BASE_URL + appointment.getUuid();
    }
}
