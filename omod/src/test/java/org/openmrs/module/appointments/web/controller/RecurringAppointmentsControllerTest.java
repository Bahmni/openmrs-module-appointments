package org.openmrs.module.appointments.web.controller;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.openmrs.module.appointments.model.Appointment;
import org.openmrs.module.appointments.model.AppointmentRecurringPattern;
import org.openmrs.module.appointments.service.AppointmentRecurringPatternService;
import org.openmrs.module.appointments.service.AppointmentsService;
import org.openmrs.module.appointments.web.contract.AppointmentRequest;
import org.openmrs.module.appointments.web.contract.RecurringAppointmentDefaultResponse;
import org.openmrs.module.appointments.web.contract.RecurringAppointmentRequest;
import org.openmrs.module.appointments.web.contract.RecurringPattern;
import org.openmrs.module.appointments.web.mapper.AppointmentMapper;
import org.openmrs.module.appointments.web.mapper.RecurringAppointmentMapper;
import org.openmrs.module.appointments.web.mapper.RecurringPatternMapper;
import org.openmrs.module.appointments.web.service.impl.AllAppointmentRecurringPatternUpdateService;
import org.openmrs.module.appointments.web.service.impl.RecurringAppointmentsService;
import org.openmrs.module.appointments.web.service.impl.SingleAppointmentRecurringPatternUpdateService;
import org.openmrs.module.appointments.web.validators.RecurringPatternValidator;
import org.openmrs.module.appointments.web.validators.TimeZoneValidator;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.Errors;

import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.powermock.api.mockito.PowerMockito.when;

public class RecurringAppointmentsControllerTest {

    @InjectMocks
    private RecurringAppointmentsController recurringAppointmentsController;

    @Mock
    private AppointmentRecurringPatternService appointmentRecurringPatternService;

    @Mock
    private RecurringAppointmentsService recurringAppointmentsService;

    @Mock
    private RecurringPatternValidator recurringPatternValidator;

    @Mock
    private TimeZoneValidator timeZoneValidator;

    @Mock
    private RecurringPatternMapper recurringPatternMapper;

    @Mock
    private RecurringAppointmentMapper recurringAppointmentMapper;

    @Mock
    private AppointmentMapper appointmentMapper;

    @Mock
    private SingleAppointmentRecurringPatternUpdateService singleAppointmentRecurringPatternUpdateService;

    @Mock
    private AllAppointmentRecurringPatternUpdateService allAppointmentRecurringPatternUpdateService;

    @Mock
    private AppointmentsService appointmentsService;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Before
    public void setUp() throws Exception {
        initMocks(this);
    }

    @Test
    public void shouldSaveAnAppointmentWhenRecurringPatternAlsoPresentsInRequest() {
        RecurringAppointmentRequest recurringAppointmentRequest = new RecurringAppointmentRequest();
        recurringAppointmentRequest.setAppointmentRequest(new AppointmentRequest());
        RecurringPattern recurringPattern = new RecurringPattern();
        recurringAppointmentRequest.getAppointmentRequest().setStartDateTime(new Date());
        recurringAppointmentRequest.getAppointmentRequest().setUuid("someUuid");
        recurringAppointmentRequest.getAppointmentRequest().setServiceUuid("someServiceUuid");
        recurringAppointmentRequest.setRecurringPattern(recurringPattern);
        Appointment appointmentOne = new Appointment();
        appointmentOne.setUuid("appointmentUuid");
        Appointment appointmentTwo = new Appointment();
        appointmentTwo.setUuid("appointmentUuid");
        List<Appointment> appointments = Arrays.asList(appointmentOne, appointmentTwo);
        AppointmentRecurringPattern appointmentRecurringPattern = new AppointmentRecurringPattern();
        appointmentRecurringPattern.setAppointments(new HashSet<>(Arrays.asList(appointmentOne, appointmentTwo)));
        when(recurringPatternMapper.fromRequest(recurringPattern)).thenReturn(appointmentRecurringPattern);
        when(appointmentRecurringPatternService.validateAndSave(appointmentRecurringPattern)).thenReturn(appointmentRecurringPattern);
        when(recurringAppointmentsService.generateRecurringAppointments(recurringAppointmentRequest)).thenReturn(appointments);
        when(recurringAppointmentMapper.constructResponse((List<Appointment>) any())).thenReturn(Collections.emptyList());
        doNothing().when(recurringPatternValidator).validate(recurringPattern, mock(Errors.class));
        appointmentRecurringPattern.setAppointments(Collections.emptySet());

        ResponseEntity<Object> responseEntity = recurringAppointmentsController.save(recurringAppointmentRequest);

        verify(recurringPatternMapper).fromRequest(recurringPattern);
        verify(appointmentRecurringPatternService).validateAndSave(appointmentRecurringPattern);
        verify(recurringAppointmentsService).generateRecurringAppointments(recurringAppointmentRequest);
        verify(recurringAppointmentMapper).constructResponse((List<Appointment>) any());
        assertEquals(responseEntity.getStatusCode(), HttpStatus.OK);
    }

