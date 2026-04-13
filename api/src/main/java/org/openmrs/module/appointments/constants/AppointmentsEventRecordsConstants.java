package org.openmrs.module.appointments.constants;

public class AppointmentsEventRecordsConstants {
    public static final String CATEGORY = "appointments";
    public static final String RAISE_EVENT_GLOBAL_PROPERTY = "eventoutbox.publish.eventsForAppointments";
    public static final String URL_PATTERN_GLOBAL_PROPERTY = "eventoutbox.event.urlPatternForAppointments";
    public static final String DEFAULT_URL_PATTERN = "/openmrs/ws/rest/v1/appointments/{uuid}";

    public static final String RECURRING_CATEGORY = "appointments";
    public static final String RECURRING_URL_PATTERN_GLOBAL_PROPERTY = "eventoutbox.event.urlPatternForRecurringAppointments";
    public static final String RECURRING_DEFAULT_URL_PATTERN = "/openmrs/ws/rest/v1/recurring-appointments?uuid={uuid}";

    public static final String SERVICE_CATEGORY = "appointmentservice";
    public static final String SERVICE_RAISE_EVENT_GLOBAL_PROPERTY = "eventoutbox.publish.eventsForAppointmentService";
    public static final String SERVICE_URL_PATTERN_GLOBAL_PROPERTY = "eventoutbox.event.urlPatternForAppointmentService";
    public static final String SERVICE_DEFAULT_URL_PATTERN = "/openmrs/ws/rest/v1/appointmentService?uuid={uuid}";
}
