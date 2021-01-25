package org.openmrs.module.appointments.web.service.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.openmrs.module.appointments.model.AppointmentProviderResponse.CANCELLED;
import static org.openmrs.module.appointments.model.AppointmentStatus.CheckedIn;
import static org.openmrs.module.appointments.model.AppointmentStatus.Completed;
import static org.openmrs.module.appointments.model.AppointmentStatus.Requested;
import static org.openmrs.module.appointments.model.AppointmentStatus.Scheduled;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TimeZone;

import org.apache.commons.lang.time.DateUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.openmrs.Location;
import org.openmrs.Patient;
import org.openmrs.Provider;
import org.openmrs.api.APIException;
import org.openmrs.module.appointments.model.Appointment;
import org.openmrs.module.appointments.model.AppointmentKind;
import org.openmrs.module.appointments.model.AppointmentProvider;
import org.openmrs.module.appointments.model.AppointmentProviderResponse;
import org.openmrs.module.appointments.model.AppointmentRecurringPattern;
import org.openmrs.module.appointments.model.AppointmentServiceDefinition;
import org.openmrs.module.appointments.model.AppointmentServiceType;
import org.openmrs.module.appointments.model.AppointmentStatus;
import org.openmrs.module.appointments.service.AppointmentsService;
import org.openmrs.module.appointments.service.impl.RecurringAppointmentType;
import org.openmrs.module.appointments.web.contract.AppointmentProviderDetail;
import org.openmrs.module.appointments.web.contract.AppointmentRequest;
import org.openmrs.module.appointments.web.contract.RecurringAppointmentRequest;
import org.openmrs.module.appointments.web.contract.RecurringPattern;
import org.openmrs.module.appointments.web.mapper.AppointmentMapper;
import org.openmrs.module.appointments.web.mapper.RecurringPatternMapper;
import org.openmrs.module.appointments.web.util.AppointmentBuilder;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
public class AllAppointmentRecurringPatternUpdateServiceTest {
    @Mock
    private AppointmentsService appointmentsService;

    @Mock
    private AppointmentMapper appointmentMapper;

    @Mock
    private RecurringPatternMapper recurringPatternMapper;

    @Mock
    private Patient patient;

    @InjectMocks
    AllAppointmentRecurringPatternUpdateService allAppointmentRecurringPatternUpdateService;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Mock
    RecurringAppointmentsService recurringAppointmentsService;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    // TODO Add tests for weekly. All tests are for daily

    @Test
    public void shouldReturnAppointmentRecurringPatternWithAllItsPendingAppointmentsUpdatedWithAppointmentRequest() throws IOException {

        RecurringAppointmentRequest recurringAppointmentRequest = new RecurringAppointmentRequest();
        recurringAppointmentRequest.setAppointmentRequest(new AppointmentRequest());
        recurringAppointmentRequest.getAppointmentRequest().setUuid("uuid2");
        recurringAppointmentRequest.setTimeZone("Asia/Calcutta");


        AppointmentProviderDetail appointmentProviderDetail = new AppointmentProviderDetail();
        appointmentProviderDetail.setUuid("101");
        recurringAppointmentRequest.getAppointmentRequest().setProviders(Arrays.asList(appointmentProviderDetail));

        RecurringPattern recurringPattern = new RecurringPattern();
        recurringPattern.setFrequency(3);
        recurringPattern.setEndDate(null);
        recurringAppointmentRequest.setRecurringPattern(recurringPattern);

        Calendar startTimeCalendar = Calendar.getInstance();
        Calendar endTimeCalendar = Calendar.getInstance();
        Calendar newStartTimeCalendar = Calendar.getInstance();
        Calendar newEndTimeCalendar = Calendar.getInstance();
        int year = startTimeCalendar.get(Calendar.YEAR);
        int month = startTimeCalendar.get(Calendar.MONTH);
        int day = startTimeCalendar.get(Calendar.DATE);
        startTimeCalendar.set(year, month, day, 10, 00, 00);
        endTimeCalendar.set(year, month, day, 10, 30, 00);
        newStartTimeCalendar.set(year, month, day, 12, 00, 00);
        newStartTimeCalendar.set(Calendar.MILLISECOND, 0);
        newEndTimeCalendar.set(year, month, day, 12, 30, 00);
        newEndTimeCalendar.set(Calendar.MILLISECOND, 0);
        AppointmentRecurringPattern appointmentRecurringPattern = new AppointmentRecurringPattern();
        appointmentRecurringPattern.setType(RecurringAppointmentType.DAY);
        appointmentRecurringPattern.setFrequency(3);
        appointmentRecurringPattern.setPeriod(2);
        Appointment appointmentOne = new AppointmentBuilder()
                .withUuid("uuid1")
                .withPatient(patient)
                .withStatus(Completed)
                .withAppointmentKind(AppointmentKind.Scheduled)
                .withStartDateTime(DateUtils.addDays(startTimeCalendar.getTime(), -2))
                .withEndDateTime(DateUtils.addDays(endTimeCalendar.getTime(), -2))
                .build();
        appointmentOne.setComments("");
        Appointment appointmentTwo = new AppointmentBuilder()
                .withUuid("uuid2")
                .withPatient(patient)
                .withStatus(Scheduled)
                .withAppointmentKind(AppointmentKind.Scheduled)
                .withStartDateTime(newStartTimeCalendar.getTime())
                .withEndDateTime(newEndTimeCalendar.getTime())
                .build();
        appointmentTwo.setComments("CHANGED COMMENTS");

        AppointmentProvider appointmentProvider = new AppointmentProvider();
        appointmentProvider.setProvider(new Provider(101));
        appointmentProvider.getProvider().setUuid("102a");
        HashSet<AppointmentProvider> appointmentProviders = new HashSet<>(Collections
                .singleton(appointmentProvider));
        appointmentTwo.setProviders(appointmentProviders);
        Appointment appointmentThree = new AppointmentBuilder()
                .withUuid("uuid3")
                .withPatient(patient)
                .withStatus(Scheduled)
                .withAppointmentKind(AppointmentKind.Scheduled)
                .withStartDateTime(DateUtils.addDays(startTimeCalendar.getTime(), 2))
                .withEndDateTime(DateUtils.addDays(endTimeCalendar.getTime(), 2))
                .build();
        appointmentThree.setAppointmentAudits(new HashSet<>());
        Set<Appointment> appointments = new HashSet<>(Arrays.asList(appointmentOne, appointmentTwo,
                appointmentThree));
        appointmentRecurringPattern.setAppointments(appointments);

        appointmentOne.setAppointmentRecurringPattern(appointmentRecurringPattern);
        appointmentThree.setAppointmentRecurringPattern(appointmentRecurringPattern);
        appointmentTwo.setAppointmentRecurringPattern(appointmentRecurringPattern);

        when(appointmentsService.getAppointmentByUuid(any())).thenReturn(appointmentTwo);
        when(recurringAppointmentsService.getUpdatedSetOfAppointments(appointmentRecurringPattern, recurringAppointmentRequest))
                .thenReturn(new ArrayList<>(appointments));

        AppointmentRecurringPattern updatedRecurringPattern = allAppointmentRecurringPatternUpdateService
                .getUpdatedRecurringPattern(recurringAppointmentRequest);

        assertEquals(3, updatedRecurringPattern.getAppointments().size());
        assertEquals(2, updatedRecurringPattern.getAppointments().stream()
                .filter(a -> a.getComments().equals("CHANGED COMMENTS")).count());
        updatedRecurringPattern.getAppointments().forEach(a -> {
            if(a.getProviders() != null) {
                int providerId = a.getProviders().iterator().next().getProvider().getId();
                assertEquals(101, providerId);
            }
        });
        verify(recurringAppointmentsService).getUpdatedSetOfAppointments(appointmentRecurringPattern, recurringAppointmentRequest);
        verify(appointmentMapper).mapAppointmentRequestToAppointment(recurringAppointmentRequest.getAppointmentRequest(), appointmentTwo);
    }

