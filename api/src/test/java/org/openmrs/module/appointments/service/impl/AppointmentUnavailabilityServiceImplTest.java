package org.openmrs.module.appointments.service.impl;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;
import org.openmrs.Location;
import org.openmrs.Provider;
import org.openmrs.User;
import org.openmrs.api.APIException;
import org.openmrs.api.LocationService;
import org.openmrs.api.ProviderService;
import org.openmrs.api.context.Context;
import org.openmrs.module.appointments.dao.AppointmentUnavailabilityDao;
import org.openmrs.module.appointments.model.AppointmentServiceDefinition;
import org.openmrs.module.appointments.model.AppointmentUnavailability;
import org.openmrs.module.appointments.search.param.AppointmentUnavailabilitySearchParams;
import org.openmrs.module.appointments.service.AppointmentServiceDefinitionService;

import java.sql.Time;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static java.util.Collections.emptyList;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.mockStatic;

public class AppointmentUnavailabilityServiceImplTest {

    private MockedStatic<Context> mockedContext;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Mock
    private AppointmentUnavailabilityDao appointmentUnavailabilityDao;

    @Mock
    private AppointmentServiceDefinitionService appointmentServiceDefinitionService;

    @Mock
    private LocationService locationService;

    @Mock
    private ProviderService providerService;

    private AppointmentUnavailabilityServiceImpl service;

