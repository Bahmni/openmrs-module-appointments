package org.openmrs.module.appointments.web.controller;

import java.util.ArrayList;
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
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.MockitoAnnotations.initMocks;
import org.openmrs.module.appointments.model.Appointment;
import org.openmrs.module.appointments.model.AppointmentServiceType;
import org.openmrs.module.appointments.service.AppointmentServiceService;
import org.openmrs.module.appointments.service.AppointmentsService;
import org.openmrs.module.appointments.util.DateUtil;
import org.openmrs.module.appointments.web.contract.AppointmentDefaultResponse;
import org.openmrs.module.appointments.web.mapper.AppointmentMapper;
import static org.powermock.api.mockito.PowerMockito.when;


public class AppointmentControllerTest {

    @Mock
    private AppointmentsService appointmentsService;

    @Mock
    private AppointmentServiceService appointmentServiceService;

    @Mock
    private AppointmentMapper appointmentMapper;

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
}
