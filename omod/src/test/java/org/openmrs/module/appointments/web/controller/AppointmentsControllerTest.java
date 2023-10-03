package org.openmrs.module.appointments.web.controller;


import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.openmrs.Patient;
import org.mockito.runners.MockitoJUnitRunner;
import org.openmrs.module.appointments.model.Appointment;
import org.openmrs.module.appointments.model.AppointmentSearchRequest;
import org.openmrs.module.appointments.model.AppointmentServiceDefinition;
import org.openmrs.module.appointments.service.AppointmentsService;
import org.openmrs.module.appointments.util.DateUtil;
import org.openmrs.module.appointments.web.contract.AppointmentDefaultResponse;
import org.openmrs.module.appointments.web.contract.AppointmentRequest;
import org.openmrs.module.appointments.web.mapper.AppointmentMapper;
import org.openmrs.module.appointments.web.validators.AppointmentSearchValidator;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.Errors;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyList;
import java.util.Map;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.powermock.api.mockito.PowerMockito.doAnswer;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.when;

@RunWith(MockitoJUnitRunner.class)
public class AppointmentsControllerTest {

    @Mock
    private AppointmentsService appointmentsService;

    @Mock
    AppointmentSearchValidator appointmentSearchValidator;

    @Mock
    private AppointmentMapper appointmentMapper;

    @InjectMocks
    private AppointmentsController appointmentsController;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Before
    public void setUp() throws Exception {
        initMocks(this);
    }

    @Test
    public void shouldGetAppointmentByUuid() throws Exception {
        String appointmentUuid = "random";
        Appointment appointment = new Appointment();
        appointment.setUuid(appointmentUuid);
        when(appointmentsService.getAppointmentByUuid(appointmentUuid)).thenReturn(appointment);
        appointmentsController.getAppointmentByUuid(appointmentUuid);
        verify(appointmentsService, times(1)).getAppointmentByUuid(appointmentUuid);
        verify(appointmentMapper, times(1)).constructResponse(appointment);
    }

    @Test
    public void shouldGetAllAppointmentsForADate() throws Exception {
        List<Appointment> appointmentsForDate = new ArrayList<>();
        Appointment appointment1 = new Appointment();
        appointment1.setId(1);
        appointment1.setUuid("uuid1");
        appointment1.setStartDateTime(DateUtil.convertToDate("2017-03-15T16:57:09.0Z",DateUtil.DateFormatType.UTC));

        Appointment appointment2 = new Appointment();
        appointment1.setId(2);
        appointment1.setUuid("uuid2");
        appointment1.setStartDateTime(DateUtil.convertToDate("2017-03-15T19:57:09.0Z",DateUtil.DateFormatType.UTC));

        appointmentsForDate.add(appointment1);
        appointmentsForDate.add(appointment2);

        Date searchDate = DateUtil.convertToLocalDateFromUTC("2017-03-15T16:57:09.0Z");


        when(appointmentsService.getAllAppointments(searchDate)).thenReturn(appointmentsForDate);
        when(appointmentMapper.constructResponse(anyList())).thenReturn(
                appointmentsForDate.stream().map(as -> { return new AppointmentDefaultResponse(); }).collect(Collectors.toList()));


        List<AppointmentDefaultResponse> allAppointments = appointmentsController.getAllAppointments("2017-03-15T16:57:09.0Z");
        verify(appointmentsService, times(1)).getAllAppointments(searchDate);
        verify(appointmentMapper, times(1)).constructResponse(appointmentsForDate);
        assertEquals(2, allAppointments.size());
    }

    @Test
    public void shouldSaveAppointment() throws Exception {

        AppointmentRequest request = new AppointmentRequest();
        request.setStartDateTime(new Date());
        request.setPatientUuid("2c33920f-7aa6-48d6-998a-60412d8ff7d5");
        request.setServiceUuid("c36006d4-9fbb-4f20-866b-0ece245615c1");

        Appointment appointment = new Appointment();
        appointment.setStartDateTime(request.getStartDateTime());
        appointment.setPatient(new Patient());
        appointment.setService(new AppointmentServiceDefinition());

        when(appointmentMapper.fromRequest(request)).thenReturn(appointment);
        appointmentsController.saveAppointment(request);


        verify(appointmentMapper, times(1)).fromRequest(request);
        verify(appointmentsService, times(1)).validateAndSave(appointment);
    }

