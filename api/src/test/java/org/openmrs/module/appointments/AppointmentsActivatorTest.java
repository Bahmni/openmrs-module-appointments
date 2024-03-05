package org.openmrs.module.appointments;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.openmrs.api.context.ServiceContext;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mockStatic;

@PowerMockIgnore({"javax.*", "org.apache.*", "org.slf4j.*"})
@PrepareForTest({ServiceContext.class})
@RunWith(PowerMockRunner.class)
public class AppointmentsActivatorTest {

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
        mockStatic(ServiceContext.class);
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