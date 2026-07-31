package org.openmrs.module.appointments.search;

public final class AppointmentSearchFields {

    public static final String APPOINTMENT_DATE = "appointment.startDate";
    public static final String LOCATION = "location.uuid";
    public static final String SERVICE_TYPE = "appointment.serviceType";
    public static final String APPOINTMENT_NUMBER = "appointment.number";

    // Generic service-attribute filter (mirrors episodes' program.attributeType.kind / .value pattern).
    // Use these two together (inside a group) to say:
    //   "the appointment's service has an attribute of type <kind> whose value = <value>"
    public static final String SERVICE_ATTRIBUTE_KIND  = "appointment.service.attributeType.kind";
    public static final String SERVICE_ATTRIBUTE_VALUE = "appointment.service.attributeType.value";
}