    @Test
    public void shouldNotSaveAppointmentWithInvalidPatient() throws Exception {

        AppointmentRequest request = new AppointmentRequest();
        request.setStartDateTime(new Date());
        request.setPatientUuid("invalidPatient");
        request.setServiceUuid("c36006d4-9fbb-4f20-866b-0ece245615c1");

        Appointment appointment = new Appointment();
        appointment.setStartDateTime(request.getStartDateTime());
        appointment.setPatient(new Patient());
        appointment.setService(new AppointmentServiceDefinition());

        when(appointmentMapper.fromRequest(request)).thenReturn(appointment);
        appointmentsController.saveAppointment(request);


        verify(appointmentMapper, times(1)).fromRequest(request);
        verify(appointmentsService, times(1)).validateAndSave(appointment);
    }


    @Test
    public void shouldReturnValidStatusIfNotFound() throws Exception {
        String appointmentUuid = "66604r42-3ca8-11e3-bf2b-0800271c13346";
        ResponseEntity<AppointmentDefaultResponse> response = appointmentsController.getAppointmentByUuid(appointmentUuid);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    public void shouldGetAppointmentsBetweenGivenDateRange() {
        AppointmentSearchRequest appointmentSearchRequest = new AppointmentSearchRequest();
        ArrayList<Appointment> appointments = new ArrayList<>();
        ArrayList<AppointmentDefaultResponse> expectedResponse = new ArrayList<>();
        when(appointmentsService.search(appointmentSearchRequest)).thenReturn(appointments);
        when(appointmentMapper.constructResponse(appointments)).thenReturn(expectedResponse);

        List<AppointmentDefaultResponse> actualResponse = appointmentsController.search(appointmentSearchRequest);

        verify(appointmentsService, times(1)).search(appointmentSearchRequest);
        verify(appointmentMapper, times(1)).constructResponse(appointments);
        assertEquals(expectedResponse, actualResponse);
    }

    @Test(expected = RuntimeException.class)
    public void shouldThrowExceptionWhenAppointmentsServiceSearchMethodReturnsNull() {
        AppointmentSearchRequest appointmentSearchRequest = new AppointmentSearchRequest();
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocationOnMock) {
                Object[] args = invocationOnMock.getArguments();
                Errors errors = (Errors) args[1];
                errors.reject("Either StartDate or EndDate not provided");
                return null;
            }
        }).when(appointmentSearchValidator).validate(any(), any());

        appointmentsController.search(appointmentSearchRequest);

        verify(appointmentsService, never()).search(appointmentSearchRequest);
        verify(appointmentMapper, never()).constructResponse(anyListOf(Appointment.class));
    }

    @Test
    public void shouldChangeStatusOfAppointment() throws Exception {
        Map statusDetails = new HashMap();
        statusDetails.put("toStatus", "Completed");
        Appointment appointment = new Appointment();
        when(appointmentsService.getAppointmentByUuid(anyString())).thenReturn(appointment);

        appointmentsController.transitionAppointment("appointmentUuid", statusDetails);

        verify(appointmentsService, times(1)).getAppointmentByUuid("appointmentUuid");
        verify(appointmentsService, times(1)).changeStatus(appointment, "Completed", null);
    }

    @Test
    public void shouldReturnErrorResponseWhenAppointmentDoesNotExist() throws Exception {
        Map<String, String> statusDetails = new HashMap();
        statusDetails.put("toStatus", "Completed");
        when(appointmentsService.getAppointmentByUuid(anyString())).thenReturn(null);

        expectedException.expect(RuntimeException.class);
        expectedException.expectMessage("Appointment does not exist");

        appointmentsController.transitionAppointment("appointmentUuid", statusDetails);

        verify(appointmentsService, times(1)).getAppointmentByUuid("appointmentUuid");
        verify(appointmentsService, never()).changeStatus(any(), any(), any());
    }

    @Test
    public void shouldReturnConflictResponseForAnAppointmentHavingNoConflicts() {
        AppointmentRequest appointmentRequest = mock(AppointmentRequest.class);
        Appointment appointment = mock(Appointment.class);

        when(appointmentMapper.fromRequestClonedAppointment(appointmentRequest)).thenReturn(appointment);
        when(appointmentsService.getAppointmentConflicts(appointment)).thenReturn(mock(Map.class));

        appointmentsController.getConflicts(appointmentRequest);

        verify(appointmentMapper).fromRequestClonedAppointment(appointmentRequest);
        verify(appointmentsService).getAppointmentConflicts(appointment);
    }
}
