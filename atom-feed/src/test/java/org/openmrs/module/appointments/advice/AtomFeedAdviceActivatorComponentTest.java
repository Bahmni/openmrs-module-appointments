package org.openmrs.module.appointments.advice;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.openmrs.api.context.ServiceContext;
import org.openmrs.module.appointments.service.AppointmentRecurringPatternService;
import org.openmrs.module.appointments.service.AppointmentServiceDefinitionService;
import org.openmrs.module.appointments.service.AppointmentsService;

import static org.mockito.Mockito.when;
import static org.mockito.Mockito.mockStatic;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.mockito.MockedStatic;
import org.junit.jupiter.api.AfterEach;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class AtomFeedAdviceActivatorComponentTest {

    private MockedStatic<ServiceContext> serviceContextMockedStatic;

    @AfterEach
    public void closeStaticMocks() {
        if (serviceContextMockedStatic != null) {
            serviceContextMockedStatic.close();
        }
    }

    @Mock
    ServiceContext serviceContext;

    @Mock
    AppointmentServiceDefinitionAdvice appointmentServiceDefinitionAdvice;

    @Mock
    AppointmentAdvice appointmentAdvice;

    @Mock
    RecurringAppointmentsAdvice recurringAppointmentsAdvice;

    AtomFeedAdviceActivatorComponent component;

    @BeforeEach
    public void setUp() throws Exception {
        serviceContextMockedStatic = Mockito.mockStatic(ServiceContext.class);
        when(ServiceContext.getInstance()).thenReturn(serviceContext);
        component = new AtomFeedAdviceActivatorComponent(appointmentServiceDefinitionAdvice, appointmentAdvice, recurringAppointmentsAdvice);
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