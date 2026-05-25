package org.openmrs.module.appointments.web.mapper;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.openmrs.Location;
import org.openmrs.Provider;
import org.openmrs.User;
import org.openmrs.api.APIException;
import org.openmrs.api.LocationService;
import org.openmrs.api.ProviderService;
import org.openmrs.module.appointments.model.AppointmentServiceDefinition;
import org.openmrs.module.appointments.model.AppointmentUnavailability;
import org.openmrs.module.appointments.service.AppointmentServiceDefinitionService;
import org.openmrs.module.appointments.web.contract.AppointmentUnavailabilityRequest;
import org.openmrs.module.appointments.web.contract.AppointmentUnavailabilityResponse;

import java.sql.Date;
import java.sql.Time;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;

public class AppointmentUnavailabilityMapperTest {

    @Mock
    private LocationService locationService;

    @Mock
    private ProviderService providerService;

    @Mock
    private AppointmentServiceDefinitionService appointmentServiceDefinitionService;

    @InjectMocks
    private AppointmentUnavailabilityMapper mapper;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    private Location testLocation;
    private Provider testProvider;
    private AppointmentServiceDefinition testService;

    @Before
    public void setUp() {
        initMocks(this);

        testLocation = new Location();
        testLocation.setUuid("location-uuid");
        testLocation.setName("Test Location");

        testProvider = mock(Provider.class);
        when(testProvider.getUuid()).thenReturn("provider-uuid");
        when(testProvider.getName()).thenReturn("Test Provider");

        testService = new AppointmentServiceDefinition();
        testService.setUuid("service-uuid");
        testService.setName("Test Service");
    }


    @Test
    public void shouldConvertRequestToUnavailabilityWithAllFields() {
        // Setup
        AppointmentUnavailabilityRequest request = createValidRequest();
        request.setAppointmentServiceUuid("service-uuid");
        request.setProviderUuid("provider-uuid");

        when(locationService.getLocationByUuid("location-uuid")).thenReturn(testLocation);
        when(appointmentServiceDefinitionService.getAppointmentServiceByUuid("service-uuid")).thenReturn(testService);
        when(providerService.getProviderByUuid("provider-uuid")).thenReturn(testProvider);

        // Execute
        AppointmentUnavailability result = mapper.fromRequest(request);

        // Verify
        assertNotNull(result);
        assertEquals(testLocation, result.getLocation());
        assertEquals(testService, result.getService());
        assertEquals(testProvider, result.getProvider());
        assertNotNull(result.getStartDate());
        assertNotNull(result.getStartTime());
        assertNotNull(result.getEndDate());
        assertNotNull(result.getEndTime());
        verify(locationService, times(1)).getLocationByUuid("location-uuid");
        verify(appointmentServiceDefinitionService, times(1)).getAppointmentServiceByUuid("service-uuid");
        verify(providerService, times(1)).getProviderByUuid("provider-uuid");
    }

    @Test
    public void shouldConvertRequestWithOnlyRequiredFields() {
        // Setup
        AppointmentUnavailabilityRequest request = createValidRequest();
        // No service or provider

        when(locationService.getLocationByUuid("location-uuid")).thenReturn(testLocation);

        // Execute
        AppointmentUnavailability result = mapper.fromRequest(request);

        // Verify
        assertNotNull(result);
        assertEquals(testLocation, result.getLocation());
        assertNull(result.getService());
        assertNull(result.getProvider());
        verify(appointmentServiceDefinitionService, never()).getAppointmentServiceByUuid(anyString());
        verify(providerService, never()).getProviderByUuid(anyString());
    }

    @Test
    public void shouldThrowExceptionWhenLocationNotFound() {
        // Setup
        AppointmentUnavailabilityRequest request = createValidRequest();
        when(locationService.getLocationByUuid("location-uuid")).thenReturn(null);

        // Execute & Verify
        expectedException.expect(APIException.class);
        expectedException.expectMessage("Invalid location or location not found");
        mapper.fromRequest(request);
    }

    @Test
    public void shouldThrowExceptionWhenServiceNotFound() {
        // Setup
        AppointmentUnavailabilityRequest request = createValidRequest();
        request.setAppointmentServiceUuid("invalid-service-uuid");

        when(locationService.getLocationByUuid("location-uuid")).thenReturn(testLocation);
        when(appointmentServiceDefinitionService.getAppointmentServiceByUuid("invalid-service-uuid")).thenReturn(null);

        // Execute & Verify
        expectedException.expect(APIException.class);
        expectedException.expectMessage("Invalid service or service not found");
        mapper.fromRequest(request);
    }

    @Test
    public void shouldHandleEmptyServiceUuid() {
        // Setup
        AppointmentUnavailabilityRequest request = createValidRequest();
        request.setAppointmentServiceUuid("");

        when(locationService.getLocationByUuid("location-uuid")).thenReturn(testLocation);

        // Execute
        AppointmentUnavailability result = mapper.fromRequest(request);

        // Verify
        assertNotNull(result);
        assertNull(result.getService());
        verify(appointmentServiceDefinitionService, never()).getAppointmentServiceByUuid(anyString());
    }

