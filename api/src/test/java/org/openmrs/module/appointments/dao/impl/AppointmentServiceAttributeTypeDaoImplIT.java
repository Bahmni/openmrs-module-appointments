package org.openmrs.module.appointments.dao.impl;

import org.junit.Before;
import org.junit.Test;
import org.openmrs.module.appointments.BaseIntegrationTest;
import org.openmrs.module.appointments.dao.AppointmentServiceAttributeTypeDao;
import org.openmrs.module.appointments.model.AppointmentServiceAttributeType;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.junit.Assert.*;

public class AppointmentServiceAttributeTypeDaoImplIT extends BaseIntegrationTest {

    @Autowired
    AppointmentServiceAttributeTypeDao appointmentServiceAttributeTypeDao;

    @Before
    public void setUp() throws Exception {
        executeDataSet("appointmentServiceAttributeTestData.xml");
    }

    @Test
    public void shouldGetAllAttributeTypesExcludingRetired() throws Exception {
        List<AppointmentServiceAttributeType> attributeTypes =
            appointmentServiceAttributeTypeDao.getAllAttributeTypes(false);

        assertEquals(2, attributeTypes.size());

        for (AppointmentServiceAttributeType type : attributeTypes) {
            assertFalse("Should not include retired types", type.getRetired());
        }
    }

    @Test
    public void shouldGetAllAttributeTypesIncludingRetired() throws Exception {
        List<AppointmentServiceAttributeType> attributeTypes =
            appointmentServiceAttributeTypeDao.getAllAttributeTypes(true);

        assertEquals(3, attributeTypes.size());

        int retiredCount = 0;
        int activeCount = 0;
        for (AppointmentServiceAttributeType type : attributeTypes) {
            if (type.getRetired()) {
                retiredCount++;
            } else {
                activeCount++;
            }
        }

        assertEquals(1, retiredCount);
        assertEquals(2, activeCount);
    }

    @Test
    public void shouldGetAttributeTypeByUuid() throws Exception {
        String uuid = "d7477c21-444f-4ff0-a48f-b87b61c4b8a8";
        AppointmentServiceAttributeType attributeType =
            appointmentServiceAttributeTypeDao.getAttributeTypeByUuid(uuid);

        assertNotNull(attributeType);
        assertEquals(uuid, attributeType.getUuid());
        assertEquals("Department Code", attributeType.getName());
        assertEquals("Department code for the service", attributeType.getDescription());
        assertEquals("org.openmrs.customdatatype.datatype.FreeTextDatatype",
            attributeType.getDatatypeClassname());
        assertFalse(attributeType.getRetired());
    }

    @Test
    public void shouldReturnNullForInvalidUuid() throws Exception {
        AppointmentServiceAttributeType attributeType =
            appointmentServiceAttributeTypeDao.getAttributeTypeByUuid("invalid-uuid");

        assertNull(attributeType);
    }

    @Test
    public void shouldGetAttributeTypeById() throws Exception {
        AppointmentServiceAttributeType attributeType =
            appointmentServiceAttributeTypeDao.getAttributeTypeById(1);

        assertNotNull(attributeType);
        assertEquals(Integer.valueOf(1), attributeType.getAppointmentServiceAttributeTypeId());
        assertEquals("Department Code", attributeType.getName());
    }

    @Test
    public void shouldReturnNullForInvalidId() throws Exception {
        AppointmentServiceAttributeType attributeType =
            appointmentServiceAttributeTypeDao.getAttributeTypeById(999);

        assertNull(attributeType);
    }

    @Test
    public void shouldSaveNewAttributeType() throws Exception {
        List<AppointmentServiceAttributeType> beforeSave =
            appointmentServiceAttributeTypeDao.getAllAttributeTypes(false);
        assertEquals(2, beforeSave.size());

        AppointmentServiceAttributeType newType = new AppointmentServiceAttributeType();
        newType.setName("Location Code");
        newType.setDescription("Location identifier for the service");
        newType.setDatatypeClassname("org.openmrs.customdatatype.datatype.FreeTextDatatype");
        newType.setMinOccurs(0);
        newType.setMaxOccurs(1);
        newType.setRetired(false);

        AppointmentServiceAttributeType savedType =
            appointmentServiceAttributeTypeDao.save(newType);

        assertNotNull(savedType);
        assertNotNull(savedType.getAppointmentServiceAttributeTypeId());
        assertNotNull(savedType.getUuid());
        assertEquals("Location Code", savedType.getName());

        List<AppointmentServiceAttributeType> afterSave =
            appointmentServiceAttributeTypeDao.getAllAttributeTypes(false);
        assertEquals(3, afterSave.size());
    }

    @Test
    public void shouldUpdateExistingAttributeType() throws Exception {
        String uuid = "d7477c21-444f-4ff0-a48f-b87b61c4b8a8";
        AppointmentServiceAttributeType attributeType =
            appointmentServiceAttributeTypeDao.getAttributeTypeByUuid(uuid);

        assertNotNull(attributeType);
        assertEquals("Department Code", attributeType.getName());

        attributeType.setName("Updated Department Code");
        attributeType.setDescription("Updated description");

        AppointmentServiceAttributeType updatedType =
            appointmentServiceAttributeTypeDao.save(attributeType);

        assertEquals(uuid, updatedType.getUuid());
        assertEquals("Updated Department Code", updatedType.getName());
        assertEquals("Updated description", updatedType.getDescription());

        AppointmentServiceAttributeType fetchedType =
            appointmentServiceAttributeTypeDao.getAttributeTypeByUuid(uuid);
        assertEquals("Updated Department Code", fetchedType.getName());
        assertEquals("Updated description", fetchedType.getDescription());
    }

    @Test
    public void shouldSaveAttributeTypeWithMinMaxOccurs() throws Exception {
        AppointmentServiceAttributeType newType = new AppointmentServiceAttributeType();
        newType.setName("Multi Value Attribute");
        newType.setDescription("Can have multiple values");
        newType.setDatatypeClassname("org.openmrs.customdatatype.datatype.FreeTextDatatype");
        newType.setMinOccurs(1);
        newType.setMaxOccurs(5);
        newType.setRetired(false);

        AppointmentServiceAttributeType savedType =
            appointmentServiceAttributeTypeDao.save(newType);

        assertNotNull(savedType);
        assertEquals(Integer.valueOf(1), savedType.getMinOccurs());
        assertEquals(Integer.valueOf(5), savedType.getMaxOccurs());
    }

    @Test
    public void shouldRetireAttributeType() throws Exception {
        String uuid = "d7477c21-444f-4ff0-a48f-b87b61c4b8a8";
        AppointmentServiceAttributeType attributeType =
            appointmentServiceAttributeTypeDao.getAttributeTypeByUuid(uuid);

        assertNotNull(attributeType);
        assertFalse(attributeType.getRetired());

        attributeType.setRetired(true);
        attributeType.setRetireReason("Testing retirement");

        appointmentServiceAttributeTypeDao.save(attributeType);

        List<AppointmentServiceAttributeType> activeTypes =
            appointmentServiceAttributeTypeDao.getAllAttributeTypes(false);
        assertEquals(1, activeTypes.size());
        assertFalse("Should not contain retired type",
            activeTypes.stream().anyMatch(t -> t.getUuid().equals(uuid)));

        List<AppointmentServiceAttributeType> allTypes =
            appointmentServiceAttributeTypeDao.getAllAttributeTypes(true);
        assertEquals(3, allTypes.size());
        assertTrue("Should contain retired type when includeRetired is true",
            allTypes.stream().anyMatch(t -> t.getUuid().equals(uuid) && t.getRetired()));
    }
}
