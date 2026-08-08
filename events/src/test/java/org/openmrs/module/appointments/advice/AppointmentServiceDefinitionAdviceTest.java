package org.openmrs.module.appointments.advice;

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

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mockStatic;

@PowerMockIgnore("javax.management.*")
@PrepareForTest(Context.class)
@RunWith(PowerMockRunner.class)
public class AppointmentServiceDefinitionAdviceTest {

    private static final String UUID = "5631b434-78aa-102b-91a0-001e378eb17e";
    private static final String RAISE_EVENT_GLOBAL_PROPERTY = "eventoutbox.publish.eventsForAppointmentService";
    private static final String URL_PATTERN_GLOBAL_PROPERTY = "eventoutbox.event.urlPatternForAppointmentService";
    private static final String DEFAULT_URL_PATTERN = "/openmrs/ws/rest/v1/appointmentService?uuid={uuid}";

    @Mock
    private AppointmentEventPublisher appointmentEventPublisher;

    @Mock
    private AdministrationService administrationService;

    @Mock
    private AppointmentServiceDefinition appointmentServiceDefinition;

    private AppointmentServiceDefinitionAdvice appointmentServiceDefinitionAdvice;

    @Before
    public void setUp() throws Exception {
        mockStatic(Context.class);
        when(Context.getAdministrationService()).thenReturn(administrationService);
        when(Context.getRegisteredComponent("appointmentEventPublisher", AppointmentEventPublisher.class))
                .thenReturn(appointmentEventPublisher);
        when(administrationService.getGlobalProperty(RAISE_EVENT_GLOBAL_PROPERTY)).thenReturn("true");
        when(administrationService.getGlobalProperty(URL_PATTERN_GLOBAL_PROPERTY, DEFAULT_URL_PATTERN))
                .thenReturn(DEFAULT_URL_PATTERN);
        when(appointmentServiceDefinition.getUuid()).thenReturn(UUID);

        appointmentServiceDefinitionAdvice = new AppointmentServiceDefinitionAdvice();
    }

    @Test
    public void shouldPublishEMREventForSave() throws Throwable {
        appointmentServiceDefinitionAdvice.afterReturning(appointmentServiceDefinition,
                this.getClass().getMethod("save"), null, null);

        ArgumentCaptor<EMREvent> captor = ArgumentCaptor.forClass(EMREvent.class);
        verify(appointmentEventPublisher, times(1)).publish(captor.capture());
        assertEquals("appointmentservice", captor.getValue().getCategory());
        assertEquals("Appointment Service", captor.getValue().getTitle());
        assertEquals("/openmrs/ws/rest/v1/appointmentService?uuid=" + UUID, captor.getValue().getContent());
    }

    @Test
    public void shouldPublishEMREventForVoidAppointmentService() throws Throwable {
        appointmentServiceDefinitionAdvice.afterReturning(appointmentServiceDefinition,
                this.getClass().getMethod("voidAppointmentService"), null, null);

        ArgumentCaptor<EMREvent> captor = ArgumentCaptor.forClass(EMREvent.class);
        verify(appointmentEventPublisher, times(1)).publish(captor.capture());
        assertEquals("appointmentservice", captor.getValue().getCategory());
        assertEquals("Appointment Service", captor.getValue().getTitle());
        assertEquals("/openmrs/ws/rest/v1/appointmentService?uuid=" + UUID, captor.getValue().getContent());
    }

    @Test
    public void shouldNotPublishEMREventIfGlobalPropertyIsFalse() throws Throwable {
        when(administrationService.getGlobalProperty(RAISE_EVENT_GLOBAL_PROPERTY)).thenReturn("false");

        appointmentServiceDefinitionAdvice.afterReturning(appointmentServiceDefinition,
                this.getClass().getMethod("save"), null, null);

        verify(appointmentEventPublisher, times(0)).publish(any(EMREvent.class));
    }

    @Test
    public void shouldNotPublishEMREventForUnhandledMethod() throws Throwable {
        appointmentServiceDefinitionAdvice.afterReturning(appointmentServiceDefinition,
                this.getClass().getMethod("dummy"), null, null);

        verify(appointmentEventPublisher, times(0)).publish(any(EMREvent.class));
    }

    @Test
    public void shouldPublishEMREventWithCustomUrlPattern() throws Throwable {
        String customPattern = "/openmrs/ws/rest/v1/appointmentServiceDefinition/test/{uuid}";
        when(administrationService.getGlobalProperty(URL_PATTERN_GLOBAL_PROPERTY, DEFAULT_URL_PATTERN))
                .thenReturn(customPattern);

        appointmentServiceDefinitionAdvice.afterReturning(appointmentServiceDefinition,
                this.getClass().getMethod("save"), null, null);

        ArgumentCaptor<EMREvent> captor = ArgumentCaptor.forClass(EMREvent.class);
        verify(appointmentEventPublisher, times(1)).publish(captor.capture());
        assertEquals("/openmrs/ws/rest/v1/appointmentServiceDefinition/test/" + UUID,
                captor.getValue().getContent());
    }

    public void save() {}
    public void voidAppointmentService() {}
    public void dummy() {}
}