    @Test
    public void shouldHandleEmptyProviderUuid() {
        // Setup
        AppointmentUnavailabilityRequest request = createValidRequest();
        request.setProviderUuid("");

        when(locationService.getLocationByUuid("location-uuid")).thenReturn(testLocation);

        // Execute
        AppointmentUnavailability result = mapper.fromRequest(request);

        // Verify
        assertNotNull(result);
        assertNull(result.getProvider());
        verify(providerService, never()).getProviderByUuid(anyString());
    }

    @Test
    public void shouldHandleNullProviderFromService() {
        AppointmentUnavailabilityRequest request = createValidRequest();
        request.setProviderUuid("non-existent-provider");

        when(locationService.getLocationByUuid("location-uuid")).thenReturn(testLocation);
        when(providerService.getProviderByUuid("non-existent-provider")).thenReturn(null);

        // Execute
        AppointmentUnavailability result = mapper.fromRequest(request);

        // Verify
        assertNotNull(result);
        assertNull(result.getProvider());
    }

    @Test
    public void shouldThrowExceptionForInvalidDateFormat() {
        // Setup
        AppointmentUnavailabilityRequest request = createValidRequest();
        request.setStartDate("invalid-date");

        when(locationService.getLocationByUuid("location-uuid")).thenReturn(testLocation);

        // Execute & Verify
        expectedException.expect(APIException.class);
        expectedException.expectMessage("Invalid date or time format");
        mapper.fromRequest(request);
    }

    @Test
    public void shouldThrowExceptionForInvalidTimeFormat() {
        // Setup
        AppointmentUnavailabilityRequest request = createValidRequest();
        request.setStartTime("invalid-time");

        when(locationService.getLocationByUuid("location-uuid")).thenReturn(testLocation);

        // Execute & Verify
        expectedException.expect(APIException.class);
        expectedException.expectMessage("Invalid date or time format");
        mapper.fromRequest(request);
    }


    @Test
    public void shouldConvertListOfRequests() {
        // Setup
        AppointmentUnavailabilityRequest request1 = createValidRequest();
        AppointmentUnavailabilityRequest request2 = createValidRequest();
        request2.setLocationUuid("location-uuid-2");

        Location location2 = new Location();
        location2.setUuid("location-uuid-2");
        location2.setName("Test Location 2");

        when(locationService.getLocationByUuid("location-uuid")).thenReturn(testLocation);
        when(locationService.getLocationByUuid("location-uuid-2")).thenReturn(location2);

        List<AppointmentUnavailabilityRequest> requests = Arrays.asList(request1, request2);

        // Execute
        List<AppointmentUnavailability> results = mapper.fromRequest(requests);

        // Verify
        assertNotNull(results);
        assertEquals(2, results.size());
        assertEquals(testLocation, results.get(0).getLocation());
        assertEquals(location2, results.get(1).getLocation());
    }

    @Test
    public void shouldReturnEmptyListForNullRequests() {
        // Execute
        List<AppointmentUnavailability> results = mapper.fromRequest((List<AppointmentUnavailabilityRequest>) null);

        // Verify
        assertNotNull(results);
        assertTrue(results.isEmpty());
    }

    @Test
    public void shouldReturnEmptyListForEmptyRequests() {
        // Execute
        List<AppointmentUnavailability> results = mapper.fromRequest(new ArrayList<>());

        // Verify
        assertNotNull(results);
        assertTrue(results.isEmpty());
    }


    @Test
    public void shouldConstructResponseWithAllFields() {
        // Setup
        AppointmentUnavailability unavailability = createUnavailability();
        unavailability.setLocation(testLocation);
        unavailability.setService(testService);
        unavailability.setProvider(testProvider);
        
        User creator = new User();
        creator.setUsername("testuser");
        unavailability.setCreator(creator);
        unavailability.setDateCreated(new java.util.Date());

        AppointmentUnavailabilityResponse response = mapper.constructResponse(unavailability);

        assertNotNull(response);
        assertEquals("test-uuid", response.getUuid());
        assertNotNull(response.getLocation());
        assertEquals("location-uuid", response.getLocation().getUuid());
        assertEquals("Test Location", response.getLocation().getName());
        assertNotNull(response.getService());
        assertEquals("service-uuid", response.getService().getUuid());
        assertEquals("Test Service", response.getService().getName());
        assertNotNull(response.getProvider());
        assertEquals("provider-uuid", response.getProvider().getUuid());
        assertEquals("Test Provider", response.getProvider().getName());
        assertEquals("2026-08-10", response.getStartDate());
        assertEquals("09:00", response.getStartTime());
        assertEquals("2026-08-10", response.getEndDate());
        assertEquals("17:00", response.getEndTime());
        assertFalse(response.getVoided());
        assertNotNull(response.getDateCreated());
        assertEquals("testuser", response.getCreatorName());
    }

    @Test
    public void shouldConstructResponseWithNullLocation() {
        AppointmentUnavailability unavailability = createUnavailability();
        unavailability.setLocation(null);

        AppointmentUnavailabilityResponse response = mapper.constructResponse(unavailability);

        assertNotNull(response);
        assertNull(response.getLocation());
    }