    @Test
    public void shouldUpdateStartTimeAndEndTimeForScheduledPendingOccurrences() throws IOException {
        RecurringAppointmentRequest recurringAppointmentRequest = new RecurringAppointmentRequest();
        recurringAppointmentRequest.setAppointmentRequest(new AppointmentRequest());
        RecurringPattern recurringPattern = new RecurringPattern();
        recurringPattern.setFrequency(3);
        recurringPattern.setEndDate(null);
        recurringAppointmentRequest.setRecurringPattern(recurringPattern);
        recurringAppointmentRequest.getAppointmentRequest().setUuid("uuid2");
        recurringAppointmentRequest.setTimeZone("Asia/Calcutta");
        Calendar startTimeCalendar = Calendar.getInstance();
        Calendar endTimeCalendar = Calendar.getInstance();
        Calendar newStartTimeCalendar = Calendar.getInstance();
        Calendar newEndTimeCalendar = Calendar.getInstance();
        int year = startTimeCalendar.get(Calendar.YEAR);
        int month = startTimeCalendar.get(Calendar.MONTH);
        int day = startTimeCalendar.get(Calendar.DATE);
        startTimeCalendar.set(year, month, day, 10, 00, 00);
        endTimeCalendar.set(year, month, day, 10, 30, 00);
        newStartTimeCalendar.set(year, month, day, 12, 00, 00);
        newStartTimeCalendar.set(Calendar.MILLISECOND, 0);
        newEndTimeCalendar.set(year, month, day, 12, 30, 00);
        newEndTimeCalendar.set(Calendar.MILLISECOND, 0);
        AppointmentRecurringPattern appointmentRecurringPattern = new AppointmentRecurringPattern();
        appointmentRecurringPattern.setType(RecurringAppointmentType.DAY);
        appointmentRecurringPattern.setFrequency(3);
        appointmentRecurringPattern.setPeriod(2);
        Appointment appointmentOne = new AppointmentBuilder()
                .withUuid("uuid1")
                .withPatient(patient)
                .withStatus(Completed)
                .withAppointmentKind(AppointmentKind.Scheduled)
                .withStartDateTime(DateUtils.addDays(startTimeCalendar.getTime(), -2))
                .withEndDateTime(DateUtils.addDays(endTimeCalendar.getTime(), -2))
                .build();
        Appointment oldAppointmentTwo = new AppointmentBuilder()
                .withUuid("uuid2")
                .withPatient(patient)
                .withStatus(Scheduled)
                .withAppointmentKind(AppointmentKind.Scheduled)
                .withStartDateTime(startTimeCalendar.getTime())
                .withEndDateTime(endTimeCalendar.getTime())
                .build();
        oldAppointmentTwo.setAppointmentAudits(new HashSet<>());
        Appointment newAppointmentTwo = new AppointmentBuilder()
                .withUuid("uuid2")
                .withPatient(patient)
                .withStatus(Scheduled)
                .withAppointmentKind(AppointmentKind.Scheduled)
                .withStartDateTime(newStartTimeCalendar.getTime())
                .withEndDateTime(newEndTimeCalendar.getTime())
                .build();
        Appointment appointmentThree = new AppointmentBuilder()
                .withUuid("uuid3")
                .withPatient(patient)
                .withStatus(Scheduled)
                .withAppointmentKind(AppointmentKind.Scheduled)
                .withStartDateTime(DateUtils.addDays(startTimeCalendar.getTime(), 2))
                .withEndDateTime(DateUtils.addDays(endTimeCalendar.getTime(), 2))
                .build();
        appointmentThree.setAppointmentAudits(new HashSet<>());
        Appointment appointmentFour = new AppointmentBuilder()
                .withUuid("uuid3")
                .withPatient(patient)
                .withStatus(CheckedIn)
                .withAppointmentKind(AppointmentKind.Scheduled)
                .withStartDateTime(DateUtils.addDays(startTimeCalendar.getTime(), 2))
                .withEndDateTime(DateUtils.addDays(endTimeCalendar.getTime(), 2))
                .build();
        Set<Appointment> appointments = new HashSet<>(Arrays.asList(appointmentOne,
                oldAppointmentTwo, appointmentThree, appointmentFour));
        appointmentRecurringPattern.setAppointments(appointments);


        appointmentOne.setAppointmentRecurringPattern(appointmentRecurringPattern);
        oldAppointmentTwo.setAppointmentRecurringPattern(appointmentRecurringPattern);
        appointmentThree.setAppointmentRecurringPattern(appointmentRecurringPattern);
        appointmentFour.setAppointmentRecurringPattern(appointmentRecurringPattern);
        newAppointmentTwo.setAppointmentRecurringPattern(appointmentRecurringPattern);

        when(appointmentsService.getAppointmentByUuid(any())).thenReturn(newAppointmentTwo);
        when(recurringAppointmentsService.getUpdatedSetOfAppointments(appointmentRecurringPattern, recurringAppointmentRequest))
                .thenReturn(new ArrayList<>(appointments));


        AppointmentRecurringPattern updatedRecurringPattern = allAppointmentRecurringPatternUpdateService
                .getUpdatedRecurringPattern(recurringAppointmentRequest);

        assertEquals(3, updatedRecurringPattern.getAppointments().size());
        assertEquals(2, updatedRecurringPattern.getAppointments()
                .stream()
                .map(appointment -> appointment.getStartDateTime())
                .filter(appointmentTime -> appointmentTime.after(startTimeCalendar.getTime())
                        || appointmentTime.equals(startTimeCalendar.getTime())).count());
        assertEquals(2, updatedRecurringPattern.getAppointments()
                .stream()
                .map(appointment -> appointment.getEndDateTime())
                .filter(appointmentTime -> appointmentTime.after(endTimeCalendar.getTime())
                        || appointmentTime.equals(endTimeCalendar.getTime())).count());
        verify(recurringAppointmentsService).getUpdatedSetOfAppointments(appointmentRecurringPattern, recurringAppointmentRequest);
        verify(appointmentMapper).mapAppointmentRequestToAppointment(recurringAppointmentRequest.getAppointmentRequest(), newAppointmentTwo);
    }

