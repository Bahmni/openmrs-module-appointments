package org.openmrs.module.appointments.advice;

import org.ict4h.atomfeed.server.repository.jdbc.AllEventRecordsQueueJdbcImpl;
import org.ict4h.atomfeed.server.service.Event;
import org.ict4h.atomfeed.server.service.EventServiceImpl;
import org.ict4h.atomfeed.transaction.AFTransactionWorkWithoutResult;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.context.Context;
import org.openmrs.module.appointments.model.Appointment;
import org.openmrs.module.appointments.model.AppointmentRecurringPattern;
import org.openmrs.module.atomfeed.transaction.support.AtomFeedSpringTransactionManager;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.transaction.PlatformTransactionManager;

import java.net.URI;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.internal.verification.VerificationModeFactory.times;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.spy;
import static org.powermock.api.mockito.PowerMockito.verifyNew;
import static org.powermock.api.mockito.PowerMockito.whenNew;

@PrepareForTest({Context.class, AppointmentAdvice.class})
@RunWith(PowerMockRunner.class)
public class RecurringAppointmentsAdviceTest {

    private static final String UUID = "5631b434-78aa-102b-91a0-001e378eb17e";
    private static final String DEFAULT_URL_PATTERN = "/openmrs/ws/rest/v1/recurring-appointments?uuid={uuid}";
    private static final String RAISE_EVENT_GLOBAL_PROPERTY = "atomfeed.publish.eventsForAppointments";
    private static final String URL_PATTERN_GLOBAL_PROPERTY = "atomfeed.event.urlPatternForRecurringAppointments";

    private AtomFeedSpringTransactionManager atomFeedSpringTransactionManager;

    @Mock
    private PlatformTransactionManager platformTransactionManager;

    @Mock
    private AllEventRecordsQueueJdbcImpl allEventRecordsQueue;

    @Mock
    private EventServiceImpl eventService;

    @Mock
    private AdministrationService administrationService;

    @Mock
    private Event event;

    private AppointmentRecurringPattern appointmentRecurringPattern = new AppointmentRecurringPattern();

    private RecurringAppointmentsAdvice recurringAppointmentsAdvice;

    @Before
    public void setUp() throws Exception {
        mockStatic(Context.class);

        atomFeedSpringTransactionManager = spy(new AtomFeedSpringTransactionManager(platformTransactionManager));

        when(Context.getRegisteredComponents(PlatformTransactionManager.class)).thenReturn(Collections.singletonList(platformTransactionManager));
        when(Context.getAdministrationService()).thenReturn(administrationService);
        when(administrationService.getGlobalProperty(RAISE_EVENT_GLOBAL_PROPERTY)).thenReturn("true");
        when(administrationService.getGlobalProperty(URL_PATTERN_GLOBAL_PROPERTY, DEFAULT_URL_PATTERN)).thenReturn(DEFAULT_URL_PATTERN);

        whenNew(AtomFeedSpringTransactionManager.class).withAnyArguments().thenReturn(atomFeedSpringTransactionManager);
        whenNew(AllEventRecordsQueueJdbcImpl.class).withArguments(this.atomFeedSpringTransactionManager).thenReturn(allEventRecordsQueue);
        whenNew(EventServiceImpl.class).withArguments(allEventRecordsQueue).thenReturn(eventService);
        whenNew(Event.class).withAnyArguments().thenReturn(event);
        Appointment appointment = new Appointment();
        appointment.setUuid(UUID);
        appointmentRecurringPattern.setAppointments(new HashSet<>(Collections.singletonList(appointment)));
        doNothing().when(eventService).notify(any());

        recurringAppointmentsAdvice = new RecurringAppointmentsAdvice();
    }

    @Test
    public void shouldRaiseAppointmentServiceChangeEventToEventRecordsTable() throws Throwable {
        recurringAppointmentsAdvice.afterReturning(appointmentRecurringPattern, this.getClass().getMethod("validateAndSave"), null, null);

        verify(eventService, times(1)).notify(any(Event.class));
        verify(atomFeedSpringTransactionManager, times(1)).executeWithTransaction(any(AFTransactionWorkWithoutResult.class));
        verifyNew(Event.class, times(1)).withArguments(anyString(), eq("RecurringAppointments"), any(Date.class), any(URI.class), eq(String.format("/openmrs/ws/rest/v1/recurring-appointments?uuid=%s", UUID)), eq("appointments"));
        verify(administrationService, times(1)).getGlobalProperty(RAISE_EVENT_GLOBAL_PROPERTY);
        verify(administrationService, times(1)).getGlobalProperty(URL_PATTERN_GLOBAL_PROPERTY, DEFAULT_URL_PATTERN);
    }