    @Test
    public void shouldConstructResponseWithNullService() {
        AppointmentUnavailability unavailability = createUnavailability();
        unavailability.setLocation(testLocation);
        unavailability.setService(null);

        AppointmentUnavailabilityResponse response = mapper.constructResponse(unavailability);

        assertNotNull(response);
        assertNull(response.getService());
    }

    @Test
    public void shouldConstructResponseWithNullProvider() {
        AppointmentUnavailability unavailability = createUnavailability();
        unavailability.setLocation(testLocation);
        unavailability.setProvider(null);

        AppointmentUnavailabilityResponse response = mapper.constructResponse(unavailability);

        assertNotNull(response);
        assertNull(response.getProvider());
    }

    @Test
    public void shouldConstructResponseWithNullDates() {
        AppointmentUnavailability unavailability = new AppointmentUnavailability();
        unavailability.setUuid("test-uuid");
        unavailability.setLocation(testLocation);
        unavailability.setStartDate(null);
        unavailability.setStartTime(null);
        unavailability.setEndDate(null);
        unavailability.setEndTime(null);
        unavailability.setVoided(false);

        AppointmentUnavailabilityResponse response = mapper.constructResponse(unavailability);

        assertNotNull(response);
        assertNull(response.getStartDate());
        assertNull(response.getStartTime());
        assertNull(response.getEndDate());
        assertNull(response.getEndTime());
    }

    @Test
    public void shouldConstructResponseWithNullDateCreated() {
        // Setup
        AppointmentUnavailability unavailability = createUnavailability();
        unavailability.setLocation(testLocation);
        unavailability.setDateCreated(null);

        // Execute
        AppointmentUnavailabilityResponse response = mapper.constructResponse(unavailability);

        // Verify
        assertNotNull(response);
        assertNull(response.getDateCreated());
    }

    @Test
    public void shouldConstructResponseWithNullCreator() {
        // Setup
        AppointmentUnavailability unavailability = createUnavailability();
        unavailability.setLocation(testLocation);
        unavailability.setCreator(null);

        // Execute
        AppointmentUnavailabilityResponse response = mapper.constructResponse(unavailability);

        // Verify
        assertNotNull(response);
        assertNull(response.getCreatorName());
    }

    @Test
    public void shouldConstructResponseWithVoidedTrue() {
        // Setup
        AppointmentUnavailability unavailability = createUnavailability();
        unavailability.setLocation(testLocation);
        unavailability.setVoided(true);

        // Execute
        AppointmentUnavailabilityResponse response = mapper.constructResponse(unavailability);

        // Verify
        assertNotNull(response);
        assertTrue(response.getVoided());
    }


    @Test
    public void shouldConstructResponseListFromUnavailabilities() {
        // Setup
        AppointmentUnavailability unavailability1 = createUnavailability();
        unavailability1.setLocation(testLocation);
        
        AppointmentUnavailability unavailability2 = createUnavailability();
        unavailability2.setUuid("test-uuid-2");
        unavailability2.setLocation(testLocation);

        List<AppointmentUnavailability> unavailabilities = Arrays.asList(unavailability1, unavailability2);

        // Execute
        List<AppointmentUnavailabilityResponse> responses = mapper.constructResponse(unavailabilities);

        // Verify
        assertNotNull(responses);
        assertEquals(2, responses.size());
        assertEquals("test-uuid", responses.get(0).getUuid());
        assertEquals("test-uuid-2", responses.get(1).getUuid());
    }

    @Test
    public void shouldReturnEmptyListForNullUnavailabilities() {
        // Execute
        List<AppointmentUnavailabilityResponse> responses = mapper.constructResponse((List<AppointmentUnavailability>) null);

        // Verify
        assertNotNull(responses);
        assertTrue(responses.isEmpty());
    }

    @Test
    public void shouldReturnEmptyListForEmptyUnavailabilities() {
        // Execute
        List<AppointmentUnavailabilityResponse> responses = mapper.constructResponse(new ArrayList<>());

        // Verify
        assertNotNull(responses);
        assertTrue(responses.isEmpty());
    }


    private AppointmentUnavailabilityRequest createValidRequest() {
        AppointmentUnavailabilityRequest request = new AppointmentUnavailabilityRequest();
        request.setLocationUuid("location-uuid");
        request.setStartDate("2026-08-10");
        request.setStartTime("09:00");
        request.setEndDate("2026-08-10");
        request.setEndTime("17:00");
        return request;
    }

    private AppointmentUnavailability createUnavailability() {
        AppointmentUnavailability unavailability = new AppointmentUnavailability();
        unavailability.setUuid("test-uuid");
        unavailability.setStartDate(Date.valueOf("2026-08-10"));
        unavailability.setStartTime(Time.valueOf("09:00:00"));
        unavailability.setEndDate(Date.valueOf("2026-08-10"));
        unavailability.setEndTime(Time.valueOf("17:00:00"));
        unavailability.setVoided(false);
        return unavailability;
    }
}
