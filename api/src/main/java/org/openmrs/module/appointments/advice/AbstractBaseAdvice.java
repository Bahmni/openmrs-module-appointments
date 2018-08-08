package org.openmrs.module.appointments.advice;

import org.ict4h.atomfeed.server.repository.AllEventRecordsQueue;
import org.ict4h.atomfeed.server.repository.jdbc.AllEventRecordsQueueJdbcImpl;
import org.ict4h.atomfeed.server.service.Event;
import org.ict4h.atomfeed.server.service.EventServiceImpl;
import org.ict4h.atomfeed.transaction.AFTransactionWorkWithoutResult;
import org.joda.time.DateTime;
import org.openmrs.api.context.Context;
import org.openmrs.module.atomfeed.transaction.support.AtomFeedSpringTransactionManager;
import org.springframework.aop.AfterReturningAdvice;
import org.springframework.transaction.PlatformTransactionManager;

import java.lang.reflect.Method;
import java.net.URI;
import java.util.UUID;

public abstract class AbstractBaseAdvice implements AfterReturningAdvice {

    private EventServiceImpl eventService;
    private AtomFeedSpringTransactionManager atomFeedSpringTransactionManager;


    public AbstractBaseAdvice() {
        atomFeedSpringTransactionManager = new AtomFeedSpringTransactionManager(getSpringPlatformTransactionManager());
        AllEventRecordsQueue allEventRecordsQueue = new AllEventRecordsQueueJdbcImpl(atomFeedSpringTransactionManager);
        this.eventService = new EventServiceImpl(allEventRecordsQueue);
    }

    protected PlatformTransactionManager getSpringPlatformTransactionManager() {
        return Context.getRegisteredComponents(PlatformTransactionManager.class).get(0);
    }


    private void raiseEvent(String contents, String title, String category) {
        final Event event = new Event(UUID.randomUUID().toString(), title, DateTime.now(), (URI) null, contents, category);
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

    @Override
    public void afterReturning(Object returnValue, Method method, Object[] arguments, Object target) throws Throwable {
        if (shouldRaiseEvent() && shouldRaiseEventForMethod(method.getName()))
            raiseEvent(getContents(returnValue), getTitle(), getCategory());
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
