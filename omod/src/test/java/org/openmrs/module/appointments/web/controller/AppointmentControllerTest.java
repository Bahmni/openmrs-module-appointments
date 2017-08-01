package org.openmrs.module.appointments.web.controller;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.openmrs.module.appointments.model.Appointment;
import org.openmrs.module.appointments.model.AppointmentService;
import org.openmrs.module.appointments.model.AppointmentServiceType;
import org.openmrs.module.appointments.service.AppointmentServiceService;
import org.openmrs.module.appointments.service.AppointmentsService;
import org.openmrs.module.appointments.web.contract.AppointmentDefaultResponse;
import org.openmrs.module.appointments.web.mapper.AppointmentMapper;
import org.openmrs.module.appointments.web.mapper.AppointmentServiceMapper;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.MockitoAnnotations.initMocks;
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
    public void shouldGetAllFututreAppointmentsForGivenServiceType() throws Exception {
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
}
