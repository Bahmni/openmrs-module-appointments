package org.openmrs.module.appointments.web.controller;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.openmrs.Patient;
import org.openmrs.api.APIException;
import org.openmrs.module.appointments.model.*;
import org.openmrs.module.appointments.service.AppointmentRecurringPatternService;
import org.openmrs.module.appointments.service.AppointmentServiceDefinitionService;
import org.openmrs.module.appointments.service.AppointmentsService;
import org.openmrs.module.appointments.util.DateUtil;
import org.openmrs.module.appointments.web.contract.*;
import org.openmrs.module.appointments.web.mapper.AbstractAppointmentRecurringPatternMapper;
import org.openmrs.module.appointments.web.mapper.AppointmentMapper;
import org.openmrs.module.appointments.web.mapper.AppointmentServiceMapper;
import org.openmrs.module.appointments.web.validators.Validator;
import org.openmrs.module.webservices.rest.SimpleObject;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.powermock.api.mockito.PowerMockito.when;


public class AppointmentControllerTest {

    @Mock
    private AppointmentsService appointmentsService;

    @Mock
    private AppointmentRecurringPatternService appointmentRecurringPatternService;

    @Mock
    private AppointmentServiceDefinitionService appointmentServiceDefinitionService;

    @Mock
    private AppointmentMapper appointmentMapper;

    @Mock
    @Qualifier("singleAppointmentRecurringPatternMapper")
    private AbstractAppointmentRecurringPatternMapper singleAppointmentRecurringPatternMapper;

    @Mock
    @Qualifier("allAppointmentRecurringPatternMapper")
    private AbstractAppointmentRecurringPatternMapper allAppointmentRecurringPatternMapper;

    @Mock
    private AppointmentServiceMapper appointmentServiceMapper;

    @Mock
    Validator<AppointmentRequest> appointmentRequestEditValidator;

    @InjectMocks
    private AppointmentController appointmentController;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Before
    public void setUp() throws Exception {
        initMocks(this);
    }

    @Test
    public void shouldGetAllFutureAppointmentsForGivenServiceType() throws Exception {
        AppointmentServiceType appointmentServiceType = new AppointmentServiceType();
        String serviceTypeUuid = "serviceTypeUuid";
        appointmentServiceType.setUuid(serviceTypeUuid);

        ArrayList<Appointment> appointments = new ArrayList<>();
        Appointment appointment = new Appointment();
        appointment.setUuid("appointmentUuid");
        appointment.setServiceType(appointmentServiceType);
        appointments.add(appointment);

        when(appointmentsService.getAllFutureAppointmentsForServiceType(appointmentServiceType)).thenReturn(appointments);
        when(appointmentServiceDefinitionService.getAppointmentServiceTypeByUuid(serviceTypeUuid)).thenReturn(appointmentServiceType);
        ArrayList<AppointmentDefaultResponse> responses = new ArrayList<>();
        AppointmentDefaultResponse defaultResponse = new AppointmentDefaultResponse();
        defaultResponse.setUuid(appointment.getUuid());
        responses.add(defaultResponse);
        when(appointmentMapper.constructResponse(appointments)).thenReturn(responses);

        List<AppointmentDefaultResponse> appointmentDefaultResponses = appointmentController.getAllFututreAppointmentsForGivenServiceType(serviceTypeUuid);

        verify(appointmentServiceDefinitionService, times(1)).getAppointmentServiceTypeByUuid(serviceTypeUuid);
        verify(appointmentsService, times(1)).getAllFutureAppointmentsForServiceType(appointmentServiceType);
        assertNotNull(appointmentDefaultResponses);
        assertEquals(1, appointmentDefaultResponses.size());
        assertEquals(appointment.getUuid(), appointmentDefaultResponses.get(0).getUuid());
    }

    @Test
    public void shouldGetAllAppointments() throws Exception {
        Appointment appointment = new Appointment();
        List<Appointment> appointmentList = new ArrayList<>();
        appointmentList.add(appointment);
        when(appointmentsService.getAllAppointments(null)).thenReturn(appointmentList);

        appointmentController.getAllAppointments(null);
        verify(appointmentsService, times(1)).getAllAppointments(null);
        verify(appointmentMapper, times(1)).constructResponse(appointmentList);
    }

