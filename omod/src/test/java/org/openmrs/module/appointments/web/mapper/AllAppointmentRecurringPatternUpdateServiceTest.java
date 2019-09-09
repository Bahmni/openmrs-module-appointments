package org.openmrs.module.appointments.web.mapper;

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
import org.openmrs.module.appointments.helper.AppointmentServiceHelper;
import org.openmrs.module.appointments.model.*;
import org.openmrs.module.appointments.service.AppointmentsService;
import org.openmrs.module.appointments.service.impl.RecurringAppointmentType;
import org.openmrs.module.appointments.web.contract.AppointmentProviderDetail;
import org.openmrs.module.appointments.web.contract.AppointmentRequest;
import org.openmrs.module.appointments.web.contract.RecurringAppointmentRequest;
import org.openmrs.module.appointments.web.contract.RecurringPattern;
import org.openmrs.module.appointments.web.service.impl.DailyRecurringAppointmentsGenerationService;
import org.openmrs.module.appointments.web.service.impl.WeeklyRecurringAppointmentsGenerationService;
import org.openmrs.module.appointments.web.util.AppointmentBuilder;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.IOException;
import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;
import static org.openmrs.module.appointments.model.AppointmentProviderResponse.CANCELLED;
import static org.openmrs.module.appointments.model.AppointmentStatus.*;

@RunWith(PowerMockRunner.class)
public class AllAppointmentRecurringPatternUpdateServiceTest {
    @Mock
    private AppointmentsService appointmentsService;

    @Mock
    private AppointmentServiceHelper appointmentServiceHelper;

    @Mock
    private AppointmentMapper appointmentMapper;

    @Mock
    private Patient patient;

    @InjectMocks
    AllAppointmentRecurringPatternUpdateService allAppointmentRecurringPatternUpdateService;

    @Mock
    DailyRecurringAppointmentsGenerationService dailyRecurringAppointmentsGenerationService;

    @Mock
    WeeklyRecurringAppointmentsGenerationService weeklyRecurringAppointmentsGenerationService;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

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
        when( appointmentServiceHelper.getAppointmentAsJsonString(any())).thenReturn(null);

        AppointmentRecurringPattern updatedRecurringPattern = allAppointmentRecurringPatternUpdateService
                .fromRequest(recurringAppointmentRequest);

        assertEquals(3, updatedRecurringPattern.getAppointments().size());
        assertEquals(2, updatedRecurringPattern.getAppointments().stream()
                .filter(a -> a.getComments().equals("CHANGED COMMENTS")).count());
        updatedRecurringPattern.getAppointments().forEach(a -> {
            if(a.getProviders() != null) {
                int providerId = a.getProviders().iterator().next().getProvider().getId();
                assertEquals(101, providerId);
            }
        });
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
        when(appointmentServiceHelper.getAppointmentAsJsonString(any())).thenReturn(null);


        AppointmentRecurringPattern updatedRecurringPattern = allAppointmentRecurringPatternUpdateService
                .fromRequest(recurringAppointmentRequest);

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
        HashSet<AppointmentProvider> appointmentProviders = new HashSet<>(Collections.singleton(appointmentProvider));

        AppointmentProvider appointmentProviderTwo = new AppointmentProvider();
        appointmentProviderTwo.setProvider(new Provider(102));
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
        when( appointmentServiceHelper.getAppointmentAsJsonString(any())).thenReturn(null);

        AppointmentRecurringPattern updatedRecurringPattern = allAppointmentRecurringPatternUpdateService
                .fromRequest(recurringAppointmentRequest);

        assertEquals(3, updatedRecurringPattern.getAppointments().size());
        updatedRecurringPattern.getAppointments().forEach(a -> {
            assertEquals(2, a.getProviders().size());
        });
        verify(appointmentsService, times(1)).getAppointmentByUuid(anyString());
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
        when( appointmentServiceHelper.getAppointmentAsJsonString(any())).thenReturn(null);

        AppointmentRecurringPattern updatedRecurringPattern = allAppointmentRecurringPatternUpdateService
                .fromRequest(recurringAppointmentRequest);

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
        when( appointmentServiceHelper.getAppointmentAsJsonString(any())).thenReturn(null);

        AppointmentRecurringPattern updatedRecurringPattern = allAppointmentRecurringPatternUpdateService
                .fromRequest(recurringAppointmentRequest);

