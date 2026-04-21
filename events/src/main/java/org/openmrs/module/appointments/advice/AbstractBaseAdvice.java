package org.openmrs.module.appointments.advice;

import org.bahmni.module.eventoutbox.EMREvent;
import org.openmrs.api.context.Context;
import org.openmrs.module.appointments.events.publisher.AppointmentEventPublisher;
import org.springframework.aop.AfterReturningAdvice;

import java.lang.reflect.Method;

public abstract class AbstractBaseAdvice implements AfterReturningAdvice {

    @Override
    public void afterReturning(Object returnValue, Method method, Object[] arguments, Object target) throws Throwable {
        if (shouldRaiseEvent() && shouldRaiseEventForMethod(method.getName()))
            raiseEvent(returnValue);
    }

    private void raiseEvent(Object returnValue) {
        String contents = getContents(returnValue);
        EMREvent<?> emrEvent = new EMREvent<>(returnValue, getCategory(), getTitle(), null, contents);
        AppointmentEventPublisher publisher = Context.getRegisteredComponent(
                "appointmentEventPublisher", AppointmentEventPublisher.class);
        publisher.publish(emrEvent);
    }

    protected String getUrlPattern(String urlPatternGlobalProperty, String defaultValue) {
        return Context.getAdministrationService().getGlobalProperty(urlPatternGlobalProperty, defaultValue);
    }

    protected abstract String getContents(Object returnValue);

    protected abstract String getTitle();

    protected abstract String getCategory();

    protected abstract boolean shouldRaiseEventForMethod(String methodName);

    protected abstract boolean shouldRaiseEvent();
}