    @Test
    public void shouldGetAllAppointmentsForDate() throws Exception {
        Appointment appointment = new Appointment();
        List<Appointment> appointmentList = new ArrayList<>();
        appointmentList.add(appointment);
        String dateString = "2017-08-15T00:00:00.0Z";
        Date forDate = DateUtil.convertToLocalDateFromUTC(dateString);

        when(appointmentsService.getAllAppointments(forDate)).thenReturn(appointmentList);

        appointmentController.getAllAppointments(dateString);
        verify(appointmentsService, times(1)).getAllAppointments(forDate);
        verify(appointmentMapper, times(1)).constructResponse(appointmentList);
    }

    @Test
    public void shouldGetAllAppointmentsSummary() throws ParseException {
        String startDateString = "2017-08-15T00:00:00.000Z";
        String endDateString = "2017-08-22T00:00:00.000Z";
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Date startDate = DateUtil.convertToLocalDateFromUTC(startDateString);
        Date endDate = DateUtil.convertToLocalDateFromUTC(endDateString);
        AppointmentServiceDefinition appointmentServiceDefinition = new AppointmentServiceDefinition();
        appointmentServiceDefinition.setName("Ortho");
        appointmentServiceDefinition.setUuid("someUuid");
        List<AppointmentServiceDefinition> appointmentServiceDefinitions = new ArrayList<>();
        appointmentServiceDefinitions.add(appointmentServiceDefinition);
        Appointment appointment = new Appointment();
        appointment.setUuid("apptUuid");
        appointment.setService(appointmentServiceDefinition);
        appointment.setStartDateTime(startDate);
        appointment.setStatus(AppointmentStatus.Scheduled);
        List<Appointment> appointmentList = new ArrayList<>();
        appointmentList.add(appointment);
        List<AppointmentStatus> appointmentStatuses = Arrays.asList(
                AppointmentStatus.Completed,
                AppointmentStatus.Scheduled,
                AppointmentStatus.CheckedIn,
                AppointmentStatus.Missed);
        AppointmentServiceDefaultResponse appointmentServiceDefaultResponse = new AppointmentServiceDefaultResponse();
        appointmentServiceDefaultResponse.setUuid("someUuid");

        when(appointmentServiceDefinitionService.getAllAppointmentServices(false)).thenReturn(appointmentServiceDefinitions);
        when(appointmentsService.getAppointmentsForService(appointmentServiceDefinition, startDate, endDate, appointmentStatuses)).thenReturn(appointmentList);
        when(appointmentServiceMapper.constructDefaultResponse(appointmentServiceDefinition)).thenReturn(appointmentServiceDefaultResponse);

        List<AppointmentsSummary> allAppointmentsSummary = appointmentController.getAllAppointmentsSummary(startDateString, endDateString);
        verify(appointmentServiceDefinitionService, times(1)).getAllAppointmentServices(false);
        verify(appointmentsService, times(1)).getAppointmentsForService(appointmentServiceDefinition, startDate, endDate, appointmentStatuses);
        assertEquals(1, allAppointmentsSummary.size());
        assertEquals("someUuid", allAppointmentsSummary.get(0).getAppointmentService().getUuid());
        assertEquals(1, allAppointmentsSummary.get(0).getAppointmentCountMap().size());
        DailyAppointmentServiceSummary dailyAppointmentServiceSummary = (DailyAppointmentServiceSummary)allAppointmentsSummary.get(0).getAppointmentCountMap().get("2017-08-15");
        assertEquals(1, dailyAppointmentServiceSummary.getAllAppointmentsCount(), 0);
        assertEquals(0, dailyAppointmentServiceSummary.getMissedAppointmentsCount(), 0);
        assertEquals(simpleDateFormat.parse(startDateString), dailyAppointmentServiceSummary.getAppointmentDate());
        assertEquals("someUuid", dailyAppointmentServiceSummary.getAppointmentServiceUuid());
    }

    @Test
    public void shouldThrowExceptionIfPatientUuidIsBlankWhileCreatingAppointment() throws Exception {
        when(appointmentsService.validateAndSave(any(Appointment.class))).thenThrow(new APIException("Exception Msg"));
        ResponseEntity<Object> responseEntity = appointmentController.saveAppointment(new AppointmentRequest());
        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
    }

    @Test
    public void shouldGetAppointmentByUuid() throws Exception {
        String appointmentUuid = "appointmentUuid";
        Appointment appointment = new Appointment();
        appointment.setUuid(appointmentUuid);
        when(appointmentsService.getAppointmentByUuid(appointmentUuid)).thenReturn(appointment);
        appointmentController.getAppointmentByUuid(appointmentUuid);
        verify(appointmentsService, times(1)).getAppointmentByUuid(appointmentUuid);
        verify(appointmentMapper, times(1)).constructResponse(appointment);
    }

