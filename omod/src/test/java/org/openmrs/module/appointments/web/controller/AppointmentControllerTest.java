package org.openmrs.module.appointments.web.controller;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.MockitoAnnotations.initMocks;
import org.openmrs.module.appointments.model.Appointment;
import org.openmrs.module.appointments.model.AppointmentService;
import org.openmrs.module.appointments.model.AppointmentServiceType;
import org.openmrs.module.appointments.model.AppointmentStatus;
import org.openmrs.module.appointments.service.AppointmentServiceService;
import org.openmrs.module.appointments.service.AppointmentsService;
import org.openmrs.module.appointments.util.DateUtil;
import org.openmrs.module.appointments.web.contract.AppointmentCount;
import org.openmrs.module.appointments.web.contract.AppointmentDefaultResponse;
import org.openmrs.module.appointments.web.contract.AppointmentServiceDefaultResponse;
import org.openmrs.module.appointments.web.contract.AppointmentsSummary;
import org.openmrs.module.appointments.web.mapper.AppointmentMapper;
import org.openmrs.module.appointments.web.mapper.AppointmentServiceMapper;

import static org.powermock.api.mockito.PowerMockito.when;


public class AppointmentControllerTest {

    @Mock
    private AppointmentsService appointmentsService;

    @Mock
    private AppointmentServiceService appointmentServiceService;

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
        when(appointmentServiceService.getAppointmentServiceTypeByUuid(serviceTypeUuid)).thenReturn(appointmentServiceType);
        ArrayList<AppointmentDefaultResponse> responses = new ArrayList<>();
        AppointmentDefaultResponse defaultResponse = new AppointmentDefaultResponse();
        defaultResponse.setUuid(appointment.getUuid());
        responses.add(defaultResponse);
        when(appointmentMapper.constructResponse(appointments)).thenReturn(responses);

        List<AppointmentDefaultResponse> appointmentDefaultResponses = appointmentController.getAllFututreAppointmentsForGivenServiceType(serviceTypeUuid);

        verify(appointmentServiceService, times(1)).getAppointmentServiceTypeByUuid(serviceTypeUuid);
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
        String startDateString = "2017-08-15T00:00:00.0Z";
        String endDateString = "2017-08-22T00:00:00.0Z";
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Date startDate = simpleDateFormat.parse(startDateString);
        Date endDate = simpleDateFormat.parse(endDateString);
        AppointmentService appointmentService = new AppointmentService();
        appointmentService.setName("Ortho");
        appointmentService.setUuid("someUuid");
        List<AppointmentService> appointmentServices = new ArrayList<>();
        appointmentServices.add(appointmentService);
        Appointment appointment = new Appointment();
        appointment.setUuid("apptUuid");
        appointment.setService(appointmentService);
        appointment.setStartDateTime(startDate);
        appointment.setStatus(AppointmentStatus.Scheduled);
        List<Appointment> appointmentList = new ArrayList<>();
        appointmentList.add(appointment);
        List<AppointmentStatus> appointmentStatuses = Arrays.asList(
                AppointmentStatus.Completed,
                AppointmentStatus.Scheduled,
                AppointmentStatus.Missed);
        AppointmentServiceDefaultResponse appointmentServiceDefaultResponse = new AppointmentServiceDefaultResponse();
        appointmentServiceDefaultResponse.setUuid("someUuid");

        when(appointmentServiceService.getAllAppointmentServices(false)).thenReturn(appointmentServices);
        when(appointmentsService.getAppointmentsForService(appointmentService, startDate, endDate, appointmentStatuses)).thenReturn(appointmentList);
        when(appointmentServiceMapper.constructDefaultResponse(appointmentService)).thenReturn(appointmentServiceDefaultResponse);

        List<AppointmentsSummary> allAppointmentsSummary = appointmentController.getAllAppointmentsSummary(startDateString, endDateString);
        verify(appointmentServiceService, times(1)).getAllAppointmentServices(false);
        verify(appointmentsService, times(1)).getAppointmentsForService(appointmentService, startDate, endDate, appointmentStatuses);
        assertEquals(1, allAppointmentsSummary.size());
        assertEquals("someUuid", allAppointmentsSummary.get(0).getAppointmentService().getUuid());
        assertEquals(1, allAppointmentsSummary.get(0).getAppointmentCountList().size());
        AppointmentCount appointmentCount = (AppointmentCount)allAppointmentsSummary.get(0).getAppointmentCountList().get(0);
        assertEquals(1, appointmentCount.getAllAppointmentsCount(), 0);
        assertEquals(0, appointmentCount.getMissedAppointmentsCount(), 0);
        assertEquals(startDate, appointmentCount.getAppointmentDate());
        assertEquals("someUuid", appointmentCount.getAppointmentServiceUuid());
    }
}
