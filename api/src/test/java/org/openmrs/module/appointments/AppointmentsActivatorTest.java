package org.openmrs.module.appointments;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.openmrs.api.context.ServiceContext;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class AppointmentsActivatorTest {

    private MockedStatic<ServiceContext> serviceContextMock;

    @Mock
    ServiceContext serviceContext;

    @Mock
    AppointmentsActivatorComponent appointmentsActivatorComponent1;

    @Mock
    AppointmentsActivatorComponent appointmentsActivatorComponent2;

    List<AppointmentsActivatorComponent> components;

    AppointmentsActivator activator;

    @Before
    public void setUp() throws Exception {
        activator = new AppointmentsActivator();
        serviceContextMock = Mockito.mockStatic(ServiceContext.class);
        serviceContextMock.when(ServiceContext::getInstance).thenReturn(serviceContext);
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

    @After
    public void tearDownServiceContextMock() {
        if (serviceContextMock != null) {
            serviceContextMock.close();
            serviceContextMock = null;
        }
    }
}
