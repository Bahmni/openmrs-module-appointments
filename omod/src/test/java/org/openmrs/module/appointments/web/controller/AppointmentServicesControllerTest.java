package org.openmrs.module.appointments.web.controller;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.openmrs.module.appointments.model.AppointmentServiceDefinition;
import org.openmrs.module.appointments.service.AppointmentServiceDefinitionService;
import org.openmrs.module.appointments.web.mapper.AppointmentServiceMapper;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.powermock.api.mockito.PowerMockito.when;

public class AppointmentServicesControllerTest {

    @Mock
    private AppointmentServiceDefinitionService appointmentServiceDefinitionService;

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
        AppointmentServiceDefinition appointmentServiceDef = new AppointmentServiceDefinition();
        appointmentServiceDef.setUuid(uuid);
        when(appointmentServiceDefinitionService.getAppointmentServiceByUuid(uuid)).thenReturn(appointmentServiceDef);

        appointmentServicesController.getAppointmentServiceByUuid(uuid);
        verify(appointmentServiceDefinitionService, times(1)).getAppointmentServiceByUuid(uuid);
        verify(appointmentServiceMapper, times(1)).constructResponse(appointmentServiceDef);
    }

    @Test(expected = RuntimeException.class)
    public void shouldThrowErrorIf() {
        AppointmentServiceDefinition appointmentServiceDef = new AppointmentServiceDefinition();
        when(appointmentServiceDefinitionService.getAppointmentServiceByUuid("randomUuid")).thenReturn(null);
        appointmentServicesController.getAppointmentServiceByUuid("randomUuid");
    }

}