    @Test
    public void shouldNotRaiseAppointmentServiceChangeEventToEventRecordsTableIfTheGlobalPropertyIsSetToFalse() throws Throwable {
        when(administrationService.getGlobalProperty(RAISE_EVENT_GLOBAL_PROPERTY)).thenReturn("false");

        recurringAppointmentsAdvice.afterReturning(appointmentRecurringPattern, this.getClass().getMethod("validateAndSave"), null, null);

        verify(administrationService, times(0)).getGlobalProperty(URL_PATTERN_GLOBAL_PROPERTY, DEFAULT_URL_PATTERN);
        verify(administrationService, times(1)).getGlobalProperty(RAISE_EVENT_GLOBAL_PROPERTY);
        verify(atomFeedSpringTransactionManager, times(0)).executeWithTransaction(any(AFTransactionWorkWithoutResult.class));
        verify(eventService, times(0)).notify(any(Event.class));
        verifyNew(Event.class, times(0)).withArguments(anyString(), anyString(), any(Date.class), any(URI.class), anyString(), anyString());
    }

    @Test
    public void shouldNotRaiseAppointmentServiceChangeEventToEventRecordsTableIfTheMethodIsNotSaveOrVoidAppointmentService() throws Throwable {
        recurringAppointmentsAdvice.afterReturning(appointmentRecurringPattern, this.getClass().getMethod("dummy"), null, null);

        verify(administrationService, times(0)).getGlobalProperty(URL_PATTERN_GLOBAL_PROPERTY, DEFAULT_URL_PATTERN);
        verify(administrationService, times(0)).getGlobalProperty(RAISE_EVENT_GLOBAL_PROPERTY);
        verify(atomFeedSpringTransactionManager, times(0)).executeWithTransaction(any(AFTransactionWorkWithoutResult.class));
        verify(eventService, times(0)).notify(any(Event.class));
        verifyNew(Event.class, times(0)).withArguments(anyString(), anyString(), any(Date.class), any(URI.class), anyString(), anyString());
    }

    @Test
    public void shouldRaiseEventWithCustomUrlPatternGivenInGlobalProperty() throws Throwable {
        when(administrationService.getGlobalProperty(URL_PATTERN_GLOBAL_PROPERTY, DEFAULT_URL_PATTERN)).thenReturn("/openmrs/ws/rest/v1/appointment/test/{uuid}");

        recurringAppointmentsAdvice.afterReturning(appointmentRecurringPattern, this.getClass().getMethod("validateAndSave"), null, null);

        verify(administrationService, times(1)).getGlobalProperty(URL_PATTERN_GLOBAL_PROPERTY, DEFAULT_URL_PATTERN);
        verify(atomFeedSpringTransactionManager, times(1)).executeWithTransaction(any(AFTransactionWorkWithoutResult.class));
        verify(eventService, times(1)).notify(any(Event.class));
        verifyNew(Event.class, times(1)).withArguments(anyString(), eq("RecurringAppointments"), any(Date.class), any(URI.class), eq(String.format("/openmrs/ws/rest/v1/appointment/test/%s", UUID)), eq("appointments"));
        verify(administrationService, times(1)).getGlobalProperty(RAISE_EVENT_GLOBAL_PROPERTY);
        verify(administrationService, times(1)).getGlobalProperty(URL_PATTERN_GLOBAL_PROPERTY, DEFAULT_URL_PATTERN);
    }

    public void validateAndSave() {
    }

    public void dummy() {
    }

    public void update() {
    }

    public void changeStatus() {
    }

