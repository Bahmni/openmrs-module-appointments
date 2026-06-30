package org.openmrs.module.appointments;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.openmrs.api.context.ServiceContext;

import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.mockito.Mockito.mockStatic;

@RunWith(MockitoJUnitRunner.Silent.class)
public class AppointmentsActivatorTest {

    @Mock
    ServiceContext serviceContext;

    @Mock
    AppointmentsActivatorComponent appointmentsActivatorComponent1;

    @Mock
    AppointmentsActivatorComponent appointmentsActivatorComponent2;

    List<AppointmentsActivatorComponent> components;

    AppointmentsActivator activator;

    private MockedStatic<ServiceContext> mockedServiceContext;

    @Before
    public void setUp() throws Exception {
        activator = new AppointmentsActivator();
        mockedServiceContext = mockStatic(ServiceContext.class);
        mockedServiceContext.when(() -> ServiceContext.getInstance()).thenReturn(serviceContext);
        components = Arrays.asList(appointmentsActivatorComponent1, appointmentsActivatorComponent2);
        when(serviceContext.getRegisteredComponents(AppointmentsActivatorComponent.class)).thenReturn(components);
    }

    @After
    public void tearDown() {
        if (mockedServiceContext != null) {
            mockedServiceContext.close();
        }
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