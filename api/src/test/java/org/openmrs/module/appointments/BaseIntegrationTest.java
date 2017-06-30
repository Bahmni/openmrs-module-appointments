package org.openmrs.module.appointments;
import org.openmrs.web.test.BaseModuleWebContextSensitiveTest;

@org.springframework.test.context.ContextConfiguration(locations = {"classpath:TestingApplicationContext.xml"}, inheritLocations = true)
public class BaseIntegrationTest extends BaseModuleWebContextSensitiveTest {
}
