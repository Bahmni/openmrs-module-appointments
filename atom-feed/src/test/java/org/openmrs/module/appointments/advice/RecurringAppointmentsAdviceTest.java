package org.openmrs.module.appointments.advice;

import org.ict4h.atomfeed.server.repository.jdbc.AllEventRecordsQueueJdbcImpl;
import org.ict4h.atomfeed.server.service.Event;
import org.ict4h.atomfeed.server.service.EventServiceImpl;
import org.ict4h.atomfeed.transaction.AFTransactionWork;
import org.ict4h.atomfeed.transaction.AFTransactionWorkWithoutResult;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.mockito.junit.MockitoJUnitRunner;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.context.Context;
import org.openmrs.module.appointments.model.Appointment;
import org.openmrs.module.appointments.model.AppointmentRecurringPattern;
import org.openmrs.module.atomfeed.transaction.support.AtomFeedSpringTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.Silent.class)
public class RecurringAppointmentsAdviceTest {

    private static final String UUID = "5631b434-78aa-102b-91a0-001e378eb17e";
    private static final String DEFAULT_URL_PATTERN = "/openmrs/ws/rest/v1/recurring-appointments?uuid={uuid}";
    private static final String RAISE_EVENT_GLOBAL_PROPERTY = "atomfeed.publish.eventsForAppointments";
    private static final String URL_PATTERN_GLOBAL_PROPERTY = "atomfeed.event.urlPatternForRecurringAppointments";

    @Mock
    private PlatformTransactionManager platformTransactionManager;

    @Mock
    private AdministrationService administrationService;

    private MockedStatic<Context> mockedContext;
    private MockedConstruction<AtomFeedSpringTransactionManager> mockedTransactionManager;
    private MockedConstruction<AllEventRecordsQueueJdbcImpl> mockedEventRecordsQueue;
    private MockedConstruction<EventServiceImpl> mockedEventService;
    private MockedConstruction<Event> mockedEvent;

    // Captured constructor arguments for every Event built during the test (replaces PowerMock verifyNew).
    private final List<List<Object>> eventConstructorArgs = new ArrayList<>();

    private AppointmentRecurringPattern appointmentRecurringPattern = new AppointmentRecurringPattern();

    private RecurringAppointmentsAdvice recurringAppointmentsAdvice;

    @Before
    public void setUp() {
        mockedContext = mockStatic(Context.class);
        mockedContext.when(() -> Context.getRegisteredComponents(PlatformTransactionManager.class))
                .thenReturn(Collections.singletonList(platformTransactionManager));
        mockedContext.when(Context::getAdministrationService).thenReturn(administrationService);
        when(administrationService.getGlobalProperty(RAISE_EVENT_GLOBAL_PROPERTY)).thenReturn("true");
        when(administrationService.getGlobalProperty(URL_PATTERN_GLOBAL_PROPERTY, DEFAULT_URL_PATTERN)).thenReturn(DEFAULT_URL_PATTERN);

        // Mockito intercepts the objects the advice constructs internally (replaces PowerMock whenNew).
        mockedTransactionManager = mockConstruction(AtomFeedSpringTransactionManager.class,
                (mock, ctx) -> doAnswer(invocation -> {
                    ((AFTransactionWork<?>) invocation.getArgument(0)).execute();
                    return null;
                }).when(mock).executeWithTransaction(any()));
        mockedEventRecordsQueue = mockConstruction(AllEventRecordsQueueJdbcImpl.class);
        mockedEventService = mockConstruction(EventServiceImpl.class,
                (mock, ctx) -> doNothing().when(mock).notify(any()));
        mockedEvent = mockConstruction(Event.class,
                (mock, ctx) -> eventConstructorArgs.add(new ArrayList<>(ctx.arguments())));

        Appointment appointment = new Appointment();
        appointment.setUuid(UUID);
        appointmentRecurringPattern.setAppointments(new HashSet<>(Collections.singletonList(appointment)));

        recurringAppointmentsAdvice = new RecurringAppointmentsAdvice();
    }

    @After
    public void tearDown() {
        mockedEvent.close();
        mockedEventService.close();
        mockedEventRecordsQueue.close();
        mockedTransactionManager.close();
        mockedContext.close();
    }

    private AtomFeedSpringTransactionManager transactionManager() {
        return mockedTransactionManager.constructed().get(0);
    }

    private EventServiceImpl eventService() {
        return mockedEventService.constructed().get(0);
    }

    private List<Object> eventUrls() {
        List<Object> urls = new ArrayList<>();
        for (List<Object> args : eventConstructorArgs) {
            urls.add(args.get(4));
        }
        return urls;
    }

    private void assertAllEventsAreRecurringAppointments() {
        for (List<Object> args : eventConstructorArgs) {
            assertEquals("RecurringAppointments", args.get(1));
            assertEquals("appointments", args.get(5));
        }
    }

    @Test
    public void shouldRaiseAppointmentServiceChangeEventToEventRecordsTable() throws Throwable {
        recurringAppointmentsAdvice.afterReturning(appointmentRecurringPattern, this.getClass().getMethod("validateAndSave"), null, null);

        verify(eventService(), times(1)).notify(any(Event.class));
        verify(transactionManager(), times(1)).executeWithTransaction(any(AFTransactionWorkWithoutResult.class));
        assertEquals(1, mockedEvent.constructed().size());
        assertTrue(eventUrls().contains(String.format("/openmrs/ws/rest/v1/recurring-appointments?uuid=%s", UUID)));
        assertAllEventsAreRecurringAppointments();
        verify(administrationService, times(1)).getGlobalProperty(RAISE_EVENT_GLOBAL_PROPERTY);
        verify(administrationService, times(1)).getGlobalProperty(URL_PATTERN_GLOBAL_PROPERTY, DEFAULT_URL_PATTERN);
    }

