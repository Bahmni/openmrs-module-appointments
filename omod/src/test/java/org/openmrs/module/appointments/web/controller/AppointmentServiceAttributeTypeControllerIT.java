package org.openmrs.module.appointments.web.controller;

import org.codehaus.jackson.type.TypeReference;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.module.appointments.web.BaseIntegrationTest;
import org.openmrs.module.appointments.web.contract.AppointmentServiceAttributeTypeResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletResponse;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class AppointmentServiceAttributeTypeControllerIT extends BaseIntegrationTest {

    @Autowired
    AppointmentServiceAttributeTypeController controller;

    @Before
    public void setUp() throws Exception {
        executeDataSet("appointmentServiceAttributeTestData.xml");
    }

    @Test
    public void shouldGetAllNonRetiredAttributeTypes() throws Exception {
        MockHttpServletResponse response = handle(newGetRequest("/rest/v1/appointment-service-attribute-types"));

        List<AppointmentServiceAttributeTypeResponse> attributeTypes = deserialize(
                response,
                new TypeReference<List<AppointmentServiceAttributeTypeResponse>>() {});

        assertNotNull(attributeTypes);
        assertEquals("Should return 2 non-retired attribute types", 2, attributeTypes.size());

        for (AppointmentServiceAttributeTypeResponse attr : attributeTypes) {
            assertFalse("All attribute types should be non-retired", attr.getRetired());
            assertNotNull("UUID should not be null", attr.getUuid());
            assertNotNull("Name should not be null", attr.getName());
        }
    }

    @Test
    public void shouldGetAllAttributeTypesIncludingRetired() throws Exception {
        MockHttpServletResponse response = handle(newGetRequest("/rest/v1/appointment-service-attribute-types",
                new Parameter("includeRetired", "true")));

        List<AppointmentServiceAttributeTypeResponse> attributeTypes = deserialize(
                response,
                new TypeReference<List<AppointmentServiceAttributeTypeResponse>>() {});

        assertNotNull(attributeTypes);
        assertEquals("Should return all 3 attribute types including retired", 3, attributeTypes.size());

        boolean hasRetired = attributeTypes.stream().anyMatch(AppointmentServiceAttributeTypeResponse::getRetired);
        assertTrue("Should include at least one retired attribute type", hasRetired);
    }

    @Test
    public void shouldGetAttributeTypeByUuid() throws Exception {
        String uuid = "d7477c21-444f-4ff0-a48f-b87b61c4b8a8";

        MockHttpServletResponse response = handle(newGetRequest("/rest/v1/appointment-service-attribute-types/" + uuid));

        AppointmentServiceAttributeTypeResponse attributeType = deserialize(
                response,
                new TypeReference<AppointmentServiceAttributeTypeResponse>() {});

        assertNotNull(attributeType);
        assertEquals(uuid, attributeType.getUuid());
        assertEquals("Department Code", attributeType.getName());
        assertEquals("Department code for the service", attributeType.getDescription());
        assertEquals("org.openmrs.customdatatype.datatype.FreeTextDatatype", attributeType.getDatatype());
        assertEquals(Integer.valueOf(0), attributeType.getMinOccurs());
        assertFalse(attributeType.getRetired());
    }

    @Test
    public void shouldReturn404ForInvalidUuid() throws Exception {
        String invalidUuid = "00000000-0000-0000-0000-000000000000";

        MockHttpServletResponse response = handle(newGetRequest("/rest/v1/appointment-service-attribute-types/" + invalidUuid));

        assertEquals(404, response.getStatus());
    }

    @Test
    public void shouldReturnAttributeTypeWithAllFields() throws Exception {
        String uuid = "d7477c21-444f-4ff0-a48f-b87b61c4b8a8";

        MockHttpServletResponse response = handle(newGetRequest("/rest/v1/appointment-service-attribute-types/" + uuid));

        AppointmentServiceAttributeTypeResponse attributeType = deserialize(
                response,
                new TypeReference<AppointmentServiceAttributeTypeResponse>() {});

        assertNotNull("UUID should be present", attributeType.getUuid());
        assertNotNull("Name should be present", attributeType.getName());
        assertNotNull("Description should be present", attributeType.getDescription());
        assertNotNull("Datatype should be present", attributeType.getDatatype());
        assertNotNull("MinOccurs should be present", attributeType.getMinOccurs());
        assertNotNull("Retired status should be present", attributeType.getRetired());
    }

    @Test
    public void shouldReturnAttributeTypesInCorrectFormat() throws Exception {
        MockHttpServletResponse response = handle(newGetRequest("/rest/v1/appointment-service-attribute-types"));

        List<AppointmentServiceAttributeTypeResponse> attributeTypes = deserialize(
                response,
                new TypeReference<List<AppointmentServiceAttributeTypeResponse>>() {});

        assertNotNull(attributeTypes);
        assertFalse("Should not return empty list", attributeTypes.isEmpty());

        AppointmentServiceAttributeTypeResponse firstType = attributeTypes.get(0);

        assertNotNull(firstType.getUuid());
        assertNotNull(firstType.getName());
        assertNotNull(firstType.getDatatype());
        assertTrue("Datatype should be fully qualified classname",
                firstType.getDatatype().contains("org.openmrs.customdatatype"));
    }
}
