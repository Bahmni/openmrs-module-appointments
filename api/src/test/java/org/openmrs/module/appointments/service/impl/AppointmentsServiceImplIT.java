package org.openmrs.module.appointments.service.impl;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.openmrs.Encounter;
import org.openmrs.api.EncounterService;
import org.openmrs.api.context.Context;
import org.openmrs.module.appointments.BaseIntegrationTest;
import org.openmrs.module.appointments.model.Appointment;
import org.openmrs.module.appointments.service.AppointmentsService;
import org.springframework.beans.factory.annotation.Autowired;

public class AppointmentsServiceImplIT extends BaseIntegrationTest {

    @Autowired
    private AppointmentsService appointmentsService;

    @Autowired
    private EncounterService encounterService;

    @Before
    public void setUp() throws Exception {
        executeDataSet("appointmentTestData.xml");
    }

    @Test
    public void voidingAppointmentShouldNotVoidFulfillingEncounters() {
        String appointmentUuid="75504r42-3ca8-11e3-bf2b-0800271c13349";
        Appointment appointment = appointmentsService.getAppointmentByUuid(appointmentUuid);
        appointment.setVoided(true);
        appointmentsService.validateAndSave(appointment);
        Context.flushSession();
        Context.clearSession();

        appointment = appointmentsService.getAppointmentByUuid(appointmentUuid);
        assertTrue(appointment.getVoided());
        Encounter enc1 = encounterService.getEncounterByUuid("f303e49f-24a9-41bb-810b-5d07b881c0e0");
        assertFalse(enc1.getVoided());
        Encounter enc2 = encounterService.getEncounterByUuid("785826c1-f137-4b4f-bb2f-07b5c546d139");
        assertFalse(enc2.getVoided());
    }
}