    @Test
    public void shouldRaiseEventRecordsForTheTwoAppointmentsInRecurringPatternForUpdateMethod() throws Throwable {
        Appointment appointmentOne = new Appointment();
        Appointment appointmentTwo = new Appointment();
        appointmentOne.setUuid(UUID);
        String anotherUuid = "Another UUID";
        appointmentTwo.setUuid(anotherUuid);
        appointmentRecurringPattern.setAppointments(new HashSet<>(Arrays.asList(appointmentOne, appointmentTwo)));
        recurringAppointmentsAdvice.afterReturning(appointmentRecurringPattern, this.getClass().getMethod("update"), null, null);

        verify(eventService, times(2)).notify(any(Event.class));
        verify(atomFeedSpringTransactionManager, times(2)).executeWithTransaction(any(AFTransactionWorkWithoutResult.class));
        verifyNew(Event.class, times(1)).withArguments(anyString(), eq("RecurringAppointments"), any(Date.class), any(URI.class), eq(String.format("/openmrs/ws/rest/v1/recurring-appointments?uuid=%s", UUID)), eq("appointments"));
        verifyNew(Event.class, times(1)).withArguments(anyString(), eq("RecurringAppointments"), any(Date.class), any(URI.class), eq(String.format("/openmrs/ws/rest/v1/recurring-appointments?uuid=%s", anotherUuid)), eq("appointments"));
        verify(administrationService, times(2)).getGlobalProperty(RAISE_EVENT_GLOBAL_PROPERTY);
        verify(administrationService, times(2)).getGlobalProperty(URL_PATTERN_GLOBAL_PROPERTY, DEFAULT_URL_PATTERN);
    }

    @Test
    public void shouldRaiseEventRecordsForTheSingleRecurringAppointmentUpdateMethod() throws Throwable {
        Appointment appointmentOne = new Appointment();
        Appointment appointmentTwo = new Appointment();
        appointmentOne.setUuid(UUID);
        String anotherUuid = "Another UUID";
        appointmentTwo.setUuid(anotherUuid);
        appointmentRecurringPattern.setAppointments(new HashSet<>(Arrays.asList(appointmentOne, appointmentTwo)));
        Object arguments[] = new Object[2];
        arguments[0] = appointmentRecurringPattern;
        arguments[1] = Arrays.asList(appointmentOne, appointmentTwo);
        recurringAppointmentsAdvice.afterReturning(appointmentRecurringPattern.getAppointments().iterator().next(),
                this.getClass().getMethod("update"), arguments, null);

        verify(eventService, times(2)).notify(any(Event.class));
        verify(atomFeedSpringTransactionManager, times(2)).executeWithTransaction(any(AFTransactionWorkWithoutResult.class));
        verifyNew(Event.class, times(1)).withArguments(anyString(), eq("RecurringAppointments"), any(Date.class), any(URI.class), eq(String.format("/openmrs/ws/rest/v1/recurring-appointments?uuid=%s", UUID)), eq("appointments"));
        verifyNew(Event.class, times(1)).withArguments(anyString(), eq("RecurringAppointments"), any(Date.class), any(URI.class), eq(String.format("/openmrs/ws/rest/v1/recurring-appointments?uuid=%s", anotherUuid)), eq("appointments"));
        verify(administrationService, times(2)).getGlobalProperty(RAISE_EVENT_GLOBAL_PROPERTY);
        verify(administrationService, times(2)).getGlobalProperty(URL_PATTERN_GLOBAL_PROPERTY, DEFAULT_URL_PATTERN);
    }

    @Test
    public void shouldRaiseEventRecordsForAllAppointmentsForChangeStatusMethod() throws Throwable {
        Appointment appointmentOne = new Appointment();
        Appointment appointmentTwo = new Appointment();
        appointmentOne.setUuid(UUID);
        String anotherUuid = "Another UUID";
        appointmentTwo.setUuid(anotherUuid);
        recurringAppointmentsAdvice.afterReturning(Arrays.asList(appointmentOne, appointmentTwo), this.getClass().getMethod("changeStatus"), null, null);

        verify(eventService, times(2)).notify(any(Event.class));
        verify(atomFeedSpringTransactionManager, times(2)).executeWithTransaction(any(AFTransactionWorkWithoutResult.class));
        verifyNew(Event.class, times(1)).withArguments(anyString(), eq("RecurringAppointments"), any(Date.class), any(URI.class), eq(String.format("/openmrs/ws/rest/v1/recurring-appointments?uuid=%s", UUID)), eq("appointments"));
        verifyNew(Event.class, times(1)).withArguments(anyString(), eq("RecurringAppointments"), any(Date.class), any(URI.class), eq(String.format("/openmrs/ws/rest/v1/recurring-appointments?uuid=%s", anotherUuid)), eq("appointments"));
        verify(administrationService, times(2)).getGlobalProperty(RAISE_EVENT_GLOBAL_PROPERTY);
        verify(administrationService, times(2)).getGlobalProperty(URL_PATTERN_GLOBAL_PROPERTY, DEFAULT_URL_PATTERN);
    }
}
