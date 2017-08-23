package org.openmrs.module.appointments.web.controller;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.openmrs.module.appointments.model.AppointmentService;
import org.openmrs.module.appointments.service.AppointmentServiceService;
import org.openmrs.module.appointments.util.DateUtil;
import org.openmrs.module.appointments.web.contract.AppointmentServiceFullResponse;
import org.openmrs.module.appointments.web.contract.AppointmentServicePayload;
import org.openmrs.module.appointments.web.mapper.AppointmentServiceMapper;
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
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

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Before
    public void setUp() throws Exception {
        initMocks(this);
    }

    @Test
    public void shouldCreateAppointmentService() throws Exception {
        AppointmentServicePayload appointmentServicePayload = new AppointmentServicePayload();
        appointmentServicePayload.setName("Cardio");
        AppointmentService mappedServicePayload = new AppointmentService();
        when(appointmentServiceMapper.getAppointmentServiceFromPayload(appointmentServicePayload)).thenReturn(mappedServicePayload);
        when(appointmentServiceService.save(mappedServicePayload)).thenReturn(mappedServicePayload);
        AppointmentServiceFullResponse response = new AppointmentServiceFullResponse();
        when(appointmentServiceMapper.constructResponse(mappedServicePayload)).thenReturn(response);

        ResponseEntity<Object> savedAppointmentService = appointmentServiceController.createAppointmentService(appointmentServicePayload);

        verify(appointmentServiceMapper, times(1)).getAppointmentServiceFromPayload(appointmentServicePayload);
        verify(appointmentServiceService, times(1)).save(any(AppointmentService.class));
        verify(appointmentServiceMapper, times(1)).constructResponse(mappedServicePayload);
        assertNotNull(savedAppointmentService);
        assertEquals(response, savedAppointmentService.getBody());
        assertEquals("200", savedAppointmentService.getStatusCode().toString());
    }

    @Test
    public void shouldThrowExceptionWhenCreateAppointmentServiceWhenEmptyPayloadIsGiven() throws Exception {
        AppointmentServicePayload appointmentServicePayload = new AppointmentServicePayload();

        expectedException.expect(RuntimeException.class);
        expectedException.expectMessage("Appointment Service name should not be null");

        appointmentServiceController.createAppointmentService(appointmentServicePayload);
    }

    @Test
    public void shouldValidateTheAppointmentServiceAndThrowAnExceptionWhenThereIsNonVoidedAppointmentServiceWithTheSameName() throws Exception {
        AppointmentServicePayload appointmentServicePayload = new AppointmentServicePayload();
        appointmentServicePayload.setName("Cardio");
        AppointmentService mappedServicePayload = new AppointmentService();
        when(appointmentServiceMapper.getAppointmentServiceFromPayload(appointmentServicePayload)).thenReturn(mappedServicePayload);
        when(appointmentServiceService.save(mappedServicePayload)).thenThrow(new RuntimeException("The service 'Cardio' is already present"));

        ResponseEntity<Object> appointmentService = appointmentServiceController.createAppointmentService(appointmentServicePayload);

        assertNotNull(appointmentService);
        assertEquals("400", appointmentService.getStatusCode().toString());
        assertEquals("The service 'Cardio' is already present", ((RuntimeException)appointmentService.getBody()).getMessage());
    }

    @Test
    public void shouldGetAllAppointmentServices() throws Exception {
        AppointmentService appointmentService = new AppointmentService();
        List<AppointmentService> appointmentServiceList = new ArrayList<>();
        appointmentServiceList.add(appointmentService);
        when(appointmentServiceService.getAllAppointmentServices(false)).thenReturn(appointmentServiceList);
        
        appointmentServiceController.getAllAppointmentServices();
        verify(appointmentServiceService, times(1)).getAllAppointmentServices(false);
        verify(appointmentServiceMapper, times(1)).constructDefaultResponseForServiceList(appointmentServiceList);
    }
    
    @Test
    public void shouldGetAppointmentServiceByUUID() throws Exception {
        String uuid = "c36006d4-9fbb-4f20-866b-0ece245615b1";
        AppointmentService appointmentService = new AppointmentService();
        appointmentService.setUuid(uuid);
        when(appointmentServiceService.getAppointmentServiceByUuid(uuid)).thenReturn(appointmentService);
        
        appointmentServiceController.getAppointmentServiceByUuid(uuid);
        verify(appointmentServiceService, times(1)).getAppointmentServiceByUuid(uuid);
        verify(appointmentServiceMapper, times(1)).constructResponse(appointmentService);
    }
    
    @Test(expected = RuntimeException.class)
    public void shouldThrowExceptionIfServiceNotFound() throws Exception {
        appointmentServiceController.getAppointmentServiceByUuid("random");
        verify(appointmentServiceService, times(1)).getAppointmentServiceByUuid("random");
    }

    @Test
    public void shouldVoidTheAppointmentService() throws Exception {
        String appointmentServiceUuid = "appointmentServiceUuid";
        String voidReason = "voidReason";
        AppointmentService appointmentService = new AppointmentService();
        appointmentService.setUuid(appointmentServiceUuid);
        appointmentService.setName("serviceName");
        when(appointmentServiceService.getAppointmentServiceByUuid(appointmentServiceUuid)).thenReturn(appointmentService);
        when(appointmentServiceService.voidAppointmentService(appointmentService, voidReason)).thenReturn(appointmentService);
        AppointmentServiceFullResponse appointmentServiceFullResponse = new AppointmentServiceFullResponse();
        when(appointmentServiceMapper.constructResponse(appointmentService)).thenReturn(appointmentServiceFullResponse);

        ResponseEntity<Object> response = appointmentServiceController.voidAppointmentService(appointmentServiceUuid, voidReason);

        verify(appointmentServiceService, times(1)).getAppointmentServiceByUuid(appointmentServiceUuid);
        verify(appointmentServiceService, times(1)).voidAppointmentService(appointmentService, voidReason);
        verify(appointmentServiceMapper, times(1)).constructResponse(appointmentService);
        assertNotNull(response);
        assertEquals(appointmentServiceFullResponse, response.getBody());
    }

    @Test
    public void shouldVoidTheAppointmentServiceBeIdempotent() throws Exception {
        String appointmentServiceUuid = "appointmentServiceUuid";
        String voidReason = "voidReason";
        AppointmentService appointmentService = new AppointmentService();
        appointmentService.setUuid(appointmentServiceUuid);
        appointmentService.setName("serviceName");
        appointmentService.setVoided(true);
        when(appointmentServiceService.getAppointmentServiceByUuid(appointmentServiceUuid)).thenReturn(appointmentService);
        when(appointmentServiceService.voidAppointmentService(appointmentService, voidReason)).thenReturn(appointmentService);
        AppointmentServiceFullResponse appointmentServiceFullResponse = new AppointmentServiceFullResponse();
        when(appointmentServiceMapper.constructResponse(appointmentService)).thenReturn(appointmentServiceFullResponse);

        ResponseEntity<Object> response = appointmentServiceController.voidAppointmentService(appointmentServiceUuid, voidReason);

        verify(appointmentServiceService, times(1)).getAppointmentServiceByUuid(appointmentServiceUuid);
        verify(appointmentServiceService, times(0)).voidAppointmentService(appointmentService, voidReason);
        verify(appointmentServiceMapper, times(1)).constructResponse(appointmentService);
        assertNotNull(response);
        assertEquals(appointmentServiceFullResponse, response.getBody());
    }

    @Test
    public void shouldThrowAnExceptionWhenThereAreFutureAppointmentsForTheServiceTryingToDelete() throws Exception {
        String appointmentServiceUuid = "appointmentServiceUuid";
        String voidReason = "voidReason";
        AppointmentService appointmentService = new AppointmentService();
        appointmentService.setUuid(appointmentServiceUuid);
        appointmentService.setName("serviceName");
        when(appointmentServiceService.getAppointmentServiceByUuid(appointmentServiceUuid)).thenReturn(appointmentService);
        String exceptionMessage = "Please cancel all future appointments for this service to proceed. After deleting this service, you will not be able to see any appointments for it";
        when(appointmentServiceService.voidAppointmentService(appointmentService, voidReason)).thenThrow(new RuntimeException(exceptionMessage));

        ResponseEntity<Object> response = appointmentServiceController.voidAppointmentService(appointmentServiceUuid, voidReason);

        verify(appointmentServiceService, times(1)).getAppointmentServiceByUuid(appointmentServiceUuid);
        verify(appointmentServiceService, times(1)).voidAppointmentService(appointmentService, voidReason);
        verify(appointmentServiceMapper, times(0)).constructResponse(appointmentService);
        assertNotNull(response);
        assertEquals(exceptionMessage, ((RuntimeException)response.getBody()).getMessage());
    }

    @Test
    public void shouldGetLoad() throws Exception {
        String appointmentServiceUuid = "appointmentServiceUuid";
        AppointmentService appointmentService = new AppointmentService();
        appointmentService.setUuid(appointmentServiceUuid);
        appointmentService.setName("serviceName");
        when(appointmentServiceService.getAppointmentServiceByUuid(appointmentServiceUuid)).thenReturn(appointmentService);
        String startDateString = "2108-08-14T18:30:00.0Z";
        String endDateString = "2108-08-15T18:30:00.0Z";
        Date startDate = DateUtil.convertToLocalDateFromUTC(startDateString);
        Date endDate = DateUtil.convertToLocalDateFromUTC(endDateString);
        when(appointmentServiceService.calculateCurrentLoad(appointmentService, startDate, endDate)).thenReturn(new Integer(45));
        Integer response = appointmentServiceController.calculateLoadForService(appointmentServiceUuid, startDateString, endDateString);
        verify(appointmentServiceService, times(1)).calculateCurrentLoad(appointmentService, startDate, endDate);
        assertNotNull(response);
        assertEquals(45, response.intValue());
    }

    @Test
    public void shouldGetAppointmentServiceWithSpecialityAndAppointmentServiceTypesInIt() {
        AppointmentService appointmentService = new AppointmentService();
        List<AppointmentService> appointmentServiceList = new ArrayList<>();
        appointmentServiceList.add(appointmentService);
        when(appointmentServiceService.getAllAppointmentServices(false)).thenReturn(appointmentServiceList);

        List<AppointmentServiceFullResponse> allAppointmentServicesWithTypes = appointmentServiceController.getAllAppointmentServicesWithTypes();
        verify(appointmentServiceService, times(1)).getAllAppointmentServices(false);
        verify(appointmentServiceMapper, times(1)).constructFullResponseForServiceList(appointmentServiceList);
    }
}
