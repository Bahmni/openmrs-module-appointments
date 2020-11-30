package org.openmrs.module.appointments;
import org.openmrs.web.test.BaseModuleWebContextSensitiveTest;

@org.springframework.test.context.ContextConfiguration(locations = {"classpath:TestingApplicationContext.xml"}, inheritLocations = true)
public abstract class BaseIntegrationTest extends BaseModuleWebContextSensitiveTest {
}
