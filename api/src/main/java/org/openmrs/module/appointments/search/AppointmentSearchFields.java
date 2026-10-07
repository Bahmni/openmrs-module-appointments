package org.openmrs.module.appointments.search;

public final class AppointmentSearchFields {
    private AppointmentSearchFields() {
    }

    public static final String APPOINTMENT_DATE = "startDate";
    public static final String LOCATION = "location.uuid";
    public static final String SERVICE_TYPE = "serviceType";
    public static final String APPOINTMENT_NUMBER = "number";

    public static final String SERVICE_ATTRIBUTE_KIND  = "service.attributeType.kind";
    public static final String SERVICE_ATTRIBUTE_VALUE = "service.attributeType.value";
}

