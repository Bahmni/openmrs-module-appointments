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
import org.openmrs.module.appointments.service.AppointmentServiceDefinitionService;
import org.openmrs.module.appointments.service.AppointmentsService;
import org.openmrs.module.appointments.util.DateUtil;
import org.openmrs.module.appointments.web.contract.*;
import org.openmrs.module.appointments.web.mapper.AppointmentMapper;
import org.openmrs.module.appointments.web.mapper.AppointmentServiceMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.powermock.api.mockito.PowerMockito.when;


public class AppointmentControllerTest {

    @Mock
    private AppointmentsService appointmentsService;

    @Mock
    private AppointmentServiceDefinitionService appointmentServiceDefinitionService;

    @Mock
    private AppointmentMapper appointmentMapper;

    @Mock
    private AppointmentServiceMapper appointmentServiceMapper;

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
                AppointmentStatus.Requested,
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
}
