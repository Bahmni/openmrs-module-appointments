package org.openmrs.module.appointments.dao.impl;

import org.junit.Before;
import org.junit.Test;
import org.openmrs.module.appointments.BaseIntegrationTest;
import org.openmrs.module.appointments.dao.AppointmentAuditDao;
import org.openmrs.module.appointments.dao.AppointmentDao;
import org.openmrs.module.appointments.model.Appointment;
import org.openmrs.module.appointments.model.AppointmentAudit;
import org.openmrs.module.appointments.model.AppointmentStatus;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNull;

public class AppointmentAuditDaoImplIT extends BaseIntegrationTest {

    @Autowired
    AppointmentAuditDao appointmentAuditDao;

    @Autowired
    AppointmentDao appointmentDao;

    @Before
    public void setUp() throws Exception {
        executeDataSet("appointmentTestData.xml");
    }

    @Test
    public void shouldSaveAppointmentAudit() throws Exception {
        Appointment appointment = appointmentDao.getAppointmentByUuid("75504r42-3ca8-11e3-bf2b-0800271c1111");
        assertNotNull(appointment);
        List<AppointmentAudit> allAuditEvents = appointmentAuditDao.getAppointmentHistoryForAppointment(appointment);
        assertEquals(1, allAuditEvents.size());
        AppointmentAudit audit = new AppointmentAudit();
        audit.setAppointment(appointment);
        audit.setStatus(AppointmentStatus.CheckedIn);
        appointmentAuditDao.save(audit);
        allAuditEvents = appointmentAuditDao.getAppointmentHistoryForAppointment(appointment);
        assertEquals(2, allAuditEvents.size());
    }

    @Test
    public void shouldGetAppointmentAuditForAppointment() throws Exception {
        Appointment appointment = appointmentDao.getAppointmentByUuid("75504r42-3ca8-11e3-bf2b-0800271c1111");
        assertNotNull(appointment);
        List<AppointmentAudit> allAuditEvents = appointmentAuditDao.getAppointmentHistoryForAppointment(appointment);
        assertEquals(1, allAuditEvents.size());
    }

    @Test
    public void shouldGetPriorStatusChangeAuditForAppointment() throws Exception {
        Appointment appointment = appointmentDao.getAppointmentByUuid("75504r42-3ca8-11e3-bf2b-0800271c12222");
        assertNotNull(appointment);
        AppointmentAudit priorStatusChangeEvent = appointmentAuditDao.getPriorStatusChangeEvent(appointment);
        assertNotEquals(appointment.getStatus().toString(), priorStatusChangeEvent.getStatus().toString());
        assertEquals("Completed", priorStatusChangeEvent.getStatus().toString());
    }

    @Test
    public void shouldGetNullIfThereIsNoStatusChangeEvent() throws Exception {
        Appointment appointment = appointmentDao.getAppointmentByUuid("75504r42-3ca8-11e3-bf2b-0800271c1b77");
        assertNotNull(appointment);
        AppointmentAudit priorStatusChangeEvent = appointmentAuditDao.getPriorStatusChangeEvent(appointment);
        assertNull(priorStatusChangeEvent);
    }
}
