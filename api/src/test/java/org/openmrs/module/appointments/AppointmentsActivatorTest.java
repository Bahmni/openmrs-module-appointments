package org.openmrs.module.appointments;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.openmrs.api.context.ServiceContext;

import java.util.Arrays;
import java.util.List;

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
public class AppointmentsActivatorTest {

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
    AppointmentsActivatorComponent appointmentsActivatorComponent1;

    @Mock
    AppointmentsActivatorComponent appointmentsActivatorComponent2;

    List<AppointmentsActivatorComponent> components;

    AppointmentsActivator activator;

    @BeforeEach
    public void setUp() throws Exception {
        activator = new AppointmentsActivator();
        serviceContextMockedStatic = Mockito.mockStatic(ServiceContext.class);
        when(ServiceContext.getInstance()).thenReturn(serviceContext);
        components = Arrays.asList(appointmentsActivatorComponent1, appointmentsActivatorComponent2);
        when(serviceContext.getRegisteredComponents(AppointmentsActivatorComponent.class)).thenReturn(components);
    }

    @Test
    public void shouldStartActivatorComponents() {
        activator.started();
        Mockito.verify(appointmentsActivatorComponent1).started();
        Mockito.verify(appointmentsActivatorComponent2).started();
    }

    @Test
    public void shouldStopActivatorComponents() {
        activator.willStop();
        Mockito.verify(appointmentsActivatorComponent1).willStop();
        Mockito.verify(appointmentsActivatorComponent2).willStop();
    }
}