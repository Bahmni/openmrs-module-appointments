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
import org.openmrs.module.appointments.model.AppointmentServiceDefinition;
import org.openmrs.module.atomfeed.transaction.support.AtomFeedSpringTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.internal.verification.VerificationModeFactory.times;

@RunWith(MockitoJUnitRunner.class)
public class AppointmentServiceDefinitionAdviceTest {

    private static final String UUID = "5631b434-78aa-102b-91a0-001e378eb17e";
    private static final String DEFAULT_URL_PATTERN = "/openmrs/ws/rest/v1/appointmentService?uuid={uuid}";
    private static final String RAISE_EVENT_GLOBAL_PROPERTY = "atomfeed.publish.eventsForAppointmentService";
    private static final String URL_PATTERN_GLOBAL_PROPERTY = "atomfeed.event.urlPatternForAppointmentService";

    @Mock
    private AppointmentServiceDefinition appointmentServiceDefinition;

    @Mock
    private PlatformTransactionManager platformTransactionManager;

    @Mock
    private AdministrationService administrationService;

    private AppointmentServiceDefinitionAdvice appointmentServiceDefinitionAdvice;

    private MockedStatic<Context> contextMockedStatic;
    private MockedConstruction<AtomFeedSpringTransactionManager> txnManagerConstruction;
    private MockedConstruction<AllEventRecordsQueueJdbcImpl> queueConstruction;
    private MockedConstruction<EventServiceImpl> eventServiceConstruction;
    private MockedConstruction<Event> eventConstruction;
    private List<Object[]> eventConstructorArgs;

    @Before
    public void setUp() throws Exception {
        contextMockedStatic = mockStatic(Context.class);

        when(Context.getRegisteredComponents(PlatformTransactionManager.class)).thenReturn(Collections.singletonList(platformTransactionManager));
        when(Context.getAdministrationService()).thenReturn(administrationService);
        when(administrationService.getGlobalProperty(RAISE_EVENT_GLOBAL_PROPERTY)).thenReturn("true");
        when(administrationService.getGlobalProperty(URL_PATTERN_GLOBAL_PROPERTY, DEFAULT_URL_PATTERN)).thenReturn(DEFAULT_URL_PATTERN);

        txnManagerConstruction = mockConstruction(AtomFeedSpringTransactionManager.class, (mock, ctx) -> {
            doAnswer(inv -> {
                AFTransactionWork<?> work = inv.getArgument(0);
                work.execute();
                return null;
            }).when(mock).executeWithTransaction(any());
        });
        queueConstruction = mockConstruction(AllEventRecordsQueueJdbcImpl.class);
        eventServiceConstruction = mockConstruction(EventServiceImpl.class, (mock, ctx) -> {
            doNothing().when(mock).notify(any());
        });
        eventConstructorArgs = new ArrayList<>();
        eventConstruction = mockConstruction(Event.class, (mock, ctx) -> {
            eventConstructorArgs.add(ctx.arguments().toArray());
        });

        when(appointmentServiceDefinition.getUuid()).thenReturn(UUID);

        appointmentServiceDefinitionAdvice = new AppointmentServiceDefinitionAdvice();
    }

    @After
    public void tearDown() {
        if (contextMockedStatic != null) contextMockedStatic.close();
        if (txnManagerConstruction != null) txnManagerConstruction.close();
        if (queueConstruction != null) queueConstruction.close();
        if (eventServiceConstruction != null) eventServiceConstruction.close();
        if (eventConstruction != null) eventConstruction.close();
    }

    private AtomFeedSpringTransactionManager txnManager() {
        return txnManagerConstruction.constructed().get(0);
    }

    private EventServiceImpl eventService() {
        return eventServiceConstruction.constructed().get(0);
    }

    @Test
    public void shouldRaiseAppointmentServiceChangeEventToEventRecordsTable() throws Throwable {
        appointmentServiceDefinitionAdvice.afterReturning(appointmentServiceDefinition, this.getClass().getMethod("save"), null, null);

        verify(txnManager(), times(1)).executeWithTransaction(any(AFTransactionWorkWithoutResult.class));
        verify(eventService(), times(1)).notify(any(Event.class));
        assertEquals(1, eventConstructorArgs.size());
        assertEventArgs(eventConstructorArgs.get(0), "Appointment Service",
                String.format("/openmrs/ws/rest/v1/appointmentService?uuid=%s", UUID), "appointmentservice");
        verify(administrationService, times(1)).getGlobalProperty(RAISE_EVENT_GLOBAL_PROPERTY);
        verify(administrationService, times(1)).getGlobalProperty(URL_PATTERN_GLOBAL_PROPERTY, DEFAULT_URL_PATTERN);
    }

