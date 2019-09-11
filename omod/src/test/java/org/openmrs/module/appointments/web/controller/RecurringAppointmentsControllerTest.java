package org.openmrs.module.appointments.web.controller;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.InjectMocks;
import org.mockito.Mock;
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
import org.openmrs.module.appointments.web.mapper.RecurringAppointmentMapper;
import org.openmrs.module.appointments.web.mapper.RecurringPatternMapper;
import org.openmrs.module.appointments.web.service.impl.AllAppointmentRecurringPatternUpdateService;
import org.openmrs.module.appointments.web.service.impl.RecurringAppointmentsService;
import org.openmrs.module.appointments.web.service.impl.SingleAppointmentRecurringPatternUpdateService;
import org.openmrs.module.appointments.web.validators.RecurringPatternValidator;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.Errors;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
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
    private RecurringPatternMapper recurringPatternMapper;

    @Mock
    private RecurringAppointmentMapper recurringAppointmentMapper;

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
        when(appointmentRecurringPatternService.validateAndSave(appointmentRecurringPattern)).thenReturn(appointments);
        when(recurringAppointmentsService.generateRecurringAppointments(recurringAppointmentRequest)).thenReturn(new ArrayList<>());
        when(recurringAppointmentMapper.constructResponse(Arrays.asList(appointmentOne, appointmentTwo))).thenReturn(Collections.emptyList());
        doNothing().when(recurringPatternValidator).validate(recurringPattern, mock(Errors.class));
        appointmentRecurringPattern.setAppointments(Collections.emptySet());

        ResponseEntity<Object> responseEntity = recurringAppointmentsController.save(recurringAppointmentRequest);

        verify(recurringPatternMapper, times(1)).fromRequest(recurringPattern);
        verify(appointmentRecurringPatternService, times(1)).validateAndSave(appointmentRecurringPattern);
        verify(recurringAppointmentsService, times(1)).generateRecurringAppointments(recurringAppointmentRequest);
        verify(recurringAppointmentMapper, times(1)).constructResponse(Collections.emptyList());
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
                errors.reject("save error");
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
        AppointmentRecurringPattern appointmentRecurringPattern = mock(AppointmentRecurringPattern.class);
        when((recurringAppointmentRequest.getAppointmentRequest())).thenReturn(appointmentRequest);
        when(recurringAppointmentRequest.requiresUpdateOfAllRecurringAppointments()).thenReturn(true);
        when(recurringAppointmentRequest.getTimeZone()).thenReturn("UTC");
        when(allAppointmentRecurringPatternUpdateService.getUpdatedRecurringPattern(recurringAppointmentRequest)).thenReturn(appointmentRecurringPattern);
        when(appointmentRecurringPattern.getAppointments()).thenReturn(new HashSet<>(Arrays.asList(appointmentMock)));
        when(appointmentRecurringPatternService.update(any())).thenReturn(mock(AppointmentRecurringPattern.class));

        recurringAppointmentsController.editAppointment(recurringAppointmentRequest);

        verify(allAppointmentRecurringPatternUpdateService).getUpdatedRecurringPattern(recurringAppointmentRequest);
        verify(appointmentRecurringPatternService).update(any());
    }

    @Test
    public void shouldCallUpdateWithRecurringPatternAndUpdatedAppointmentsWhenApplyForAllIsNull() {
        RecurringAppointmentRequest recurringAppointmentRequest = mock(RecurringAppointmentRequest.class);
        AppointmentRequest appointmentRequest = mock(AppointmentRequest.class);
        when(recurringAppointmentRequest.getAppointmentRequest()).thenReturn(appointmentRequest);
        when(recurringAppointmentRequest.requiresUpdateOfAllRecurringAppointments()).thenReturn(false);
        Appointment appointment = mock(Appointment.class);
        Appointment newAppointment = mock(Appointment.class);
        Appointment updatedAppointment = mock(Appointment.class);
        List<Appointment> appointments = Arrays.asList(newAppointment, appointment);
        final AppointmentRecurringPattern appointmentRecurringPattern = mock(AppointmentRecurringPattern.class);

        when(singleAppointmentRecurringPatternUpdateService.getUpdatedAppointment(recurringAppointmentRequest)).thenReturn(newAppointment);
        when(newAppointment.getAppointmentRecurringPattern()).thenReturn(appointmentRecurringPattern);
        when(newAppointment.getRelatedAppointment()).thenReturn(appointment);
        when(appointmentRecurringPatternService.update(appointmentRecurringPattern, appointments)).thenReturn(updatedAppointment);
        RecurringAppointmentDefaultResponse defaultResponse = mock(RecurringAppointmentDefaultResponse.class);
        when(recurringAppointmentMapper.constructResponse(updatedAppointment)).thenReturn(defaultResponse);

        RecurringAppointmentDefaultResponse actualResponse =
                (RecurringAppointmentDefaultResponse) recurringAppointmentsController.editAppointment(recurringAppointmentRequest).getBody();

        verify(singleAppointmentRecurringPatternUpdateService).getUpdatedAppointment(recurringAppointmentRequest);
        verify(appointmentRecurringPatternService).update(appointmentRecurringPattern, appointments);
        verify(recurringAppointmentMapper).constructResponse(updatedAppointment);
        assertEquals(defaultResponse, actualResponse);
    }

    @Test
    public void shouldThrowAPIExceptionWhenRequestIsInvalidForEdit() {
        RecurringAppointmentRequest recurringAppointmentRequest = new RecurringAppointmentRequest();
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocationOnMock) {
                Object[] args = invocationOnMock.getArguments();
                Errors errors = (Errors) args[1];
                errors.reject("some error");
                return null;
            }
        }).when(recurringPatternValidator).validate(any(), any());

        recurringAppointmentsController.editAppointment(recurringAppointmentRequest);

        ResponseEntity<Object> responseEntity = recurringAppointmentsController.save(recurringAppointmentRequest);
        verify(allAppointmentRecurringPatternUpdateService, never()).getUpdatedRecurringPattern(any());
        verify(singleAppointmentRecurringPatternUpdateService, never()).getUpdatedAppointment(any());
        verify(appointmentRecurringPatternService, never()).validateAndSave(any());
        verify(recurringAppointmentMapper, never()).constructResponse(Collections.emptyList());
        assertEquals(responseEntity.getStatusCode(), HttpStatus.BAD_REQUEST);
        assertEquals(((Map) ((Map) responseEntity.getBody()).get("error")).get("message"), "some error");
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
}