    private User authenticatedUser;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        service = new AppointmentUnavailabilityServiceImpl(appointmentUnavailabilityDao, appointmentServiceDefinitionService);
        mockedContext = mockStatic(Context.class);
        authenticatedUser = new User(1);
        mockedContext.when(Context::getAuthenticatedUser).thenReturn(authenticatedUser);
        mockedContext.when(Context::getLocationService).thenReturn(locationService);
        mockedContext.when(Context::getProviderService).thenReturn(providerService);
    }

    @After
    public void tearDown() {
        if (mockedContext != null) {
            mockedContext.close();
        }
    }

    @Test
    public void shouldSaveValidUnavailabilityBlock() {
        List<AppointmentUnavailability> unavailabilities = createValidUnavailabilityList();
        Location location = createLocation(1, "Location 1", false);

        when(locationService.getLocation(1)).thenReturn(location);
        when(appointmentUnavailabilityDao.save(any(AppointmentUnavailability.class)))
                .thenAnswer(invocation -> invocation.getArguments()[0]);

        List<AppointmentUnavailability> result = service.save(unavailabilities);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(appointmentUnavailabilityDao, times(1)).save(any(AppointmentUnavailability.class));
    }

    @Test
    public void shouldRejectEmptyList() {
        expectedException.expect(APIException.class);
        expectedException.expectMessage("at least one unavailability block");

        service.save(new ArrayList<>());
    }

    @Test
    public void shouldRejectNullList() {
        expectedException.expect(APIException.class);
        expectedException.expectMessage("at least one unavailability block");

        service.save(null);
    }

    @Test
    public void shouldRejectWhenLocationIsRetired() {
        List<AppointmentUnavailability> unavailabilities = createValidUnavailabilityList();
        Location location = createLocation(1, "Location 1", true); // retired

        when(locationService.getLocation(1)).thenReturn(location);

        expectedException.expect(APIException.class);
        expectedException.expectMessage("[0] location is invalid or retired");

        service.save(unavailabilities);
    }

    @Test
    public void shouldRejectWhenServiceIsVoided() {
        List<AppointmentUnavailability> unavailabilities = createValidUnavailabilityList();
        Location location = createLocation(1, "Location 1", false);
        AppointmentServiceDefinition appointmentService = createService(1, "Service 1", location, true); // voided
        unavailabilities.get(0).setService(appointmentService);

        when(locationService.getLocation(1)).thenReturn(location);
        when(appointmentServiceDefinitionService.getAppointmentServiceByUuid("service-uuid"))
                .thenReturn(appointmentService);

        expectedException.expect(APIException.class);
        expectedException.expectMessage("[0] service is invalid or voided");

        this.service.save(unavailabilities);
    }

    @Test
    public void shouldRejectWhenServiceLocationDoesNotMatch() {
        List<AppointmentUnavailability> unavailabilities = createValidUnavailabilityList();
        Location location1 = createLocation(1, "Location 1", false);
        Location location2 = createLocation(2, "Location 2", false);
        AppointmentServiceDefinition appointmentService = createService(1, "Service 1", location2, false);
        unavailabilities.get(0).setService(appointmentService);

        when(locationService.getLocation(1)).thenReturn(location1);
        when(appointmentServiceDefinitionService.getAppointmentServiceByUuid("service-uuid"))
                .thenReturn(appointmentService);

        expectedException.expect(APIException.class);
        expectedException.expectMessage("[0] Service does not belong to the specified location");

        this.service.save(unavailabilities);
    }

    @Test
    public void shouldRejectWhenProviderIsRetired() {
        List<AppointmentUnavailability> unavailabilities = createValidUnavailabilityList();
        Location location = createLocation(1, "Location 1", false);
        Provider provider = createProvider(1, "Provider 1", true); // retired
        unavailabilities.get(0).setProvider(provider);

        when(locationService.getLocation(1)).thenReturn(location);
        when(providerService.getProvider(1)).thenReturn(provider);

        expectedException.expect(APIException.class);
        expectedException.expectMessage("[0] provider is invalid or retired");

        service.save(unavailabilities);
    }

    @Test
    public void shouldRejectWhenEndDateBeforeStartDate() {
        List<AppointmentUnavailability> unavailabilities = createValidUnavailabilityList();
        Location location = createLocation(1, "Location 1", false);
        LocalDate startDate = LocalDate.now().plusDays(35);
        LocalDate endDate = LocalDate.now().plusDays(30);
        unavailabilities.get(0).setStartDate(java.sql.Date.valueOf(startDate));
        unavailabilities.get(0).setEndDate(java.sql.Date.valueOf(endDate));

        when(locationService.getLocation(1)).thenReturn(location);

        expectedException.expect(APIException.class);
        expectedException.expectMessage("[0] End date/time must be after start date/time");

        service.save(unavailabilities);
    }

    @Test
    public void shouldRejectWhenEndTimeBeforeStartTimeOnSameDate() {
        List<AppointmentUnavailability> unavailabilities = createValidUnavailabilityList();
        Location location = createLocation(1, "Location 1", false);
        LocalDate futureDate = LocalDate.now().plusDays(30);
        unavailabilities.get(0).setStartDate(java.sql.Date.valueOf(futureDate));
        unavailabilities.get(0).setEndDate(java.sql.Date.valueOf(futureDate));
        unavailabilities.get(0).setStartTime(Time.valueOf("17:00:00"));
        unavailabilities.get(0).setEndTime(Time.valueOf("09:00:00"));

        when(locationService.getLocation(1)).thenReturn(location);

        expectedException.expect(APIException.class);
        expectedException.expectMessage("[0] End date/time must be after start date/time");

        service.save(unavailabilities);
    }

    @Test
    public void shouldRejectWhenEndTimeIsInPast() {
        List<AppointmentUnavailability> unavailabilities = createValidUnavailabilityList();
        Location location = createLocation(1, "Location 1", false);
        unavailabilities.get(0).setStartDate(java.sql.Date.valueOf("2020-01-01"));
        unavailabilities.get(0).setEndDate(java.sql.Date.valueOf("2020-01-02"));

        when(locationService.getLocation(1)).thenReturn(location);

        expectedException.expect(APIException.class);
        expectedException.expectMessage("[0] Cannot create unavailability block that has already ended");

        service.save(unavailabilities);
    }

    @Test
    public void shouldAcceptWhenStartInPastButEndInFuture() {
        List<AppointmentUnavailability> unavailabilities = createValidUnavailabilityList();
        Location location = createLocation(1, "Location 1", false);
        LocalDate today = LocalDate.now();
        unavailabilities.get(0).setStartDate(java.sql.Date.valueOf(today.minusDays(1)));
        unavailabilities.get(0).setEndDate(java.sql.Date.valueOf(today.plusYears(1)));

        when(locationService.getLocation(1)).thenReturn(location);
        when(appointmentUnavailabilityDao.save(any(AppointmentUnavailability.class)))
                .thenAnswer(invocation -> invocation.getArguments()[0]);

        List<AppointmentUnavailability> result = service.save(unavailabilities);

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    public void shouldDelegateGetByUuidToDao() {
        String uuid = "test-uuid";
        service.getByUuid(uuid);
        verify(appointmentUnavailabilityDao, times(1)).getByUuid(uuid);
    }

    @Test
    public void shouldDelegateGetAllToDao() {
        AppointmentUnavailabilitySearchParams searchParams = new AppointmentUnavailabilitySearchParams();
        searchParams.setLocationUuid("location-uuid-1");

        when(appointmentUnavailabilityDao.getAll(searchParams)).thenReturn(emptyList());

        service.getAll(searchParams);

        verify(appointmentUnavailabilityDao, times(1)).getAll(searchParams);
    }

    @Test
    public void shouldVoidAppointmentUnavailability() {
        AppointmentUnavailability unavailability = createValidUnavailability();
        unavailability.setVoided(false);

        service.voidAppointmentUnavailability(unavailability, "Test reason");

        assertTrue(unavailability.getVoided());
        assertEquals("Test reason", unavailability.getVoidReason());
        assertNotNull(unavailability.getVoidedBy());
        assertNotNull(unavailability.getDateVoided());
        verify(appointmentUnavailabilityDao, times(1)).save(unavailability);
    }

    private List<AppointmentUnavailability> createValidUnavailabilityList() {
        List<AppointmentUnavailability> list = new ArrayList<>();
        list.add(createValidUnavailability());
        return list;
    }

    private AppointmentUnavailability createValidUnavailability() {
        AppointmentUnavailability unavailability = new AppointmentUnavailability();
        Location location = createLocation(1, "Location 1", false);
        unavailability.setLocation(location);
        LocalDate futureDate = LocalDate.now().plusDays(30);
        unavailability.setStartDate(java.sql.Date.valueOf(futureDate));
        unavailability.setStartTime(Time.valueOf("09:00:00"));
        unavailability.setEndDate(java.sql.Date.valueOf(futureDate));
        unavailability.setEndTime(Time.valueOf("17:00:00"));
        return unavailability;
    }

    private Location createLocation(Integer id, String name, boolean retired) {
        Location location = new Location();
        location.setLocationId(id);
        location.setName(name);
        location.setRetired(retired);
        return location;
    }

    private AppointmentServiceDefinition createService(Integer id, String name, Location location, boolean voided) {
        AppointmentServiceDefinition appointmentService = new AppointmentServiceDefinition();
        appointmentService.setAppointmentServiceId(id);
        appointmentService.setName(name);
        appointmentService.setLocation(location);
        appointmentService.setVoided(voided);
        appointmentService.setUuid("service-uuid");
        return appointmentService;
    }

    private Provider createProvider(Integer id, String name, boolean retired) {
        Provider provider = new Provider();
        provider.setProviderId(id);
        provider.setName(name);
        provider.setRetired(retired);
        return provider;
    }
}
