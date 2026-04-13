package org.openmrs.module.appointments.events.advice;

import org.bahmni.module.eventoutbox.EMREvent;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.context.Context;
import org.openmrs.module.appointments.events.AppointmentBookingEvent;
import org.openmrs.module.appointments.events.publisher.AppointmentEventPublisher;
import org.openmrs.module.appointments.model.Appointment;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.lang.reflect.Method;
import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mockStatic;

@PowerMockIgnore("javax.management.*")
@PrepareForTest(Context.class)
@RunWith(PowerMockRunner.class)
public class AppointmentEventsAdviceTest {

    private static final String UUID = "test-appointment-uuid";
    private static final String RAISE_EVENT_GP = "eventoutbox.publish.eventsForAppointments";
    private static final String URL_PATTERN_GP = "eventoutbox.event.urlPatternForAppointments";
    private static final String DEFAULT_URL = "/openmrs/ws/rest/v1/appointments/{uuid}";

    @Mock
    private AppointmentEventPublisher eventPublisher;

    @Mock
    private AdministrationService administrationService;

    @Mock
    private Appointment appointment;

    private AppointmentEventsAdvice advice;

    @Before
    public void setUp() {
        mockStatic(Context.class);
        when(Context.getRegisteredComponent("appointmentEventPublisher", AppointmentEventPublisher.class))
                .thenReturn(eventPublisher);
        when(Context.getAdministrationService()).thenReturn(administrationService);
        when(administrationService.getGlobalProperty(RAISE_EVENT_GP, "true")).thenReturn("true");
        when(administrationService.getGlobalProperty(URL_PATTERN_GP, DEFAULT_URL)).thenReturn(DEFAULT_URL);
        when(appointment.getUuid()).thenReturn(UUID);
        when(appointment.getId()).thenReturn(1);
        advice = new AppointmentEventsAdvice();
    }

    @Test
    public void shouldPublishEMREventForValidateAndSave() throws Throwable {
        advice.before(getMethod("validateAndSave"), new Object[]{appointment}, null);
        advice.afterReturning(appointment, getMethod("validateAndSave"), new Object[]{appointment}, null);

        ArgumentCaptor<EMREvent> captor = ArgumentCaptor.forClass(EMREvent.class);
        verify(eventPublisher, times(1)).publishEvent(any(AppointmentBookingEvent.class));
        verify(eventPublisher, times(1)).publishEMREvent(captor.capture());
        EMREvent emrEvent = captor.getValue();
        assertEquals("appointments", emrEvent.getCategory());
        assertEquals("Appointment", emrEvent.getTitle());
        assertEquals("/openmrs/ws/rest/v1/appointments/" + UUID, emrEvent.getContent());
    }

    @Test
    public void shouldPublishEMREventForChangeStatus() throws Throwable {
        advice.afterReturning(null, getMethod("changeStatus"), new Object[]{appointment}, null);

        ArgumentCaptor<EMREvent> captor = ArgumentCaptor.forClass(EMREvent.class);
        verify(eventPublisher, times(1)).publishEMREvent(captor.capture());
        EMREvent emrEvent = captor.getValue();
        assertEquals("appointments", emrEvent.getCategory());
        assertEquals("Appointment", emrEvent.getTitle());
        assertEquals("/openmrs/ws/rest/v1/appointments/" + UUID, emrEvent.getContent());
    }

    @Test
    public void shouldPublishEMREventForUndoStatusChange() throws Throwable {
        advice.afterReturning(null, getMethod("undoStatusChange"), new Object[]{appointment}, null);

        ArgumentCaptor<EMREvent> captor = ArgumentCaptor.forClass(EMREvent.class);
        verify(eventPublisher, times(1)).publishEMREvent(captor.capture());
        EMREvent emrEvent = captor.getValue();
        assertEquals("appointments", emrEvent.getCategory());
        assertEquals("Appointment", emrEvent.getTitle());
        assertEquals("/openmrs/ws/rest/v1/appointments/" + UUID, emrEvent.getContent());
    }

    @Test
    public void shouldNotPublishEMREventForUnhandledMethod() throws Throwable {
        advice.afterReturning(appointment, getMethod("dummy"), null, null);

        verify(eventPublisher, times(0)).publishEMREvent(any(EMREvent.class));
    }

    @Test
    public void shouldUseCustomUrlPatternFromGlobalProperty() throws Throwable {
        String customPattern = "/custom/appointment/{uuid}";
        when(administrationService.getGlobalProperty(URL_PATTERN_GP, DEFAULT_URL)).thenReturn(customPattern);

        advice.afterReturning(null, getMethod("changeStatus"), new Object[]{appointment}, null);

        ArgumentCaptor<EMREvent> captor = ArgumentCaptor.forClass(EMREvent.class);
        verify(eventPublisher, times(1)).publishEMREvent(captor.capture());
        assertEquals("/custom/appointment/" + UUID, captor.getValue().getContent());
    }

    @Test
    public void shouldNotPublishEMREventWhenGateIsFalse() throws Throwable {
        when(administrationService.getGlobalProperty(RAISE_EVENT_GP, "true")).thenReturn("false");

        advice.afterReturning(null, getMethod("changeStatus"), new Object[]{appointment}, null);

        verify(eventPublisher, times(0)).publishEMREvent(any(EMREvent.class));
    }

    // Stub methods for Method reflection
    public void validateAndSave() {}
    public void changeStatus() {}
    public void undoStatusChange() {}
    public void dummy() {}

    private Method getMethod(String name) throws NoSuchMethodException {
        return this.getClass().getMethod(name);
    }
}