    @Test
    public void shouldUpdateNewProviderForAllPendingOccurrences() throws IOException {
        RecurringAppointmentRequest recurringAppointmentRequest = new RecurringAppointmentRequest();
        recurringAppointmentRequest.setAppointmentRequest(new AppointmentRequest());
        RecurringPattern recurringPattern = new RecurringPattern();
        recurringPattern.setFrequency(3);
        recurringPattern.setEndDate(null);
        recurringAppointmentRequest.setRecurringPattern(recurringPattern);
        recurringAppointmentRequest.getAppointmentRequest().setUuid("uuid2");
        recurringAppointmentRequest.setTimeZone("Asia/Calcutta");
        Calendar startTimeCalendar = Calendar.getInstance();
        Calendar endTimeCalendar = Calendar.getInstance();
        int year = startTimeCalendar.get(Calendar.YEAR);
        int month = startTimeCalendar.get(Calendar.MONTH);
        int day = startTimeCalendar.get(Calendar.DATE);
        startTimeCalendar.set(year, month, day, 18, 00, 00);
        endTimeCalendar.set(year, month, day, 18, 30, 00);
        AppointmentRecurringPattern appointmentRecurringPattern = new AppointmentRecurringPattern();
        appointmentRecurringPattern.setType(RecurringAppointmentType.DAY);
        appointmentRecurringPattern.setFrequency(3);
        appointmentRecurringPattern.setPeriod(2);
        AppointmentProvider appointmentProvider = new AppointmentProvider();
        appointmentProvider.setProvider(new Provider(101));
        appointmentProvider.setResponse(AppointmentProviderResponse.ACCEPTED);
        HashSet<AppointmentProvider> appointmentProviders = new HashSet<>(Collections.singleton(appointmentProvider));

        AppointmentProvider appointmentProviderTwo = new AppointmentProvider();
        appointmentProviderTwo.setProvider(new Provider(102));
        appointmentProviderTwo.setResponse(AppointmentProviderResponse.ACCEPTED);
        HashSet<AppointmentProvider> appointmentProvidersTwo = new HashSet<>(Arrays.asList(appointmentProvider, appointmentProviderTwo));

        Appointment appointmentOne = createAppointment("uuid1", patient, Scheduled,
                AppointmentKind.Scheduled, new AppointmentServiceDefinition(), new AppointmentServiceType(),
                new Location(), appointmentProviders, "appOne",
                DateUtils.addDays(startTimeCalendar.getTime(), 2),
                DateUtils.addDays(endTimeCalendar.getTime(), 2));
        Appointment appointmentTwo = createAppointment("uuid2", patient, Scheduled,
                AppointmentKind.Scheduled, new AppointmentServiceDefinition(), new AppointmentServiceType(),
                new Location(), appointmentProvidersTwo, "appTwo", startTimeCalendar.getTime(),
                endTimeCalendar.getTime());
        Appointment appointmentThree = createAppointment("uuid3", patient, Scheduled,
                AppointmentKind.Scheduled, new AppointmentServiceDefinition(), new AppointmentServiceType(),
                new Location(), appointmentProviders, "appThree",
                DateUtils.addDays(startTimeCalendar.getTime(), 2),
                DateUtils.addDays(endTimeCalendar.getTime(), 2));

        appointmentThree.setAppointmentAudits(new HashSet<>());
        Set<Appointment> appointments = new HashSet<>(Arrays.asList(appointmentOne, appointmentTwo, appointmentThree));
        appointmentRecurringPattern.setAppointments(appointments);
        appointmentOne.setAppointmentRecurringPattern(appointmentRecurringPattern);
        appointmentTwo.setAppointmentRecurringPattern(appointmentRecurringPattern);
        appointmentThree.setAppointmentRecurringPattern(appointmentRecurringPattern);


        when(appointmentsService.getAppointmentByUuid(any())).thenReturn(appointmentTwo);
        when(recurringAppointmentsService.getUpdatedSetOfAppointments(appointmentRecurringPattern, recurringAppointmentRequest))
                .thenReturn(new ArrayList<>(appointments));

        AppointmentRecurringPattern updatedRecurringPattern = allAppointmentRecurringPatternUpdateService
                .getUpdatedRecurringPattern(recurringAppointmentRequest);

        assertEquals(3, updatedRecurringPattern.getAppointments().size());
        updatedRecurringPattern.getAppointments().forEach(a -> {
            assertEquals(2, a.getProviders().size());
        });
        verify(appointmentsService, times(1)).getAppointmentByUuid(anyString());
        verify(recurringAppointmentsService).getUpdatedSetOfAppointments(appointmentRecurringPattern, recurringAppointmentRequest);
        verify(appointmentMapper).mapAppointmentRequestToAppointment(recurringAppointmentRequest.getAppointmentRequest(), appointmentTwo);
    }

