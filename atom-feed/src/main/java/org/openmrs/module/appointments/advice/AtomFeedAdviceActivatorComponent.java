package org.openmrs.module.appointments.advice;

import org.aopalliance.aop.Advice;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.annotation.OpenmrsProfile;
import org.openmrs.api.context.Context;
import org.openmrs.module.appointments.AppointmentsActivatorComponent;
import org.openmrs.module.appointments.service.AppointmentRecurringPatternService;
import org.openmrs.module.appointments.service.AppointmentServiceDefinitionService;
import org.openmrs.module.appointments.service.AppointmentsService;

/**
 * Registers AOP advice when the context starts up.
 * This is done instead of declaring advice in config.xml in order to allow it to be conditionally loaded
 */
@OpenmrsProfile(modules = { "openmrs-atomfeed:*" })
public class AtomFeedAdviceActivatorComponent implements AppointmentsActivatorComponent {

    private static final Log log = LogFactory.getLog(AtomFeedAdviceActivatorComponent.class);

    private final AppointmentServiceDefinitionAdvice appointmentServiceDefinitionAdvice;

    private final AppointmentAdvice appointmentAdvice;

    private final RecurringAppointmentsAdvice recurringAppointmentsAdvice;

    public AtomFeedAdviceActivatorComponent() {
        this(new AppointmentServiceDefinitionAdvice(), new AppointmentAdvice(), new RecurringAppointmentsAdvice());
    }

    public AtomFeedAdviceActivatorComponent(AppointmentServiceDefinitionAdvice appointmentServiceDefinitionAdvice,
                                            AppointmentAdvice appointmentAdvice,
                                            RecurringAppointmentsAdvice recurringAppointmentsAdvice) {
        this.appointmentServiceDefinitionAdvice = appointmentServiceDefinitionAdvice;
        this.appointmentAdvice = appointmentAdvice;
        this.recurringAppointmentsAdvice = recurringAppointmentsAdvice;
    }

    protected void addAdvice(Class<?> advicePoint, Advice advice) {
        log.info("Adding AOP: " + advicePoint.getSimpleName() + " -> " + advice.getClass().getSimpleName());
        Context.addAdvice(advicePoint, advice);
    }

    protected void removeAdvice(Class<?> advicePoint, Advice advice) {
        log.info("Removing AOP: " + advicePoint.getSimpleName() + " -> " + advice.getClass().getSimpleName());
        Context.removeAdvice(advicePoint, advice);
    }

    @Override
    public void started() {
        addAdvice(AppointmentServiceDefinitionService.class, appointmentServiceDefinitionAdvice);
        addAdvice(AppointmentsService.class, appointmentAdvice);
        addAdvice(AppointmentRecurringPatternService.class, recurringAppointmentsAdvice);
    }

    @Override
    public void willStop() {
        removeAdvice(AppointmentServiceDefinitionService.class, appointmentServiceDefinitionAdvice);
        removeAdvice(AppointmentsService.class, appointmentAdvice);
        removeAdvice(AppointmentRecurringPatternService.class, recurringAppointmentsAdvice);
    }
}
