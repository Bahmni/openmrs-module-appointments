package org.openmrs.module.appointments.web.controller;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.openmrs.module.appointments.model.AppointmentUnavailability;
import org.openmrs.module.appointments.search.param.AppointmentUnavailabilitySearchParams;
import org.openmrs.module.appointments.service.AppointmentUnavailabilityService;
import org.openmrs.module.appointments.web.contract.AppointmentUnavailabilityLocationResponse;
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyList;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
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

    @Before
    public void setUp() {
        initMocks(this);
    }


    @Test
    public void shouldCreateAppointmentUnavailabilitySuccessfully() {
        List<AppointmentUnavailabilityRequest> requests = createValidRequests();
        List<AppointmentUnavailability> unavailabilities = createUnavailabilityList();
        List<AppointmentUnavailabilityResponse> responses = createResponseList();

        doNothing().when(unavailabilityRequestValidator).validate(anyList(), any(Errors.class));
        when(appointmentUnavailabilityMapper.fromRequest(requests)).thenReturn(unavailabilities);
        when(appointmentUnavailabilityService.save(unavailabilities)).thenReturn(unavailabilities);
        when(appointmentUnavailabilityMapper.constructResponse(unavailabilities)).thenReturn(responses);

        ResponseEntity<Object> response = controller.createAppointmentUnavailability(requests);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(responses, response.getBody());
        verify(unavailabilityRequestValidator, times(1)).validate(eq(requests), any(Errors.class));
        verify(appointmentUnavailabilityMapper, times(1)).fromRequest(requests);
        verify(appointmentUnavailabilityService, times(1)).save(unavailabilities);
        verify(appointmentUnavailabilityMapper, times(1)).constructResponse(unavailabilities);
    }

    @Test
    public void shouldThrowExceptionWhenValidationFails() {
        List<AppointmentUnavailabilityRequest> requests = createValidRequests();

        doAnswer(invocation -> {
            Errors errors = (Errors) invocation.getArguments()[1];
            errors.reject("invalid", "Validation failed: locationUuid is required");
            return null;
        }).when(unavailabilityRequestValidator).validate(anyList(), any(Errors.class));

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
    public void shouldGetAllAppointmentUnavailabilitiesWithNoFilters() {
        List<AppointmentUnavailability> unavailabilities = createUnavailabilityList();
        List<AppointmentUnavailabilityResponse> responses = createResponseList();

        when(appointmentUnavailabilityService.getAll(any(AppointmentUnavailabilitySearchParams.class)))
                .thenReturn(unavailabilities);
        when(appointmentUnavailabilityMapper.constructResponse(unavailabilities)).thenReturn(responses);

        AppointmentUnavailabilitySearchParams searchParams = new AppointmentUnavailabilitySearchParams();
        ResponseEntity<Object> response = controller.getAllAppointmentUnavailabilities(searchParams);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(responses, response.getBody());
        verify(appointmentUnavailabilityService, times(1)).getAll(searchParams);
    }

    @Test
    public void shouldGetAllAppointmentUnavailabilitiesWithLocationFilter() {
        String locationUuid = "location-uuid";
        List<AppointmentUnavailability> unavailabilities = createUnavailabilityList();
        List<AppointmentUnavailabilityResponse> responses = createResponseList();

        when(appointmentUnavailabilityService.getAll(any(AppointmentUnavailabilitySearchParams.class)))
                .thenReturn(unavailabilities);
        when(appointmentUnavailabilityMapper.constructResponse(unavailabilities)).thenReturn(responses);

        AppointmentUnavailabilitySearchParams searchParams = new AppointmentUnavailabilitySearchParams();
        searchParams.setLocationUuid(locationUuid);
        ResponseEntity<Object> response = controller.getAllAppointmentUnavailabilities(searchParams);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(appointmentUnavailabilityService, times(1)).getAll(searchParams);
    }

    @Test
    public void shouldGetAllAppointmentUnavailabilitiesWithServiceFilter() {
        String serviceUuid = "service-uuid";
        List<AppointmentUnavailability> unavailabilities = createUnavailabilityList();
        List<AppointmentUnavailabilityResponse> responses = createResponseList();

        when(appointmentUnavailabilityService.getAll(any(AppointmentUnavailabilitySearchParams.class)))
                .thenReturn(unavailabilities);
        when(appointmentUnavailabilityMapper.constructResponse(unavailabilities)).thenReturn(responses);

        AppointmentUnavailabilitySearchParams searchParams = new AppointmentUnavailabilitySearchParams();
        searchParams.setServiceUuid(serviceUuid);
        ResponseEntity<Object> response = controller.getAllAppointmentUnavailabilities(searchParams);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(appointmentUnavailabilityService, times(1)).getAll(searchParams);
    }

    @Test
    public void shouldGetAllAppointmentUnavailabilitiesWithProviderFilter() {
        String providerUuid = "provider-uuid";
        List<AppointmentUnavailability> unavailabilities = createUnavailabilityList();
        List<AppointmentUnavailabilityResponse> responses = createResponseList();

        when(appointmentUnavailabilityService.getAll(any(AppointmentUnavailabilitySearchParams.class)))
                .thenReturn(unavailabilities);
        when(appointmentUnavailabilityMapper.constructResponse(unavailabilities)).thenReturn(responses);

        AppointmentUnavailabilitySearchParams searchParams = new AppointmentUnavailabilitySearchParams();
        searchParams.setProviderUuid(providerUuid);
        ResponseEntity<Object> response = controller.getAllAppointmentUnavailabilities(searchParams);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(appointmentUnavailabilityService, times(1)).getAll(searchParams);
    }

    @Test
    public void shouldGetAllAppointmentUnavailabilitiesWithDateFilters() {
        String startDate = "2026-08-01";
        String endDate = "2026-08-31";
        List<AppointmentUnavailability> unavailabilities = createUnavailabilityList();
        List<AppointmentUnavailabilityResponse> responses = createResponseList();

        when(appointmentUnavailabilityService.getAll(any(AppointmentUnavailabilitySearchParams.class)))
                .thenReturn(unavailabilities);
        when(appointmentUnavailabilityMapper.constructResponse(unavailabilities)).thenReturn(responses);

        AppointmentUnavailabilitySearchParams searchParams = new AppointmentUnavailabilitySearchParams();
        searchParams.setStartDate(startDate);
        searchParams.setEndDate(endDate);
        ResponseEntity<Object> response = controller.getAllAppointmentUnavailabilities(searchParams);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(appointmentUnavailabilityService, times(1)).getAll(searchParams);
    }

    @Test
    public void shouldGetAllAppointmentUnavailabilitiesWithIncludeVoided() {
        List<AppointmentUnavailability> unavailabilities = createUnavailabilityList();
        List<AppointmentUnavailabilityResponse> responses = createResponseList();

        when(appointmentUnavailabilityService.getAll(any(AppointmentUnavailabilitySearchParams.class)))
                .thenReturn(unavailabilities);
        when(appointmentUnavailabilityMapper.constructResponse(unavailabilities)).thenReturn(responses);

        AppointmentUnavailabilitySearchParams searchParams = new AppointmentUnavailabilitySearchParams();
        searchParams.setIncludeVoided(true);
        ResponseEntity<Object> response = controller.getAllAppointmentUnavailabilities(searchParams);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(appointmentUnavailabilityService, times(1)).getAll(searchParams);
    }

    @Test
    public void shouldGetAllAppointmentUnavailabilitiesWithLimit() {
        Integer limit = 10;
        List<AppointmentUnavailability> unavailabilities = createUnavailabilityList();
        List<AppointmentUnavailabilityResponse> responses = createResponseList();

        when(appointmentUnavailabilityService.getAll(any(AppointmentUnavailabilitySearchParams.class)))
                .thenReturn(unavailabilities);
        when(appointmentUnavailabilityMapper.constructResponse(unavailabilities)).thenReturn(responses);

        AppointmentUnavailabilitySearchParams searchParams = new AppointmentUnavailabilitySearchParams();
        searchParams.setLimit(limit);
        ResponseEntity<Object> response = controller.getAllAppointmentUnavailabilities(searchParams);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(appointmentUnavailabilityService, times(1)).getAll(searchParams);
    }

    @Test
    public void shouldGetAllAppointmentUnavailabilitiesWithAllFilters() {
        String locationUuid = "location-uuid";
        String serviceUuid = "service-uuid";
        String providerUuid = "provider-uuid";
        String startDate = "2026-08-01";
        String endDate = "2026-08-31";
        boolean includeVoided = true;
        Integer limit = 5;

        List<AppointmentUnavailability> unavailabilities = createUnavailabilityList();
        List<AppointmentUnavailabilityResponse> responses = createResponseList();

        when(appointmentUnavailabilityService.getAll(any(AppointmentUnavailabilitySearchParams.class)))
                .thenReturn(unavailabilities);
        when(appointmentUnavailabilityMapper.constructResponse(unavailabilities)).thenReturn(responses);

        AppointmentUnavailabilitySearchParams searchParams = new AppointmentUnavailabilitySearchParams();
        searchParams.setLocationUuid(locationUuid);
        searchParams.setServiceUuid(serviceUuid);
        searchParams.setProviderUuid(providerUuid);
        searchParams.setStartDate(startDate);
        searchParams.setEndDate(endDate);
        searchParams.setIncludeVoided(includeVoided);
        searchParams.setLimit(limit);
        ResponseEntity<Object> response = controller.getAllAppointmentUnavailabilities(searchParams);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(appointmentUnavailabilityService, times(1)).getAll(searchParams);
    }

    @Test
    public void shouldGetAllAppointmentUnavailabilitiesWithEmptyStringFilters() {
        List<AppointmentUnavailability> unavailabilities = createUnavailabilityList();
        List<AppointmentUnavailabilityResponse> responses = createResponseList();

        when(appointmentUnavailabilityService.getAll(any(AppointmentUnavailabilitySearchParams.class)))
                .thenReturn(unavailabilities);
        when(appointmentUnavailabilityMapper.constructResponse(unavailabilities)).thenReturn(responses);

        AppointmentUnavailabilitySearchParams searchParams = new AppointmentUnavailabilitySearchParams();
        searchParams.setLocationUuid("");
        searchParams.setServiceUuid("");
        searchParams.setProviderUuid("");
        ResponseEntity<Object> response = controller.getAllAppointmentUnavailabilities(searchParams);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(appointmentUnavailabilityService, times(1)).getAll(searchParams);
    }

    @Test
    public void shouldReturnEmptyListWhenNoUnavailabilitiesFound() {
        List<AppointmentUnavailability> emptyList = new ArrayList<>();
        List<AppointmentUnavailabilityResponse> emptyResponses = new ArrayList<>();

        when(appointmentUnavailabilityService.getAll(any(AppointmentUnavailabilitySearchParams.class)))
                .thenReturn(emptyList);
        when(appointmentUnavailabilityMapper.constructResponse(emptyList)).thenReturn(emptyResponses);

        AppointmentUnavailabilitySearchParams searchParams = new AppointmentUnavailabilitySearchParams();
        ResponseEntity<Object> response = controller.getAllAppointmentUnavailabilities(searchParams);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        List<AppointmentUnavailabilityResponse> result = (List<AppointmentUnavailabilityResponse>) response.getBody();
        assertTrue(result.isEmpty());
    }


    @Test
    public void shouldGetAppointmentUnavailabilityByUuid() {
        String uuid = "test-uuid";
        AppointmentUnavailability unavailability = createUnavailability(uuid);
        AppointmentUnavailabilityResponse expectedResponse = createResponse(uuid);

        when(appointmentUnavailabilityService.getByUuid(uuid)).thenReturn(unavailability);
        when(appointmentUnavailabilityMapper.constructResponse(unavailability)).thenReturn(expectedResponse);

        ResponseEntity<Object> response = controller.getAppointmentUnavailabilityByUuid(uuid);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectedResponse, response.getBody());
        verify(appointmentUnavailabilityService, times(1)).getByUuid(uuid);
        verify(appointmentUnavailabilityMapper, times(1)).constructResponse(unavailability);
    }

    @Test
    public void shouldReturnNotFoundWhenUnavailabilityNotFoundByUuid() {
        String uuid = "non-existent-uuid";
        when(appointmentUnavailabilityService.getByUuid(uuid)).thenReturn(null);

        ResponseEntity<Object> response = controller.getAppointmentUnavailabilityByUuid(uuid);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("Appointment Unavailability not found", response.getBody());
        verify(appointmentUnavailabilityService, times(1)).getByUuid(uuid);
        verify(appointmentUnavailabilityMapper, never()).constructResponse(any(AppointmentUnavailability.class));
    }

    @Test
    public void shouldVoidAppointmentUnavailabilitySuccessfully() {
        String uuid = "test-uuid";
        String voidReason = "Test void reason";
        AppointmentUnavailability unavailability = createUnavailability(uuid);
        unavailability.setVoided(false);

        when(appointmentUnavailabilityService.getByUuid(uuid)).thenReturn(unavailability);

        ResponseEntity<Object> response = controller.voidAppointmentUnavailability(uuid, voidReason);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(appointmentUnavailabilityService, times(1)).getByUuid(uuid);
        verify(appointmentUnavailabilityService, times(1)).voidAppointmentUnavailability(unavailability, voidReason);
    }

    @Test
    public void shouldVoidAppointmentUnavailabilityWithNullReason() {
        String uuid = "test-uuid";
        AppointmentUnavailability unavailability = createUnavailability(uuid);
        unavailability.setVoided(false);

        when(appointmentUnavailabilityService.getByUuid(uuid)).thenReturn(unavailability);

        ResponseEntity<Object> response = controller.voidAppointmentUnavailability(uuid, null);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(appointmentUnavailabilityService, times(1)).voidAppointmentUnavailability(unavailability, null);
    }

    @Test
    public void shouldReturnNotFoundWhenVoidingNonExistentUnavailability() {
        String uuid = "non-existent-uuid";
        when(appointmentUnavailabilityService.getByUuid(uuid)).thenReturn(null);

        ResponseEntity<Object> response = controller.voidAppointmentUnavailability(uuid, "reason");

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("Appointment Unavailability not found", response.getBody());
        verify(appointmentUnavailabilityService, times(1)).getByUuid(uuid);
        verify(appointmentUnavailabilityService, never()).voidAppointmentUnavailability(any(), anyString());
    }

    @Test
    public void shouldNotVoidAlreadyVoidedUnavailability() {
        String uuid = "test-uuid";
        String voidReason = "Another reason";
        AppointmentUnavailability unavailability = createUnavailability(uuid);
        unavailability.setVoided(true);

        when(appointmentUnavailabilityService.getByUuid(uuid)).thenReturn(unavailability);

        ResponseEntity<Object> response = controller.voidAppointmentUnavailability(uuid, voidReason);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(appointmentUnavailabilityService, times(1)).getByUuid(uuid);
        verify(appointmentUnavailabilityService, never()).voidAppointmentUnavailability(any(), anyString());
    }


    @Test
    public void shouldHandleRuntimeException() {
        RuntimeException exception = new RuntimeException("Test runtime exception");

        ResponseEntity<Object> response = controller.handleException(exception);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        Map<String, Object> errors = (Map<String, Object>) response.getBody();
        assertEquals("Test runtime exception", errors.get("message"));
    }

    @Test
    public void shouldHandleParseException() {
        ParseException exception = new ParseException("Invalid date format", 0);

        ResponseEntity<Object> response = controller.handleException(exception);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        Map<String, Object> errors = (Map<String, Object>) response.getBody();
        assertEquals("Invalid date format", errors.get("message"));
    }

    @Test
    public void shouldHandleExceptionWithNullMessage() {
        RuntimeException exception = new RuntimeException((String) null);

        ResponseEntity<Object> response = controller.handleException(exception);

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
        unavailability.setVoided(false);
        return unavailability;
    }

    private List<AppointmentUnavailabilityResponse> createResponseList() {
        return Collections.singletonList(createResponse("test-uuid"));
    }

    private AppointmentUnavailabilityResponse createResponse(String uuid) {
        AppointmentUnavailabilityResponse response = new AppointmentUnavailabilityResponse();
        response.setUuid(uuid);
        response.setLocation(new AppointmentUnavailabilityLocationResponse("location-uuid", "Test Location"));
        response.setStartDate("2026-08-10");
        response.setStartTime("09:00");
        response.setEndDate("2026-08-10");
        response.setEndTime("17:00");
        response.setVoided(false);
        return response;
    }
}
