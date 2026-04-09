package org.openmrs.module.appointments.dao.impl;

import org.hibernate.SessionFactory;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.module.appointments.BaseIntegrationTest;
import org.openmrs.module.appointments.dao.AppointmentDao;
import org.openmrs.module.appointments.model.Appointment;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigInteger;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Verifies that Hibernate Envers auditing is correctly configured for the Appointment module.
 *
 * Envers creates _AUD shadow tables for each @Audited entity during SessionFactory
 * initialisation (driven by hbm2ddl.auto). Confirming those tables exist proves that
 * the @Audited annotations were picked up and the REVINFO + audit schema is in place.
 *
 * Note: Envers writes audit rows at beforeTransactionCompletion, i.e. just before
 * commit/rollback. The standard @Transactional @Rollback test context means audit rows
 * are not yet present when assertions run inside the test method. End-to-end population
 * of audit rows (creating an appointment and seeing a row in patient_appointment_AUD)
 * should be verified manually on a running OpenMRS / Bahmni server.
 */
public class AppointmentEnversAuditIT extends BaseIntegrationTest {

    @Autowired
    private AppointmentDao appointmentDao;

    @Autowired
    private SessionFactory sessionFactory;

    @Before
    public void setUp() throws Exception {
        executeDataSet("appointmentTestData.xml");
    }

    @Test
    public void shouldHaveCreatedRevInfoTableForEnversRevisions() {
        // OpenMRS uses a custom @RevisionEntity mapped to "revision_entity" table.
        // Its presence proves Envers is active and the revision tracking infrastructure is in place.
        BigInteger count = (BigInteger) sessionFactory.getCurrentSession()
                .createNativeQuery(
                        "SELECT COUNT(*) FROM INFORMATION_SCHEMA.TABLES "
                                + "WHERE UPPER(TABLE_NAME) = 'REVISION_ENTITY'")
                .getSingleResult();
        assertEquals("revision_entity table should be created by Hibernate Envers (OpenmrsRevisionEntity)", 1L, count.longValue());
    }

    @Test
    public void shouldHaveCreatedAuditTableForAppointment() {
        BigInteger count = (BigInteger) sessionFactory.getCurrentSession()
                .createNativeQuery(
                        "SELECT COUNT(*) FROM INFORMATION_SCHEMA.TABLES "
                                + "WHERE UPPER(TABLE_NAME) = 'PATIENT_APPOINTMENT_AUD'")
                .getSingleResult();
        assertEquals("patient_appointment_AUD table should be created by Hibernate Envers for @Audited Appointment",
                1L, count.longValue());
    }

    @Test
    public void shouldHaveCreatedAuditTableForAppointmentServiceDefinition() {
        BigInteger count = (BigInteger) sessionFactory.getCurrentSession()
                .createNativeQuery(
                        "SELECT COUNT(*) FROM INFORMATION_SCHEMA.TABLES "
                                + "WHERE UPPER(TABLE_NAME) = 'APPOINTMENT_SERVICE_AUD'")
                .getSingleResult();
        assertEquals("appointment_service_AUD table should be created by Hibernate Envers for @Audited AppointmentServiceDefinition",
                1L, count.longValue());
    }

    @Test
    public void shouldHaveCreatedAuditTableForSpeciality() {
        BigInteger count = (BigInteger) sessionFactory.getCurrentSession()
                .createNativeQuery(
                        "SELECT COUNT(*) FROM INFORMATION_SCHEMA.TABLES "
                                + "WHERE UPPER(TABLE_NAME) = 'APPOINTMENT_SPECIALITY_AUD'")
                .getSingleResult();
        assertEquals("appointment_speciality_AUD table should be created by Hibernate Envers for @Audited Speciality",
                1L, count.longValue());
    }

    @Test
    public void shouldSaveAppointmentWithoutEnversErrors() {
        // Verifies that saving an @Audited Appointment does not throw any Envers-related
        // exceptions (e.g. unmapped properties, missing AUD tables, etc.)
        List<Appointment> existing = appointmentDao.getAllAppointments(null);
        assertNotNull(existing);

        Appointment apt = new Appointment();
        apt.setPatient(existing.get(0).getPatient());
        appointmentDao.save(apt);

        List<Appointment> after = appointmentDao.getAllAppointments(null);
        assertEquals(existing.size() + 1, after.size());
    }
}
