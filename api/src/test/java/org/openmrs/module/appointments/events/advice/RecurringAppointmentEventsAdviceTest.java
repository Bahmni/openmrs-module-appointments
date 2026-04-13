package org.openmrs.module.appointments.events.advice;

import org.bahmni.module.eventoutbox.EMREvent;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.context.Context;
import org.openmrs.module.appointments.events.RecurringAppointmentEvent;
import org.openmrs.module.appointments.events.publisher.AppointmentEventPublisher;
import org.openmrs.module.appointments.model.Appointment;
import org.openmrs.module.appointments.model.AppointmentRecurringPattern;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mockStatic;

@PowerMockIgnore("javax.management.*")
@PrepareForTest(Context.class)
@RunWith(PowerMockRunner.class)
public class RecurringAppointmentEventsAdviceTest {

    private static final String UUID_1 = "uuid-1";
    private static final String UUID_2 = "uuid-2";
    private static final String RAISE_EVENT_GP = "eventoutbox.publish.eventsForAppointments";
    private static final String URL_PATTERN_GP = "eventoutbox.event.urlPatternForRecurringAppointments";
    private static final String DEFAULT_URL = "/openmrs/ws/rest/v1/recurring-appointments?uuid={uuid}";

    @Mock
    private AppointmentEventPublisher eventPublisher;

    @Mock
    private AdministrationService administrationService;

    private AppointmentRecurringPattern recurringPattern;
    private Appointment appointment1;
    private Appointment appointment2;
    private RecurringAppointmentEventsAdvice advice;

    @Before
    public void setUp() {
        mockStatic(Context.class);
        when(Context.getRegisteredComponent("appointmentEventPublisher", AppointmentEventPublisher.class))
                .thenReturn(eventPublisher);
        when(Context.getAdministrationService()).thenReturn(administrationService);
        when(administrationService.getGlobalProperty(RAISE_EVENT_GP, "true")).thenReturn("true");
        when(administrationService.getGlobalProperty(URL_PATTERN_GP, DEFAULT_URL)).thenReturn(DEFAULT_URL);

        appointment1 = new Appointment();
        appointment1.setUuid(UUID_1);
        appointment2 = new Appointment();
        appointment2.setUuid(UUID_2);

        recurringPattern = new AppointmentRecurringPattern();
        recurringPattern.setAppointments(new HashSet<>(Collections.singletonList(appointment1)));

        advice = new RecurringAppointmentEventsAdvice();
    }

    @Test
    public void shouldPublishEMREventForValidateAndSave() throws Throwable {
        advice.before(getMethod("validateAndSave"), new Object[]{recurringPattern}, null);
        advice.afterReturning(recurringPattern, getMethod("validateAndSave"), null, null);

        verify(eventPublisher, times(1)).publishEvent(any(RecurringAppointmentEvent.class));
        ArgumentCaptor<EMREvent> captor = ArgumentCaptor.forClass(EMREvent.class);
        verify(eventPublisher, times(1)).publishEMREvent(captor.capture());
        assertEquals("appointments", captor.getValue().getCategory());
        assertEquals("RecurringAppointments", captor.getValue().getTitle());
        assertEquals("/openmrs/ws/rest/v1/recurring-appointments?uuid=" + UUID_1, captor.getValue().getContent());
    }

    @Test
    public void shouldPublishEMREventForAllAppointmentsOnUpdateReturningPattern() throws Throwable {
        recurringPattern.setAppointments(new HashSet<>(Arrays.asList(appointment1, appointment2)));
        advice.afterReturning(recurringPattern, getMethod("update"), null, null);

        ArgumentCaptor<EMREvent> captor = ArgumentCaptor.forClass(EMREvent.class);
        verify(eventPublisher, times(2)).publishEMREvent(captor.capture());
        List<EMREvent> events = captor.getAllValues();
        assertEquals(2, events.size());
    }

    @Test
    public void shouldPublishEMREventForListOfAppointmentsOnUpdateReturningSingleAppointment() throws Throwable {
        List<Appointment> updatedAppointments = Arrays.asList(appointment1, appointment2);
        Object[] args = new Object[]{recurringPattern, updatedAppointments};
        advice.afterReturning(appointment1, getMethod("update"), args, null);

        ArgumentCaptor<EMREvent> captor = ArgumentCaptor.forClass(EMREvent.class);
        verify(eventPublisher, times(2)).publishEMREvent(captor.capture());
        assertEquals(2, captor.getAllValues().size());
    }

    @Test
    public void shouldPublishEMREventForAllAppointmentsOnChangeStatus() throws Throwable {
        List<Appointment> appointments = Arrays.asList(appointment1, appointment2);
        advice.afterReturning(appointments, getMethod("changeStatus"), null, null);

        ArgumentCaptor<EMREvent> captor = ArgumentCaptor.forClass(EMREvent.class);
        verify(eventPublisher, times(2)).publishEMREvent(captor.capture());
        assertEquals(2, captor.getAllValues().size());
    }

    @Test
    public void shouldNotPublishEMREventForUnhandledMethod() throws Throwable {
        advice.afterReturning(recurringPattern, getMethod("dummy"), null, null);

        verify(eventPublisher, times(0)).publishEMREvent(any(EMREvent.class));
    }

    @Test
    public void shouldNotPublishEMREventWhenGateIsFalse() throws Throwable {
        when(administrationService.getGlobalProperty(RAISE_EVENT_GP, "true")).thenReturn("false");

        advice.afterReturning(recurringPattern, getMethod("validateAndSave"), null, null);

        verify(eventPublisher, times(0)).publishEMREvent(any(EMREvent.class));
    }

    // Stub methods for Method reflection
    public void validateAndSave() {}
    public void update() {}
    public void changeStatus() {}
    public void dummy() {}

    private Method getMethod(String name) throws NoSuchMethodException {
        return this.getClass().getMethod(name);
    }
}