    @Test
    public void shouldThrowExceptionIfAppointmentDoesNotExist() throws Exception {
        when(appointmentsService.getAppointmentByUuid(any(String.class))).thenReturn(null);
        expectedException.expect(RuntimeException.class);
        expectedException.expectMessage("Appointment does not exist");
        appointmentController.getAppointmentByUuid("randomUuid");
    }

    @Test
    public void shouldChangeStatusOfAppointment() throws Exception {
        Map statusDetails = new HashMap();
        statusDetails.put("toStatus", "Completed");
        Appointment appointment = new Appointment();
        when(appointmentsService.getAppointmentByUuid(anyString())).thenReturn(appointment);
        appointmentController.transitionAppointment("appointmentUuid", statusDetails);
        Mockito.verify(appointmentsService, times(1)).getAppointmentByUuid("appointmentUuid");
        Mockito.verify(appointmentsService, times(1)).changeStatus(appointment, "Completed", null);
    }

    @Test
    public void shouldChangeStatusOfRecurringAppointment() throws Exception {
        Map statusDetails = new HashMap();
        statusDetails.put("toStatus", "Completed");
        statusDetails.put("applyForAll", "true");
        statusDetails.put("timeZone", "Asia/Calcutta");
        Appointment appointment = new Appointment();
        when(appointmentsService.getAppointmentByUuid(anyString())).thenReturn(appointment);
        appointmentController.transitionAppointment("appointmentUuid", statusDetails);
        Mockito.verify(appointmentsService, times(1)).getAppointmentByUuid("appointmentUuid");
        Mockito.verify(appointmentRecurringPatternService, times(1)).changeStatus(appointment, "Completed", null, "Asia/Calcutta");
        Mockito.verify(appointmentsService, never()).changeStatus(appointment, "Completed", null);
    }

    @Test
    public void shouldReturnErrorResponseWhenAppointmentDoesNotExist() throws Exception {
        Map<String,String> statusDetails = new HashMap();
        statusDetails.put("toStatus", "Completed");
        when(appointmentsService.getAppointmentByUuid(anyString())).thenReturn(null);
        appointmentController.transitionAppointment("appointmentUuid", statusDetails);
        Mockito.verify(appointmentsService, times(1)).getAppointmentByUuid("appointmentUuid");
        Mockito.verify(appointmentsService, never()).changeStatus(any(),any(),any());
    }

    @Test
    public void shouldChangeStatusOfScheduledFutureAppointments() throws Exception {
        Map<String,String> statusDetails = new HashMap();
        statusDetails.put("toStatus", "Completed");
        statusDetails.put("applyForAll", "true");
        when(appointmentsService.getAppointmentByUuid(anyString())).thenReturn(new Appointment());

        ResponseEntity<Object> responseEntity = appointmentController.transitionAppointment("appointmentUuid", statusDetails);
        Mockito.verify(appointmentRecurringPatternService, never()).changeStatus(any(),any(),any(), any());
        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
    }

    @Test
    public void shouldUndoStatusOfAppointment() throws Exception {
        Appointment appointment = new Appointment();
        appointment.setUuid("appointmentUuid");
        when(appointmentsService.getAppointmentByUuid(appointment.getUuid())).thenReturn(appointment);
        ResponseEntity<Object> responseEntity = appointmentController.undoStatusChange(appointment.getUuid());
        Mockito.verify(appointmentsService, times(1)).getAppointmentByUuid(appointment.getUuid());
        Mockito.verify(appointmentsService, times(1)).undoStatusChange(appointment);
        Mockito.verify(appointmentMapper, times(1)).constructResponse(appointment);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
    }

    @Test
    public void shouldReturnErrorResponseWhenAppointmentDoesNotExistOnUndoStaus() throws Exception {
        String appointmentUuid = "appointmentUuid";
        when(appointmentsService.getAppointmentByUuid(appointmentUuid)).thenReturn(null);
        ResponseEntity<Object> responseEntity = appointmentController.undoStatusChange(appointmentUuid);
        Mockito.verify(appointmentsService, times(1)).getAppointmentByUuid(appointmentUuid);
        Mockito.verify(appointmentsService, never()).undoStatusChange(any());
        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
    }

