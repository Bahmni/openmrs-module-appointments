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

    private final AppointmentServiceDefinitionAdvice appointmentServiceDefinitionAdvice;

    private final AppointmentAdvice appointmentAdvice;

    private final RecurringAppointmentsAdvice recurringAppointmentsAdvice;

    public AtomFeedAdviceProvider() {
        this(new AppointmentServiceDefinitionAdvice(), new AppointmentAdvice(), new RecurringAppointmentsAdvice());
    }

    public AtomFeedAdviceProvider(AppointmentServiceDefinitionAdvice appointmentServiceDefinitionAdvice,
                                  AppointmentAdvice appointmentAdvice,
                                  RecurringAppointmentsAdvice recurringAppointmentsAdvice) {
        this.appointmentServiceDefinitionAdvice = appointmentServiceDefinitionAdvice;
        this.appointmentAdvice = appointmentAdvice;
        this.recurringAppointmentsAdvice = recurringAppointmentsAdvice;
    }

    protected void addAdvice(Class<?> advicePoint, Advice advice) {
        log.info("Adding AOP: " + advicePoint + " -> " + advice);
        Context.addAdvice(advicePoint, advice);
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        addAdvice(AppointmentServiceDefinitionService.class, appointmentServiceDefinitionAdvice);
        addAdvice(AppointmentsService.class, appointmentAdvice);
        addAdvice(AppointmentRecurringPatternService.class, recurringAppointmentsAdvice);
    }
}
