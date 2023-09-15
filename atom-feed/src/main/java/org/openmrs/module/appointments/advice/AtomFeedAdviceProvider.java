package org.openmrs.module.appointments.advice;

import org.aopalliance.aop.Advice;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.context.Context;
import org.openmrs.module.appointments.service.AppointmentRecurringPatternService;
import org.openmrs.module.appointments.service.AppointmentServiceDefinitionService;
import org.openmrs.module.appointments.service.AppointmentsService;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * Registers AOP advice when the context starts up.
 * This is done instead of declaring advice in config.xml in order to allow it to be conditionally loaded
 */
public class AtomFeedAdviceProvider implements ApplicationContextAware {

    private static final Log log = LogFactory.getLog(AtomFeedAdviceProvider.class);

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        addAdvice(AppointmentServiceDefinitionService.class, new AppointmentServiceDefinitionAdvice());
        addAdvice(AppointmentsService.class, new AppointmentAdvice());
        addAdvice(AppointmentRecurringPatternService.class, new RecurringAppointmentsAdvice());
    }

    private void addAdvice(Class<?> advicePoint, Advice advice) {
        log.info("Adding AOP: " + advicePoint.getSimpleName() + " -> " + advice.getClass().getSimpleName());
        Context.addAdvice(advicePoint, advice);
    }
}
