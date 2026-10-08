package org.openmrs.module.appointments.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.mockStatic;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;

public class AppointmentUnavailabilityServiceImplTest {

    private MockedStatic<Context> contextMockedStatic;

    @AfterEach
    public void closeStaticMocks() {
        if (contextMockedStatic != null) {
            contextMockedStatic.close();
        }
    }
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

    @BeforeEach
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        service = new AppointmentUnavailabilityServiceImpl(appointmentUnavailabilityDao, appointmentServiceDefinitionService);
        contextMockedStatic = Mockito.mockStatic(Context.class);
        authenticatedUser = new User(1);
        Mockito.when(Context.getAuthenticatedUser()).thenReturn(authenticatedUser);
        Mockito.when(Context.getLocationService()).thenReturn(locationService);
        Mockito.when(Context.getProviderService()).thenReturn(providerService);
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
        APIException exception = assertThrows(APIException.class, () -> {
            service.save(new ArrayList<>());
        });
        assertThat(exception.getMessage(), containsString("at least one unavailability block"));
    }

    @Test
    public void shouldRejectNullList() {
        APIException exception = assertThrows(APIException.class, () -> {
            service.save(null);
        });
        assertThat(exception.getMessage(), containsString("at least one unavailability block"));
    }

    @Test
    public void shouldRejectWhenLocationIsRetired() {
        List<AppointmentUnavailability> unavailabilities = createValidUnavailabilityList();
        Location location = createLocation(1, "Location 1", true); // retired

        when(locationService.getLocation(1)).thenReturn(location);

        APIException exception = assertThrows(APIException.class, () -> {
            service.save(unavailabilities);
        });
        assertThat(exception.getMessage(), containsString("[0] location is invalid or retired"));
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

        APIException exception = assertThrows(APIException.class, () -> {
            this.service.save(unavailabilities);
        });
        assertThat(exception.getMessage(), containsString("[0] service is invalid or voided"));
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

        APIException exception = assertThrows(APIException.class, () -> {
            this.service.save(unavailabilities);
        });
        assertThat(exception.getMessage(), containsString("[0] Service does not belong to the specified location"));
    }

    @Test
    public void shouldRejectWhenProviderIsRetired() {
        List<AppointmentUnavailability> unavailabilities = createValidUnavailabilityList();
        Location location = createLocation(1, "Location 1", false);
        Provider provider = createProvider(1, "Provider 1", true); // retired
        unavailabilities.get(0).setProvider(provider);

        when(locationService.getLocation(1)).thenReturn(location);
        when(providerService.getProvider(1)).thenReturn(provider);

        APIException exception = assertThrows(APIException.class, () -> {
            service.save(unavailabilities);
        });
        assertThat(exception.getMessage(), containsString("[0] provider is invalid or retired"));
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

        APIException exception = assertThrows(APIException.class, () -> {
            service.save(unavailabilities);
        });
        assertThat(exception.getMessage(), containsString("[0] End date/time must be after start date/time"));
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

        APIException exception = assertThrows(APIException.class, () -> {
            service.save(unavailabilities);
        });
        assertThat(exception.getMessage(), containsString("[0] End date/time must be after start date/time"));
    }

    @Test
    public void shouldRejectWhenEndTimeIsInPast() {
        List<AppointmentUnavailability> unavailabilities = createValidUnavailabilityList();
        Location location = createLocation(1, "Location 1", false);
        unavailabilities.get(0).setStartDate(java.sql.Date.valueOf("2020-01-01"));
        unavailabilities.get(0).setEndDate(java.sql.Date.valueOf("2020-01-02"));

        when(locationService.getLocation(1)).thenReturn(location);

        APIException exception = assertThrows(APIException.class, () -> {
            service.save(unavailabilities);
        });
        assertThat(exception.getMessage(), containsString("[0] Cannot create unavailability block that has already ended"));
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
    public void shouldReturnEmptyListWhenDaoReturnsNullForGetAll() {
        AppointmentUnavailabilitySearchParams searchParams = new AppointmentUnavailabilitySearchParams();
        searchParams.setLocationUuid("location-uuid-1");

        when(appointmentUnavailabilityDao.getAll(searchParams)).thenReturn(null);

        List<AppointmentUnavailability> result = service.getAll(searchParams);

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(appointmentUnavailabilityDao, times(1)).getAll(searchParams);
    }

    @Test
    public void shouldReturnUnavailabilitiesWhenDaoReturnsNonEmptyListForGetAll() {
        AppointmentUnavailabilitySearchParams searchParams = new AppointmentUnavailabilitySearchParams();
        searchParams.setLocationUuid("location-uuid-1");
        List<AppointmentUnavailability> daoResult = createValidUnavailabilityList();

        when(appointmentUnavailabilityDao.getAll(searchParams)).thenReturn(daoResult);

        List<AppointmentUnavailability> result = service.getAll(searchParams);

        assertNotNull(result);
        assertEquals(1, result.size());
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
