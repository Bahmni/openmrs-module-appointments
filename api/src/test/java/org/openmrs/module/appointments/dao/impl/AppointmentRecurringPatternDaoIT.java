package org.openmrs.module.appointments.dao.impl;

import org.junit.Before;
import org.junit.Test;
import org.openmrs.module.appointments.BaseIntegrationTest;
import org.openmrs.module.appointments.dao.AppointmentDao;
import org.openmrs.module.appointments.dao.AppointmentRecurringPatternDao;
import org.openmrs.module.appointments.model.Appointment;
import org.openmrs.module.appointments.model.AppointmentRecurringPattern;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.openmrs.module.appointments.service.impl.RecurringAppointmentType.DAY;

public class AppointmentRecurringPatternDaoIT extends BaseIntegrationTest {

    @Autowired
    private AppointmentDao appointmentDao;

    @Autowired
    private AppointmentRecurringPatternDao appointmentRecurringPatternDao;

    @Before
    public void setUp() throws Exception {
        executeDataSet("appointmentTestData.xml");
    }

    @Test
    public void shouldSaveRecurringPatternAndMapWithTwoRecurringAppointments() {
        Appointment recurringAppointmentOne = appointmentDao
                .getAppointmentByUuid("91c62b6b-d7ef-49b4-9a8b-164b2aa55edf");
        Appointment recurringAppointmentTwo = appointmentDao
                .getAppointmentByUuid("638180c8-98f3-4fe4-b14a-9196da665bc6");
        AppointmentRecurringPattern appointmentRecurringPattern = new AppointmentRecurringPattern();
        appointmentRecurringPattern.setType(DAY);
        appointmentRecurringPattern.setFrequency(2);
        appointmentRecurringPattern.setPeriod(2);
        appointmentRecurringPattern.setAppointments(new HashSet<>(Arrays
                .asList(recurringAppointmentOne, recurringAppointmentTwo)));


        appointmentRecurringPatternDao.save(appointmentRecurringPattern);
        List<AppointmentRecurringPattern> allAppointmentRecurringPatterns = appointmentRecurringPatternDao
                .getAllAppointmentRecurringPatterns();

        assertEquals(1, allAppointmentRecurringPatterns.size());

        AppointmentRecurringPattern actualAppointmentRecurringPattern = allAppointmentRecurringPatterns.get(0);

        assertEquals(appointmentRecurringPattern.getType(), actualAppointmentRecurringPattern.getType());
        assertEquals(appointmentRecurringPattern.getFrequency(), actualAppointmentRecurringPattern.getFrequency());
        assertEquals(appointmentRecurringPattern.getPeriod(), actualAppointmentRecurringPattern.getPeriod());
        assertNull(actualAppointmentRecurringPattern.getEndDate());
        assertNull(actualAppointmentRecurringPattern.getDaysOfWeek());

    }
}
