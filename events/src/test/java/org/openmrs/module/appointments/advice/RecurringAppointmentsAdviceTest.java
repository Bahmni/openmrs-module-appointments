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
import org.openmrs.module.appointments.model.Appointment;
import org.openmrs.module.appointments.model.AppointmentRecurringPattern;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

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
public class RecurringAppointmentsAdviceTest {

    private static final String UUID = "5631b434-78aa-102b-91a0-001e378eb17e";
    private static final String ANOTHER_UUID = "another-uuid-000";
    private static final String RAISE_EVENT_GLOBAL_PROPERTY = "eventoutbox.publish.eventsForAppointments";
    private static final String URL_PATTERN_GLOBAL_PROPERTY = "eventoutbox.event.urlPatternForRecurringAppointments";
    private static final String DEFAULT_URL_PATTERN = "/openmrs/ws/rest/v1/recurring-appointments?uuid={uuid}";

    @Mock
    private AppointmentEventPublisher appointmentEventPublisher;

    @Mock
    private AdministrationService administrationService;

    private AppointmentRecurringPattern appointmentRecurringPattern = new AppointmentRecurringPattern();
    private RecurringAppointmentsAdvice recurringAppointmentsAdvice;

    @Before
    public void setUp() throws Exception {
        mockStatic(Context.class);
        when(Context.getAdministrationService()).thenReturn(administrationService);
        when(Context.getRegisteredComponent("appointmentEventPublisher", AppointmentEventPublisher.class))
                .thenReturn(appointmentEventPublisher);
        when(administrationService.getGlobalProperty(RAISE_EVENT_GLOBAL_PROPERTY)).thenReturn("true");
        when(administrationService.getGlobalProperty(URL_PATTERN_GLOBAL_PROPERTY, DEFAULT_URL_PATTERN))
                .thenReturn(DEFAULT_URL_PATTERN);

        Appointment appointment = new Appointment();
        appointment.setUuid(UUID);
        appointmentRecurringPattern.setAppointments(new HashSet<>(Collections.singletonList(appointment)));

        recurringAppointmentsAdvice = new RecurringAppointmentsAdvice();
    }

    @Test
    public void shouldPublishEMREventForValidateAndSave() throws Throwable {
        recurringAppointmentsAdvice.afterReturning(appointmentRecurringPattern,
                this.getClass().getMethod("validateAndSave"), null, null);

        ArgumentCaptor<EMREvent> captor = ArgumentCaptor.forClass(EMREvent.class);
        verify(appointmentEventPublisher, times(1)).publish(captor.capture());
        assertEquals("appointments", captor.getValue().getCategory());
        assertEquals("RecurringAppointments", captor.getValue().getTitle());
        assertEquals("/openmrs/ws/rest/v1/recurring-appointments?uuid=" + UUID, captor.getValue().getContent());
    }

    @Test
    public void shouldPublishEMREventForAllAppointmentsOnUpdateReturningPattern() throws Throwable {
        Appointment appointmentOne = new Appointment();
        Appointment appointmentTwo = new Appointment();
        appointmentOne.setUuid(UUID);
        appointmentTwo.setUuid(ANOTHER_UUID);
        appointmentRecurringPattern.setAppointments(new HashSet<>(Arrays.asList(appointmentOne, appointmentTwo)));

        recurringAppointmentsAdvice.afterReturning(appointmentRecurringPattern,
                this.getClass().getMethod("update"), null, null);

        verify(appointmentEventPublisher, times(2)).publish(any(EMREvent.class));
    }

    @Test
    public void shouldPublishEMREventForListOfAppointmentsOnUpdateReturningSingleAppointment() throws Throwable {
        Appointment appointmentOne = new Appointment();
        Appointment appointmentTwo = new Appointment();
        appointmentOne.setUuid(UUID);
        appointmentTwo.setUuid(ANOTHER_UUID);
        appointmentRecurringPattern.setAppointments(new HashSet<>(Arrays.asList(appointmentOne, appointmentTwo)));
        Object[] arguments = new Object[2];
        arguments[0] = appointmentRecurringPattern;
        arguments[1] = Arrays.asList(appointmentOne, appointmentTwo);

        recurringAppointmentsAdvice.afterReturning(appointmentRecurringPattern.getAppointments().iterator().next(),
                this.getClass().getMethod("update"), arguments, null);

        verify(appointmentEventPublisher, times(2)).publish(any(EMREvent.class));
    }

    @Test
    public void shouldPublishEMREventForAllAppointmentsOnChangeStatus() throws Throwable {
        Appointment appointmentOne = new Appointment();
        Appointment appointmentTwo = new Appointment();
        appointmentOne.setUuid(UUID);
        appointmentTwo.setUuid(ANOTHER_UUID);
        List<Appointment> appointments = Arrays.asList(appointmentOne, appointmentTwo);

        recurringAppointmentsAdvice.afterReturning(appointments,
                this.getClass().getMethod("changeStatus"), null, null);

        verify(appointmentEventPublisher, times(2)).publish(any(EMREvent.class));
    }

    @Test
    public void shouldNotPublishEMREventIfGlobalPropertyIsFalse() throws Throwable {
        when(administrationService.getGlobalProperty(RAISE_EVENT_GLOBAL_PROPERTY)).thenReturn("false");

        recurringAppointmentsAdvice.afterReturning(appointmentRecurringPattern,
                this.getClass().getMethod("validateAndSave"), null, null);

        verify(appointmentEventPublisher, times(0)).publish(any(EMREvent.class));
    }

    @Test
    public void shouldNotPublishEMREventForUnhandledMethod() throws Throwable {
        recurringAppointmentsAdvice.afterReturning(appointmentRecurringPattern,
                this.getClass().getMethod("dummy"), null, null);

        verify(appointmentEventPublisher, times(0)).publish(any(EMREvent.class));
    }

    @Test
    public void shouldPublishEMREventWithCustomUrlPattern() throws Throwable {
        String customPattern = "/openmrs/ws/rest/v1/recurring-appointments/test/{uuid}";
        when(administrationService.getGlobalProperty(URL_PATTERN_GLOBAL_PROPERTY, DEFAULT_URL_PATTERN))
                .thenReturn(customPattern);

        recurringAppointmentsAdvice.afterReturning(appointmentRecurringPattern,
                this.getClass().getMethod("validateAndSave"), null, null);

        ArgumentCaptor<EMREvent> captor = ArgumentCaptor.forClass(EMREvent.class);
        verify(appointmentEventPublisher, times(1)).publish(captor.capture());
        assertEquals("/openmrs/ws/rest/v1/recurring-appointments/test/" + UUID, captor.getValue().getContent());
    }

    public void validateAndSave() {}
    public void update() {}
    public void changeStatus() {}
    public void dummy() {}
}