    @Test
    public void shouldUpdateNewAwaitingProviderForAllPendingOccurrences() throws IOException {
        RecurringAppointmentRequest recurringAppointmentRequest = new RecurringAppointmentRequest();
        recurringAppointmentRequest.setAppointmentRequest(new AppointmentRequest());
        RecurringPattern recurringPattern = new RecurringPattern();
        recurringPattern.setFrequency(3);
        recurringPattern.setEndDate(null);
        recurringAppointmentRequest.setRecurringPattern(recurringPattern);
        recurringAppointmentRequest.getAppointmentRequest().setUuid("uuid2");
        recurringAppointmentRequest.setTimeZone("Asia/Calcutta");
        Calendar startTimeCalendar = Calendar.getInstance();
        Calendar endTimeCalendar = Calendar.getInstance();
        int year = startTimeCalendar.get(Calendar.YEAR);
        int month = startTimeCalendar.get(Calendar.MONTH);
        int day = startTimeCalendar.get(Calendar.DATE);
        startTimeCalendar.set(year, month, day, 18, 00, 00);
        endTimeCalendar.set(year, month, day, 18, 30, 00);
        AppointmentRecurringPattern appointmentRecurringPattern = new AppointmentRecurringPattern();
        appointmentRecurringPattern.setType(RecurringAppointmentType.DAY);
        appointmentRecurringPattern.setFrequency(3);
        appointmentRecurringPattern.setPeriod(2);
        AppointmentProvider appointmentProvider = new AppointmentProvider();
        appointmentProvider.setProvider(new Provider(101));
        appointmentProvider.setResponse(AppointmentProviderResponse.AWAITING);
        HashSet<AppointmentProvider> appointmentProviders = new HashSet<>(Collections.singleton(appointmentProvider));

        AppointmentProvider appointmentProviderTwo = new AppointmentProvider();
        appointmentProviderTwo.setProvider(new Provider(102));
        appointmentProviderTwo.setResponse(AppointmentProviderResponse.AWAITING);
        HashSet<AppointmentProvider> appointmentProvidersTwo = new HashSet<>(Arrays.asList(appointmentProvider, appointmentProviderTwo));

        Appointment appointmentOne = createAppointment("uuid1", patient, Requested,
                AppointmentKind.Scheduled, new AppointmentServiceDefinition(), new AppointmentServiceType(),
                new Location(), appointmentProviders, "appOne",
                DateUtils.addDays(startTimeCalendar.getTime(), 2),
                DateUtils.addDays(endTimeCalendar.getTime(), 2));
        Appointment appointmentTwo = createAppointment("uuid2", patient, Requested,
                AppointmentKind.Scheduled, new AppointmentServiceDefinition(), new AppointmentServiceType(),
                new Location(), appointmentProvidersTwo, "appTwo", startTimeCalendar.getTime(),
                endTimeCalendar.getTime());
        Appointment appointmentThree = createAppointment("uuid3", patient, Requested,
                AppointmentKind.Scheduled, new AppointmentServiceDefinition(), new AppointmentServiceType(),
                new Location(), appointmentProviders, "appThree",
                DateUtils.addDays(startTimeCalendar.getTime(), 2),
                DateUtils.addDays(endTimeCalendar.getTime(), 2));

        appointmentThree.setAppointmentAudits(new HashSet<>());
        Set<Appointment> appointments = new HashSet<>(Arrays.asList(appointmentOne, appointmentTwo, appointmentThree));
        appointmentRecurringPattern.setAppointments(appointments);
        appointmentOne.setAppointmentRecurringPattern(appointmentRecurringPattern);
        appointmentTwo.setAppointmentRecurringPattern(appointmentRecurringPattern);
        appointmentThree.setAppointmentRecurringPattern(appointmentRecurringPattern);


        when(appointmentsService.getAppointmentByUuid(any())).thenReturn(appointmentTwo);
        when(recurringAppointmentsService.getUpdatedSetOfAppointments(appointmentRecurringPattern, recurringAppointmentRequest))
                .thenReturn(new ArrayList<>(appointments));

        AppointmentRecurringPattern updatedRecurringPattern = allAppointmentRecurringPatternUpdateService
                .getUpdatedRecurringPattern(recurringAppointmentRequest);

        assertEquals(3, updatedRecurringPattern.getAppointments().size());
        updatedRecurringPattern.getAppointments().forEach(a -> {
            assertEquals(2, a.getProviders().size());
        });
        verify(appointmentsService, times(1)).getAppointmentByUuid(anyString());
        verify(recurringAppointmentsService).getUpdatedSetOfAppointments(appointmentRecurringPattern, recurringAppointmentRequest);
        verify(appointmentMapper).mapAppointmentRequestToAppointment(recurringAppointmentRequest.getAppointmentRequest(), appointmentTwo);
    }

