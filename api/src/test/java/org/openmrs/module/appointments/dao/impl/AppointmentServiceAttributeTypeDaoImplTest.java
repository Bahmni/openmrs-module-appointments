package org.openmrs.module.appointments.dao.impl;

import org.junit.Before;
import org.junit.Test;
import org.openmrs.module.appointments.BaseIntegrationTest;
import org.openmrs.module.appointments.dao.AppointmentServiceAttributeTypeDao;
import org.openmrs.module.appointments.model.AppointmentServiceAttributeType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

@Sql(scripts = "classpath:appointmentServiceAttributeTestData.xml", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = "classpath:tear-down.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
public class AppointmentServiceAttributeTypeDaoImplTest extends BaseIntegrationTest {

    @Autowired
    private AppointmentServiceAttributeTypeDao appointmentServiceAttributeTypeDao;

    @Before
    public void setUp() throws Exception {
        executeDataSet("appointmentServiceAttributeTestData.xml");
    }

    @Test
    public void shouldGetAllAttributeTypesIncludingRetired() {
        List<AppointmentServiceAttributeType> attributeTypes = appointmentServiceAttributeTypeDao.getAllAttributeTypes(true);

        assertNotNull(attributeTypes);
        assertEquals(3, attributeTypes.size());
    }

    @Test
    public void shouldGetAllAttributeTypesExcludingRetired() {
        List<AppointmentServiceAttributeType> attributeTypes = appointmentServiceAttributeTypeDao.getAllAttributeTypes(false);

        assertNotNull(attributeTypes);
        assertEquals(2, attributeTypes.size());
    }

    @Test
    public void shouldGetAttributeTypeByUuid() {
        String uuid = "attr-type-uuid-1";
        AppointmentServiceAttributeType attributeType = appointmentServiceAttributeTypeDao.getAttributeTypeByUuid(uuid);

        assertNotNull(attributeType);
        assertEquals(uuid, attributeType.getUuid());
        assertEquals("consultation_fee", attributeType.getName());
        assertEquals("Consultation Fee", attributeType.getDescription());
    }

    @Test
    public void shouldReturnNullForInvalidUuid() {
        AppointmentServiceAttributeType attributeType = appointmentServiceAttributeTypeDao.getAttributeTypeByUuid("invalid-uuid");

        assertNull(attributeType);
    }

    @Test
    public void shouldGetAttributeTypeById() {
        AppointmentServiceAttributeType attributeType = appointmentServiceAttributeTypeDao.getAttributeTypeById(1);

        assertNotNull(attributeType);
        assertEquals("consultation_fee", attributeType.getName());
        assertEquals("Consultation Fee", attributeType.getDescription());
    }

    @Test
    public void shouldReturnNullForInvalidId() {
        AppointmentServiceAttributeType attributeType = appointmentServiceAttributeTypeDao.getAttributeTypeById(999);

        assertNull(attributeType);
    }

    @Test
    public void shouldSaveNewAttributeType() {
        AppointmentServiceAttributeType attributeType = new AppointmentServiceAttributeType();
        attributeType.setName("test_attribute");
        attributeType.setDescription("Test Attribute");
        attributeType.setDatatypeClassname("org.openmrs.customdatatype.datatype.FreeTextDatatype");
        attributeType.setMinOccurs(0);
        attributeType.setMaxOccurs(1);

        AppointmentServiceAttributeType saved = appointmentServiceAttributeTypeDao.save(attributeType);

        assertNotNull(saved);
        assertNotNull(saved.getAppointmentServiceAttributeTypeId());
        assertEquals("test_attribute", saved.getName());
    }

    @Test
    public void shouldUpdateExistingAttributeType() {
        AppointmentServiceAttributeType attributeType = appointmentServiceAttributeTypeDao.getAttributeTypeById(1);
        assertNotNull(attributeType);

        String newDescription = "Updated Consultation Fee";
        attributeType.setDescription(newDescription);

        AppointmentServiceAttributeType updated = appointmentServiceAttributeTypeDao.save(attributeType);

        assertNotNull(updated);
        assertEquals(newDescription, updated.getDescription());
    }
}