    @Test
    public void shouldSearchWithAppointment() throws Exception {
        List<Appointment> appointments = new ArrayList<>();
        Appointment appointment = new Appointment();
        appointment.setUuid("appointmentUuid");
        Patient patient = new Patient();
        patient.setUuid("somePatientUuid");
        appointment.setPatient(patient);
        appointments.add(appointment);
        AppointmentServiceDefinition appointmentServiceDefinition = new AppointmentServiceDefinition();
        appointmentServiceDefinition.setUuid("someServiceUuid");
        AppointmentQuery appointmentQuery = new AppointmentQuery();
        appointmentQuery.setLocationUuid("someLocationUuid");
        appointmentQuery.setPatientUuid("somePatientUuid");
        appointmentQuery.setProviderUuid("someProviderUuid");
        appointmentQuery.setServiceUuid("someServiceUuid");

        AppointmentDefaultResponse appointmentDefaultResponse = new AppointmentDefaultResponse();
        appointmentDefaultResponse.setUuid("appointmentUuid1");

        List<AppointmentDefaultResponse> appointmentDefaultResponses = new ArrayList<>();
        appointmentDefaultResponses.add(appointmentDefaultResponse);

        when(appointmentMapper.mapQueryToAppointment(appointmentQuery)).thenReturn(appointment);
        when(appointmentsService.search(appointment)).thenReturn(appointments);
        when(appointmentMapper.constructResponse(appointments)).thenReturn(appointmentDefaultResponses);
        List<AppointmentDefaultResponse> appointmentResponses = appointmentController.searchAppointments(appointmentQuery);
        AppointmentDefaultResponse appointmentResponse = appointmentDefaultResponses.get(0);
        assertEquals("appointmentUuid1", appointmentResponse.getUuid());
    }

    @Test
    public void shouldSaveAnAppointment() throws Exception{
        AppointmentRequest appointmentRequest = new AppointmentRequest();
        appointmentRequest.setPatientUuid("somePatientUuid");
        appointmentRequest.setUuid("someUuid");
        appointmentRequest.setServiceUuid("someServiceUuid");

        Appointment appointment = new Appointment();
        appointment.setUuid("appointmentUuid");

        when(appointmentMapper.fromRequest(appointmentRequest)).thenReturn(appointment);
        when(appointmentsService.validateAndSave(appointment)).thenReturn(appointment);

        appointmentController.saveAppointment(appointmentRequest);
        Mockito.verify(appointmentMapper, times(1)).fromRequest(appointmentRequest);
        Mockito.verify(appointmentsService, times(1)).validateAndSave(appointment);
    }

    @Test
    public void shouldCallUpdateOfAppointmentsServiceWhenApplyForAllIsFalseForUpdateOfAppointment() {
        AppointmentRequest appointmentRequest = mock(AppointmentRequest.class);
        Appointment appointmentMock = mock(Appointment.class);
        when(appointmentRequestEditValidator.validate(appointmentRequest)).thenReturn(true);
        when(appointmentMapper.fromRequest(appointmentRequest)).thenReturn(appointmentMock);
        when(appointmentRequest.getApplyForAll()).thenReturn(false);

        appointmentController.editAppointment(appointmentRequest);

        verify(appointmentMapper).fromRequest(any(AppointmentRequest.class));
        verify(appointmentsService).validateAndUpdate(appointmentMock);
    }

    @Test
    public void shouldNotCallUpdateOfAppointmentsServiceWhenApplyForAllIsTrueForUpdateOfAppointment() {
        AppointmentRequest appointmentRequest = mock(AppointmentRequest.class);
        Appointment appointmentMock = mock(Appointment.class);
        AppointmentRecurringPattern appointmentRecurringPattern = mock(AppointmentRecurringPattern.class);
        when(appointmentRequestEditValidator.validate(appointmentRequest)).thenReturn(true);
        when(appointmentMapper.fromRequest(appointmentRequest)).thenReturn(appointmentMock);
        when(appointmentRequest.requiresUpdateOfAllRecurringAppointments()).thenReturn(true);
        when(appointmentRequest.getTimeZone()).thenReturn("UTC");
        when(appointmentRecurringPattern.getAppointments()).thenReturn(new HashSet<>());
        when(appointmentRecurringPatternService.update(any())).thenReturn(appointmentRecurringPattern);
        appointmentController.editAppointment(appointmentRequest);

        verify(appointmentsService, never()).validateAndUpdate(appointmentMock);
    }

