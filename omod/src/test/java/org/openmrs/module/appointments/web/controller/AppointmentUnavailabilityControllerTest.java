package org.openmrs.module.appointments.web.controller;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.openmrs.Location;
import org.openmrs.Provider;
import org.openmrs.module.appointments.model.AppointmentServiceDefinition;
import org.openmrs.module.appointments.model.AppointmentUnavailability;
import org.openmrs.module.appointments.search.param.AppointmentUnavailabilitySearchParams;
import org.openmrs.module.appointments.service.AppointmentUnavailabilityService;
import org.openmrs.module.appointments.web.contract.AppointmentUnavailabilityRequest;
import org.openmrs.module.appointments.web.contract.AppointmentUnavailabilityResponse;
import org.openmrs.module.appointments.web.mapper.AppointmentUnavailabilityMapper;
import org.openmrs.module.appointments.web.validators.AppointmentUnavailabilityRequestValidator;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.Errors;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyList;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;

public class AppointmentUnavailabilityControllerTest {

    @Mock
    private AppointmentUnavailabilityService appointmentUnavailabilityService;

    @Mock
    private AppointmentUnavailabilityMapper appointmentUnavailabilityMapper;

    @Mock
    private AppointmentUnavailabilityRequestValidator unavailabilityRequestValidator;

    @InjectMocks
    private AppointmentUnavailabilityController controller;

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

        testProvider = new Provider();
        testProvider.setUuid("provider-uuid");
        testProvider.setName("Test Provider");

