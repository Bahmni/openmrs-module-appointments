package org.openmrs.module.appointments.web.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.openmrs.Person;
import org.openmrs.PersonName;
import org.openmrs.User;
import org.openmrs.api.context.Context;
import org.openmrs.module.appointments.web.BaseIntegrationTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

public class AppointmentSearchControllerIT extends BaseIntegrationTest {

    private static final String SEARCH_REQUEST = "{\"entity\": \"appointment\", \"criteria\": {\"field\": \"startDate\","
            + " \"comparator\": \"gt\", \"value\": \"2024-01-01T00:00:00.000+0000\"}}";

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Test
    public void shouldSearchWhenTheUserHasTheViewAppointmentsPrivilege() throws Exception {
        MockHttpServletResponse response = search();

        assertEquals(200, response.getStatus());
        assertEquals(true, new ObjectMapper().readValue(response.getContentAsString(), Map.class).get("success"));
    }

    @Test
    public void shouldRespondForbiddenWhenTheUserLacksTheViewAppointmentsPrivilege() throws Exception {
        Person person = new Person();
        person.setGender("F");
        person.addName(new PersonName("Search", null, "Denied"));
        User user = new User(person);
        user.setUsername("search-denied");
        Context.getUserService().createUser(user, "Search123");
        Context.logout();
        Context.authenticate("search-denied", "Search123");

        MockHttpServletResponse response = search();

        assertEquals(403, response.getStatus());
        Map<?, ?> error = (Map<?, ?>) new ObjectMapper().readValue(response.getContentAsString(), Map.class).get("error");
        assertEquals(403, error.get("status"));
    }

    // MockMvc dispatches like the real DispatcherServlet, so a denial from the service's @Authorized check reaches
    // AppointmentSearchExceptionHandler the way it does on a server
    private MockHttpServletResponse search() throws Exception {
        return MockMvcBuilders.webAppContextSetup(webApplicationContext).build()
                .perform(post("/rest/v1/appointmentSearch").contentType(MediaType.APPLICATION_JSON).content(SEARCH_REQUEST))
                .andReturn().getResponse();
    }
}
