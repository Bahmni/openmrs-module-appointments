package org.openmrs.module.appointments.service.impl;

import org.bahmni.module.email.notification.EmailNotificationException;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.openmrs.module.appointments.BaseIntegrationTest;
import org.openmrs.module.appointments.dao.AppointmentDao;
import org.openmrs.module.appointments.model.Appointment;
import org.openmrs.module.appointments.service.TeleconsultationAppointmentNotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.TestPropertySource;

import static org.junit.Assert.assertNotNull;

@TestPropertySource("classpath:email-notification.properties")
public class TeleconsultationAppointmentNotificationServiceTest extends BaseIntegrationTest {

    // TODO: delete this test after email notification feature has been implemented
    //  or rename and transform to a proper integration test
    //  with a fake Java SMTP server

    @Autowired
    private AppointmentDao appointmentDao;

    @Autowired
    private TeleconsultationAppointmentNotificationService teleconsultationAppointmentNotificationService;

    @Before
    public void setUp() throws Exception {
        executeDataSet("teleconsultationAppointmentTestData.xml");
    }

    /**
     * To send a test email
     * 1) Remove @Ignore annotation below
     * 2) Configure SMTP credentials in email-notification.properties
     * 3) Change patient's email address to yours in test data set
     */
    @Ignore
    @Test
    public void testSendNotification() throws EmailNotificationException {
        Appointment appointment = appointmentDao.getAppointmentByUuid("75504r42-3ca8-11e3-bf2b-0800271c1b77");
        assertNotNull(appointment);
        assertNotNull(appointment.getPatient().getAttribute("email"));
        teleconsultationAppointmentNotificationService.sendTeleconsultationAppointmentLinkEmail(appointment);
    }
}
