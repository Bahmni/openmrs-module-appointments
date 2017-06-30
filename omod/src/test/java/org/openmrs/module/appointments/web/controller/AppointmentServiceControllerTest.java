package org.openmrs.module.appointments.web.controller;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.openmrs.module.appointments.model.AppointmentService;
import org.openmrs.module.appointments.service.AppointmentServiceService;
import org.openmrs.module.appointments.web.contract.AppointmentServicePayload;
import org.openmrs.module.appointments.web.mapper.AppointmentServiceMapper;
import org.springframework.validation.BindingResult;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.powermock.api.mockito.PowerMockito.when;


public class AppointmentServiceControllerTest {

    @Mock
    private AppointmentServiceService appointmentServiceService;
    @Mock
    private AppointmentServiceMapper appointmentServiceMapper;

    @InjectMocks
    private AppointmentServiceController appointmentServiceController;

    @Before
    public void setUp() throws Exception {
        initMocks(this);
    }

    @Test
    public void shouldCreateAppointmentService() throws Exception {
        AppointmentServicePayload appointmentServicePayload = new AppointmentServicePayload();
        BindingResult result = mock(BindingResult.class);
        when(result.hasErrors()).thenReturn(false);
        appointmentServiceController.createAppointmentService(appointmentServicePayload);
        verify(appointmentServiceMapper, times(1)).getAppointmentServiceFromPayload(appointmentServicePayload);
        verify(appointmentServiceService, times(1)).save(any(AppointmentService.class));
    }

    @Test
    public void shouldGetAllAppointmentServices() throws Exception {
        appointmentServiceController.getAllAppointmentServices();
        verify(appointmentServiceService, times(1)).getAllAppointmentServices();
    }
}
