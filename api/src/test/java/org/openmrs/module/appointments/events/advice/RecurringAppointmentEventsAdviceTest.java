package org.openmrs.module.appointments.events.advice;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.openmrs.api.context.Context;
import org.openmrs.module.appointments.events.RecurringAppointmentEvent;
import org.openmrs.module.appointments.events.publisher.AppointmentEventPublisher;
import org.openmrs.module.appointments.model.Appointment;
import org.openmrs.module.appointments.model.AppointmentRecurringPattern;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashSet;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.openmrs.module.appointments.events.AppointmentEventType.BAHMNI_RECURRING_APPOINTMENT_CREATED;
import static org.openmrs.module.appointments.events.AppointmentEventType.BAHMNI_RECURRING_APPOINTMENT_UPDATED;
import static org.powermock.api.mockito.PowerMockito.mockStatic;

@PowerMockIgnore("javax.management.*")
@PrepareForTest(Context.class)
@RunWith(PowerMockRunner.class)
public class RecurringAppointmentEventsAdviceTest {

    @Mock
    private AppointmentEventPublisher eventPublisher;

    private AppointmentRecurringPattern recurringPattern;
    private RecurringAppointmentEventsAdvice advice;

    @Before
    public void setUp() {
        mockStatic(Context.class);
        when(Context.getRegisteredComponent("appointmentEventPublisher", AppointmentEventPublisher.class))
                .thenReturn(eventPublisher);

        Appointment appointment = new Appointment();
        appointment.setUuid("uuid-1");
        recurringPattern = new AppointmentRecurringPattern();
        recurringPattern.setAppointments(new HashSet<>(Collections.singletonList(appointment)));

        advice = new RecurringAppointmentEventsAdvice();
    }

    @Test
    public void shouldPublishCreatedEventForNewPatternOnValidateAndSave() throws Throwable {
        recurringPattern.setId(null);

        advice.before(getMethod("validateAndSave"), new Object[]{recurringPattern}, null);
        advice.afterReturning(recurringPattern, getMethod("validateAndSave"), null, null);

        ArgumentCaptor<RecurringAppointmentEvent> captor = ArgumentCaptor.forClass(RecurringAppointmentEvent.class);
        verify(eventPublisher, times(1)).publishEvent(captor.capture());
        assertEquals(BAHMNI_RECURRING_APPOINTMENT_CREATED, captor.getValue().eventType);
    }

    @Test
    public void shouldPublishUpdatedEventForExistingPatternOnValidateAndSave() throws Throwable {
        recurringPattern.setId(1);

        advice.before(getMethod("validateAndSave"), new Object[]{recurringPattern}, null);
        advice.afterReturning(recurringPattern, getMethod("validateAndSave"), null, null);

        ArgumentCaptor<RecurringAppointmentEvent> captor = ArgumentCaptor.forClass(RecurringAppointmentEvent.class);
        verify(eventPublisher, times(1)).publishEvent(captor.capture());
        assertEquals(BAHMNI_RECURRING_APPOINTMENT_UPDATED, captor.getValue().eventType);
    }

    @Test
    public void shouldNotPublishEventWhenBeforeIsNotCalled() throws Throwable {
        advice.afterReturning(recurringPattern, getMethod("validateAndSave"), null, null);

        verify(eventPublisher, times(0)).publishEvent(any(RecurringAppointmentEvent.class));
    }

    @Test
    public void shouldNotPublishEventForUnhandledMethod() throws Throwable {
        advice.before(getMethod("dummy"), new Object[]{recurringPattern}, null);
        advice.afterReturning(recurringPattern, getMethod("dummy"), null, null);

        verify(eventPublisher, times(0)).publishEvent(any(RecurringAppointmentEvent.class));
    }

    // Stub methods for Method reflection
    public void validateAndSave() {}
    public void dummy() {}

    private Method getMethod(String name) throws NoSuchMethodException {
        return this.getClass().getMethod(name);
    }
}
