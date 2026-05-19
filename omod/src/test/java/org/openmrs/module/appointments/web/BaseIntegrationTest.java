package org.openmrs.module.appointments.web;

import java.util.Locale;
import java.util.TimeZone;
import org.junit.BeforeClass;

@org.springframework.test.context.ContextConfiguration(locations = {"classpath:TestingApplicationContext.xml"}, inheritLocations = true)
public abstract class BaseIntegrationTest extends BaseWebControllerTest {

    @BeforeClass
    public static void pinJvmTimeZoneAndLocale() {
        // Several tests assert against Date.toString() / java.sql.Time formatting,
        // which depends on the default TimeZone and Locale. Pin them here so the
        // tests pass regardless of how the JVM was launched (mvn argLine, IDE, etc.).
        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Kolkata"));
        Locale.setDefault(Locale.US);
    }

    @Override
    public void executeDataSet(String datasetFilename) {
        super.executeDataSet(datasetFilename);
        // DBUnit inserts data via JDBC, bypassing both the Hibernate session and
        // the L2 cache. clearHibernateCache() evicts both, so subsequent loads see
        // the newly-inserted rows. Without it, joined-subclass entities like
        // Patient/Person fail with WrongClassException because the L2 cache has
        // the Person side already loaded when the Patient row arrives.
        // (Context.clearSession() alone is not enough — it only touches the L1
        // session, leaving the L2 cache stale.)
        clearHibernateCache();
    }
}