    @Test
    public void shouldCallUpdateOfRecurringAppointmentServiceWhenApplyForAllIsTrue() {
        AppointmentRequest appointmentRequest = mock(AppointmentRequest.class);
        Appointment appointmentMock = mock(Appointment.class);
        AppointmentRecurringPattern appointmentRecurringPattern = mock(AppointmentRecurringPattern.class);
        when(appointmentRequestEditValidator.validate(appointmentRequest)).thenReturn(true);
        when(appointmentMapper.fromRequest(appointmentRequest)).thenReturn(appointmentMock);
        when(appointmentRequest.requiresUpdateOfAllRecurringAppointments()).thenReturn(true);
        when(appointmentRequest.getTimeZone()).thenReturn("UTC");
        when(allAppointmentRecurringPatternMapper.fromRequest(appointmentRequest)).thenReturn(appointmentRecurringPattern);
        when(appointmentRecurringPattern.getAppointments()).thenReturn(new HashSet<>(Arrays.asList(appointmentMock)));

        appointmentController.editAppointment(appointmentRequest);

        verify(appointmentsService, never()).validateAndUpdate(appointmentMock);
        verify(allAppointmentRecurringPatternMapper).fromRequest(appointmentRequest);
    }

    @Test
    public void shouldCallUpdateOfRecurringAppointmentServiceWhenApplyForAllIsNull() {
        AppointmentRequest appointmentRequest = mock(AppointmentRequest.class);
        Appointment appointmentMock = mock(Appointment.class);
        when(appointmentMapper.fromRequest(appointmentRequest)).thenReturn(appointmentMock);
        when(appointmentRequest.getApplyForAll()).thenReturn(null);
        when(appointmentRequestEditValidator.validate(appointmentRequest)).thenReturn(true);

        appointmentController.editAppointment(appointmentRequest);

        verify(appointmentMapper).fromRequest(any(AppointmentRequest.class));
        verify(appointmentsService).validateAndUpdate(appointmentMock);
    }

    @Test
    public void shouldThrowExceptionIfAppointmentTimeZoneIsNull() throws Exception {
        AppointmentRequest appointmentRequest = mock(AppointmentRequest.class);
        when(appointmentRequest.requiresUpdateOfAllRecurringAppointments()).thenReturn(true);
        when(appointmentRequest.getTimeZone()).thenReturn(null);
        Appointment appointmentMock = mock(Appointment.class);
        when(appointmentMapper.fromRequest(appointmentRequest)).thenReturn(appointmentMock);
        when(appointmentRequestEditValidator.validate(any())).thenReturn(true);
        ResponseEntity<Object> responseEntity = appointmentController.editAppointment(appointmentRequest);

        Mockito.verify(appointmentMapper, never()).fromRequest(any());
        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
    }