    @Test
    public void shouldSetAllProvidersToCancelWhenAllProvidersRemoved() throws IOException {
        RecurringAppointmentRequest recurringAppointmentRequest = new RecurringAppointmentRequest();
        recurringAppointmentRequest.setAppointmentRequest(new AppointmentRequest());
        RecurringPattern recurringPattern = new RecurringPattern();
        recurringPattern.setFrequency(3);
        recurringPattern.setEndDate(null);
        recurringAppointmentRequest.setRecurringPattern(recurringPattern);
        recurringAppointmentRequest.getAppointmentRequest().setUuid("uuid2");
        recurringAppointmentRequest.setTimeZone("Asia/Calcutta");
        Calendar startTimeCalendar = Calendar.getInstance();
        Calendar endTimeCalendar = Calendar.getInstance();
        Calendar newStartTimeCalendar = Calendar.getInstance();
        Calendar newEndTimeCalendar = Calendar.getInstance();
        int year = startTimeCalendar.get(Calendar.YEAR);
        int month = startTimeCalendar.get(Calendar.MONTH);
        int day = startTimeCalendar.get(Calendar.DATE);
        startTimeCalendar.set(year, month, day, 10, 00, 00);
        endTimeCalendar.set(year, month, day, 10, 30, 00);
        newStartTimeCalendar.set(year, month, day, 12, 00, 00);
        newStartTimeCalendar.set(Calendar.MILLISECOND, 0);
        newEndTimeCalendar.set(year, month, day, 12, 30, 00);
        newEndTimeCalendar.set(Calendar.MILLISECOND, 0);
        AppointmentRecurringPattern appointmentRecurringPattern = new AppointmentRecurringPattern();
        appointmentRecurringPattern.setType(RecurringAppointmentType.DAY);
        appointmentRecurringPattern.setFrequency(3);
        appointmentRecurringPattern.setPeriod(2);

        AppointmentProvider appointmentProviderOne = new AppointmentProvider();
        appointmentProviderOne.setProvider(new Provider(101));
        AppointmentProvider appointmentProviderTwo = new AppointmentProvider();
        appointmentProviderTwo.setProvider(new Provider(102));
        AppointmentProvider appointmentProviderThree = new AppointmentProvider();
        appointmentProviderThree.setProvider(new Provider(103));
        HashSet<AppointmentProvider> appointmentProvidersOne = new HashSet<>(Arrays
                .asList(appointmentProviderOne,appointmentProviderTwo,appointmentProviderThree));
        HashSet<AppointmentProvider> appointmentProvidersThree = new HashSet<>(Collections
                .singletonList(appointmentProviderOne));

        Appointment appointmentOne = new AppointmentBuilder()
                .withUuid("uuid1")
                .withPatient(patient)
                .withStatus(Scheduled)
                .withAppointmentKind(AppointmentKind.Scheduled)
                .withStartDateTime(DateUtils.addDays(startTimeCalendar.getTime(), 2))
                .withEndDateTime(DateUtils.addDays(endTimeCalendar.getTime(), 2))
                .withProviders(appointmentProvidersOne)
                .build();
        appointmentOne.setComments("");
        Appointment appointmentTwo = new AppointmentBuilder()
                .withUuid("uuid2")
                .withPatient(patient)
                .withStatus(Scheduled)
                .withAppointmentKind(AppointmentKind.Scheduled)
                .withStartDateTime(newStartTimeCalendar.getTime())
                .withEndDateTime(newEndTimeCalendar.getTime())
                .build();
        appointmentTwo.setComments("CHANGED COMMENTS");
        Appointment appointmentThree = new AppointmentBuilder()
                .withUuid("uuid3")
                .withPatient(patient)
                .withStatus(Scheduled)
                .withAppointmentKind(AppointmentKind.Scheduled)
                .withStartDateTime(DateUtils.addDays(startTimeCalendar.getTime(), 2))
                .withEndDateTime(DateUtils.addDays(endTimeCalendar.getTime(), 2))
                .withProviders(appointmentProvidersThree)
                .build();
        appointmentThree.setAppointmentAudits(new HashSet<>());
        Set<Appointment> appointments = new HashSet<>(Arrays.asList(appointmentOne, appointmentTwo,
                appointmentThree));
        appointmentRecurringPattern.setAppointments(appointments);

        appointmentOne.setAppointmentRecurringPattern(appointmentRecurringPattern);
        appointmentThree.setAppointmentRecurringPattern(appointmentRecurringPattern);
        appointmentTwo.setAppointmentRecurringPattern(appointmentRecurringPattern);

        when(appointmentsService.getAppointmentByUuid(any())).thenReturn(appointmentTwo);
        when(recurringAppointmentsService.getUpdatedSetOfAppointments(appointmentRecurringPattern, recurringAppointmentRequest))
                .thenReturn(new ArrayList<>(appointments));

        AppointmentRecurringPattern updatedRecurringPattern = allAppointmentRecurringPatternUpdateService
                .getUpdatedRecurringPattern(recurringAppointmentRequest);

        assertEquals(3, updatedRecurringPattern.getAppointments().size());
        assertEquals(3, updatedRecurringPattern.getAppointments().stream()
                .filter(a -> a.getComments().equals("CHANGED COMMENTS")).count());
        updatedRecurringPattern.getAppointments().forEach(a -> {
            if (a.getProviders() != null) {
                String uuid = a.getUuid();
                int cancelledProvidersCount = uuid.equals("uuid1")
                        ? appointmentOne.getProviders().size()
                        : uuid.equals("uuid3") ? appointmentThree.getProviders().size() : 0;
                assertEquals(cancelledProvidersCount, a.getProviders().stream()
                        .filter(provider -> provider.getResponse().equals(CANCELLED)).count());
            }
        });
        verify(appointmentsService, times(1)).getAppointmentByUuid(anyString());
        verify(recurringAppointmentsService).getUpdatedSetOfAppointments(appointmentRecurringPattern, recurringAppointmentRequest);
        verify(appointmentMapper).mapAppointmentRequestToAppointment(recurringAppointmentRequest.getAppointmentRequest(), appointmentTwo);
    }

