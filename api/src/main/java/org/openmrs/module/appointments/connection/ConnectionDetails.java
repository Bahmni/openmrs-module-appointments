package org.openmrs.module.appointments.connection;

import org.openmrs.module.appointments.properties.AppointmentProperties;

public class ConnectionDetails {
    private static String AUTH_URI;
    private static String OPENMRS_USER;
    private static String OPENMRS_PASSWORD;
    private static int OPENMRS_WEBCLIENT_CONNECT_TIMEOUT;
    private static int OPENMRS_WEBCLIENT_READ_TIMEOUT;

    public void setAuthUri(String authURINonstatic) {
        String uriProperty = AppointmentProperties.getProperty("openmrs.auth.uri");
        if (uriProperty!=null || !uriProperty.isEmpty())
            ConnectionDetails.AUTH_URI=uriProperty;
        else
            ConnectionDetails.AUTH_URI = authURINonstatic;
    }

    public void setOpenmrsUser(String openmrsUserNonstatic) {
        String openmrsUserProperty = AppointmentProperties.getProperty("openmrs.user");
        if (openmrsUserProperty!=null || !openmrsUserProperty.isEmpty())
            ConnectionDetails.OPENMRS_USER=openmrsUserProperty;
        else
            ConnectionDetails.OPENMRS_USER = openmrsUserNonstatic;
    }

    public void setOpenmrsPassword(String openmrsPasswordNonstatic) {
        String openmrsPasswordProperty = AppointmentProperties.getProperty("openmrs.password");
        if (openmrsPasswordProperty!=null || !openmrsPasswordProperty.isEmpty())
            ConnectionDetails.OPENMRS_PASSWORD=openmrsPasswordProperty;
        else
            ConnectionDetails.OPENMRS_PASSWORD = openmrsPasswordNonstatic;
    }
    public void setOpenmrsWebclientReadTimeout(int connectionTimeNonstatic) {
        String connectionTimeoutProperty = AppointmentProperties.getProperty("openmrs.connectionTimeoutInMilliseconds");
        if (connectionTimeoutProperty!=null || !connectionTimeoutProperty.isEmpty())
            ConnectionDetails.OPENMRS_WEBCLIENT_CONNECT_TIMEOUT= Integer.parseInt(connectionTimeoutProperty);
        else
            ConnectionDetails.OPENMRS_WEBCLIENT_CONNECT_TIMEOUT = connectionTimeNonstatic;
    }

    public void setOpenmrsWebclientConnectTimeout(int replyTimeNonstatic) {
        String replyTimeNonstaticProperty = AppointmentProperties.getProperty("openmrs.replyTimeoutInMilliseconds");
        if (replyTimeNonstaticProperty!=null || !replyTimeNonstaticProperty.isEmpty())
            ConnectionDetails.OPENMRS_WEBCLIENT_READ_TIMEOUT= Integer.parseInt(replyTimeNonstaticProperty);
        else
            ConnectionDetails.OPENMRS_WEBCLIENT_READ_TIMEOUT = replyTimeNonstatic;
    }
    public static org.bahmni.webclients.ConnectionDetails get() {
        return new org.bahmni.webclients.ConnectionDetails(
                AUTH_URI,
                OPENMRS_USER,
                OPENMRS_PASSWORD,
                OPENMRS_WEBCLIENT_CONNECT_TIMEOUT,
                OPENMRS_WEBCLIENT_READ_TIMEOUT);
    }
}
