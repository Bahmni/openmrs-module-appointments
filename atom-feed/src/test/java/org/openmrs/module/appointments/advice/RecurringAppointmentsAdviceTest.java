package org.openmrs.module.appointments.advice;

import org.ict4h.atomfeed.server.repository.jdbc.AllEventRecordsQueueJdbcImpl;
import org.ict4h.atomfeed.server.service.Event;
import org.ict4h.atomfeed.server.service.EventServiceImpl;
import org.ict4h.atomfeed.transaction.AFTransactionWorkWithoutResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.context.Context;
import org.openmrs.module.appointments.model.Appointment;
import org.openmrs.module.appointments.model.AppointmentRecurringPattern;
import org.openmrs.module.atomfeed.transaction.support.AtomFeedSpringTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;

import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.internal.verification.VerificationModeFactory.times;
import static org.mockito.Mockito.mockStatic;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.junit.jupiter.api.AfterEach;
import org.mockito.MockedConstruction;
import org.ict4h.atomfeed.transaction.AFTransactionWork;
import java.util.ArrayList;
import java.util.List;
import static org.mockito.Mockito.mockConstruction;
import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class RecurringAppointmentsAdviceTest {

    private MockedStatic<Context> contextMockedStatic;

    @AfterEach
    public void closeStaticMocks() {
        if (contextMockedStatic != null) {
            contextMockedStatic.close();
        }
        for (MockedConstruction<?> construction : Arrays.asList(transactionManagerConstruction,
                allEventRecordsQueueConstruction, eventServiceConstruction, eventConstruction)) {
            if (construction != null) {
                construction.close();
            }
        }
    }

    private static final String UUID = "5631b434-78aa-102b-91a0-001e378eb17e";
    private static final String DEFAULT_URL_PATTERN = "/openmrs/ws/rest/v1/recurring-appointments?uuid={uuid}";
    private static final String RAISE_EVENT_GLOBAL_PROPERTY = "atomfeed.publish.eventsForAppointments";
    private static final String URL_PATTERN_GLOBAL_PROPERTY = "atomfeed.event.urlPatternForRecurringAppointments";

    private AtomFeedSpringTransactionManager atomFeedSpringTransactionManager;

    @Mock
    private PlatformTransactionManager platformTransactionManager;


    private EventServiceImpl eventService;

    private MockedConstruction<AtomFeedSpringTransactionManager> transactionManagerConstruction;

    private MockedConstruction<AllEventRecordsQueueJdbcImpl> allEventRecordsQueueConstruction;

    private MockedConstruction<EventServiceImpl> eventServiceConstruction;

    private MockedConstruction<Event> eventConstruction;

    private final List<List<?>> eventConstructorArguments = new ArrayList<>();

    @Mock
    private AdministrationService administrationService;


    private AppointmentRecurringPattern appointmentRecurringPattern = new AppointmentRecurringPattern();

    private RecurringAppointmentsAdvice recurringAppointmentsAdvice;

    @BeforeEach
    public void setUp() throws Exception {
        contextMockedStatic = Mockito.mockStatic(Context.class);

        when(Context.getRegisteredComponents(PlatformTransactionManager.class)).thenReturn(Collections.singletonList(platformTransactionManager));
        when(Context.getAdministrationService()).thenReturn(administrationService);
        when(administrationService.getGlobalProperty(RAISE_EVENT_GLOBAL_PROPERTY)).thenReturn("true");
        when(administrationService.getGlobalProperty(URL_PATTERN_GLOBAL_PROPERTY, DEFAULT_URL_PATTERN)).thenReturn(DEFAULT_URL_PATTERN);

        transactionManagerConstruction = mockConstruction(AtomFeedSpringTransactionManager.class, (mock, context) ->
                when(mock.executeWithTransaction(any())).thenAnswer(invocation -> ((AFTransactionWork<?>) invocation.getArgument(0)).execute()));
        allEventRecordsQueueConstruction = mockConstruction(AllEventRecordsQueueJdbcImpl.class);
        eventServiceConstruction = mockConstruction(EventServiceImpl.class);
        eventConstruction = mockConstruction(Event.class, (mock, context) -> eventConstructorArguments.add(context.arguments()));
        Appointment appointment = new Appointment();
        appointment.setUuid(UUID);
        appointmentRecurringPattern.setAppointments(new HashSet<>(Collections.singletonList(appointment)));

        recurringAppointmentsAdvice = new RecurringAppointmentsAdvice();

        atomFeedSpringTransactionManager = transactionManagerConstruction.constructed().get(0);

        eventService = eventServiceConstruction.constructed().get(0);
    }

    @Test
    public void shouldRaiseAppointmentServiceChangeEventToEventRecordsTable() throws Throwable {
        recurringAppointmentsAdvice.afterReturning(appointmentRecurringPattern, this.getClass().getMethod("validateAndSave"), null, null);

        verify(eventService, times(1)).notify(any(Event.class));
        verify(atomFeedSpringTransactionManager, times(1)).executeWithTransaction(any(AFTransactionWorkWithoutResult.class));
        verifyEventConstructed(1, "RecurringAppointments", String.format("/openmrs/ws/rest/v1/recurring-appointments?uuid=%s", UUID), "appointments");
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
        verifyEventConstructed(0, null, null, null);
    }

    @Test
    public void shouldNotRaiseAppointmentServiceChangeEventToEventRecordsTableIfTheMethodIsNotSaveOrVoidAppointmentService() throws Throwable {
        recurringAppointmentsAdvice.afterReturning(appointmentRecurringPattern, this.getClass().getMethod("dummy"), null, null);

        verify(administrationService, times(0)).getGlobalProperty(URL_PATTERN_GLOBAL_PROPERTY, DEFAULT_URL_PATTERN);
        verify(administrationService, times(0)).getGlobalProperty(RAISE_EVENT_GLOBAL_PROPERTY);
        verify(atomFeedSpringTransactionManager, times(0)).executeWithTransaction(any(AFTransactionWorkWithoutResult.class));
        verify(eventService, times(0)).notify(any(Event.class));
        verifyEventConstructed(0, null, null, null);
    }

    @Test
    public void shouldRaiseEventWithCustomUrlPatternGivenInGlobalProperty() throws Throwable {
        when(administrationService.getGlobalProperty(URL_PATTERN_GLOBAL_PROPERTY, DEFAULT_URL_PATTERN)).thenReturn("/openmrs/ws/rest/v1/appointment/test/{uuid}");

        recurringAppointmentsAdvice.afterReturning(appointmentRecurringPattern, this.getClass().getMethod("validateAndSave"), null, null);

        verify(administrationService, times(1)).getGlobalProperty(URL_PATTERN_GLOBAL_PROPERTY, DEFAULT_URL_PATTERN);
        verify(atomFeedSpringTransactionManager, times(1)).executeWithTransaction(any(AFTransactionWorkWithoutResult.class));
        verify(eventService, times(1)).notify(any(Event.class));
        verifyEventConstructed(1, "RecurringAppointments", String.format("/openmrs/ws/rest/v1/appointment/test/%s", UUID), "appointments");
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
        verifyEventConstructed(1, "RecurringAppointments", String.format("/openmrs/ws/rest/v1/recurring-appointments?uuid=%s", UUID), "appointments");
        verifyEventConstructed(1, "RecurringAppointments", String.format("/openmrs/ws/rest/v1/recurring-appointments?uuid=%s", anotherUuid), "appointments");
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
        verifyEventConstructed(1, "RecurringAppointments", String.format("/openmrs/ws/rest/v1/recurring-appointments?uuid=%s", UUID), "appointments");
        verifyEventConstructed(1, "RecurringAppointments", String.format("/openmrs/ws/rest/v1/recurring-appointments?uuid=%s", anotherUuid), "appointments");
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
        verifyEventConstructed(1, "RecurringAppointments", String.format("/openmrs/ws/rest/v1/recurring-appointments?uuid=%s", UUID), "appointments");
        verifyEventConstructed(1, "RecurringAppointments", String.format("/openmrs/ws/rest/v1/recurring-appointments?uuid=%s", anotherUuid), "appointments");
        verify(administrationService, times(2)).getGlobalProperty(RAISE_EVENT_GLOBAL_PROPERTY);
        verify(administrationService, times(2)).getGlobalProperty(URL_PATTERN_GLOBAL_PROPERTY, DEFAULT_URL_PATTERN);
    }

    private void verifyEventConstructed(int times, String title, String contents, String category) {
        int matching = 0;
        for (List<?> arguments : eventConstructorArguments) {
            if (arguments.get(0) instanceof String && (title == null || title.equals(arguments.get(1)))
                    && arguments.get(2) instanceof LocalDateTime && arguments.get(3) == null
                    && (contents == null || contents.equals(arguments.get(4)))
                    && (category == null || category.equals(arguments.get(5)))) {
                matching++;
            }
        }
        assertEquals(times, matching);
    }
}
