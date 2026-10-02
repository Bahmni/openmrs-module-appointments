package org.openmrs.module.appointments.web.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openmrs.module.appointments.web.BaseIntegrationTest;
import org.openmrs.module.appointments.web.contract.AppointmentServiceAttributeTypeResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletResponse;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class AppointmentServiceAttributeTypeControllerIT extends BaseIntegrationTest {

    @Autowired
    AppointmentServiceAttributeTypeController controller;

    @BeforeEach
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
        assertEquals(2, attributeTypes.size(), "Should return 2 non-retired attribute types");

        for (AppointmentServiceAttributeTypeResponse attr : attributeTypes) {
            assertFalse(attr.getRetired(), "All attribute types should be non-retired");
            assertNotNull(attr.getUuid(), "UUID should not be null");
            assertNotNull(attr.getName(), "Name should not be null");
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
        assertEquals(3, attributeTypes.size(), "Should return all 3 attribute types including retired");

        boolean hasRetired = attributeTypes.stream().anyMatch(AppointmentServiceAttributeTypeResponse::getRetired);
        assertTrue(hasRetired, "Should include at least one retired attribute type");
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

        assertNotNull(attributeType.getUuid(), "UUID should be present");
        assertNotNull(attributeType.getName(), "Name should be present");
        assertNotNull(attributeType.getDescription(), "Description should be present");
        assertNotNull(attributeType.getDatatype(), "Datatype should be present");
        assertNotNull(attributeType.getMinOccurs(), "MinOccurs should be present");
        assertNotNull(attributeType.getRetired(), "Retired status should be present");
    }

    @Test
    public void shouldReturnAttributeTypesInCorrectFormat() throws Exception {
        MockHttpServletResponse response = handle(newGetRequest("/rest/v1/appointment-service-attribute-types"));

        List<AppointmentServiceAttributeTypeResponse> attributeTypes = deserialize(
                response,
                new TypeReference<List<AppointmentServiceAttributeTypeResponse>>() {});

        assertNotNull(attributeTypes);
        assertFalse(attributeTypes.isEmpty(), "Should not return empty list");

        AppointmentServiceAttributeTypeResponse firstType = attributeTypes.get(0);

        assertNotNull(firstType.getUuid());
        assertNotNull(firstType.getName());
        assertNotNull(firstType.getDatatype());
        assertTrue(firstType.getDatatype().contains("org.openmrs.customdatatype"), "Datatype should be fully qualified classname");
    }
}
