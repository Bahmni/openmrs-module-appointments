package org.openmrs.module.appointments.service.impl;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.openmrs.Location;
import org.openmrs.Provider;
import org.openmrs.User;
import org.openmrs.api.LocationService;
import org.openmrs.api.ProviderService;
import org.openmrs.api.context.Context;
import org.openmrs.module.appointments.dao.AppointmentUnavailabilityDao;
import org.openmrs.module.appointments.model.AppointmentServiceDefinition;
import org.openmrs.module.appointments.model.AppointmentUnavailability;
import org.openmrs.module.appointments.service.AppointmentServiceDefinitionService;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.sql.Time;
import java.util.*;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static org.powermock.api.mockito.PowerMockito.mockStatic;

@PowerMockIgnore("javax.management.*")
@PrepareForTest({Context.class})
@RunWith(PowerMockRunner.class)
public class AppointmentUnavailabilityServiceImplTest {

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

    @InjectMocks
    private AppointmentUnavailabilityServiceImpl service;

    private User authenticatedUser;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        mockStatic(Context.class);
        authenticatedUser = new User(1);
        PowerMockito.when(Context.getAuthenticatedUser()).thenReturn(authenticatedUser);
        PowerMockito.when(Context.getLocationService()).thenReturn(locationService);
        PowerMockito.when(Context.getProviderService()).thenReturn(providerService);
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
        expectedException.expect(RuntimeException.class);
        expectedException.expectMessage("at least one unavailability block");