    @Test
    public void shouldThrowAPIExceptionWhenRequestIsInvalidForSave() {
        RecurringAppointmentRequest recurringAppointmentRequest = new RecurringAppointmentRequest();
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocationOnMock) {
                Object[] args = invocationOnMock.getArguments();
                Errors errors = (Errors) args[1];
                errors.reject("invalid","save error");
                return null;
            }
        }).when(recurringPatternValidator).validate(any(), any());

        recurringAppointmentsController.editAppointment(recurringAppointmentRequest);

        ResponseEntity<Object> responseEntity = recurringAppointmentsController.save(recurringAppointmentRequest);
        verify(appointmentRecurringPatternService, never()).validateAndSave(any());
        verify(recurringAppointmentsService, never()).generateRecurringAppointments(recurringAppointmentRequest);
        verify(recurringAppointmentMapper, never()).constructResponse(Collections.emptyList());
        assertEquals(responseEntity.getStatusCode(), HttpStatus.BAD_REQUEST);
        assertEquals(((Map) ((Map) responseEntity.getBody()).get("error")).get("message"), "save error");
    }

    @Test
    public void shouldCallUpdateOfRecurringAppointmentServiceWhenApplyForAllIsTrue() {
        RecurringAppointmentRequest recurringAppointmentRequest = mock(RecurringAppointmentRequest.class);
        Appointment appointmentMock = mock(Appointment.class);
        AppointmentRequest appointmentRequest = mock(AppointmentRequest.class);
        List<Appointment> appointments = Arrays.asList(appointmentMock);
        RecurringAppointmentDefaultResponse defaultResponse = mock(RecurringAppointmentDefaultResponse.class);
        List<RecurringAppointmentDefaultResponse> defaultResponses = Arrays.asList(defaultResponse);
        AppointmentRecurringPattern appointmentRecurringPattern = mock(AppointmentRecurringPattern.class);
        AppointmentRecurringPattern updatedRecurringPattern = mock(AppointmentRecurringPattern.class);
        when((recurringAppointmentRequest.getAppointmentRequest())).thenReturn(appointmentRequest);
        when(recurringAppointmentRequest.getApplyForAll()).thenReturn(true);
        when(recurringAppointmentRequest.getTimeZone()).thenReturn("UTC");
        when(appointmentMock.getUuid()).thenReturn("uuid");
        when(appointmentRequest.getUuid()).thenReturn("uuid");
        when(allAppointmentRecurringPatternUpdateService.getUpdatedRecurringPattern(recurringAppointmentRequest)).thenReturn(appointmentRecurringPattern);
        when(appointmentRecurringPattern.getAppointments()).thenReturn(new HashSet<>(appointments));
        when(appointmentRecurringPatternService.update(appointmentRecurringPattern, appointmentMock)).thenReturn(updatedRecurringPattern);
        when(recurringAppointmentMapper.constructResponse((List<Appointment>) any())).thenReturn(defaultResponses);

        List<RecurringAppointmentDefaultResponse> responses = (List<RecurringAppointmentDefaultResponse>) recurringAppointmentsController.editAppointment(recurringAppointmentRequest).getBody();

        verify(appointmentMock).getUuid();
        verify(appointmentRequest).getUuid();
        verify(allAppointmentRecurringPatternUpdateService).getUpdatedRecurringPattern(recurringAppointmentRequest);
        verify(appointmentRecurringPatternService).update(appointmentRecurringPattern, appointmentMock);
        assertEquals(defaultResponses,responses);
    }

    @Test
    public void shouldCallUpdateWithRecurringPatternAndUpdatedAppointmentsWhenApplyForAllIsNull() {
        RecurringAppointmentRequest recurringAppointmentRequest = mock(RecurringAppointmentRequest.class);
        AppointmentRequest appointmentRequest = mock(AppointmentRequest.class);
        RecurringAppointmentDefaultResponse defaultResponse = mock(RecurringAppointmentDefaultResponse.class);
        List<RecurringAppointmentDefaultResponse> defaultResponses = Arrays.asList(defaultResponse);
        when(recurringAppointmentRequest.getAppointmentRequest()).thenReturn(appointmentRequest);
        when(recurringAppointmentRequest.getApplyForAll()).thenReturn(false);
        Appointment appointment = mock(Appointment.class);
        Appointment newAppointment = mock(Appointment.class);
        Appointment updatedAppointment = mock(Appointment.class);
        List<Appointment> appointments = Arrays.asList(newAppointment, appointment);
        final AppointmentRecurringPattern appointmentRecurringPattern = mock(AppointmentRecurringPattern.class);

        when(singleAppointmentRecurringPatternUpdateService.getUpdatedAppointment(recurringAppointmentRequest)).thenReturn(newAppointment);
        when(newAppointment.getAppointmentRecurringPattern()).thenReturn(appointmentRecurringPattern);
        when(newAppointment.getRelatedAppointment()).thenReturn(appointment);
        when(appointmentRecurringPatternService.update(appointmentRecurringPattern, appointments)).thenReturn(updatedAppointment);
        when(recurringAppointmentMapper.constructResponse((List<Appointment>) any())).thenReturn(defaultResponses);

        List<RecurringAppointmentDefaultResponse> actualResponse =
                (List<RecurringAppointmentDefaultResponse>) recurringAppointmentsController.editAppointment(recurringAppointmentRequest).getBody();

        verify(singleAppointmentRecurringPatternUpdateService).getUpdatedAppointment(recurringAppointmentRequest);
        verify(appointmentRecurringPatternService).update(appointmentRecurringPattern, appointments);
        assertEquals(defaultResponses, actualResponse);
    }

    @Test
    public void shouldThrowAPIExceptionWhenRequestIsInvalidForEdit() {
        RecurringAppointmentRequest recurringAppointmentRequest = new RecurringAppointmentRequest();
        updateErrorsObject();

        recurringAppointmentsController.editAppointment(recurringAppointmentRequest);

        ResponseEntity<Object> responseEntity = recurringAppointmentsController.save(recurringAppointmentRequest);
        verify(allAppointmentRecurringPatternUpdateService, never()).getUpdatedRecurringPattern(any());
        verify(singleAppointmentRecurringPatternUpdateService, never()).getUpdatedAppointment(any());
        verify(appointmentRecurringPatternService, never()).validateAndSave(any());
        verify(recurringAppointmentMapper, never()).constructResponse(Collections.emptyList());
        assertEquals(responseEntity.getStatusCode(), HttpStatus.BAD_REQUEST);
        assertEquals(((Map) ((Map) responseEntity.getBody()).get("error")).get("message"), "some error");
    }

    private void updateErrorsObject() {
        doAnswer(invocationOnMock -> {
            Object[] args = invocationOnMock.getArguments();
            Errors errors = (Errors) args[1];
            errors.reject("invalid","some error");
            return null;
        }).when(recurringPatternValidator).validate(any(), any());
    }

    @Test
    public void shouldGetAppointmentByUuid() {
        String appointmentUuid = "appointment";
        Appointment appointment = new Appointment();
        appointment.setUuid(appointmentUuid);
        when(appointmentsService.getAppointmentByUuid(appointmentUuid)).thenReturn(appointment);

        recurringAppointmentsController.getAppointmentByUuid(appointmentUuid);

        verify(appointmentsService, times(1)).getAppointmentByUuid(appointmentUuid);
        verify(recurringAppointmentMapper, times(1)).constructResponse(appointment);
    }

    @Test
    public void shouldThrowExceptionIfAppointmentDoesNotExist() {
        when(appointmentsService.getAppointmentByUuid(any(String.class))).thenReturn(null);
        expectedException.expect(RuntimeException.class);
        expectedException.expectMessage("Appointment does not exist");

        recurringAppointmentsController.getAppointmentByUuid("randomUuid");
    }

    @Test
    public void shouldChangeStatusOfAllRecurringAppointments() throws Exception {
        Map statusDetails = new HashMap();
        statusDetails.put("toStatus", "Cancelled");
        statusDetails.put("timeZone", "Asia/Calcutta");
        Appointment appointment = new Appointment();
        when(appointmentsService.getAppointmentByUuid(anyString())).thenReturn(appointment);
        recurringAppointmentsController.transitionAppointment("appointmentUuid", statusDetails);
        Mockito.verify(appointmentsService).getAppointmentByUuid("appointmentUuid");
        Mockito.verify(appointmentRecurringPatternService).changeStatus(appointment, "Cancelled", "Asia/Calcutta");
    }

    @Test
    public void shouldThrowExceptionWhenTimeZoneIsMissing() throws Exception {
        Map<String,String> statusDetails = new HashMap();
        doAnswer(invocationOnMock -> {
            Object[] args = invocationOnMock.getArguments();
            Errors errors = (Errors) args[1];
            errors.reject("invalid","Time Zone is missing");
            return null;
        }).when(timeZoneValidator).validate(any(), any());
        statusDetails.put("toStatus", "Cancelled");
        when(appointmentsService.getAppointmentByUuid(anyString())).thenReturn(new Appointment());

        ResponseEntity<Object> responseEntity = recurringAppointmentsController.transitionAppointment("appointmentUuid", statusDetails);
        Mockito.verify(appointmentRecurringPatternService, never()).changeStatus(any(),any(), any());
        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
        assertEquals(((Map)((Map)responseEntity.getBody()).get("error")).get("message"), "Time Zone is missing");
    }

    @Test
    public void shouldThrowExceptionIfAppointmentIsNotValid() throws Exception {
        Map<String,String> statusDetails = new HashMap<>();
        doNothing().when(timeZoneValidator).validate(any(), any());
        statusDetails.put("toStatus", "Cancelled");
        statusDetails.put("timeZone", "Asia/Calcutta");
        when(appointmentsService.getAppointmentByUuid(anyString())).thenReturn(null);
        ResponseEntity<Object> responseEntity = recurringAppointmentsController.transitionAppointment("appointmentUuid", statusDetails);
        Mockito.verify(appointmentRecurringPatternService, never()).changeStatus(any(),any(), any());
        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
        assertEquals(((Map)((Map)responseEntity.getBody()).get("error")).get("message"), "Appointment does not exist");
    }

    @Test
    public void shouldThrowAPIExceptionWhenRequestIsInvalidForConflicts() {
        RecurringAppointmentRequest recurringAppointmentRequest = new RecurringAppointmentRequest();
        updateErrorsObject();

        ResponseEntity<Object> responseEntity = recurringAppointmentsController.getConflicts(recurringAppointmentRequest);
        verify(appointmentMapper, never()).constructConflictResponse(Collections.emptyMap());
        verify(recurringAppointmentsService, never()).generateRecurringAppointments(any());
        assertEquals(responseEntity.getStatusCode(), HttpStatus.INTERNAL_SERVER_ERROR);
        assertEquals(((Map) ((Map) responseEntity.getBody()).get("error")).get("message"), "some error");
    }

    @Test
    public void shouldConstructResponseWhenConflictsForRecurringAppointmentRequestExists() {
        RecurringAppointmentRequest recurringAppointmentRequest = new RecurringAppointmentRequest();
        recurringAppointmentRequest.setAppointmentRequest(mock(AppointmentRequest.class));
        List<Appointment> appointments = mock(List.class);
        Map<Enum, List<Appointment>> conflicts = mock(Map.class);
        when(recurringAppointmentsService.generateRecurringAppointments(recurringAppointmentRequest)).thenReturn(appointments);
        when(appointmentsService.getAppointmentsConflicts(appointments)).thenReturn(conflicts);

        ResponseEntity<Object> responseEntity = recurringAppointmentsController.getConflicts(recurringAppointmentRequest);
        verify(recurringAppointmentsService).generateRecurringAppointments(recurringAppointmentRequest);
        verify(appointmentsService).getAppointmentsConflicts(appointments);
        verify(appointmentMapper).constructConflictResponse(conflicts);
        assertEquals(responseEntity.getStatusCode(), HttpStatus.OK);
        assertNotNull(responseEntity.getBody());
    }

    @Test
    public void shouldConstructConflictResponseInAppointmentEdit() {
        AppointmentRequest appointmentRequest = new AppointmentRequest();
        appointmentRequest.setUuid("1");
        RecurringAppointmentRequest recurringAppointmentRequest = new RecurringAppointmentRequest();
        recurringAppointmentRequest.setAppointmentRequest(appointmentRequest);
        Set<Appointment> appointments = mock(Set.class);
        AppointmentRecurringPattern appointmentRecurringPattern = new AppointmentRecurringPattern();
        appointmentRecurringPattern.setAppointments(appointments);
        when(allAppointmentRecurringPatternUpdateService.getUpdatedRecurringPatternForConflicts(recurringAppointmentRequest)).thenReturn(mock(AppointmentRecurringPattern.class));
        ResponseEntity<Object> responseEntity = recurringAppointmentsController.getConflicts(recurringAppointmentRequest);
        verify(allAppointmentRecurringPatternUpdateService).getUpdatedRecurringPatternForConflicts(recurringAppointmentRequest);
        assertEquals(HttpStatus.NO_CONTENT, responseEntity.getStatusCode());
        assertNull(responseEntity.getBody());
    }

    @Test
    public void shouldReturnNoContentWhenThereAreNoAppointmentsForGivenAppointmentRequest() {
        RecurringAppointmentRequest recurringAppointmentRequest = new RecurringAppointmentRequest();
        recurringAppointmentRequest.setAppointmentRequest(new AppointmentRequest());
        RecurringPattern recurringPattern = new RecurringPattern();
        recurringAppointmentRequest.getAppointmentRequest().setStartDateTime(new Date());
        recurringAppointmentRequest.getAppointmentRequest().setUuid("someUuid");
        recurringAppointmentRequest.getAppointmentRequest().setServiceUuid("someServiceUuid");
        recurringAppointmentRequest.setRecurringPattern(recurringPattern);
        AppointmentRecurringPattern appointmentRecurringPattern = new AppointmentRecurringPattern();
        appointmentRecurringPattern.setAppointments(Collections.emptySet());
        when(recurringPatternMapper.fromRequest(recurringPattern)).thenReturn(appointmentRecurringPattern);
        when(appointmentRecurringPatternService.validateAndSave(appointmentRecurringPattern)).thenReturn(appointmentRecurringPattern);
        when(recurringAppointmentsService.generateRecurringAppointments(recurringAppointmentRequest)).thenReturn(Collections.emptyList());
        when(recurringAppointmentMapper.constructResponse((List<Appointment>) any())).thenReturn(Collections.emptyList());
        doNothing().when(recurringPatternValidator).validate(recurringPattern, mock(Errors.class));
        appointmentRecurringPattern.setAppointments(Collections.emptySet());

        ResponseEntity<Object> responseEntity = recurringAppointmentsController.save(recurringAppointmentRequest);

        verify(recurringPatternMapper).fromRequest(recurringPattern);
        verify(recurringAppointmentsService).generateRecurringAppointments(recurringAppointmentRequest);
        assertEquals(responseEntity.getStatusCode(), HttpStatus.NO_CONTENT);
    }
}
