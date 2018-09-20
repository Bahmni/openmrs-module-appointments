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
import org.openmrs.module.appointments.model.AppointmentServiceDefinition;
import org.openmrs.module.atomfeed.transaction.support.AtomFeedSpringTransactionManager;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.transaction.PlatformTransactionManager;

import java.net.URI;
import java.util.Collections;
import java.util.Date;

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

@PrepareForTest({Context.class, AppointmentServiceDefinitionAdvice.class})
@RunWith(PowerMockRunner.class)
public class AppointmentServiceDefinitionAdviceTest {

    private static final String UUID = "5631b434-78aa-102b-91a0-001e378eb17e";
    private static final String DEFAULT_URL_PATTERN = "/openmrs/ws/rest/v1/appointmentService?uuid={uuid}";
    private static final String RAISE_EVENT_GLOBAL_PROPERTY = "atomfeed.publish.eventsForAppointmentService";
    private static final String URL_PATTERN_GLOBAL_PROPERTY = "atomfeed.event.urlPatternForAppointmentService";

    @Mock
    private AppointmentServiceDefinition appointmentServiceDefinition;

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
    private AppointmentServiceDefinitionAdvice appointmentServiceDefinitionAdvice;

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
        when(appointmentServiceDefinition.getUuid()).thenReturn(UUID);
        doNothing().when(eventService).notify(any());

        appointmentServiceDefinitionAdvice = new AppointmentServiceDefinitionAdvice();
    }

    @Test
    public void shouldRaiseAppointmentServiceChangeEventToEventRecordsTable() throws Throwable {
        appointmentServiceDefinitionAdvice.afterReturning(appointmentServiceDefinition, this.getClass().getMethod("save"), null, null);

        verify(atomFeedSpringTransactionManager, times(1)).executeWithTransaction(any(AFTransactionWorkWithoutResult.class));
        verify(eventService, times(1)).notify(any(Event.class));
        verifyNew(Event.class, times(1)).withArguments(anyString(), eq("Appointment Service"), any(Date.class), any(URI.class), eq(String.format("/openmrs/ws/rest/v1/appointmentService?uuid=%s", UUID)), eq("appointmentservice"));
        verify(administrationService, times(1)).getGlobalProperty(RAISE_EVENT_GLOBAL_PROPERTY);
        verify(administrationService, times(1)).getGlobalProperty(URL_PATTERN_GLOBAL_PROPERTY, DEFAULT_URL_PATTERN);
    }

    @Test
    public void shouldRaiseAppointmentServiceVoidChangeEventToEventRecordsTable() throws Throwable {
        appointmentServiceDefinitionAdvice.afterReturning(appointmentServiceDefinition, this.getClass().getMethod("voidAppointmentService"), null, null);

        verify(atomFeedSpringTransactionManager, times(1)).executeWithTransaction(any(AFTransactionWorkWithoutResult.class));
        verify(eventService, times(1)).notify(any(Event.class));
        verifyNew(Event.class, times(1)).withArguments(anyString(), eq("Appointment Service"), any(Date.class), any(URI.class), eq(String.format("/openmrs/ws/rest/v1/appointmentService?uuid=%s", UUID)), eq("appointmentservice"));
        verify(administrationService, times(1)).getGlobalProperty(RAISE_EVENT_GLOBAL_PROPERTY);
        verify(administrationService, times(1)).getGlobalProperty(URL_PATTERN_GLOBAL_PROPERTY, DEFAULT_URL_PATTERN);
    }

    @Test
    public void shouldNotRaiseAppointmentServiceChangeEventToEventRecordsTableIfTheGlobalPropertyIsSetToFalse() throws Throwable {
        when(administrationService.getGlobalProperty(RAISE_EVENT_GLOBAL_PROPERTY)).thenReturn("false");

        appointmentServiceDefinitionAdvice.afterReturning(appointmentServiceDefinition, this.getClass().getMethod("save"), null, null);

        verify(administrationService, times(1)).getGlobalProperty(RAISE_EVENT_GLOBAL_PROPERTY);
        verify(administrationService, times(0)).getGlobalProperty(URL_PATTERN_GLOBAL_PROPERTY, DEFAULT_URL_PATTERN);
        verify(atomFeedSpringTransactionManager, times(0)).executeWithTransaction(any(AFTransactionWorkWithoutResult.class));
        verify(eventService, times(0)).notify(any(Event.class));
        verifyNew(Event.class, times(0)).withArguments(anyString(), anyString(), any(Date.class), any(URI.class), anyString(), anyString());
    }

    @Test
    public void shouldNotRaiseAppointmentServiceChangeEventToEventRecordsTableIfTheMethodIsNotSaveOrVoidAppointmentService() throws Throwable {
        appointmentServiceDefinitionAdvice.afterReturning(appointmentServiceDefinition, this.getClass().getMethod("dummy"), null, null);

        verify(administrationService, times(1)).getGlobalProperty(RAISE_EVENT_GLOBAL_PROPERTY);
        verify(atomFeedSpringTransactionManager, times(0)).executeWithTransaction(any(AFTransactionWorkWithoutResult.class));
        verify(administrationService, times(0)).getGlobalProperty(URL_PATTERN_GLOBAL_PROPERTY, DEFAULT_URL_PATTERN);
        verify(eventService, times(0)).notify(any(Event.class));
        verifyNew(Event.class, times(0)).withArguments(anyString(), anyString(), any(Date.class), any(URI.class), anyString(), anyString());
    }

    @Test
    public void shouldRaiseEventWithCustomUrlPatternGivenInGlobalProperty() throws Throwable {
        when(administrationService.getGlobalProperty(URL_PATTERN_GLOBAL_PROPERTY, DEFAULT_URL_PATTERN)).thenReturn("/openmrs/ws/rest/v1/appointmentServiceDefinition/test/{uuid}");

        appointmentServiceDefinitionAdvice.afterReturning(appointmentServiceDefinition, this.getClass().getMethod("save"), null, null);

        verify(atomFeedSpringTransactionManager, times(1)).executeWithTransaction(any(AFTransactionWorkWithoutResult.class));
        verify(administrationService, times(1)).getGlobalProperty(URL_PATTERN_GLOBAL_PROPERTY, DEFAULT_URL_PATTERN);
        verify(eventService, times(1)).notify(any(Event.class));
        verifyNew(Event.class, times(1)).withArguments(anyString(), eq("Appointment Service"), any(Date.class), any(URI.class), eq(String.format("/openmrs/ws/rest/v1/appointmentServiceDefinition/test/%s", UUID)), eq("appointmentservice"));
        verify(administrationService, times(1)).getGlobalProperty(RAISE_EVENT_GLOBAL_PROPERTY);
        verify(administrationService, times(1)).getGlobalProperty(URL_PATTERN_GLOBAL_PROPERTY, DEFAULT_URL_PATTERN);
    }

    public void save() {
    }

    public void voidAppointmentService() {
    }

    public void dummy() {
    }
}