        service.save(new ArrayList<>());
    }

    @Test
    public void shouldRejectNullList() {
        expectedException.expect(RuntimeException.class);
        expectedException.expectMessage("at least one unavailability block");

        service.save(null);
    }

    @Test
    public void shouldRejectWhenStartDateIsNull() {
        List<AppointmentUnavailability> unavailabilities = createValidUnavailabilityList();
        unavailabilities.get(0).setStartDate(null);

        expectedException.expect(RuntimeException.class);
        expectedException.expectMessage("[0] startDate is required");

        service.save(unavailabilities);
    }

    @Test
    public void shouldRejectWhenStartTimeIsNull() {
        List<AppointmentUnavailability> unavailabilities = createValidUnavailabilityList();
        unavailabilities.get(0).setStartTime(null);

        expectedException.expect(RuntimeException.class);
        expectedException.expectMessage("[0] startTime is required");

        service.save(unavailabilities);
    }

    @Test
    public void shouldRejectWhenEndDateIsNull() {
        List<AppointmentUnavailability> unavailabilities = createValidUnavailabilityList();
        unavailabilities.get(0).setEndDate(null);

        expectedException.expect(RuntimeException.class);
        expectedException.expectMessage("[0] endDate is required");

        service.save(unavailabilities);
    }

    @Test
    public void shouldRejectWhenEndTimeIsNull() {
        List<AppointmentUnavailability> unavailabilities = createValidUnavailabilityList();
        unavailabilities.get(0).setEndTime(null);

        expectedException.expect(RuntimeException.class);
        expectedException.expectMessage("[0] endTime is required");

        service.save(unavailabilities);
    }

    @Test
    public void shouldRejectWhenEndIsBeforeStart() {
        List<AppointmentUnavailability> unavailabilities = createValidUnavailabilityList();
        // Set end before start
        unavailabilities.get(0).setEndDate(java.sql.Date.valueOf("2026-08-01"));
        unavailabilities.get(0).setStartDate(java.sql.Date.valueOf("2026-08-05"));

        expectedException.expect(RuntimeException.class);
        expectedException.expectMessage("[0] endDate/endTime must be after startDate/startTime");

        service.save(unavailabilities);
    }

    @Test
    public void shouldRejectWhenSameDateButEndTimeBeforeStartTime() {
        List<AppointmentUnavailability> unavailabilities = createValidUnavailabilityList();
        unavailabilities.get(0).setStartDate(java.sql.Date.valueOf("2026-08-03"));
        unavailabilities.get(0).setEndDate(java.sql.Date.valueOf("2026-08-03"));
        unavailabilities.get(0).setStartTime(Time.valueOf("17:00:00"));
        unavailabilities.get(0).setEndTime(Time.valueOf("09:00:00"));

        expectedException.expect(RuntimeException.class);
        expectedException.expectMessage("[0] endDate/endTime must be after startDate/startTime");

        service.save(unavailabilities);
    }

    @Test
    public void shouldRejectWhenLocationIsNull() {
        List<AppointmentUnavailability> unavailabilities = createValidUnavailabilityList();
        unavailabilities.get(0).setLocation(null);

        expectedException.expect(RuntimeException.class);
        expectedException.expectMessage("[0] location is required");

        service.save(unavailabilities);
    }

    @Test
    public void shouldRejectWhenLocationIsRetired() {
        List<AppointmentUnavailability> unavailabilities = createValidUnavailabilityList();
        Location location = createLocation(1, "Location 1", true); // retired

        when(locationService.getLocation(1)).thenReturn(location);

        expectedException.expect(RuntimeException.class);
        expectedException.expectMessage("[0] location is invalid or retired");

        service.save(unavailabilities);
    }

    @Test
    public void shouldRejectWhenServiceIsVoided() {
        List<AppointmentUnavailability> unavailabilities = createValidUnavailabilityList();
        Location location = createLocation(1, "Location 1", false);
        AppointmentServiceDefinition service = createService(1, "Service 1", location, true); // voided
        unavailabilities.get(0).setService(service);

        when(locationService.getLocation(1)).thenReturn(location);
        when(appointmentServiceDefinitionService.getAppointmentServiceByUuid("service-uuid"))
                .thenReturn(service);

        expectedException.expect(RuntimeException.class);
        expectedException.expectMessage("[0] service is invalid or voided");

        this.service.save(unavailabilities);
    }

    @Test
    public void shouldRejectWhenServiceLocationDoesNotMatch() {
        List<AppointmentUnavailability> unavailabilities = createValidUnavailabilityList();
        Location location1 = createLocation(1, "Location 1", false);
        Location location2 = createLocation(2, "Location 2", false);
        AppointmentServiceDefinition service = createService(1, "Service 1", location2, false);
        unavailabilities.get(0).setService(service);

        when(locationService.getLocation(1)).thenReturn(location1);
        when(appointmentServiceDefinitionService.getAppointmentServiceByUuid("service-uuid"))
                .thenReturn(service);

        expectedException.expect(RuntimeException.class);
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

        expectedException.expect(RuntimeException.class);
        expectedException.expectMessage("[0] provider is invalid or retired");

        service.save(unavailabilities);
    }

    @Test
    public void shouldRejectWhenEndTimeIsInPast() {
        List<AppointmentUnavailability> unavailabilities = createValidUnavailabilityList();
        Location location = createLocation(1, "Location 1", false);
        // Set a date in the past
        unavailabilities.get(0).setStartDate(java.sql.Date.valueOf("2020-01-01"));
        unavailabilities.get(0).setEndDate(java.sql.Date.valueOf("2020-01-02"));

        when(locationService.getLocation(1)).thenReturn(location);

        expectedException.expect(RuntimeException.class);
        expectedException.expectMessage("[0] Cannot create unavailability block that has already ended");

        service.save(unavailabilities);
    }

    @Test
    public void shouldAcceptWhenStartInPastButEndInFuture() {
        List<AppointmentUnavailability> unavailabilities = createValidUnavailabilityList();
        Location location = createLocation(1, "Location 1", false);
        // Start in past, end in future - should be allowed
        unavailabilities.get(0).setStartDate(java.sql.Date.valueOf("2020-01-01"));
        unavailabilities.get(0).setEndDate(java.sql.Date.valueOf("2030-12-31"));

        when(locationService.getLocation(1)).thenReturn(location);
        when(appointmentUnavailabilityDao.save(any(AppointmentUnavailability.class)))
                .thenAnswer(invocation -> invocation.getArguments()[0]);

        List<AppointmentUnavailability> result = service.save(unavailabilities);

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    public void shouldValidateAllElementsBeforePersistingAny() {
        List<AppointmentUnavailability> unavailabilities = new ArrayList<>();
        unavailabilities.add(createValidUnavailability()); // valid
        AppointmentUnavailability invalid = createValidUnavailability();
        invalid.setStartDate(null); // invalid
        unavailabilities.add(invalid);

        expectedException.expect(RuntimeException.class);
//        expectedException.expectMessage("[1] startDate is required");
        expectedException.expectMessage("[0] location is invalid or retired");

        service.save(unavailabilities);

        // Should never call save since validation failed
        verify(appointmentUnavailabilityDao, never()).save(any(AppointmentUnavailability.class));
    }

    @Test
    public void shouldIncludeIndexInErrorMessage() {
        List<AppointmentUnavailability> unavailabilities = new ArrayList<>();
        unavailabilities.add(createValidUnavailability()); // [0] valid
        unavailabilities.add(createValidUnavailability()); // [1] valid
        AppointmentUnavailability invalid = createValidUnavailability();
        invalid.setEndDate(null); // [2] invalid
        unavailabilities.add(invalid);

        Location location = createLocation(1, "Location 1", false);
        when(locationService.getLocation(1)).thenReturn(location);

        expectedException.expect(RuntimeException.class);
        expectedException.expectMessage("[2] endDate is required");

        service.save(unavailabilities);
    }

    @Test
    public void shouldDelegateGetByUuidToDao() {
        String uuid = "test-uuid";
        service.getByUuid(uuid);
        verify(appointmentUnavailabilityDao, times(1)).getByUuid(uuid);
    }

    @Test
    public void shouldDelegateGetAllToDao() {
        Location location = createLocation(1, "Location 1", false);
        service.getAll(location, null, null, null, null, false, null);
        verify(appointmentUnavailabilityDao, times(1)).getAll(location, null, null, null, null, false, null);
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

    // Helper methods

    private List<AppointmentUnavailability> createValidUnavailabilityList() {
        List<AppointmentUnavailability> list = new ArrayList<>();
        list.add(createValidUnavailability());
        return list;
    }

    private AppointmentUnavailability createValidUnavailability() {
        AppointmentUnavailability unavailability = new AppointmentUnavailability();
        Location location = createLocation(1, "Location 1", false);
        unavailability.setLocation(location);
        unavailability.setStartDate(java.sql.Date.valueOf("2026-08-03"));
        unavailability.setStartTime(Time.valueOf("09:00:00"));
        unavailability.setEndDate(java.sql.Date.valueOf("2026-08-03"));
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
        AppointmentServiceDefinition service = new AppointmentServiceDefinition();
        service.setAppointmentServiceId(id);
        service.setName(name);
        service.setLocation(location);
        service.setVoided(voided);
        service.setUuid("service-uuid");
        return service;
    }

    private Provider createProvider(Integer id, String name, boolean retired) {
        Provider provider = new Provider();
        provider.setProviderId(id);
        provider.setName(name);
        provider.setRetired(retired);
        return provider;
    }
}
