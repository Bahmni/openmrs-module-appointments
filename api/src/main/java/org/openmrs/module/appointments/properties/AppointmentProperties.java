package org.openmrs.module.appointments.properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.util.OpenmrsUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
public class AppointmentProperties {


    public static Properties properties;
    private static Log log = LogFactory.getLog(AppointmentProperties.class);

    public static void load() {
        String propertyFilePath = new File(OpenmrsUtil.getApplicationDataDirectory(), "appointment.properties").getAbsolutePath();
        File propertyFile = new File(propertyFilePath);

        if (!propertyFile.exists()) {
            log.warn(String.format("Property file not found: %s", propertyFilePath));
            return;
        }
        log.info(String.format("Reading openmrs properties from: %s", propertyFilePath));
        try {
            properties = new Properties(System.getProperties());
            properties.load(new FileInputStream(propertyFile));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static String getProperty(String key) {
        if (properties == null) {
            load();
        }
        return properties.getProperty(key);
    }

    public static void initalize(Properties props) {
        properties = props;
    }
}
