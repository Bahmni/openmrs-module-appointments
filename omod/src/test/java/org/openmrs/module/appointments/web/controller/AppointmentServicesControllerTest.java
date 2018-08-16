package org.openmrs.module.appointments.web.controller;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.openmrs.module.appointments.model.AppointmentService;
import org.openmrs.module.appointments.service.AppointmentServiceService;
import org.openmrs.module.appointments.web.mapper.AppointmentServiceMapper;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.powermock.api.mockito.PowerMockito.when;

public class AppointmentServicesControllerTest {

    @Mock
    private AppointmentServiceService appointmentServiceService;

    @Mock
    private AppointmentServiceMapper appointmentServiceMapper;

    @InjectMocks
    private AppointmentServicesController appointmentServicesController;


    @Before
    public void setUp() throws Exception {
        initMocks(this);
    }

    @Test
    public void shouldGetASpecificAppointmentService() {
        String uuid = "randomServiceUuid";
        AppointmentService appointmentService = new AppointmentService();
        appointmentService.setUuid(uuid);
        when(appointmentServiceService.getAppointmentServiceByUuid(uuid)).thenReturn(appointmentService);

        appointmentServicesController.getAppointmentServiceByUuid(uuid);
        verify(appointmentServiceService, times(1)).getAppointmentServiceByUuid(uuid);
        verify(appointmentServiceMapper, times(1)).constructResponse(appointmentService);
    }

    @Test(expected = RuntimeException.class)
    public void shouldThrowErrorIf() {
        AppointmentService appointmentService = new AppointmentService();
        when(appointmentServiceService.getAppointmentServiceByUuid("randomUuid")).thenReturn(null);
        appointmentServicesController.getAppointmentServiceByUuid("randomUuid");
    }

}
