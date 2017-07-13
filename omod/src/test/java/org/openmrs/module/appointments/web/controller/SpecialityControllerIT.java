package org.openmrs.module.appointments.web.controller;

import org.codehaus.jackson.type.TypeReference;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.module.appointments.service.SpecialityService;
import org.openmrs.module.appointments.web.BaseIntegrationTest;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

public class SpecialityControllerIT extends BaseIntegrationTest{
    @Autowired
    SpecialityController specialityController;

    @Autowired
    SpecialityService specialityService;

    @Before
    public void setUp() throws Exception {
        executeDataSet("specialityTestData.xml");
    }
    @Test
    public void getAllAppointmentServices() throws Exception {
        List specialities = deserialize(handle(newGetRequest("/rest/v1/speciality/all")), new TypeReference<List>() {});
        assertEquals(2, specialities.size());
    }

}
