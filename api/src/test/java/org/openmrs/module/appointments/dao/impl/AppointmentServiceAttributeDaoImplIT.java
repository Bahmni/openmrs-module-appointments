package org.openmrs.module.appointments.dao.impl;

import org.hibernate.SessionFactory;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.module.appointments.BaseIntegrationTest;
import org.openmrs.module.appointments.dao.AppointmentServiceAttributeDao;
import org.openmrs.module.appointments.dao.AppointmentServiceAttributeTypeDao;
import org.openmrs.module.appointments.model.AppointmentServiceAttribute;
import org.openmrs.module.appointments.model.AppointmentServiceAttributeType;
import org.openmrs.module.appointments.model.AppointmentServiceDefinition;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.junit.Assert.*;

public class AppointmentServiceAttributeDaoImplIT extends BaseIntegrationTest {

    @Autowired
    AppointmentServiceAttributeDao appointmentServiceAttributeDao;

    @Autowired
    AppointmentServiceAttributeTypeDao appointmentServiceAttributeTypeDao;

    @Autowired
    SessionFactory sessionFactory;

    @Before
    public void setUp() throws Exception {
        executeDataSet("appointmentServiceAttributeTestData.xml");
    }

    private AppointmentServiceDefinition getServiceById(Integer id) {
        return (AppointmentServiceDefinition) sessionFactory.getCurrentSession()
            .get(AppointmentServiceDefinition.class, id);
    }

    @Test
    public void shouldGetAttributesByServiceExcludingVoided() throws Exception {
        List<AppointmentServiceAttribute> attributes =
            appointmentServiceAttributeDao.getAttributesByService(1, false);

        assertEquals(2, attributes.size());

        for (AppointmentServiceAttribute attr : attributes) {
            assertFalse("Should not include voided attributes", attr.getVoided());
        }
    }

    @Test
    public void shouldGetAttributesByServiceIncludingVoided() throws Exception {
        List<AppointmentServiceAttribute> attributes =
            appointmentServiceAttributeDao.getAttributesByService(1, true);

        assertEquals(3, attributes.size());

        int voidedCount = 0;
        int activeCount = 0;
        for (AppointmentServiceAttribute attr : attributes) {
            if (attr.getVoided()) {
                voidedCount++;
            } else {
                activeCount++;
            }
        }

        assertEquals(1, voidedCount);
        assertEquals(2, activeCount);
    }

    @Test
    public void shouldGetAttributesByDifferentServices() throws Exception {
        List<AppointmentServiceAttribute> service1Attributes =
            appointmentServiceAttributeDao.getAttributesByService(1, false);
        assertEquals(2, service1Attributes.size());

        List<AppointmentServiceAttribute> service2Attributes =
            appointmentServiceAttributeDao.getAttributesByService(2, false);
        assertEquals(1, service2Attributes.size());
    }

    @Test
    public void shouldReturnEmptyListForServiceWithNoAttributes() throws Exception {
        List<AppointmentServiceAttribute> attributes =
            appointmentServiceAttributeDao.getAttributesByService(999, false);

        assertNotNull(attributes);
        assertEquals(0, attributes.size());
    }

    @Test
    public void shouldGetAttributeByUuid() throws Exception {
        String uuid = "a1234567-1111-4f20-866b-0ece245615a1";
        AppointmentServiceAttribute attribute =
            appointmentServiceAttributeDao.getAttributeByUuid(uuid);

        assertNotNull(attribute);
        assertEquals(uuid, attribute.getUuid());
        assertEquals("DEPT-001", attribute.getValueReference());
        assertFalse(attribute.getVoided());

        assertNotNull(attribute.getAppointmentService());
        assertEquals(Integer.valueOf(1), attribute.getAppointmentService().getAppointmentServiceId());

        assertNotNull(attribute.getAttributeType());
        assertEquals("Department Code", attribute.getAttributeType().getName());
    }

    @Test
    public void shouldReturnNullForInvalidUuid() throws Exception {
        AppointmentServiceAttribute attribute =
            appointmentServiceAttributeDao.getAttributeByUuid("invalid-uuid");

        assertNull(attribute);
    }

    @Test
    public void shouldSaveNewAttribute() throws Exception {
        // Get service by ID directly from session to avoid eager loading issues
        AppointmentServiceDefinition service = getServiceById(1);
        AppointmentServiceAttributeType attributeType =
            appointmentServiceAttributeTypeDao.getAttributeTypeByUuid("e8588d22-555g-5gg1-b59g-c98c72d5c9b9");

        assertNotNull(service);
        assertNotNull(attributeType);

        List<AppointmentServiceAttribute> beforeSave =
            appointmentServiceAttributeDao.getAttributesByService(1, false);
        int initialCount = beforeSave.size();

        AppointmentServiceAttribute newAttribute = new AppointmentServiceAttribute();
        newAttribute.setAppointmentService(service);
        newAttribute.setAttributeType(attributeType);
        newAttribute.setValueReferenceInternal("Secondary Care");
        newAttribute.setVoided(false);

        AppointmentServiceAttribute savedAttribute =
            appointmentServiceAttributeDao.save(newAttribute);

        assertNotNull(savedAttribute);
        assertNotNull(savedAttribute.getAppointmentServiceAttributeId());
        assertNotNull(savedAttribute.getUuid());
        assertEquals("Secondary Care", savedAttribute.getValueReference());
        assertFalse(savedAttribute.getVoided());

        List<AppointmentServiceAttribute> afterSave =
            appointmentServiceAttributeDao.getAttributesByService(1, false);
        assertEquals(initialCount + 1, afterSave.size());
    }

