package org.openmrs.module.appointments.web;

import org.openmrs.Person;
import org.openmrs.api.context.Context;

@org.springframework.test.context.ContextConfiguration(locations = {"classpath:TestingApplicationContext.xml"}, inheritLocations = true)
public abstract class BaseIntegrationTest extends BaseWebControllerTest {

    @Override
    public void executeDataSet(String datasetFilename) {
        super.executeDataSet(datasetFilename);
        // the datasets turn person 1, already loaded and cached as the authenticated user's person, into a patient, which
        // Hibernate 7 cannot reconcile with the cached Person instance, so drop the session and the cached persons
        Context.clearSession();
        Context.evictAllEntities(Person.class);
    }
}