        assertEquals(3, updatedRecurringPattern.getAppointments().size());
        updatedRecurringPattern.getAppointments().forEach(a -> {
            if (a.getProviders() != null && a.getProviders().size() != 0) {
                assertEquals(101, a.getProviders().stream().mapToInt(p -> p.getProvider()
                        .getProviderId()).toArray()[0]);
            }
        });
        verify(appointmentsService, times(1)).getAppointmentByUuid(anyString());
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
    public void shouldUpdateRecurringPatternByRemovingAppointmentsWhenFrequencyIsDecreased() throws IOException {
        RecurringAppointmentRequest recurringAppointmentRequest = new RecurringAppointmentRequest();
        recurringAppointmentRequest.setAppointmentRequest(new AppointmentRequest());
        RecurringPattern recurringPattern = new RecurringPattern();
        recurringPattern.setFrequency(2);
        recurringPattern.setEndDate(null);
        recurringAppointmentRequest.setRecurringPattern(recurringPattern);
        recurringAppointmentRequest.getAppointmentRequest().setUuid("uuid2");
        recurringAppointmentRequest.setTimeZone("Asia/Calcutta");

        Calendar startTimeCalendar = Calendar.getInstance();
        Calendar endTimeCalendar = Calendar.getInstance();
        int year = startTimeCalendar.get(Calendar.YEAR);
        int month = startTimeCalendar.get(Calendar.MONTH);
        int day = startTimeCalendar.get(Calendar.DATE);
        int hour = startTimeCalendar.get(Calendar.HOUR_OF_DAY);
        startTimeCalendar.set(year, month, day, hour +1 , 00, 00);
        endTimeCalendar.set(year, month, day, hour + 1, 30, 00);
        AppointmentRecurringPattern appointmentRecurringPattern = new AppointmentRecurringPattern();
        appointmentRecurringPattern.setType(RecurringAppointmentType.DAY);
        appointmentRecurringPattern.setFrequency(3);
        appointmentRecurringPattern.setPeriod(2);
        Appointment appointmentOne = createAppointment("uuid1", patient, Completed,
                AppointmentKind.Scheduled, new AppointmentServiceDefinition(), new AppointmentServiceType(),
                new Location(), new HashSet<>(), "appOne",
                DateUtils.addDays(startTimeCalendar.getTime(), -2),
                DateUtils.addDays(endTimeCalendar.getTime(), -2));
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
        when( appointmentServiceHelper.getAppointmentAsJsonString(any())).thenReturn(null);
        when(dailyRecurringAppointmentsGenerationService.removeRecurringAppointments(appointmentRecurringPattern,recurringAppointmentRequest)).thenReturn(Arrays.asList(appointmentOne, appointmentTwo));

        AppointmentRecurringPattern updatedRecurringPattern = allAppointmentRecurringPatternUpdateService
                .fromRequest(recurringAppointmentRequest);

        assertEquals(2, updatedRecurringPattern.getActiveAppointments().size());
        updatedRecurringPattern.getAppointments().forEach(a -> {
            if (a.getProviders() != null && a.getProviders().size() != 0) {
                assertEquals(101, a.getProviders().stream().mapToInt(p -> p.getProvider()
                        .getProviderId()).toArray()[0]);
            }
        });
        verify(appointmentsService, times(1)).getAppointmentByUuid(anyString());

    }

    @Test
    public void shouldUpdateRecurringPatternByAddingNeWAppointmentsWhenFrequencyIsIncreased() throws IOException {
        RecurringAppointmentRequest recurringAppointmentRequest = new RecurringAppointmentRequest();
        recurringAppointmentRequest.setAppointmentRequest(new AppointmentRequest());
        RecurringPattern recurringPattern = new RecurringPattern();
        recurringPattern.setFrequency(5);
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
                DateUtils.addDays(startTimeCalendar.getTime(), -2),
                DateUtils.addDays(endTimeCalendar.getTime(), -2));
        Appointment appointmentTwo = createAppointment("uuid2", patient, Scheduled,
                AppointmentKind.Scheduled, new AppointmentServiceDefinition(), new AppointmentServiceType(),
                new Location(), new HashSet<>(), "oldAppTwo", startTimeCalendar.getTime(),
                endTimeCalendar.getTime());
        Appointment appointmentThree = createAppointment("uuid3", patient, Scheduled,
                AppointmentKind.Scheduled, new AppointmentServiceDefinition(), new AppointmentServiceType(),
                new Location(), new HashSet<>(), "appThree",
                DateUtils.addDays(startTimeCalendar.getTime(), 2),
                DateUtils.addDays(endTimeCalendar.getTime(), 2));
        Appointment appointmentFour = createAppointment("uuid4", patient, Scheduled,
                AppointmentKind.Scheduled, new AppointmentServiceDefinition(), new AppointmentServiceType(),
                new Location(), new HashSet<>(), "appThree",
                DateUtils.addDays(startTimeCalendar.getTime(), 2),
                DateUtils.addDays(endTimeCalendar.getTime(), 2));
        Appointment appointmentFive = createAppointment("uuid5", patient, Scheduled,
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
        when(appointmentMapper.fromRequest(recurringAppointmentRequest.getAppointmentRequest())).thenAnswer(x -> new Appointment());
        when(appointmentsService.getAppointmentByUuid(any())).thenReturn(appointmentTwo);
        when( appointmentServiceHelper.getAppointmentAsJsonString(any())).thenReturn(null);
        when(dailyRecurringAppointmentsGenerationService.addAppointments(appointmentRecurringPattern, recurringAppointmentRequest))
                .thenReturn(Arrays.asList(appointmentOne, appointmentTwo, appointmentThree, appointmentFour, appointmentFive));

        AppointmentRecurringPattern updatedRecurringPattern = allAppointmentRecurringPatternUpdateService
                .fromRequest(recurringAppointmentRequest);

        assertEquals(5, updatedRecurringPattern.getActiveAppointments().size());
        verify(appointmentsService, times(1)).getAppointmentByUuid(anyString());

    }

