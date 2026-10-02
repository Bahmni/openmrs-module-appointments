package org.openmrs.module.appointments.service.impl;

import org.junit.jupiter.api.Test;
import org.openmrs.module.appointments.model.Appointment;

import java.text.SimpleDateFormat;
import java.util.Date;

public class DefaultAppointmentNumberGeneratorImplTest {

    @Test
    public void shouldGenerateAppointmentNumber() {
        DefaultAppointmentNumberGeneratorImpl appointmentNumberGenerator = new DefaultAppointmentNumberGeneratorImpl();
        String appointmentNumberPart = new SimpleDateFormat("YYMMddHHmm").format(new Date());
        String generated = appointmentNumberGenerator.generateAppointmentNumber(new Appointment());
        org.junit.jupiter.api.Assertions.assertTrue(generated.startsWith(appointmentNumberPart));
    }

}