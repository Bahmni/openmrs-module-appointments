package org.openmrs.module.appointments.advice;

import org.openmrs.api.context.Context;
import org.openmrs.module.appointments.model.Appointment;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;

import static java.util.Objects.isNull;
import static org.openmrs.module.appointments.constants.AppointmentsEventRecordsConstants.CATEGORY;
import static org.openmrs.module.appointments.constants.AppointmentsEventRecordsConstants.RAISE_EVENT_GLOBAL_PROPERTY;

public class AppointmentAdvice extends AbstractBaseAdvice {

    private static final String TITLE = "Appointment";
    private static final ArrayList<String> METHOD_NAMES = new ArrayList<>(Arrays.asList("validateAndSave", "changeStatus", "undoStatusChange"));
    private static final ArrayList<String> VOIDED_METHOD_NAMES = new ArrayList<>(Arrays.asList("changeStatus", "undoStatusChange"));
    private static final String URL_PATTERN_GLOBAL_PROPERTY = "atomfeed.event.urlPatternForAppointments";
    private static final String DEFAULT_URL_PATTERN = "/openmrs/ws/rest/v1/appointment?uuid={uuid}";


    @Override
    protected String getContents(Object returnValue) {
        return getUrlPattern(URL_PATTERN_GLOBAL_PROPERTY, DEFAULT_URL_PATTERN)
                .replace("{uuid}", ((Appointment) returnValue).getUuid());
    }

    @Override
    public void afterReturning(Object returnValue, Method method, Object[] arguments, Object target) throws Throwable {
        // Hacky fix for void method
        Object processedReturnValue = returnValue;

        if (VOIDED_METHOD_NAMES.contains(method.getName()) && !isNull(arguments))
            processedReturnValue = arguments[0];

        super.afterReturning(processedReturnValue, method, arguments, target);
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
    protected boolean shouldRaiseEventForMethod(String methodName) {
        return METHOD_NAMES.contains(methodName);
    }

    @Override
    protected boolean shouldRaiseEvent() {
        return Boolean.valueOf(Context.getAdministrationService().getGlobalProperty(RAISE_EVENT_GLOBAL_PROPERTY));
    }
}