    @Test
    public void shouldUpdateRecurringPatternByRemovingAppointmentsWhenEndDateIsDecreased() throws IOException {
        RecurringAppointmentRequest recurringAppointmentRequest = new RecurringAppointmentRequest();
        recurringAppointmentRequest.setAppointmentRequest(new AppointmentRequest());
        Calendar startTimeCalendar = Calendar.getInstance();
        Calendar endTimeCalendar = Calendar.getInstance();
        int year = startTimeCalendar.get(Calendar.YEAR);
        int month = startTimeCalendar.get(Calendar.MONTH);
        int day = startTimeCalendar.get(Calendar.DATE);
        int hour = startTimeCalendar.get(Calendar.HOUR_OF_DAY);
        startTimeCalendar.set(year, month, day, hour + 1, 00, 00);
        endTimeCalendar.set(year, month, day, hour + 1, 30, 00);
        RecurringPattern recurringPattern = new RecurringPattern();
        recurringPattern.setFrequency(0);
        recurringPattern.setEndDate(startTimeCalendar.getTime());
        recurringAppointmentRequest.setRecurringPattern(recurringPattern);
        recurringAppointmentRequest.getAppointmentRequest().setUuid("uuid2");
        recurringAppointmentRequest.setTimeZone("Asia/Calcutta");



        AppointmentRecurringPattern appointmentRecurringPattern = new AppointmentRecurringPattern();
        appointmentRecurringPattern.setType(RecurringAppointmentType.DAY);
        appointmentRecurringPattern.setFrequency(0);
        appointmentRecurringPattern.setEndDate(DateUtils.addDays(startTimeCalendar.getTime(), 2));
        appointmentRecurringPattern.setPeriod(2);
        Appointment appointmentOne = createAppointment("uuid1", patient, Completed,
                AppointmentKind.Scheduled, new AppointmentServiceDefinition(), new AppointmentServiceType(),
                new Location(), new HashSet<>(), "appOne",
                DateUtils.addDays(startTimeCalendar.getTime(), -2),
                DateUtils.addDays(endTimeCalendar.getTime(), -2));
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
        when(appointmentServiceHelper.getAppointmentAsJsonString(any())).thenReturn(null);
        when(dailyRecurringAppointmentsGenerationService.removeRecurringAppointments(appointmentRecurringPattern, recurringAppointmentRequest)).thenReturn(Arrays.asList(appointmentOne, appointmentTwo));
        AppointmentRecurringPattern updatedRecurringPattern = allAppointmentRecurringPatternUpdateService
                .fromRequest(recurringAppointmentRequest);

        assertEquals(2, updatedRecurringPattern.getActiveAppointments().size());
        updatedRecurringPattern.getAppointments().forEach(a -> {
            if (a.getProviders() != null && a.getProviders().size() != 0) {
                assertEquals(101, a.getProviders().stream().mapToInt(p -> p.getProvider()
                        .getProviderId()).toArray()[0]);
            }
        });
        verify(appointmentsService, times(1)).getAppointmentByUuid(anyString());

    }

