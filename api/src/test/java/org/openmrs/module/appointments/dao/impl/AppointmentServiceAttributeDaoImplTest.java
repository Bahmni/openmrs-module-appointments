package org.openmrs.module.appointments.dao.impl;

import org.junit.Before;
import org.junit.Test;
import org.openmrs.module.appointments.BaseIntegrationTest;
import org.openmrs.module.appointments.dao.AppointmentServiceAttributeDao;
import org.openmrs.module.appointments.dao.AppointmentServiceAttributeTypeDao;
import org.openmrs.module.appointments.dao.AppointmentServiceDao;
import org.openmrs.module.appointments.model.AppointmentServiceAttribute;
import org.openmrs.module.appointments.model.AppointmentServiceAttributeType;
import org.openmrs.module.appointments.model.AppointmentServiceDefinition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

@Sql(scripts = "classpath:appointmentServiceAttributeTestData.xml", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = "classpath:tear-down.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
public class AppointmentServiceAttributeDaoImplTest extends BaseIntegrationTest {

    @Autowired
    private AppointmentServiceAttributeDao appointmentServiceAttributeDao;

    @Autowired
    private AppointmentServiceDao appointmentServiceDao;

    @Autowired
    private AppointmentServiceAttributeTypeDao appointmentServiceAttributeTypeDao;

    @Before
    public void setUp() throws Exception {
        executeDataSet("appointmentServiceAttributeTestData.xml");
    }

    @Test
    public void shouldGetAttributesByServiceIncludingVoided() {
        List<AppointmentServiceAttribute> attributes = appointmentServiceAttributeDao.getAttributesByService(1, true);

        assertNotNull(attributes);
        assertEquals(3, attributes.size());
    }

    @Test
    public void shouldGetAttributesByServiceExcludingVoided() {
        List<AppointmentServiceAttribute> attributes = appointmentServiceAttributeDao.getAttributesByService(1, false);

        assertNotNull(attributes);
        assertEquals(2, attributes.size());
        for (AppointmentServiceAttribute attr : attributes) {
            assertFalse(attr.getVoided());
        }
    }

    @Test
    public void shouldReturnEmptyListForServiceWithNoAttributes() {
        List<AppointmentServiceAttribute> attributes = appointmentServiceAttributeDao.getAttributesByService(999, false);

        assertNotNull(attributes);
        assertEquals(0, attributes.size());
    }

    @Test
    public void shouldGetAttributeByUuid() {
        String uuid = "attr-uuid-1";
        AppointmentServiceAttribute attribute = appointmentServiceAttributeDao.getAttributeByUuid(uuid);

        assertNotNull(attribute);
        assertEquals(uuid, attribute.getUuid());
        assertEquals("100", attribute.getValueReference());
        assertFalse(attribute.getVoided());
    }

    @Test
    public void shouldReturnNullForInvalidUuid() {
        AppointmentServiceAttribute attribute = appointmentServiceAttributeDao.getAttributeByUuid("invalid-uuid");

        assertNull(attribute);
    }

    @Test
    public void shouldSaveNewAttribute() throws Exception {
        AppointmentServiceDefinition service = appointmentServiceDao.getAppointmentServiceByUuid("c36006e5-9fbb-4f20-866b-0ece245615a6");
        AppointmentServiceAttributeType attributeType = appointmentServiceAttributeTypeDao.getAttributeTypeById(1);

        AppointmentServiceAttribute attribute = new AppointmentServiceAttribute();
        attribute.setAppointmentService(service);
        attribute.setAttributeType(attributeType);
        attribute.setValue("150");

        AppointmentServiceAttribute saved = appointmentServiceAttributeDao.save(attribute);

        assertNotNull(saved);
        assertNotNull(saved.getAppointmentServiceAttributeId());
        assertEquals("150", saved.getValueReference());
        assertEquals(service.getAppointmentServiceId(), saved.getAppointmentService().getAppointmentServiceId());
    }

    @Test
    public void shouldUpdateExistingAttribute() throws Exception {
        AppointmentServiceAttribute attribute = appointmentServiceAttributeDao.getAttributeByUuid("attr-uuid-1");
        assertNotNull(attribute);

        String newValue = "250";
        attribute.setValue(newValue);

        AppointmentServiceAttribute updated = appointmentServiceAttributeDao.save(attribute);

        assertNotNull(updated);
        assertEquals(newValue, updated.getValueReference());
    }

    @Test
    public void shouldVoidAttribute() {
        AppointmentServiceAttribute attribute = appointmentServiceAttributeDao.getAttributeByUuid("attr-uuid-1");
        assertNotNull(attribute);
        assertFalse(attribute.getVoided());

        attribute.setVoided(true);
        attribute.setVoidReason("Test void");

        AppointmentServiceAttribute voided = appointmentServiceAttributeDao.save(attribute);

        assertNotNull(voided);
        assertTrue(voided.getVoided());
        assertEquals("Test void", voided.getVoidReason());
    }
}
