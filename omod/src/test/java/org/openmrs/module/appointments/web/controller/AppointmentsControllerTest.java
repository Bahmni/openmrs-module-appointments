package org.openmrs.module.appointments.web.controller;


import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.openmrs.module.appointments.model.Appointment;
import org.openmrs.module.appointments.service.AppointmentsService;
import org.openmrs.module.appointments.web.mapper.AppointmentMapper;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.powermock.api.mockito.PowerMockito.when;

public class AppointmentsControllerTest {

    @Mock
    private AppointmentsService appointmentsService;

    @Mock
    private AppointmentMapper appointmentMapper;


    @InjectMocks
    private AppointmentsController appointmentsController;

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
}
