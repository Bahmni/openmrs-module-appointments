package org.openmrs.module.appointments.events.advice;

import com.google.common.collect.Sets;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bahmni.module.eventoutbox.EMREvent;
import org.openmrs.api.context.Context;
import org.openmrs.module.appointments.events.publisher.AppointmentEventPublisher;
import org.openmrs.module.appointments.events.AppointmentEventType;
import org.openmrs.module.appointments.model.Appointment;
import org.openmrs.module.appointments.model.AppointmentRecurringPattern;
import org.openmrs.module.appointments.events.RecurringAppointmentEvent;
import org.springframework.aop.AfterReturningAdvice;
import org.springframework.aop.MethodBeforeAdvice;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.openmrs.module.appointments.constants.AppointmentsEventRecordsConstants.CATEGORY;
import static org.openmrs.module.appointments.constants.AppointmentsEventRecordsConstants.RAISE_EVENT_GLOBAL_PROPERTY;
import static org.openmrs.module.appointments.constants.AppointmentsEventRecordsConstants.RECURRING_URL_PATTERN_GLOBAL_PROPERTY;
import static org.openmrs.module.appointments.constants.AppointmentsEventRecordsConstants.RECURRING_DEFAULT_URL_PATTERN;
import static org.openmrs.module.appointments.events.AppointmentEventType.BAHMNI_RECURRING_APPOINTMENT_CREATED;
import static org.openmrs.module.appointments.events.AppointmentEventType.BAHMNI_RECURRING_APPOINTMENT_UPDATED;

public class RecurringAppointmentEventsAdvice implements AfterReturningAdvice, MethodBeforeAdvice {

    private final Logger log = LogManager.getLogger(RecurringAppointmentEventsAdvice.class);
    private final AppointmentEventPublisher eventPublisher;
    private final ThreadLocal<Map<String, Integer>> threadLocal = new ThreadLocal<>();
    private final String RECURRING_APPOINTMENT_ID_KEY = "recurringAppointmentId";
    private final Set<String> adviceMethodNames = Sets.newHashSet("validateAndSave");
    private final Set<String> emrMethodNames = Sets.newHashSet("validateAndSave", "update", "changeStatus");
    private static final String TITLE = "RecurringAppointments";

    public RecurringAppointmentEventsAdvice() {
        this.eventPublisher = Context.getRegisteredComponent("appointmentEventPublisher", AppointmentEventPublisher.class);
    }

    @Override
    public void afterReturning(Object returnValue, Method method, Object[] arguments, Object target) {
        // Existing block — publishes internal RecurringAppointmentEvent (for SMS etc.) — DO NOT MODIFY
        if (adviceMethodNames.contains(method.getName())) {
            Map<String, Integer> appointmentInfo = threadLocal.get();
            if (appointmentInfo != null) {
                AppointmentEventType eventType = appointmentInfo.get(RECURRING_APPOINTMENT_ID_KEY) == null ? BAHMNI_RECURRING_APPOINTMENT_CREATED : BAHMNI_RECURRING_APPOINTMENT_UPDATED;
                threadLocal.remove();
                AppointmentRecurringPattern appointmentRecurringPattern = (AppointmentRecurringPattern) returnValue;
                RecurringAppointmentEvent recurringAppointmentEvent = new RecurringAppointmentEvent(eventType, appointmentRecurringPattern);
                eventPublisher.publishEvent(recurringAppointmentEvent);
                log.info("Successfully published event with uuid : " + recurringAppointmentEvent.payloadId);
            }
        }

        // EMR outbox block — publishes EMREvent for external consumers (replaces atom feed)
        if (emrMethodNames.contains(method.getName())) {
            boolean shouldPublish = Boolean.parseBoolean(
                    Context.getAdministrationService().getGlobalProperty(RAISE_EVENT_GLOBAL_PROPERTY, "true"));
            if (shouldPublish) {
                List<Appointment> appointments = extractAppointments(method.getName(), returnValue, arguments);
                String urlPattern = Context.getAdministrationService()
                        .getGlobalProperty(RECURRING_URL_PATTERN_GLOBAL_PROPERTY, RECURRING_DEFAULT_URL_PATTERN);
                for (Appointment appointment : appointments) {
                    String content = urlPattern.replace("{uuid}", appointment.getUuid());
                    EMREvent<Appointment> emrEvent = new EMREvent<>(appointment, CATEGORY, TITLE, null, content);
                    eventPublisher.publishEMREvent(emrEvent);
                    log.info("Successfully published EMR event for recurring appointment uuid : " + appointment.getUuid());
                }
            }
        }
    }

    private List<Appointment> extractAppointments(String methodName, Object returnValue, Object[] arguments) {
        List<Appointment> appointments = new ArrayList<>();
        if ("validateAndSave".equals(methodName) || (("update".equals(methodName)) && returnValue instanceof AppointmentRecurringPattern)) {
            appointments.addAll(((AppointmentRecurringPattern) returnValue).getAppointments());
        } else if ("update".equals(methodName) && returnValue instanceof Appointment) {
            appointments = (List<Appointment>) arguments[1];
        } else if ("changeStatus".equals(methodName)) {
            appointments = (List<Appointment>) returnValue;
        }
        return appointments;
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