    @Test
    public void shouldUpdateMetadataForAllPendingRecurringAppointments() throws IOException {
        RecurringAppointmentRequest recurringAppointmentRequest = new RecurringAppointmentRequest();
        recurringAppointmentRequest.setAppointmentRequest(new AppointmentRequest());
        RecurringPattern recurringPattern = new RecurringPattern();
        recurringPattern.setFrequency(3);
        recurringPattern.setEndDate(null);
        recurringAppointmentRequest.setRecurringPattern(recurringPattern);
        recurringAppointmentRequest.getAppointmentRequest().setUuid("uuid2");
        recurringAppointmentRequest.setTimeZone("Asia/Calcutta");

        Calendar startTimeCalendar = Calendar.getInstance();
        Calendar endTimeCalendar = Calendar.getInstance();
        int year = startTimeCalendar.get(Calendar.YEAR);
        int month = startTimeCalendar.get(Calendar.MONTH);
        int day = startTimeCalendar.get(Calendar.DATE);
        startTimeCalendar.set(year, month, day, 18, 00, 00);
        endTimeCalendar.set(year, month, day, 18, 30, 00);
        AppointmentRecurringPattern appointmentRecurringPattern = new AppointmentRecurringPattern();
        appointmentRecurringPattern.setType(RecurringAppointmentType.DAY);
        appointmentRecurringPattern.setFrequency(3);
        appointmentRecurringPattern.setPeriod(2);
        Appointment appointmentOne = createAppointment("uuid1", patient, Completed,
                AppointmentKind.Scheduled, new AppointmentServiceDefinition(), new AppointmentServiceType(),
                new Location(), new HashSet<>(), "appOne",
                DateUtils.addDays(startTimeCalendar.getTime(), 2),
                DateUtils.addDays(endTimeCalendar.getTime(), 2));
        Appointment appointmentTwo = createAppointment("uuid2", patient, Scheduled,
                AppointmentKind.Scheduled, new AppointmentServiceDefinition(), new AppointmentServiceType(),
                new Location(), new HashSet<>(), "oldAppTwo", startTimeCalendar.getTime(),
                endTimeCalendar.getTime());
        Appointment appointmentThree = createAppointment("uuid3", patient, Scheduled,
                AppointmentKind.Scheduled, new AppointmentServiceDefinition(), new AppointmentServiceType(),
                new Location(), new HashSet<>(), "appThree",
                DateUtils.addDays(startTimeCalendar.getTime(), 2),
                DateUtils.addDays(endTimeCalendar.getTime(), 2));
        AppointmentServiceDefinition appointmentServiceDefinition = new AppointmentServiceDefinition();
        appointmentServiceDefinition.setUuid("serviceUuid");
        AppointmentServiceType appointmentServiceType = new AppointmentServiceType();
        appointmentServiceType.setUuid("serviceTypeUuid");
        Location location = new Location();
        location.setUuid("locationUuid");
        AppointmentProvider appointmentProvider = new AppointmentProvider();
        appointmentProvider.setProvider(new Provider(101));
        HashSet<AppointmentProvider> appointmentProviders = new HashSet<>(Collections
                .singleton(appointmentProvider));
        appointmentThree.setAppointmentAudits(new HashSet<>());
        Set<Appointment> appointments = new HashSet<>(Arrays.asList(appointmentOne,
                appointmentTwo, appointmentThree));
        appointmentRecurringPattern.setAppointments(appointments);
        appointmentOne.setAppointmentRecurringPattern(appointmentRecurringPattern);
        appointmentTwo.setAppointmentRecurringPattern(appointmentRecurringPattern);
        appointmentThree.setAppointmentRecurringPattern(appointmentRecurringPattern);

        appointmentTwo.setProviders(appointmentProviders);

        when(appointmentsService.getAppointmentByUuid(any())).thenReturn(appointmentTwo);
        when(recurringAppointmentsService.getUpdatedSetOfAppointments(appointmentRecurringPattern, recurringAppointmentRequest))
                .thenReturn(new ArrayList<>(appointments));

        AppointmentRecurringPattern updatedRecurringPattern = allAppointmentRecurringPatternUpdateService
                .getUpdatedRecurringPattern(recurringAppointmentRequest);

        assertEquals(3, updatedRecurringPattern.getAppointments().size());
        updatedRecurringPattern.getAppointments().forEach(a -> {
            if (a.getProviders() != null && a.getProviders().size() != 0) {
                assertEquals(101, a.getProviders().stream().mapToInt(p -> p.getProvider()
                        .getProviderId()).toArray()[0]);
            }
        });
        verify(appointmentsService, times(1)).getAppointmentByUuid(anyString());
        verify(recurringAppointmentsService).getUpdatedSetOfAppointments(appointmentRecurringPattern, recurringAppointmentRequest);
        verify(appointmentMapper).mapAppointmentRequestToAppointment(recurringAppointmentRequest.getAppointmentRequest(), appointmentTwo);
    }

