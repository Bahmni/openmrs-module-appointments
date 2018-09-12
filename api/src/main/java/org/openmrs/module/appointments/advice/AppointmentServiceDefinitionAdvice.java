package org.openmrs.module.appointments.advice;

import org.openmrs.api.context.Context;
import org.openmrs.module.appointments.model.AppointmentServiceDefinition;

import java.util.ArrayList;
import java.util.Arrays;

public class AppointmentServiceDefinitionAdvice extends AbstractBaseAdvice {

    private static final String TITLE = "Appointment Service";
    private static final String CATEGORY = "appointmentservice";
    private static final ArrayList<String> METHOD_NAMES = new ArrayList<>(Arrays.asList("save", "voidAppointmentService"));
    private static final String DEFAULT_URL_PATTERN = "/openmrs/ws/rest/v1/appointmentService?uuid={uuid}";
    private static final String RAISE_EVENT_GLOBAL_PROPERTY = "atomfeed.publish.eventsForAppointmentService";
    private static final String URL_PATTERN_GLOBAL_PROPERTY = "atomfeed.event.urlPatternForAppointmentService";

    @Override
    protected String getContents(Object returnValue) {
        return getUrlPattern(URL_PATTERN_GLOBAL_PROPERTY, DEFAULT_URL_PATTERN)
                .replace("{uuid}", ((AppointmentServiceDefinition) returnValue).getUuid());
    }

    @Override
    protected String getTitle() {
        return TITLE;
    }

    @Override
    protected String getCategory() {
        return CATEGORY;
    }

    @Override
    protected boolean shouldRaiseEventForMethod(String methodName){
        return METHOD_NAMES.contains(methodName);
    }

    @Override
    protected boolean shouldRaiseEvent() {
        return Boolean.valueOf(Context.getAdministrationService().getGlobalProperty(RAISE_EVENT_GLOBAL_PROPERTY));
    }
}