        testService = new AppointmentServiceDefinition();
        testService.setUuid("service-uuid");
        testService.setName("Test Service");
    }


    @Test
    public void shouldCreateAppointmentUnavailabilitySuccessfully() {
        // Setup
        List<AppointmentUnavailabilityRequest> requests = createValidRequests();
        List<AppointmentUnavailability> unavailabilities = createUnavailabilityList();
        List<AppointmentUnavailabilityResponse> responses = createResponseList();

        doNothing().when(unavailabilityRequestValidator).validate(anyList(), any(Errors.class));
        when(appointmentUnavailabilityMapper.fromRequest(requests)).thenReturn(unavailabilities);
        when(appointmentUnavailabilityService.save(unavailabilities)).thenReturn(unavailabilities);
        when(appointmentUnavailabilityMapper.constructResponse(unavailabilities)).thenReturn(responses);

        // Execute
        ResponseEntity<Object> response = controller.createAppointmentUnavailability(requests);

        // Verify
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(responses, response.getBody());
        verify(unavailabilityRequestValidator, times(1)).validate(eq(requests), any(Errors.class));
        verify(appointmentUnavailabilityMapper, times(1)).fromRequest(requests);
        verify(appointmentUnavailabilityService, times(1)).save(unavailabilities);
        verify(appointmentUnavailabilityMapper, times(1)).constructResponse(unavailabilities);
    }

    @Test
    public void shouldThrowExceptionWhenValidationFails() {
        // Setup
        List<AppointmentUnavailabilityRequest> requests = createValidRequests();

        doAnswer(invocation -> {
            Errors errors = (Errors) invocation.getArguments()[1];
            errors.reject("invalid", "Validation failed: locationUuid is required");
            return null;
        }).when(unavailabilityRequestValidator).validate(anyList(), any(Errors.class));

        // Execute & Verify
        expectedException.expect(RuntimeException.class);
        expectedException.expectMessage("Validation failed: locationUuid is required");
        controller.createAppointmentUnavailability(requests);
    }

    @Test
    public void shouldCreateMultipleUnavailabilityBlocks() {
        List<AppointmentUnavailabilityRequest> requests = Arrays.asList(
                createRequest("location-1", "service-1", "provider-1"),
                createRequest("location-2", "service-2", "provider-2")
        );
        List<AppointmentUnavailability> unavailabilities = Arrays.asList(
                createUnavailability("uuid-1"),
                createUnavailability("uuid-2")
        );
        List<AppointmentUnavailabilityResponse> responses = Arrays.asList(
                createResponse("uuid-1"),
                createResponse("uuid-2")
        );

        doNothing().when(unavailabilityRequestValidator).validate(anyList(), any(Errors.class));
        when(appointmentUnavailabilityMapper.fromRequest(requests)).thenReturn(unavailabilities);
        when(appointmentUnavailabilityService.save(unavailabilities)).thenReturn(unavailabilities);
        when(appointmentUnavailabilityMapper.constructResponse(unavailabilities)).thenReturn(responses);

        ResponseEntity<Object> response = controller.createAppointmentUnavailability(requests);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        List<AppointmentUnavailabilityResponse> result = (List<AppointmentUnavailabilityResponse>) response.getBody();
        assertEquals(2, result.size());
    }


    @Test
    public void shouldGetAllAppointmentUnavailabilitiesWithNoFilters() throws ParseException {
        // Setup
        List<AppointmentUnavailability> unavailabilities = createUnavailabilityList();
        List<AppointmentUnavailabilityResponse> responses = createResponseList();

        when(appointmentUnavailabilityService.getAll(any(AppointmentUnavailabilitySearchParams.class)))
                .thenReturn(unavailabilities);
        when(appointmentUnavailabilityMapper.constructResponse(unavailabilities)).thenReturn(responses);

        // Execute
        ResponseEntity<Object> response = controller.getAllAppointmentUnavailabilities(
                null, null, null, null, null, false, null);

        // Verify
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(responses, response.getBody());
        
        ArgumentCaptor<AppointmentUnavailabilitySearchParams> paramsCaptor = 
                ArgumentCaptor.forClass(AppointmentUnavailabilitySearchParams.class);
        verify(appointmentUnavailabilityService, times(1)).getAll(paramsCaptor.capture());
        
        AppointmentUnavailabilitySearchParams capturedParams = paramsCaptor.getValue();
        assertNull(capturedParams.getLocationUuid());
        assertNull(capturedParams.getServiceUuid());
        assertNull(capturedParams.getProviderUuid());
        assertFalse(capturedParams.isIncludeVoided());
    }

    @Test
    public void shouldGetAllAppointmentUnavailabilitiesWithLocationFilter() throws ParseException {
        // Setup
        String locationUuid = "location-uuid";
        List<AppointmentUnavailability> unavailabilities = createUnavailabilityList();
        List<AppointmentUnavailabilityResponse> responses = createResponseList();

        when(appointmentUnavailabilityService.getAll(any(AppointmentUnavailabilitySearchParams.class)))
                .thenReturn(unavailabilities);
        when(appointmentUnavailabilityMapper.constructResponse(unavailabilities)).thenReturn(responses);

        // Execute
        ResponseEntity<Object> response = controller.getAllAppointmentUnavailabilities(
                locationUuid, null, null, null, null, false, null);

        // Verify
        assertEquals(HttpStatus.OK, response.getStatusCode());
        
        ArgumentCaptor<AppointmentUnavailabilitySearchParams> paramsCaptor = 
                ArgumentCaptor.forClass(AppointmentUnavailabilitySearchParams.class);
        verify(appointmentUnavailabilityService, times(1)).getAll(paramsCaptor.capture());
        
        AppointmentUnavailabilitySearchParams capturedParams = paramsCaptor.getValue();
        assertEquals(locationUuid, capturedParams.getLocationUuid());
    }

    @Test
    public void shouldGetAllAppointmentUnavailabilitiesWithServiceFilter() throws ParseException {
        // Setup
        String serviceUuid = "service-uuid";
        List<AppointmentUnavailability> unavailabilities = createUnavailabilityList();
        List<AppointmentUnavailabilityResponse> responses = createResponseList();

        when(appointmentUnavailabilityService.getAll(any(AppointmentUnavailabilitySearchParams.class)))
                .thenReturn(unavailabilities);
        when(appointmentUnavailabilityMapper.constructResponse(unavailabilities)).thenReturn(responses);

        // Execute
        ResponseEntity<Object> response = controller.getAllAppointmentUnavailabilities(
                null, serviceUuid, null, null, null, false, null);

        // Verify
        assertEquals(HttpStatus.OK, response.getStatusCode());
        
        ArgumentCaptor<AppointmentUnavailabilitySearchParams> paramsCaptor = 
                ArgumentCaptor.forClass(AppointmentUnavailabilitySearchParams.class);
        verify(appointmentUnavailabilityService, times(1)).getAll(paramsCaptor.capture());
        
        AppointmentUnavailabilitySearchParams capturedParams = paramsCaptor.getValue();
        assertEquals(serviceUuid, capturedParams.getServiceUuid());
    }

    @Test
    public void shouldGetAllAppointmentUnavailabilitiesWithProviderFilter() throws ParseException {
        // Setup
        String providerUuid = "provider-uuid";
        List<AppointmentUnavailability> unavailabilities = createUnavailabilityList();
        List<AppointmentUnavailabilityResponse> responses = createResponseList();

        when(appointmentUnavailabilityService.getAll(any(AppointmentUnavailabilitySearchParams.class)))
                .thenReturn(unavailabilities);
        when(appointmentUnavailabilityMapper.constructResponse(unavailabilities)).thenReturn(responses);

        // Execute
        ResponseEntity<Object> response = controller.getAllAppointmentUnavailabilities(
                null, null, providerUuid, null, null, false, null);

        // Verify
        assertEquals(HttpStatus.OK, response.getStatusCode());
        
        ArgumentCaptor<AppointmentUnavailabilitySearchParams> paramsCaptor = 
                ArgumentCaptor.forClass(AppointmentUnavailabilitySearchParams.class);
        verify(appointmentUnavailabilityService, times(1)).getAll(paramsCaptor.capture());
        
        AppointmentUnavailabilitySearchParams capturedParams = paramsCaptor.getValue();
        assertEquals(providerUuid, capturedParams.getProviderUuid());
    }

    @Test
    public void shouldGetAllAppointmentUnavailabilitiesWithDateFilters() throws ParseException {
        // Setup
        String startDateStr = "2026-08-01";
        String endDateStr = "2026-08-31";
        List<AppointmentUnavailability> unavailabilities = createUnavailabilityList();
        List<AppointmentUnavailabilityResponse> responses = createResponseList();

        when(appointmentUnavailabilityService.getAll(any(AppointmentUnavailabilitySearchParams.class)))
                .thenReturn(unavailabilities);
        when(appointmentUnavailabilityMapper.constructResponse(unavailabilities)).thenReturn(responses);

        // Execute
        ResponseEntity<Object> response = controller.getAllAppointmentUnavailabilities(
                null, null, null, startDateStr, endDateStr, false, null);

        // Verify
        assertEquals(HttpStatus.OK, response.getStatusCode());
        
        ArgumentCaptor<AppointmentUnavailabilitySearchParams> paramsCaptor = 
                ArgumentCaptor.forClass(AppointmentUnavailabilitySearchParams.class);
        verify(appointmentUnavailabilityService, times(1)).getAll(paramsCaptor.capture());
        
        AppointmentUnavailabilitySearchParams capturedParams = paramsCaptor.getValue();
        assertNotNull(capturedParams.getStartDate());
        assertNotNull(capturedParams.getEndDate());
    }

    @Test
    public void shouldGetAllAppointmentUnavailabilitiesWithIncludeVoided() throws ParseException {
        // Setup
        List<AppointmentUnavailability> unavailabilities = createUnavailabilityList();
        List<AppointmentUnavailabilityResponse> responses = createResponseList();

        when(appointmentUnavailabilityService.getAll(any(AppointmentUnavailabilitySearchParams.class)))
                .thenReturn(unavailabilities);
        when(appointmentUnavailabilityMapper.constructResponse(unavailabilities)).thenReturn(responses);

        // Execute
        ResponseEntity<Object> response = controller.getAllAppointmentUnavailabilities(
                null, null, null, null, null, true, null);

        // Verify
        assertEquals(HttpStatus.OK, response.getStatusCode());
        
        ArgumentCaptor<AppointmentUnavailabilitySearchParams> paramsCaptor = 
                ArgumentCaptor.forClass(AppointmentUnavailabilitySearchParams.class);
        verify(appointmentUnavailabilityService, times(1)).getAll(paramsCaptor.capture());
        
        AppointmentUnavailabilitySearchParams capturedParams = paramsCaptor.getValue();
        assertTrue(capturedParams.isIncludeVoided());
    }

    @Test
    public void shouldGetAllAppointmentUnavailabilitiesWithLimit() throws ParseException {
        // Setup
        Integer limit = 10;
        List<AppointmentUnavailability> unavailabilities = createUnavailabilityList();
        List<AppointmentUnavailabilityResponse> responses = createResponseList();

        when(appointmentUnavailabilityService.getAll(any(AppointmentUnavailabilitySearchParams.class)))
                .thenReturn(unavailabilities);
        when(appointmentUnavailabilityMapper.constructResponse(unavailabilities)).thenReturn(responses);

        // Execute
        ResponseEntity<Object> response = controller.getAllAppointmentUnavailabilities(
                null, null, null, null, null, false, limit);

        // Verify
        assertEquals(HttpStatus.OK, response.getStatusCode());
        
        ArgumentCaptor<AppointmentUnavailabilitySearchParams> paramsCaptor = 
                ArgumentCaptor.forClass(AppointmentUnavailabilitySearchParams.class);
        verify(appointmentUnavailabilityService, times(1)).getAll(paramsCaptor.capture());
        
        AppointmentUnavailabilitySearchParams capturedParams = paramsCaptor.getValue();
        assertEquals(limit, capturedParams.getLimit());
    }

    @Test
    public void shouldGetAllAppointmentUnavailabilitiesWithAllFilters() throws ParseException {
        // Setup
        String locationUuid = "location-uuid";
        String serviceUuid = "service-uuid";
        String providerUuid = "provider-uuid";
        String startDateStr = "2026-08-01";
        String endDateStr = "2026-08-31";
        boolean includeVoided = true;
        Integer limit = 5;

        List<AppointmentUnavailability> unavailabilities = createUnavailabilityList();
        List<AppointmentUnavailabilityResponse> responses = createResponseList();

        when(appointmentUnavailabilityService.getAll(any(AppointmentUnavailabilitySearchParams.class)))
                .thenReturn(unavailabilities);
        when(appointmentUnavailabilityMapper.constructResponse(unavailabilities)).thenReturn(responses);

        // Execute
        ResponseEntity<Object> response = controller.getAllAppointmentUnavailabilities(
                locationUuid, serviceUuid, providerUuid, startDateStr, endDateStr, includeVoided, limit);

        // Verify
        assertEquals(HttpStatus.OK, response.getStatusCode());
        
        ArgumentCaptor<AppointmentUnavailabilitySearchParams> paramsCaptor = 
                ArgumentCaptor.forClass(AppointmentUnavailabilitySearchParams.class);
        verify(appointmentUnavailabilityService, times(1)).getAll(paramsCaptor.capture());
        
        AppointmentUnavailabilitySearchParams capturedParams = paramsCaptor.getValue();
        assertEquals(locationUuid, capturedParams.getLocationUuid());
        assertEquals(serviceUuid, capturedParams.getServiceUuid());
        assertEquals(providerUuid, capturedParams.getProviderUuid());
        assertNotNull(capturedParams.getStartDate());
        assertNotNull(capturedParams.getEndDate());
        assertTrue(capturedParams.isIncludeVoided());
        assertEquals(limit, capturedParams.getLimit());
    }

    @Test
    public void shouldGetAllAppointmentUnavailabilitiesWithEmptyStringFilters() throws ParseException {
        List<AppointmentUnavailability> unavailabilities = createUnavailabilityList();
        List<AppointmentUnavailabilityResponse> responses = createResponseList();

        when(appointmentUnavailabilityService.getAll(any(AppointmentUnavailabilitySearchParams.class)))
                .thenReturn(unavailabilities);
        when(appointmentUnavailabilityMapper.constructResponse(unavailabilities)).thenReturn(responses);

        // Execute
        ResponseEntity<Object> response = controller.getAllAppointmentUnavailabilities(
                "", "", "", "", "", false, null);

        // Verify
        assertEquals(HttpStatus.OK, response.getStatusCode());
        
        ArgumentCaptor<AppointmentUnavailabilitySearchParams> paramsCaptor = 
                ArgumentCaptor.forClass(AppointmentUnavailabilitySearchParams.class);
        verify(appointmentUnavailabilityService, times(1)).getAll(paramsCaptor.capture());
        
        AppointmentUnavailabilitySearchParams capturedParams = paramsCaptor.getValue();
        // Empty strings are passed as-is, DAO handles blank checks
        assertEquals("", capturedParams.getLocationUuid());
        assertEquals("", capturedParams.getServiceUuid());
        assertEquals("", capturedParams.getProviderUuid());
    }

    @Test
    public void shouldReturnEmptyListWhenNoUnavailabilitiesFound() throws ParseException {
        // Setup
        List<AppointmentUnavailability> emptyList = new ArrayList<>();
        List<AppointmentUnavailabilityResponse> emptyResponses = new ArrayList<>();

        when(appointmentUnavailabilityService.getAll(any(AppointmentUnavailabilitySearchParams.class)))
                .thenReturn(emptyList);
        when(appointmentUnavailabilityMapper.constructResponse(emptyList)).thenReturn(emptyResponses);

        // Execute
        ResponseEntity<Object> response = controller.getAllAppointmentUnavailabilities(
                null, null, null, null, null, false, null);

        // Verify
        assertEquals(HttpStatus.OK, response.getStatusCode());
        List<AppointmentUnavailabilityResponse> result = (List<AppointmentUnavailabilityResponse>) response.getBody();
        assertTrue(result.isEmpty());
    }


    @Test
    public void shouldGetAppointmentUnavailabilityByUuid() {
        // Setup
        String uuid = "test-uuid";
        AppointmentUnavailability unavailability = createUnavailability(uuid);
        AppointmentUnavailabilityResponse expectedResponse = createResponse(uuid);

        when(appointmentUnavailabilityService.getByUuid(uuid)).thenReturn(unavailability);
        when(appointmentUnavailabilityMapper.constructResponse(unavailability)).thenReturn(expectedResponse);

        // Execute
        ResponseEntity<Object> response = controller.getAppointmentUnavailabilityByUuid(uuid);

        // Verify
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectedResponse, response.getBody());
        verify(appointmentUnavailabilityService, times(1)).getByUuid(uuid);
        verify(appointmentUnavailabilityMapper, times(1)).constructResponse(unavailability);
    }

    @Test
    public void shouldReturnNotFoundWhenUnavailabilityNotFoundByUuid() {
        // Setup
        String uuid = "non-existent-uuid";
        when(appointmentUnavailabilityService.getByUuid(uuid)).thenReturn(null);

        // Execute
        ResponseEntity<Object> response = controller.getAppointmentUnavailabilityByUuid(uuid);

        // Verify
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("Appointment Unavailability not found", response.getBody());
        verify(appointmentUnavailabilityService, times(1)).getByUuid(uuid);
        verify(appointmentUnavailabilityMapper, never()).constructResponse(any(AppointmentUnavailability.class));
    }

    // ========================= VOID TESTS =========================

    @Test
    public void shouldVoidAppointmentUnavailabilitySuccessfully() {
        // Setup
        String uuid = "test-uuid";
        String voidReason = "Test void reason";
        AppointmentUnavailability unavailability = createUnavailability(uuid);
        unavailability.setVoided(false);

        when(appointmentUnavailabilityService.getByUuid(uuid)).thenReturn(unavailability);

        // Execute
        ResponseEntity<Object> response = controller.voidAppointmentUnavailability(uuid, voidReason);

        // Verify
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(appointmentUnavailabilityService, times(1)).getByUuid(uuid);
        verify(appointmentUnavailabilityService, times(1)).voidAppointmentUnavailability(unavailability, voidReason);
    }

    @Test
    public void shouldVoidAppointmentUnavailabilityWithNullReason() {
        // Setup
        String uuid = "test-uuid";
        AppointmentUnavailability unavailability = createUnavailability(uuid);
        unavailability.setVoided(false);

        when(appointmentUnavailabilityService.getByUuid(uuid)).thenReturn(unavailability);

        // Execute
        ResponseEntity<Object> response = controller.voidAppointmentUnavailability(uuid, null);

        // Verify
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(appointmentUnavailabilityService, times(1)).voidAppointmentUnavailability(unavailability, null);
    }

    @Test
    public void shouldReturnNotFoundWhenVoidingNonExistentUnavailability() {
        // Setup
        String uuid = "non-existent-uuid";
        when(appointmentUnavailabilityService.getByUuid(uuid)).thenReturn(null);

        // Execute
        ResponseEntity<Object> response = controller.voidAppointmentUnavailability(uuid, "reason");

        // Verify
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("Appointment Unavailability not found", response.getBody());
        verify(appointmentUnavailabilityService, times(1)).getByUuid(uuid);
        verify(appointmentUnavailabilityService, never()).voidAppointmentUnavailability(any(), anyString());
    }

    @Test
    public void shouldNotVoidAlreadyVoidedUnavailability() {
        // Setup
        String uuid = "test-uuid";
        String voidReason = "Another reason";
        AppointmentUnavailability unavailability = createUnavailability(uuid);
        unavailability.setVoided(true);

        when(appointmentUnavailabilityService.getByUuid(uuid)).thenReturn(unavailability);

        // Execute
        ResponseEntity<Object> response = controller.voidAppointmentUnavailability(uuid, voidReason);

        // Verify
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(appointmentUnavailabilityService, times(1)).getByUuid(uuid);
        verify(appointmentUnavailabilityService, never()).voidAppointmentUnavailability(any(), anyString());
    }


    @Test
    public void shouldHandleRuntimeException() {
        // Setup
        RuntimeException exception = new RuntimeException("Test runtime exception");

        // Execute
        ResponseEntity<Object> response = controller.handleException(exception);

        // Verify
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        Map<String, Object> errors = (Map<String, Object>) response.getBody();
        assertEquals("Test runtime exception", errors.get("message"));
    }

    @Test
    public void shouldHandleParseException() {
        // Setup
        ParseException exception = new ParseException("Invalid date format", 0);

        // Execute
        ResponseEntity<Object> response = controller.handleException(exception);

        // Verify
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        Map<String, Object> errors = (Map<String, Object>) response.getBody();
        assertEquals("Invalid date format", errors.get("message"));
    }

    @Test
    public void shouldHandleExceptionWithNullMessage() {
        // Setup
        RuntimeException exception = new RuntimeException((String) null);

        // Execute
        ResponseEntity<Object> response = controller.handleException(exception);

        // Verify
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        Map<String, Object> errors = (Map<String, Object>) response.getBody();
        assertNull(errors.get("message"));
    }



    private List<AppointmentUnavailabilityRequest> createValidRequests() {
        AppointmentUnavailabilityRequest request = new AppointmentUnavailabilityRequest();
        request.setLocationUuid("location-uuid");
        request.setStartDate("2026-08-10");
        request.setStartTime("09:00");
        request.setEndDate("2026-08-10");
        request.setEndTime("17:00");
        return Collections.singletonList(request);
    }

    private AppointmentUnavailabilityRequest createRequest(String locationUuid, String serviceUuid, String providerUuid) {
        AppointmentUnavailabilityRequest request = new AppointmentUnavailabilityRequest();
        request.setLocationUuid(locationUuid);
        request.setAppointmentServiceUuid(serviceUuid);
        request.setProviderUuid(providerUuid);
        request.setStartDate("2026-08-10");
        request.setStartTime("09:00");
        request.setEndDate("2026-08-10");
        request.setEndTime("17:00");
        return request;
    }

    private List<AppointmentUnavailability> createUnavailabilityList() {
        return Collections.singletonList(createUnavailability("test-uuid"));
    }

    private AppointmentUnavailability createUnavailability(String uuid) {
        AppointmentUnavailability unavailability = new AppointmentUnavailability();
        unavailability.setUuid(uuid);
        unavailability.setLocation(testLocation);
        unavailability.setVoided(false);
        return unavailability;
    }

    private List<AppointmentUnavailabilityResponse> createResponseList() {
        return Collections.singletonList(createResponse("test-uuid"));
    }

    private AppointmentUnavailabilityResponse createResponse(String uuid) {
        AppointmentUnavailabilityResponse response = new AppointmentUnavailabilityResponse();
        response.setUuid(uuid);
        response.setLocationUuid("location-uuid");
        response.setLocationName("Test Location");
        response.setStartDate("2026-08-10");
        response.setStartTime("09:00");
        response.setEndDate("2026-08-10");
        response.setEndTime("17:00");
        response.setVoided(false);
        return response;
    }
}