    @Test
    public void shouldUpdateExistingAttribute() throws Exception {
        String uuid = "a1234567-1111-4f20-866b-0ece245615a1";
        AppointmentServiceAttribute attribute =
            appointmentServiceAttributeDao.getAttributeByUuid(uuid);

        assertNotNull(attribute);
        assertEquals("DEPT-001", attribute.getValueReference());

        attribute.setValueReferenceInternal("DEPT-001-UPDATED");

        AppointmentServiceAttribute updatedAttribute =
            appointmentServiceAttributeDao.save(attribute);

        assertEquals(uuid, updatedAttribute.getUuid());
        assertEquals("DEPT-001-UPDATED", updatedAttribute.getValueReference());

        AppointmentServiceAttribute fetchedAttribute =
            appointmentServiceAttributeDao.getAttributeByUuid(uuid);
        assertEquals("DEPT-001-UPDATED", fetchedAttribute.getValueReference());
    }

    @Test
    public void shouldVoidAttribute() throws Exception {
        String uuid = "a1234567-1111-4f20-866b-0ece245615a1";
        AppointmentServiceAttribute attribute =
            appointmentServiceAttributeDao.getAttributeByUuid(uuid);

        assertNotNull(attribute);
        assertFalse(attribute.getVoided());

        List<AppointmentServiceAttribute> beforeVoid =
            appointmentServiceAttributeDao.getAttributesByService(1, false);
        int initialCount = beforeVoid.size();

        attribute.setVoided(true);
        attribute.setVoidReason("Testing void functionality");

        appointmentServiceAttributeDao.save(attribute);

        List<AppointmentServiceAttribute> afterVoid =
            appointmentServiceAttributeDao.getAttributesByService(1, false);
        assertEquals(initialCount - 1, afterVoid.size());
        assertFalse("Should not contain voided attribute",
            afterVoid.stream().anyMatch(a -> a.getUuid().equals(uuid)));

        List<AppointmentServiceAttribute> allAttributes =
            appointmentServiceAttributeDao.getAttributesByService(1, true);
        assertTrue("Should contain voided attribute when includeVoided is true",
            allAttributes.stream().anyMatch(a -> a.getUuid().equals(uuid) && a.getVoided()));
    }

    @Test
    public void shouldGetVoidedAttributeByUuid() throws Exception {
        String uuid = "d4567890-4444-4f20-866b-0ece245615d4";
        AppointmentServiceAttribute attribute =
            appointmentServiceAttributeDao.getAttributeByUuid(uuid);

        assertNotNull(attribute);
        assertTrue("Should be voided", attribute.getVoided());
        assertEquals("Incorrect value", attribute.getVoidReason());
    }

    @Test
    public void shouldMaintainRelationshipsAfterSave() throws Exception {
        // Get service by ID directly from session to avoid eager loading issues
        AppointmentServiceDefinition service = getServiceById(2);
        AppointmentServiceAttributeType attributeType =
            appointmentServiceAttributeTypeDao.getAttributeTypeByUuid("d7477c21-444f-4ff0-a48f-b87b61c4b8a8");

        assertNotNull(service);
        assertNotNull(attributeType);

        AppointmentServiceAttribute newAttribute = new AppointmentServiceAttribute();
        newAttribute.setAppointmentService(service);
        newAttribute.setAttributeType(attributeType);
        newAttribute.setValueReferenceInternal("DEPT-999");
        newAttribute.setVoided(false);

        AppointmentServiceAttribute savedAttribute =
            appointmentServiceAttributeDao.save(newAttribute);

        assertNotNull(savedAttribute.getAppointmentService());
        assertEquals(service.getAppointmentServiceId(),
            savedAttribute.getAppointmentService().getAppointmentServiceId());

        assertNotNull(savedAttribute.getAttributeType());
        assertEquals(attributeType.getAppointmentServiceAttributeTypeId(),
            savedAttribute.getAttributeType().getAppointmentServiceAttributeTypeId());

        AppointmentServiceAttribute fetchedAttribute =
            appointmentServiceAttributeDao.getAttributeByUuid(savedAttribute.getUuid());

        assertNotNull(fetchedAttribute.getAppointmentService());
        assertNotNull(fetchedAttribute.getAttributeType());
        assertEquals(service.getAppointmentServiceId(),
            fetchedAttribute.getAppointmentService().getAppointmentServiceId());
        assertEquals(attributeType.getAppointmentServiceAttributeTypeId(),
            fetchedAttribute.getAttributeType().getAppointmentServiceAttributeTypeId());
    }
}