    @Test
    public void shouldNotRaiseAppointmentServiceChangeEventToEventRecordsTableIfTheGlobalPropertyIsSetToFalse() throws Throwable {
        when(administrationService.getGlobalProperty(RAISE_EVENT_GLOBAL_PROPERTY)).thenReturn("false");

        recurringAppointmentsAdvice.afterReturning(appointmentRecurringPattern, this.getClass().getMethod("validateAndSave"), null, null);

        verify(administrationService, times(0)).getGlobalProperty(URL_PATTERN_GLOBAL_PROPERTY, DEFAULT_URL_PATTERN);
        verify(administrationService, times(1)).getGlobalProperty(RAISE_EVENT_GLOBAL_PROPERTY);
        verify(transactionManager(), times(0)).executeWithTransaction(any(AFTransactionWorkWithoutResult.class));
        verify(eventService(), times(0)).notify(any(Event.class));
        assertEquals(0, mockedEvent.constructed().size());
    }

    @Test
    public void shouldNotRaiseAppointmentServiceChangeEventToEventRecordsTableIfTheMethodIsNotSaveOrVoidAppointmentService() throws Throwable {
        recurringAppointmentsAdvice.afterReturning(appointmentRecurringPattern, this.getClass().getMethod("dummy"), null, null);

        verify(administrationService, times(0)).getGlobalProperty(URL_PATTERN_GLOBAL_PROPERTY, DEFAULT_URL_PATTERN);
        verify(administrationService, times(0)).getGlobalProperty(RAISE_EVENT_GLOBAL_PROPERTY);
        verify(transactionManager(), times(0)).executeWithTransaction(any(AFTransactionWorkWithoutResult.class));
        verify(eventService(), times(0)).notify(any(Event.class));
        assertEquals(0, mockedEvent.constructed().size());
    }

    @Test
    public void shouldRaiseEventWithCustomUrlPatternGivenInGlobalProperty() throws Throwable {
        when(administrationService.getGlobalProperty(URL_PATTERN_GLOBAL_PROPERTY, DEFAULT_URL_PATTERN)).thenReturn("/openmrs/ws/rest/v1/appointment/test/{uuid}");

        recurringAppointmentsAdvice.afterReturning(appointmentRecurringPattern, this.getClass().getMethod("validateAndSave"), null, null);

        verify(administrationService, times(1)).getGlobalProperty(URL_PATTERN_GLOBAL_PROPERTY, DEFAULT_URL_PATTERN);
        verify(transactionManager(), times(1)).executeWithTransaction(any(AFTransactionWorkWithoutResult.class));
        verify(eventService(), times(1)).notify(any(Event.class));
        assertEquals(1, mockedEvent.constructed().size());
        assertTrue(eventUrls().contains(String.format("/openmrs/ws/rest/v1/appointment/test/%s", UUID)));
        assertAllEventsAreRecurringAppointments();
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

        verify(eventService(), times(2)).notify(any(Event.class));
        verify(transactionManager(), times(2)).executeWithTransaction(any(AFTransactionWorkWithoutResult.class));
        assertEquals(2, mockedEvent.constructed().size());
        assertTrue(eventUrls().contains(String.format("/openmrs/ws/rest/v1/recurring-appointments?uuid=%s", UUID)));
        assertTrue(eventUrls().contains(String.format("/openmrs/ws/rest/v1/recurring-appointments?uuid=%s", anotherUuid)));
        assertAllEventsAreRecurringAppointments();
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

        verify(eventService(), times(2)).notify(any(Event.class));
        verify(transactionManager(), times(2)).executeWithTransaction(any(AFTransactionWorkWithoutResult.class));
        assertEquals(2, mockedEvent.constructed().size());
        assertTrue(eventUrls().contains(String.format("/openmrs/ws/rest/v1/recurring-appointments?uuid=%s", UUID)));
        assertTrue(eventUrls().contains(String.format("/openmrs/ws/rest/v1/recurring-appointments?uuid=%s", anotherUuid)));
        assertAllEventsAreRecurringAppointments();
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

        verify(eventService(), times(2)).notify(any(Event.class));
        verify(transactionManager(), times(2)).executeWithTransaction(any(AFTransactionWorkWithoutResult.class));
        assertEquals(2, mockedEvent.constructed().size());
        assertTrue(eventUrls().contains(String.format("/openmrs/ws/rest/v1/recurring-appointments?uuid=%s", UUID)));
        assertTrue(eventUrls().contains(String.format("/openmrs/ws/rest/v1/recurring-appointments?uuid=%s", anotherUuid)));
        assertAllEventsAreRecurringAppointments();
        verify(administrationService, times(2)).getGlobalProperty(RAISE_EVENT_GLOBAL_PROPERTY);
        verify(administrationService, times(2)).getGlobalProperty(URL_PATTERN_GLOBAL_PROPERTY, DEFAULT_URL_PATTERN);
    }
}
