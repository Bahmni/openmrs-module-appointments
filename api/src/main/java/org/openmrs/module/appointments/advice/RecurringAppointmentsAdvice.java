package org.openmrs.module.appointments.advice;

import org.openmrs.api.context.Context;
import org.openmrs.module.appointments.model.Appointment;
import org.openmrs.module.appointments.model.AppointmentRecurringPattern;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;

import static org.openmrs.module.appointments.constants.AppointmentsEventRecordsConstants.CATEGORY;
import static org.openmrs.module.appointments.constants.AppointmentsEventRecordsConstants.DEFAULT_URL_PATTERN;
import static org.openmrs.module.appointments.constants.AppointmentsEventRecordsConstants.RAISE_EVENT_GLOBAL_PROPERTY;
import static org.openmrs.module.appointments.constants.AppointmentsEventRecordsConstants.URL_PATTERN_GLOBAL_PROPERTY;

public class RecurringAppointmentsAdvice extends AbstractBaseAdvice {

    public static final String TITLE = "RecurringAppointment";
    public static final String VALIDATE_AND_SAVE = "validateAndSave";
    private static final ArrayList<String> METHOD_NAMES = new ArrayList<>(Collections.singletonList(VALIDATE_AND_SAVE));

    @Override
    public void afterReturning(Object returnValue, Method method, Object[] arguments, Object target) throws Throwable {
        Object processedReturnValue = returnValue;
        if (VALIDATE_AND_SAVE.equals(method.getName())) {
            AppointmentRecurringPattern appointmentRecurringPattern = (AppointmentRecurringPattern) processedReturnValue;
            for (Appointment appointment : appointmentRecurringPattern.getAppointments()) {
                super.afterReturning(appointment, method, arguments, target);
            }
        }
    }

    @Override
    protected String getContents(Object returnValue) {
        return getUrlPattern(URL_PATTERN_GLOBAL_PROPERTY, DEFAULT_URL_PATTERN)
                .replace("{uuid}", ((Appointment) returnValue).getUuid());
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
