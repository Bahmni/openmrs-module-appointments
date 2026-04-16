package org.openmrs.module.appointments.events.advice;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.openmrs.api.context.Context;
import org.openmrs.module.appointments.events.AppointmentBookingEvent;
import org.openmrs.module.appointments.events.publisher.AppointmentEventPublisher;
import org.openmrs.module.appointments.model.Appointment;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.lang.reflect.Method;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.openmrs.module.appointments.events.AppointmentEventType.BAHMNI_APPOINTMENT_CREATED;
import static org.openmrs.module.appointments.events.AppointmentEventType.BAHMNI_APPOINTMENT_UPDATED;
import static org.powermock.api.mockito.PowerMockito.mockStatic;

@PowerMockIgnore("javax.management.*")
@PrepareForTest(Context.class)
@RunWith(PowerMockRunner.class)
public class AppointmentEventsAdviceTest {

    private static final String UUID = "test-appointment-uuid";

    @Mock
    private AppointmentEventPublisher eventPublisher;

    @Mock
    private Appointment appointment;

    private AppointmentEventsAdvice advice;

    @Before
    public void setUp() {
        mockStatic(Context.class);
        when(Context.getRegisteredComponent("appointmentEventPublisher", AppointmentEventPublisher.class))
                .thenReturn(eventPublisher);
        when(appointment.getUuid()).thenReturn(UUID);
        advice = new AppointmentEventsAdvice();
    }

    @Test
    public void shouldPublishCreatedEventForNewAppointmentOnValidateAndSave() throws Throwable {
        when(appointment.getId()).thenReturn(null);

        advice.before(getMethod("validateAndSave"), new Object[]{appointment}, null);
        advice.afterReturning(appointment, getMethod("validateAndSave"), new Object[]{appointment}, null);

        ArgumentCaptor<AppointmentBookingEvent> captor = ArgumentCaptor.forClass(AppointmentBookingEvent.class);
        verify(eventPublisher, times(1)).publishEvent(captor.capture());
        assertEquals(BAHMNI_APPOINTMENT_CREATED, captor.getValue().eventType);
    }

    @Test
    public void shouldPublishUpdatedEventForExistingAppointmentOnValidateAndSave() throws Throwable {
        when(appointment.getId()).thenReturn(1);

        advice.before(getMethod("validateAndSave"), new Object[]{appointment}, null);
        advice.afterReturning(appointment, getMethod("validateAndSave"), new Object[]{appointment}, null);

        ArgumentCaptor<AppointmentBookingEvent> captor = ArgumentCaptor.forClass(AppointmentBookingEvent.class);
        verify(eventPublisher, times(1)).publishEvent(captor.capture());
        assertEquals(BAHMNI_APPOINTMENT_UPDATED, captor.getValue().eventType);
    }

    @Test
    public void shouldNotPublishEventWhenBeforeIsNotCalled() throws Throwable {
        advice.afterReturning(appointment, getMethod("validateAndSave"), new Object[]{appointment}, null);

        verify(eventPublisher, times(0)).publishEvent(any(AppointmentBookingEvent.class));
    }

    @Test
    public void shouldNotPublishEventForUnhandledMethod() throws Throwable {
        advice.before(getMethod("changeStatus"), new Object[]{appointment}, null);
        advice.afterReturning(appointment, getMethod("changeStatus"), new Object[]{appointment}, null);

        verify(eventPublisher, times(0)).publishEvent(any(AppointmentBookingEvent.class));
    }

    // Stub methods for Method reflection
    public void validateAndSave() {}
    public void changeStatus() {}
    public void dummy() {}

    private Method getMethod(String name) throws NoSuchMethodException {
        return this.getClass().getMethod(name);
    }
}
