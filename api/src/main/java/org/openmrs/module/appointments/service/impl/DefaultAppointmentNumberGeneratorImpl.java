package org.openmrs.module.appointments.service.impl;

import org.openmrs.module.appointments.model.Appointment;
import org.openmrs.module.appointments.service.AppointmentNumberGenerator;

import javax.validation.constraints.NotNull;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DefaultAppointmentNumberGeneratorImpl implements AppointmentNumberGenerator {
    @Override
    public String generateAppointmentNumber(@NotNull Appointment appointment) {
        return new SimpleDateFormat("YYMMddHHmmssSSS").format(new Date());
    }
}