    private Appointment createAppointment(String uuid, Patient patient,
                                          AppointmentStatus appointmentStatus,
                                          AppointmentKind appointmentKind,
                                          AppointmentServiceDefinition appointmentServiceDefinition,
                                          AppointmentServiceType appointmentServiceType,
                                          Location location,
                                          Set<AppointmentProvider> appointmentProviders,
                                          String comments, Date startDateTime, Date endDateTime) {
        return new AppointmentBuilder()
                .withUuid(uuid)
                .withPatient(patient)
                .withStatus(appointmentStatus)
                .withAppointmentKind(appointmentKind)
                .withService(appointmentServiceDefinition)
                .withServiceType(appointmentServiceType)
                .withLocation(location)
                .withProviders(appointmentProviders)
                .withComments(comments)
                .withStartDateTime(startDateTime)
                .withEndDateTime(endDateTime)
                .build();
    }

    @Test
    public void shouldThrowAPIExceptionIfUuidInRequestIsNull() {
        RecurringAppointmentRequest recurringAppointmentRequest = new RecurringAppointmentRequest();
        recurringAppointmentRequest.setTimeZone("some time zone");
        recurringAppointmentRequest.setAppointmentRequest(new AppointmentRequest());
        expectedException.expect(APIException.class);
        expectedException.expectMessage("Invalid appointment for edit");

        allAppointmentRecurringPatternUpdateService.getUpdatedRecurringPattern(recurringAppointmentRequest);
    }

    @Test
    public void shouldExtendAndUpdateMetadata() {
        RecurringAppointmentRequest recurringAppointmentRequest = new RecurringAppointmentRequest();
        recurringAppointmentRequest.setAppointmentRequest(new AppointmentRequest());
        RecurringPattern recurringPattern = new RecurringPattern();
        recurringPattern.setFrequency(3);
        recurringPattern.setEndDate(null);
        recurringAppointmentRequest.setRecurringPattern(recurringPattern);
        recurringAppointmentRequest.getAppointmentRequest().setUuid("uuid2");
        recurringAppointmentRequest.setTimeZone("Asia/Calcutta");

        AppointmentRecurringPattern updatedRecurringPattern = new AppointmentRecurringPattern();
        updatedRecurringPattern.setFrequency(3);
        updatedRecurringPattern.setPeriod(2);
        updatedRecurringPattern.setType(RecurringAppointmentType.DAY);

        Calendar startTimeCalendar = Calendar.getInstance();
        Calendar endTimeCalendar = Calendar.getInstance();
        int year = startTimeCalendar.get(Calendar.YEAR);
        int month = startTimeCalendar.get(Calendar.MONTH);
        int day = startTimeCalendar.get(Calendar.DATE);
        startTimeCalendar.set(year, month, day, 18, 00, 00);
        endTimeCalendar.set(year, month, day, 18, 30, 00);
        AppointmentRecurringPattern appointmentRecurringPattern = new AppointmentRecurringPattern();
        appointmentRecurringPattern.setType(RecurringAppointmentType.DAY);
        appointmentRecurringPattern.setFrequency(2);
        appointmentRecurringPattern.setPeriod(2);
        Appointment appointmentOne = createAppointment("uuid1", patient, Completed,
                AppointmentKind.Scheduled, new AppointmentServiceDefinition(), new AppointmentServiceType(),
                new Location(), new HashSet<>(), "appOne",
                DateUtils.addDays(startTimeCalendar.getTime(), 2),
                DateUtils.addDays(endTimeCalendar.getTime(), 2));
        Appointment appointmentTwo = createAppointment("uuid2", patient, Scheduled,
                AppointmentKind.Scheduled, new AppointmentServiceDefinition(), new AppointmentServiceType(),
                new Location(), new HashSet<>(), "oldAppTwo", startTimeCalendar.getTime(),
                endTimeCalendar.getTime());
        appointmentTwo.setComments("comment");
        appointmentTwo.setAppointmentRecurringPattern(appointmentRecurringPattern);
        Appointment addedAppointmentThree = createAppointment("uuid3", patient, Scheduled,
                AppointmentKind.Scheduled, new AppointmentServiceDefinition(), new AppointmentServiceType(),
                new Location(), new HashSet<>(), "appThree",
                DateUtils.addDays(startTimeCalendar.getTime(), 2),
                DateUtils.addDays(endTimeCalendar.getTime(), 2));

        when(appointmentsService.getAppointmentByUuid(anyString())).thenReturn(appointmentTwo);
        when(recurringAppointmentsService.getUpdatedSetOfAppointments(any(), any())).thenReturn(Arrays.asList(appointmentOne, appointmentTwo, addedAppointmentThree));

        allAppointmentRecurringPatternUpdateService.getUpdatedRecurringPattern(recurringAppointmentRequest);

        assertEquals("comment", addedAppointmentThree.getComments());
        verify(appointmentsService).getAppointmentByUuid(anyString());
        verify(recurringAppointmentsService).getUpdatedSetOfAppointments(appointmentRecurringPattern, recurringAppointmentRequest);
    }