    @Test
    public void shouldRaiseAppointmentServiceVoidChangeEventToEventRecordsTable() throws Throwable {
        appointmentServiceDefinitionAdvice.afterReturning(appointmentServiceDefinition, this.getClass().getMethod("voidAppointmentService"), null, null);

        verify(txnManager(), times(1)).executeWithTransaction(any(AFTransactionWorkWithoutResult.class));
        verify(eventService(), times(1)).notify(any(Event.class));
        assertEquals(1, eventConstructorArgs.size());
        assertEventArgs(eventConstructorArgs.get(0), "Appointment Service",
                String.format("/openmrs/ws/rest/v1/appointmentService?uuid=%s", UUID), "appointmentservice");
        verify(administrationService, times(1)).getGlobalProperty(RAISE_EVENT_GLOBAL_PROPERTY);
        verify(administrationService, times(1)).getGlobalProperty(URL_PATTERN_GLOBAL_PROPERTY, DEFAULT_URL_PATTERN);
    }

    @Test
    public void shouldNotRaiseAppointmentServiceChangeEventToEventRecordsTableIfTheGlobalPropertyIsSetToFalse() throws Throwable {
        when(administrationService.getGlobalProperty(RAISE_EVENT_GLOBAL_PROPERTY)).thenReturn("false");

        appointmentServiceDefinitionAdvice.afterReturning(appointmentServiceDefinition, this.getClass().getMethod("save"), null, null);

        verify(administrationService, times(1)).getGlobalProperty(RAISE_EVENT_GLOBAL_PROPERTY);
        verify(administrationService, times(0)).getGlobalProperty(URL_PATTERN_GLOBAL_PROPERTY, DEFAULT_URL_PATTERN);
        verify(txnManager(), times(0)).executeWithTransaction(any(AFTransactionWorkWithoutResult.class));
        verify(eventService(), times(0)).notify(any(Event.class));
        assertEquals(0, eventConstructorArgs.size());
    }

    @Test
    public void shouldNotRaiseAppointmentServiceChangeEventToEventRecordsTableIfTheMethodIsNotSaveOrVoidAppointmentService() throws Throwable {
        appointmentServiceDefinitionAdvice.afterReturning(appointmentServiceDefinition, this.getClass().getMethod("dummy"), null, null);

        verify(administrationService, times(1)).getGlobalProperty(RAISE_EVENT_GLOBAL_PROPERTY);
        verify(txnManager(), times(0)).executeWithTransaction(any(AFTransactionWorkWithoutResult.class));
        verify(administrationService, times(0)).getGlobalProperty(URL_PATTERN_GLOBAL_PROPERTY, DEFAULT_URL_PATTERN);
        verify(eventService(), times(0)).notify(any(Event.class));
        assertEquals(0, eventConstructorArgs.size());
    }

    @Test
    public void shouldRaiseEventWithCustomUrlPatternGivenInGlobalProperty() throws Throwable {
        when(administrationService.getGlobalProperty(URL_PATTERN_GLOBAL_PROPERTY, DEFAULT_URL_PATTERN)).thenReturn("/openmrs/ws/rest/v1/appointmentServiceDefinition/test/{uuid}");

        appointmentServiceDefinitionAdvice.afterReturning(appointmentServiceDefinition, this.getClass().getMethod("save"), null, null);

        verify(txnManager(), times(1)).executeWithTransaction(any(AFTransactionWorkWithoutResult.class));
        verify(administrationService, times(1)).getGlobalProperty(URL_PATTERN_GLOBAL_PROPERTY, DEFAULT_URL_PATTERN);
        verify(eventService(), times(1)).notify(any(Event.class));
        assertEquals(1, eventConstructorArgs.size());
        assertEventArgs(eventConstructorArgs.get(0), "Appointment Service",
                String.format("/openmrs/ws/rest/v1/appointmentServiceDefinition/test/%s", UUID), "appointmentservice");
        verify(administrationService, times(1)).getGlobalProperty(RAISE_EVENT_GLOBAL_PROPERTY);
        verify(administrationService, times(1)).getGlobalProperty(URL_PATTERN_GLOBAL_PROPERTY, DEFAULT_URL_PATTERN);
    }

    public void save() {
    }

    public void voidAppointmentService() {
    }

    public void dummy() {
    }

    // Positions in the org.ict4h.atomfeed.server.service.Event 6-arg constructor:
    // (uuid, title, dateCreated, uri, contents, category).
    private static final int EVENT_TITLE = 1;
    private static final int EVENT_CONTENTS = 4;
    private static final int EVENT_CATEGORY = 5;
    private static final int EVENT_ARG_COUNT = 6;

    private void assertEventArgs(Object[] args, String title, String contents, String category) {
        assertEquals(EVENT_ARG_COUNT, args.length);
        assertEquals(title, args[EVENT_TITLE]);
        assertEquals(contents, args[EVENT_CONTENTS]);
        assertEquals(category, args[EVENT_CATEGORY]);
    }
}
