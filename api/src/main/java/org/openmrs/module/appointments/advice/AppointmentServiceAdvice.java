package org.openmrs.module.appointments.advice;

import org.ict4h.atomfeed.server.repository.AllEventRecordsQueue;
import org.ict4h.atomfeed.server.repository.jdbc.AllEventRecordsQueueJdbcImpl;
import org.ict4h.atomfeed.server.service.Event;
import org.ict4h.atomfeed.server.service.EventServiceImpl;
import org.ict4h.atomfeed.transaction.AFTransactionWorkWithoutResult;
import org.joda.time.DateTime;
import org.openmrs.api.context.Context;
import org.openmrs.module.appointments.model.AppointmentService;
import org.openmrs.module.atomfeed.transaction.support.AtomFeedSpringTransactionManager;
import org.springframework.aop.AfterReturningAdvice;
import org.springframework.transaction.PlatformTransactionManager;

import java.lang.reflect.Method;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.UUID;

public class AppointmentServiceAdvice implements AfterReturningAdvice {

    private static final String TITLE = "Appointment Service";
    private static final String CATEGORY = "appointmentservice";
    private static final ArrayList<String> METHOD_NAMES = new ArrayList<>(Arrays.asList("save", "voidAppointmentService"));
    private static final String DEFAULT_URL_PATTERN = "/openmrs/ws/rest/v1/appointmentService?uuid={uuid}";
    private static final String RAISE_EVENT_GLOBAL_PROPERTY = "atomfeed.publish.eventsForAppointmentService";
    private static final String URL_PATTERN_GLOBAL_PROPERTY = "atomfeed.event.urlPatternForAppointmentService";


    private EventServiceImpl eventService;
    private AtomFeedSpringTransactionManager atomFeedSpringTransactionManager;

    public AppointmentServiceAdvice() {
        atomFeedSpringTransactionManager = new AtomFeedSpringTransactionManager(getSpringPlatformTransactionManager());
        AllEventRecordsQueue allEventRecordsQueue = new AllEventRecordsQueueJdbcImpl(atomFeedSpringTransactionManager);
        this.eventService = new EventServiceImpl(allEventRecordsQueue);
    }

    private boolean shouldCreateEventForMethod(String methodName){
        return METHOD_NAMES.contains(methodName);
    }

    @Override
    public void afterReturning(Object returnValue, Method method, Object[] arguments, Object target) throws Throwable {
        if (shouldRaiseEvent() && shouldCreateEventForMethod(method.getName()) ){
            String contents = getUrlPattern().replace("{uuid}", ((AppointmentService) returnValue).getUuid());
            final Event event = new Event(UUID.randomUUID().toString(), TITLE, DateTime.now(), (URI) null, contents, CATEGORY);
            atomFeedSpringTransactionManager.executeWithTransaction(
                    new AFTransactionWorkWithoutResult() {
                        @Override
                        public PropagationDefinition getTxPropagationDefinition() {
                            return PropagationDefinition.PROPAGATION_REQUIRED;
                        }

                        @Override
                        protected void doInTransaction() {
                            eventService.notify(event);
                        }
                    }
            );
        }

    }

    private boolean shouldRaiseEvent() {
        return Boolean.valueOf(Context.getAdministrationService().getGlobalProperty(RAISE_EVENT_GLOBAL_PROPERTY));
    }

    private String getUrlPattern() {
        return Context.getAdministrationService().getGlobalProperty(URL_PATTERN_GLOBAL_PROPERTY, DEFAULT_URL_PATTERN);
    }

    private PlatformTransactionManager getSpringPlatformTransactionManager() {
        return Context.getRegisteredComponents(PlatformTransactionManager.class).get(0);
    }
}