    @Test
    public void shouldVoidTheCancelledProviders() {
    	TimeZone.setDefault(TimeZone.getTimeZone("IST"));
        RecurringAppointmentRequest recurringAppointmentRequest = new RecurringAppointmentRequest();
        AppointmentProviderDetail appointmentProviderDetail = new AppointmentProviderDetail();
        appointmentProviderDetail.setResponse(AppointmentProviderResponse.ACCEPTED.toString());
        appointmentProviderDetail.setUuid("provider-uuid");
        AppointmentRequest appointmentRequest = new AppointmentRequest();
        appointmentRequest.setUuid("uuid1");
        appointmentRequest.setProviders(Collections.singletonList(appointmentProviderDetail));
        RecurringPattern recurringPattern = new RecurringPattern();
        recurringPattern.setFrequency(2);
        recurringPattern.setEndDate(null);
        recurringAppointmentRequest.setAppointmentRequest(appointmentRequest);
        recurringAppointmentRequest.setRecurringPattern(recurringPattern);
        recurringAppointmentRequest.setTimeZone("Asia/Calcutta");
        AppointmentRecurringPattern updatedRecurringPattern = new AppointmentRecurringPattern();
        updatedRecurringPattern.setFrequency(2);
        updatedRecurringPattern.setPeriod(2);
        updatedRecurringPattern.setType(RecurringAppointmentType.DAY);
        Calendar startTimeCalendar = Calendar.getInstance();
        Calendar endTimeCalendar = Calendar.getInstance();
        int year = startTimeCalendar.get(Calendar.YEAR);
        int month = startTimeCalendar.get(Calendar.MONTH);
        int day = startTimeCalendar.get(Calendar.DATE);
        startTimeCalendar.set(year, month, day, 18, 00, 00);
        endTimeCalendar.set(year, month, day, 18, 30, 00);
        AppointmentRecurringPattern appointmentRecurringPattern = new AppointmentRecurringPattern();
        appointmentRecurringPattern.setType(RecurringAppointmentType.DAY);
        appointmentRecurringPattern.setFrequency(2);
        appointmentRecurringPattern.setPeriod(2);
        AppointmentProvider appointmentProviderOne = new AppointmentProvider();
        appointmentProviderOne.setResponse(AppointmentProviderResponse.ACCEPTED);
        appointmentProviderOne.setProvider(new Provider(101));
        appointmentProviderOne.getProvider().setUuid("provider-uuid");
        AppointmentProvider appointmentProviderTwo = new AppointmentProvider();
        appointmentProviderTwo.setResponse(AppointmentProviderResponse.CANCELLED);
        appointmentProviderTwo.setProvider(new Provider(102));
        appointmentProviderTwo.getProvider().setUuid("provider-uuid-tobe-cancel");
        HashSet<AppointmentProvider> existingProviders = new HashSet<>(Arrays.asList(appointmentProviderOne,appointmentProviderTwo));
        Appointment appointmentOne = createAppointment("uuid1", patient, Completed,
                AppointmentKind.Scheduled, new AppointmentServiceDefinition(), new AppointmentServiceType(),
                new Location(),existingProviders, "appOne",
                startTimeCalendar.getTime(),
                endTimeCalendar.getTime());
        Appointment appointmentTwo = createAppointment("uuid2", patient, Scheduled,
                AppointmentKind.Scheduled, new AppointmentServiceDefinition(), new AppointmentServiceType(),
                new Location(), Collections.singleton(appointmentProviderOne), "oldAppTwo", startTimeCalendar.getTime(),
                endTimeCalendar.getTime());
        appointmentTwo.setProviders(existingProviders);
        appointmentOne.setComments("comment");
        appointmentOne.setAppointmentRecurringPattern(appointmentRecurringPattern);
        when(appointmentsService.getAppointmentByUuid(anyString())).thenReturn(appointmentOne);
        when(recurringAppointmentsService.getUpdatedSetOfAppointments(any(), any())).thenReturn(Arrays.asList(appointmentOne, appointmentTwo));
        AppointmentRecurringPattern updated = allAppointmentRecurringPatternUpdateService.getUpdatedRecurringPattern(recurringAppointmentRequest);
        verify(appointmentsService).getAppointmentByUuid(anyString());
        verify(recurringAppointmentsService).getUpdatedSetOfAppointments(appointmentRecurringPattern, recurringAppointmentRequest);

        List<Appointment> appointments = new ArrayList<>(updated.getAppointments());
        List<AppointmentProvider> providers = new ArrayList<>(appointments.get(0).getProviders());
        assertEquals(2, updated.getAppointments().size());
        assertEquals(2, providers.size());
        assertTrue(providers.stream().anyMatch(provider ->
                provider.getResponse().equals(AppointmentProviderResponse.CANCELLED) && provider.getVoided()));
    }
}
