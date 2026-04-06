package org.openmrs.module.appointments.events.advice;

import com.google.common.collect.Sets;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bahmni.module.eventoutbox.EMREvent;
import org.openmrs.api.context.Context;
import org.openmrs.module.appointments.events.publisher.AppointmentEventPublisher;
import org.openmrs.module.appointments.model.AppointmentServiceDefinition;
import org.springframework.aop.AfterReturningAdvice;

import java.lang.reflect.Method;
import java.util.Set;

import static org.openmrs.module.appointments.constants.AppointmentsEventRecordsConstants.SERVICE_CATEGORY;
import static org.openmrs.module.appointments.constants.AppointmentsEventRecordsConstants.SERVICE_RAISE_EVENT_GLOBAL_PROPERTY;
import static org.openmrs.module.appointments.constants.AppointmentsEventRecordsConstants.SERVICE_URL_PATTERN_GLOBAL_PROPERTY;
import static org.openmrs.module.appointments.constants.AppointmentsEventRecordsConstants.SERVICE_DEFAULT_URL_PATTERN;

public class AppointmentServiceDefinitionEventsAdvice implements AfterReturningAdvice {

    private final Logger log = LogManager.getLogger(AppointmentServiceDefinitionEventsAdvice.class);
    private final AppointmentEventPublisher eventPublisher;
    private final Set<String> emrMethodNames = Sets.newHashSet("save", "voidAppointmentService");
    private static final String TITLE = "Appointment Service";

    public AppointmentServiceDefinitionEventsAdvice() {
        this.eventPublisher = Context.getRegisteredComponent("appointmentEventPublisher", AppointmentEventPublisher.class);
    }

    @Override
    public void afterReturning(Object returnValue, Method method, Object[] arguments, Object target) {
        if (emrMethodNames.contains(method.getName())) {
            boolean shouldPublish = Boolean.parseBoolean(
                    Context.getAdministrationService().getGlobalProperty(SERVICE_RAISE_EVENT_GLOBAL_PROPERTY, "true"));
            if (shouldPublish) {
                AppointmentServiceDefinition serviceDefinition = (AppointmentServiceDefinition) returnValue;
                String content = Context.getAdministrationService()
                        .getGlobalProperty(SERVICE_URL_PATTERN_GLOBAL_PROPERTY, SERVICE_DEFAULT_URL_PATTERN)
                        .replace("{uuid}", serviceDefinition.getUuid());
                EMREvent<AppointmentServiceDefinition> emrEvent = new EMREvent<>(serviceDefinition, SERVICE_CATEGORY, TITLE, null, content);
                eventPublisher.publishEMREvent(emrEvent);
                log.info("Successfully published EMR event for appointment service uuid : " + serviceDefinition.getUuid());
            }
        }
    }
}
