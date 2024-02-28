package org.openmrs.module.appointments.advice;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.openmrs.api.context.ServiceContext;
import org.openmrs.module.appointments.service.AppointmentRecurringPatternService;
import org.openmrs.module.appointments.service.AppointmentServiceDefinitionService;
import org.openmrs.module.appointments.service.AppointmentsService;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.context.ApplicationContext;

import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mockStatic;

@PowerMockIgnore({"javax.*", "org.apache.*", "org.slf4j.*"})
@PrepareForTest({ServiceContext.class})
@RunWith(PowerMockRunner.class)
public class AtomFeedAdviceProviderTest {

    @Mock
    ServiceContext serviceContext;

    @Mock
    AppointmentServiceDefinitionAdvice appointmentServiceDefinitionAdvice;

    @Mock
    AppointmentAdvice appointmentAdvice;

    @Mock
    RecurringAppointmentsAdvice recurringAppointmentsAdvice;

    @Mock
    ApplicationContext applicationContext;

    AtomFeedAdviceProvider provider;

    @Before
    public void setUp() throws Exception {
        mockStatic(ServiceContext.class);
        when(ServiceContext.getInstance()).thenReturn(serviceContext);
        provider = new AtomFeedAdviceProvider(appointmentServiceDefinitionAdvice, appointmentAdvice, recurringAppointmentsAdvice);
    }

    @Test
    public void shouldRegisterAdvice() {
        provider.setApplicationContext(applicationContext);
        Mockito.verify(serviceContext).addAdvice(AppointmentServiceDefinitionService.class, appointmentServiceDefinitionAdvice);
        Mockito.verify(serviceContext).addAdvice(AppointmentsService.class, appointmentAdvice);
        Mockito.verify(serviceContext).addAdvice(AppointmentRecurringPatternService.class, recurringAppointmentsAdvice);
    }
}