package org.openmrs.module.appointments.events.advice;

import com.google.common.collect.Sets;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openmrs.api.context.Context;
import org.openmrs.module.appointments.events.publisher.AppointmentEventPublisher;
import org.openmrs.module.appointments.events.AppointmentEventType;
import org.openmrs.module.appointments.model.AppointmentRecurringPattern;
import org.openmrs.module.appointments.events.RecurringAppointmentEvent;
import org.springframework.aop.AfterReturningAdvice;
import org.springframework.aop.MethodBeforeAdvice;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.openmrs.module.appointments.events.AppointmentEventType.BAHMNI_RECURRING_APPOINTMENT_CREATED;
import static org.openmrs.module.appointments.events.AppointmentEventType.BAHMNI_RECURRING_APPOINTMENT_UPDATED;

public class RecurringAppointmentEventsAdvice implements AfterReturningAdvice, MethodBeforeAdvice {

    private final Logger log = LogManager.getLogger(RecurringAppointmentEventsAdvice.class);
    private final AppointmentEventPublisher eventPublisher;
    private final ThreadLocal<Map<String, Integer>> threadLocal = new ThreadLocal<>();
    private final String RECURRING_APPOINTMENT_ID_KEY = "recurringAppointmentId";
    private final Set<String> adviceMethodNames = Sets.newHashSet("validateAndSave");
    private final RecurringAppointmentEvent recurringAppointmentEvent;

    public RecurringAppointmentEventsAdvice() {
        this.eventPublisher = Context.getRegisteredComponent("appointmentEventPublisher", AppointmentEventPublisher.class);
        this.recurringAppointmentEvent = Context.getRegisteredComponent("recurringAppointmentEvent", RecurringAppointmentEvent.class);
    }

    @Override
    public void afterReturning(Object returnValue, Method method, Object[] arguments, Object target) {
        if (adviceMethodNames.contains(method.getName())) {
            Map<String, Integer> appointmentInfo = threadLocal.get();
            if (appointmentInfo != null) {
                AppointmentEventType eventType = appointmentInfo.get(RECURRING_APPOINTMENT_ID_KEY) == null ? BAHMNI_RECURRING_APPOINTMENT_CREATED : BAHMNI_RECURRING_APPOINTMENT_UPDATED;
                threadLocal.remove();
                AppointmentRecurringPattern appointmentRecurringPattern = (AppointmentRecurringPattern) returnValue;
                recurringAppointmentEvent.createRecurringAppointmentEvent(eventType, appointmentRecurringPattern);
                eventPublisher.publishEvent(recurringAppointmentEvent);
                log.info("Successfully published event with uuid : " + recurringAppointmentEvent.getPayloadId());
            }
        }
    }

    @Override
    public void before(Method method, Object[] objects, Object o) {
        if (adviceMethodNames.contains(method.getName())) {
            AppointmentRecurringPattern appointmentRecurringPattern = (AppointmentRecurringPattern) objects[0];
            Map<String, Integer> appointmentInfo = new HashMap<>(1);
            appointmentInfo.put(RECURRING_APPOINTMENT_ID_KEY, appointmentRecurringPattern.getId());
            threadLocal.set(appointmentInfo);
        }
    }
}