package org.openmrs.module.appointments.service.impl;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.openmrs.User;
import org.openmrs.api.context.Context;
import org.openmrs.module.appointments.dao.AppointmentServiceDao;
import org.openmrs.module.appointments.model.Appointment;
import org.openmrs.module.appointments.model.AppointmentService;
import org.openmrs.module.appointments.model.AppointmentServiceType;
import org.openmrs.module.appointments.model.ServiceWeeklyAvailability;
import org.openmrs.module.appointments.service.AppointmentsService;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.sql.Time;
import java.time.DayOfWeek;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mockStatic;

@PrepareForTest({Context.class})
@RunWith(PowerMockRunner.class)
public class AppointmentServiceServiceImplTest{

    @Captor
    private ArgumentCaptor<AppointmentService> captor;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Mock
    private AppointmentServiceDao appointmentServiceDao;

    @Mock
    private AppointmentsService appointmentsService;

    @InjectMocks
    AppointmentServiceServiceImpl appointmentServiceService;

    private User authenticatedUser;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        mockStatic(Context.class);
        authenticatedUser = new User(8);
        PowerMockito.when(Context.getAuthenticatedUser()).thenReturn(authenticatedUser);
    }

    @Test
    public void testCreateAppointmentService() throws Exception {
        AppointmentService appointmentService = new AppointmentService();
        appointmentService.setName("Cardiology OPD");
        appointmentServiceService.save(appointmentService);
        Mockito.verify(appointmentServiceDao, times(1)).save(appointmentService);
    }

    @Test
    public void testGetAllAppointmentServices() throws Exception {
        appointmentServiceService.getAllAppointmentServices(false);
        Mockito.verify(appointmentServiceDao, times(1)).getAllAppointmentServices(false);
    }

    @Test
    public void testGetAppointmentsByUuid() throws Exception {
        appointmentServiceService.getAppointmentServiceByUuid("uuid");
        Mockito.verify(appointmentServiceDao, times(1)).getAppointmentServiceByUuid("uuid");
    }

    @Test
    public void shouldVoidTheAppointmentService() throws Exception {
        String voidReason = "voidReason";
        AppointmentService appointmentService = new AppointmentService();
        appointmentService.setUuid("uuid");
        appointmentService.setName("name");

        appointmentServiceService.voidAppointmentService(appointmentService, voidReason);

        Mockito.verify(appointmentServiceDao, times(1)).save(captor.capture());
        Assert.assertEquals(captor.getValue().getVoided(), true);
        Assert.assertNotNull(captor.getValue().getDateVoided());
        Assert.assertEquals(captor.getValue().getVoidedBy(), authenticatedUser);
        Assert.assertEquals(captor.getValue().getVoidReason(), voidReason);
    }


    @Test
    public void shouldVoidTheAppointmentServiceAlongWithServiceAvailability() throws Exception {
        String voidReason = "voidReason";
        AppointmentService appointmentService = new AppointmentService();
        appointmentService.setUuid("uuid");
        appointmentService.setName("name");
        ServiceWeeklyAvailability serviceWeeklyAvailability1 = new ServiceWeeklyAvailability();
        serviceWeeklyAvailability1.setId(1);
        serviceWeeklyAvailability1.setStartTime(Time.valueOf("10:10:10"));
        serviceWeeklyAvailability1.setEndTime(Time.valueOf("12:12:12"));
        serviceWeeklyAvailability1.setDayOfWeek(DayOfWeek.MONDAY);
        ServiceWeeklyAvailability serviceWeeklyAvailability2 = new ServiceWeeklyAvailability();
        serviceWeeklyAvailability2.setId(2);
        serviceWeeklyAvailability2.setStartTime(Time.valueOf("10:10:10"));
        serviceWeeklyAvailability2.setEndTime(Time.valueOf("12:12:12"));
        serviceWeeklyAvailability2.setDayOfWeek(DayOfWeek.MONDAY);
        Set<ServiceWeeklyAvailability> weeklyAvailability = new LinkedHashSet<>();
        weeklyAvailability.add(serviceWeeklyAvailability1);
        weeklyAvailability.add(serviceWeeklyAvailability2);
        appointmentService.setWeeklyAvailability(weeklyAvailability);


        appointmentServiceService.voidAppointmentService(appointmentService, voidReason);

        Mockito.verify(appointmentServiceDao, times(1)).save(captor.capture());
        Assert.assertEquals(true, captor.getValue().getVoided());
        Assert.assertNotNull(captor.getValue().getDateVoided());
        Assert.assertEquals(authenticatedUser, captor.getValue().getVoidedBy());
        Assert.assertEquals(voidReason, captor.getValue().getVoidReason());

        Iterator<ServiceWeeklyAvailability> iterator = captor.getValue().getWeeklyAvailability(true).iterator();
        ServiceWeeklyAvailability firstWeeklyAvailability = iterator.next();
        ServiceWeeklyAvailability secondWeeklyAvailability = iterator.next();

        Assert.assertEquals( 1, firstWeeklyAvailability.getId(), 0);
        Assert.assertEquals( true, firstWeeklyAvailability.getVoided());
        Assert.assertEquals( voidReason, firstWeeklyAvailability.getVoidReason());
        Assert.assertEquals( authenticatedUser, firstWeeklyAvailability.getVoidedBy());
        Assert.assertNotNull(firstWeeklyAvailability.getVoidedBy());

        Assert.assertEquals( 2, secondWeeklyAvailability.getId(), 0);
        Assert.assertEquals( true, secondWeeklyAvailability.getVoided());
        Assert.assertEquals( voidReason, secondWeeklyAvailability.getVoidReason());
        Assert.assertEquals( authenticatedUser, secondWeeklyAvailability.getVoidedBy());
        Assert.assertNotNull(secondWeeklyAvailability.getVoidedBy());
    }

    @Test
    public void shouldVoidTheAppointmentServiceAlongWithServiceTypes() throws Exception {
        String voidReason = "voidReason";
        AppointmentService appointmentService = new AppointmentService();
        appointmentService.setUuid("uuid");
        appointmentService.setName("name");
        AppointmentServiceType appointmentServiceType1 = new AppointmentServiceType();
        AppointmentServiceType appointmentServiceType2 = new AppointmentServiceType();
        appointmentServiceType1.setId(1);
        appointmentServiceType1.setName("type1");
        appointmentServiceType2.setId(2);
        appointmentServiceType2.setName("type2");

        Set<AppointmentServiceType> serviceTypes = new LinkedHashSet<>();
        serviceTypes.add(appointmentServiceType1);
        serviceTypes.add(appointmentServiceType2);

        appointmentService.setServiceTypes(serviceTypes);


        appointmentServiceService.voidAppointmentService(appointmentService, voidReason);

        Mockito.verify(appointmentServiceDao, times(1)).save(captor.capture());
        Assert.assertEquals(true, captor.getValue().getVoided());
        Assert.assertNotNull(captor.getValue().getDateVoided());
        Assert.assertEquals(authenticatedUser, captor.getValue().getVoidedBy());
        Assert.assertEquals(voidReason, captor.getValue().getVoidReason());

        Iterator<AppointmentServiceType> iterator = captor.getValue().getServiceTypes().iterator();
        AppointmentServiceType serviceType1 = iterator.next();
        AppointmentServiceType serviceType2 = iterator.next();

        Assert.assertEquals( 1, serviceType1.getId(), 0);
        Assert.assertEquals( true, serviceType1.getVoided());
        Assert.assertEquals( voidReason, serviceType1.getVoidReason());
        Assert.assertEquals( authenticatedUser, serviceType1.getVoidedBy());
        Assert.assertNotNull(serviceType1.getVoidedBy());

        Assert.assertEquals( 2, serviceType2.getId(), 0);
        Assert.assertEquals( true, serviceType2.getVoided());
        Assert.assertEquals( voidReason, serviceType2.getVoidReason());
        Assert.assertEquals( authenticatedUser, serviceType2.getVoidedBy());
        Assert.assertNotNull(serviceType2.getVoidedBy());
    }

    @Test
    public void shouldValidateTheAppointmentServiceAndThrowAnExceptionWhenThereIsNonVoidedAppointmentServiceWithTheSameName() throws Exception {
        String serviceName = "serviceName";
        AppointmentService appointmentService = new AppointmentService();
        appointmentService.setUuid("uuid");
        appointmentService.setName(serviceName);
        AppointmentServiceType appointmentServiceType = new AppointmentServiceType();
        appointmentServiceType.setId(1);
        appointmentServiceType.setName("type1");
        LinkedHashSet<AppointmentServiceType> serviceTypes = new LinkedHashSet<>();
        serviceTypes.add(appointmentServiceType);
        appointmentService.setServiceTypes(serviceTypes);
        when(appointmentServiceDao.getNonVoidedAppointmentServiceByName(serviceName)).thenReturn(appointmentService);

        expectedException.expect(RuntimeException.class);
        expectedException.expectMessage("The service 'serviceName' is already present");

        appointmentServiceService.save(appointmentService);
    }

    @Test
    public void shouldThrowAnExceptionWhenAppointmentServiceHasFutureAppointments() throws Exception {
        String voidReason = "voidReason";
        AppointmentService appointmentService = new AppointmentService();
        appointmentService.setUuid("uuid");
        appointmentService.setName("name");

        ArrayList<Appointment> appointments = new ArrayList<>();
        Appointment appointment = new Appointment();
        appointment.setUuid("appointmentUuin");
        appointments.add(appointment);
        when(appointmentsService.getAllFutureAppointmentsForService(appointmentService)).thenReturn(appointments);

        expectedException.expect(RuntimeException.class);
        expectedException.expectMessage("Please cancel all future appointments for this service to proceed. After deleting this service, you will not be able to see any appointments for it");

        appointmentServiceService.voidAppointmentService(appointmentService, voidReason);

        Mockito.verify(appointmentsService, times(1)).getAllFutureAppointmentsForService(appointmentService);
        Mockito.verify(appointmentServiceDao, times(0)).save(captor.capture());
    }
}