    @Test
    public void shouldUpdateSingleRecurringAppointmentByCreatingANewAppointmentReferencingTheExisitngAppointment() {
        AppointmentRequest appointmentRequest = mock(AppointmentRequest.class);

        when(appointmentRequest.isRecurringAppointment()).thenReturn(true);
        when(appointmentRequest.requiresUpdateOfAllRecurringAppointments()).thenReturn(false);

        AppointmentRecurringPattern appointmentRecurringPattern = mock(AppointmentRecurringPattern.class);
        Appointment appointment = mock(Appointment.class);

        when(appointmentRequestEditValidator.validate(appointmentRequest)).thenReturn(true);
        when(singleAppointmentRecurringPatternMapper.fromRequest(appointmentRequest)).thenReturn(appointmentRecurringPattern);
        final AppointmentRecurringPattern updatedRecurringAppointmentPattern = mock(AppointmentRecurringPattern.class);
        when(appointmentRecurringPatternService.update(any())).thenReturn(updatedRecurringAppointmentPattern);
        when(updatedRecurringAppointmentPattern.getAppointments()).thenReturn(new HashSet<>(Arrays.asList(appointment)));
        when(appointment.getUuid()).thenReturn("uuid");
        when(appointmentRequest.getUuid()).thenReturn("uuid");

        final ResponseEntity<Object> responseEntity = appointmentController.editAppointment(appointmentRequest);

        Mockito.verify(singleAppointmentRecurringPatternMapper, times(1)).fromRequest(appointmentRequest);
        Mockito.verify(appointmentRecurringPatternService, times(1)).update(appointmentRecurringPattern);

        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());

    }

    @Test
    public void shouldUpdateSingleRecurringAppointmentAndReturnTheNewAppointmentWhenAppointmentInRequestDoesNotHaveARelatedAppointment() {
        AppointmentRequest appointmentRequest = mock(AppointmentRequest.class);

        when(appointmentRequest.isRecurringAppointment()).thenReturn(true);
        when(appointmentRequest.requiresUpdateOfAllRecurringAppointments()).thenReturn(false);

        AppointmentRecurringPattern appointmentRecurringPattern = mock(AppointmentRecurringPattern.class);
        Appointment appointment = mock(Appointment.class);
        Appointment newAppointment = mock(Appointment.class);

        when(singleAppointmentRecurringPatternMapper.fromRequest(appointmentRequest)).thenReturn(appointmentRecurringPattern);
        final AppointmentRecurringPattern updatedRecurringAppointmentPattern = mock(AppointmentRecurringPattern.class);
        when(appointmentRecurringPatternService.update(any())).thenReturn(updatedRecurringAppointmentPattern);
        when(updatedRecurringAppointmentPattern.getActiveAppointments()).thenReturn(new HashSet<>(Arrays.asList(appointment, newAppointment)));
        when(appointment.getUuid()).thenReturn("uuid");
        when(appointmentRequest.getUuid()).thenReturn("uuid");
        when(newAppointment.getRelatedAppointment()).thenReturn(appointment);
        when(newAppointment.getUuid()).thenReturn("newUuid");

        when(appointment.getVoided()).thenReturn(true);
        when(appointmentRequestEditValidator.validate(appointmentRequest)).thenReturn(true);

        appointmentController.editAppointment(appointmentRequest);

        verify(appointmentMapper, times(1)).constructResponse(newAppointment);

    }

    @Test
    public void shouldUpdateRecurringAppointmentAndReturnTheSameAppointmentWhenThisAppointmentHasRelatedAppointment() {
        AppointmentRequest appointmentRequest = mock(AppointmentRequest.class);

        when(appointmentRequest.isRecurringAppointment()).thenReturn(true);
        when(appointmentRequest.requiresUpdateOfAllRecurringAppointments()).thenReturn(false);

        AppointmentRecurringPattern appointmentRecurringPattern = mock(AppointmentRecurringPattern.class);
        Appointment appointment = mock(Appointment.class);
        Appointment oldRecurringAppointment = mock(Appointment.class);

        when(singleAppointmentRecurringPatternMapper.fromRequest(appointmentRequest)).thenReturn(appointmentRecurringPattern);
        final AppointmentRecurringPattern updatedRecurringAppointmentPattern = mock(AppointmentRecurringPattern.class);
        when(appointmentRecurringPatternService.update(any())).thenReturn(updatedRecurringAppointmentPattern);
        when(updatedRecurringAppointmentPattern.getActiveAppointments()).thenReturn(new HashSet<>(Arrays.asList(appointment, oldRecurringAppointment)));
        when(appointment.getUuid()).thenReturn("uuid");
        when(appointmentRequest.getUuid()).thenReturn("uuid");
        when(appointment.getRelatedAppointment()).thenReturn(oldRecurringAppointment);
        when(oldRecurringAppointment.getUuid()).thenReturn("oldUuid");
        when(appointment.getVoided()).thenReturn(false);
        when(appointmentRequestEditValidator.validate(appointmentRequest)).thenReturn(true);


        appointmentController.editAppointment(appointmentRequest);

        verify(appointmentMapper, times(1)).constructResponse(appointment);
    }

    @Test
    public void shouldThrowExceptionWhenAppointmentRequestValidationFailed() {
        AppointmentRequest appointmentRequest = mock(AppointmentRequest.class);

        when(appointmentRequest.isRecurringAppointment()).thenReturn(true);
        when(appointmentRequest.requiresUpdateOfAllRecurringAppointments()).thenReturn(false);

        when(appointmentRequestEditValidator.validate(appointmentRequest)).thenReturn(false);
        final String error = "error";
        when(appointmentRequestEditValidator.getError()).thenReturn(error);

        final ResponseEntity<Object> responseEntity = appointmentController.editAppointment(appointmentRequest);
        verify(appointmentRequestEditValidator,times(1)).getError();
        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
        final String message = (String)((LinkedHashMap) ((SimpleObject) responseEntity.getBody()).get("error")).get("message");
        assertEquals(error, message);


    }
}