    @Test
    public void shouldUpdateRecurringPatternByAddingNeWAppointmentsWhenEndDateIsIncreased() throws IOException {
        RecurringAppointmentRequest recurringAppointmentRequest = new RecurringAppointmentRequest();
        recurringAppointmentRequest.setAppointmentRequest(new AppointmentRequest());
        Calendar startTimeCalendar = Calendar.getInstance();
        Calendar endTimeCalendar = Calendar.getInstance();
        int year = startTimeCalendar.get(Calendar.YEAR);
        int month = startTimeCalendar.get(Calendar.MONTH);
        int day = startTimeCalendar.get(Calendar.DATE);
        startTimeCalendar.set(year, month, day, 18, 00, 00);
        endTimeCalendar.set(year, month, day, 18, 30, 00);
        RecurringPattern recurringPattern = new RecurringPattern();
        recurringPattern.setFrequency(5);
        recurringPattern.setEndDate(DateUtils.addDays(startTimeCalendar.getTime(), 6));
        recurringAppointmentRequest.setRecurringPattern(recurringPattern);
        recurringAppointmentRequest.getAppointmentRequest().setUuid("uuid2");
        recurringAppointmentRequest.setTimeZone("Asia/Calcutta");


        AppointmentRecurringPattern appointmentRecurringPattern = new AppointmentRecurringPattern();
        appointmentRecurringPattern.setType(RecurringAppointmentType.DAY);
        appointmentRecurringPattern.setFrequency(3);
        appointmentRecurringPattern.setEndDate(DateUtils.addDays(startTimeCalendar.getTime(), 2));
        appointmentRecurringPattern.setPeriod(2);
        Appointment appointmentOne = createAppointment("uuid1", patient, Completed,
                AppointmentKind.Scheduled, new AppointmentServiceDefinition(), new AppointmentServiceType(),
                new Location(), new HashSet<>(), "appOne",
                DateUtils.addDays(startTimeCalendar.getTime(), -2),
                DateUtils.addDays(endTimeCalendar.getTime(), -2));
        Appointment appointmentTwo = createAppointment("uuid2", patient, Scheduled,
                AppointmentKind.Scheduled, new AppointmentServiceDefinition(), new AppointmentServiceType(),
                new Location(), new HashSet<>(), "oldAppTwo", startTimeCalendar.getTime(),
                endTimeCalendar.getTime());
        Appointment appointmentThree = createAppointment("uuid3", patient, Scheduled,
                AppointmentKind.Scheduled, new AppointmentServiceDefinition(), new AppointmentServiceType(),
                new Location(), new HashSet<>(), "appThree",
                DateUtils.addDays(startTimeCalendar.getTime(), 2),
                DateUtils.addDays(endTimeCalendar.getTime(), 2));
        Appointment appointmentFour = createAppointment("uuid4", patient, Scheduled,
                AppointmentKind.Scheduled, new AppointmentServiceDefinition(), new AppointmentServiceType(),
                new Location(), new HashSet<>(), "appThree",
                DateUtils.addDays(startTimeCalendar.getTime(), 2),
                DateUtils.addDays(endTimeCalendar.getTime(), 2));
        Appointment appointmentFive = createAppointment("uuid5", patient, Scheduled,
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
        List<Appointment> appointments = Arrays.asList(appointmentOne,
                appointmentTwo, appointmentThree);
        appointmentRecurringPattern.setAppointments(new HashSet(appointments));
        appointmentOne.setAppointmentRecurringPattern(appointmentRecurringPattern);
        appointmentTwo.setAppointmentRecurringPattern(appointmentRecurringPattern);
        appointmentThree.setAppointmentRecurringPattern(appointmentRecurringPattern);
        appointmentTwo.setProviders(appointmentProviders);
        when(appointmentMapper.fromRequest(recurringAppointmentRequest.getAppointmentRequest())).thenAnswer(x -> new Appointment());
        when(appointmentsService.getAppointmentByUuid(any())).thenReturn(appointmentTwo);
        when( appointmentServiceHelper.getAppointmentAsJsonString(any())).thenReturn(null);
        when(dailyRecurringAppointmentsGenerationService.addAppointments(appointmentRecurringPattern,recurringAppointmentRequest))
                .thenReturn((Arrays.asList(appointmentOne, appointmentTwo, appointmentThree,appointmentFour, appointmentFive)));

        AppointmentRecurringPattern updatedRecurringPattern = allAppointmentRecurringPatternUpdateService
                .fromRequest(recurringAppointmentRequest);

        assertEquals(5, updatedRecurringPattern.getActiveAppointments().size());
        verify(appointmentsService, times(1)).getAppointmentByUuid(anyString());

    }

    @Test
    public void shouldThrowAPIExceptionIfUuidInRequestIsNull() {
        RecurringAppointmentRequest recurringAppointmentRequest = new RecurringAppointmentRequest();
        recurringAppointmentRequest.setTimeZone("some time zone");
        recurringAppointmentRequest.setAppointmentRequest(new AppointmentRequest());
        expectedException.expect(APIException.class);
        expectedException.expectMessage("Invalid appointment for edit");

        allAppointmentRecurringPatternUpdateService.fromRequest(recurringAppointmentRequest);
    }
}
