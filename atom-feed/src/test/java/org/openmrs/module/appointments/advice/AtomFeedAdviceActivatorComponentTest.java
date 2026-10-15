package org.openmrs.module.appointments.advice;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.openmrs.api.context.ServiceContext;
import org.openmrs.module.appointments.service.AppointmentRecurringPatternService;
import org.openmrs.module.appointments.service.AppointmentServiceDefinitionService;
import org.openmrs.module.appointments.service.AppointmentsService;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class AtomFeedAdviceActivatorComponentTest {

    private MockedStatic<ServiceContext> serviceContextMock;

    @Mock
    ServiceContext serviceContext;

    @Mock
    AppointmentServiceDefinitionAdvice appointmentServiceDefinitionAdvice;

    @Mock
    AppointmentAdvice appointmentAdvice;

    @Mock
    RecurringAppointmentsAdvice recurringAppointmentsAdvice;

    AtomFeedAdviceActivatorComponent component;

    @Before
    public void setUp() throws Exception {
        serviceContextMock = Mockito.mockStatic(ServiceContext.class);
        serviceContextMock.when(ServiceContext::getInstance).thenReturn(serviceContext);
        component = new AtomFeedAdviceActivatorComponent(appointmentServiceDefinitionAdvice, appointmentAdvice, recurringAppointmentsAdvice);
    }

    @After
    public void tearDownServiceContextMock() {
        serviceContextMock.close();
    }

    @Test
    public void shouldAddAdvice() {
        component.started();
        Mockito.verify(serviceContext).addAdvice(AppointmentServiceDefinitionService.class, appointmentServiceDefinitionAdvice);
        Mockito.verify(serviceContext).addAdvice(AppointmentsService.class, appointmentAdvice);
        Mockito.verify(serviceContext).addAdvice(AppointmentRecurringPatternService.class, recurringAppointmentsAdvice);
    }

    @Test
    public void shouldRemoveAdvice() {
        component.willStop();
        Mockito.verify(serviceContext).removeAdvice(AppointmentServiceDefinitionService.class, appointmentServiceDefinitionAdvice);
        Mockito.verify(serviceContext).removeAdvice(AppointmentsService.class, appointmentAdvice);
        Mockito.verify(serviceContext).removeAdvice(AppointmentRecurringPatternService.class, recurringAppointmentsAdvice);
    }
}