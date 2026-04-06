package org.openmrs.module.appointments.events.advice;

import org.bahmni.module.eventoutbox.EMREvent;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.context.Context;
import org.openmrs.module.appointments.events.publisher.AppointmentEventPublisher;
import org.openmrs.module.appointments.model.AppointmentServiceDefinition;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.lang.reflect.Method;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mockStatic;

@PowerMockIgnore("javax.management.*")
@PrepareForTest(Context.class)
@RunWith(PowerMockRunner.class)
public class AppointmentServiceDefinitionEventsAdviceTest {

    private static final String UUID = "service-def-uuid";
    private static final String RAISE_EVENT_GP = "bahmnievents.publish.eventsForAppointmentService";
    private static final String URL_PATTERN_GP = "bahmnievents.event.urlPatternForAppointmentService";
    private static final String DEFAULT_URL = "/openmrs/ws/rest/v1/appointmentService?uuid={uuid}";

    @Mock
    private AppointmentEventPublisher eventPublisher;

    @Mock
    private AdministrationService administrationService;

    @Mock
    private AppointmentServiceDefinition serviceDefinition;

    private AppointmentServiceDefinitionEventsAdvice advice;

    @Before
    public void setUp() {
        mockStatic(Context.class);
        when(Context.getRegisteredComponent("appointmentEventPublisher", AppointmentEventPublisher.class))
                .thenReturn(eventPublisher);
        when(Context.getAdministrationService()).thenReturn(administrationService);
        when(administrationService.getGlobalProperty(RAISE_EVENT_GP, "true")).thenReturn("true");
        when(administrationService.getGlobalProperty(URL_PATTERN_GP, DEFAULT_URL)).thenReturn(DEFAULT_URL);
        when(serviceDefinition.getUuid()).thenReturn(UUID);
        advice = new AppointmentServiceDefinitionEventsAdvice();
    }

    @Test
    public void shouldPublishEMREventForSave() throws Throwable {
        advice.afterReturning(serviceDefinition, getMethod("save"), null, null);

        ArgumentCaptor<EMREvent> captor = ArgumentCaptor.forClass(EMREvent.class);
        verify(eventPublisher, times(1)).publishEMREvent(captor.capture());
        EMREvent emrEvent = captor.getValue();
        assertEquals("appointmentservice", emrEvent.getCategory());
        assertEquals("Appointment Service", emrEvent.getTitle());
        assertEquals("/openmrs/ws/rest/v1/appointmentService?uuid=" + UUID, emrEvent.getContent());
    }

    @Test
    public void shouldPublishEMREventForVoidAppointmentService() throws Throwable {
        advice.afterReturning(serviceDefinition, getMethod("voidAppointmentService"), null, null);

        ArgumentCaptor<EMREvent> captor = ArgumentCaptor.forClass(EMREvent.class);
        verify(eventPublisher, times(1)).publishEMREvent(captor.capture());
        EMREvent emrEvent = captor.getValue();
        assertEquals("appointmentservice", emrEvent.getCategory());
        assertEquals("Appointment Service", emrEvent.getTitle());
        assertEquals("/openmrs/ws/rest/v1/appointmentService?uuid=" + UUID, emrEvent.getContent());
    }

    @Test
    public void shouldNotPublishEMREventForUnhandledMethod() throws Throwable {
        advice.afterReturning(serviceDefinition, getMethod("dummy"), null, null);

        verify(eventPublisher, times(0)).publishEMREvent(any(EMREvent.class));
    }

    @Test
    public void shouldUseCustomUrlPatternFromGlobalProperty() throws Throwable {
        String customPattern = "/custom/appointmentService/{uuid}";
        when(administrationService.getGlobalProperty(URL_PATTERN_GP, DEFAULT_URL)).thenReturn(customPattern);

        advice.afterReturning(serviceDefinition, getMethod("save"), null, null);

        ArgumentCaptor<EMREvent> captor = ArgumentCaptor.forClass(EMREvent.class);
        verify(eventPublisher, times(1)).publishEMREvent(captor.capture());
        assertEquals("/custom/appointmentService/" + UUID, captor.getValue().getContent());
    }

    @Test
    public void shouldNotPublishEMREventWhenGateIsFalse() throws Throwable {
        when(administrationService.getGlobalProperty(RAISE_EVENT_GP, "true")).thenReturn("false");

        advice.afterReturning(serviceDefinition, getMethod("save"), null, null);

        verify(eventPublisher, times(0)).publishEMREvent(any(EMREvent.class));
    }

    // Stub methods for Method reflection
    public void save() {}
    public void voidAppointmentService() {}
    public void dummy() {}

    private Method getMethod(String name) throws NoSuchMethodException {
        return this.getClass().getMethod(name);
    }
}
