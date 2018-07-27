package org.openmrs.module.appointments.service.impl;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.openmrs.User;
import org.openmrs.api.context.Context;
import org.openmrs.module.appointments.dao.AppointmentServiceDao;
import org.openmrs.module.appointments.model.*;
import org.openmrs.module.appointments.service.AppointmentsService;
import org.openmrs.module.appointments.util.DateUtil;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.sql.Time;
import java.time.DayOfWeek;
import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mockStatic;

@PrepareForTest({Context.class})
@RunWith(PowerMockRunner.class)
public class AppointmentServiceDefinitionServiceImplTest {

    @Captor
    private ArgumentCaptor<AppointmentServiceDefinition> captor;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Mock
    private AppointmentServiceDao appointmentServiceDao;

    @Mock
    private AppointmentsService appointmentsService;

    @InjectMocks
    AppointmentServiceDefinitionServiceImpl appointmentServiceService;

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
        AppointmentServiceDefinition appointmentServiceDefinition = new AppointmentServiceDefinition();
        appointmentServiceDefinition.setName("Cardiology OPD");
        appointmentServiceService.save(appointmentServiceDefinition);
        Mockito.verify(appointmentServiceDao, times(1)).save(appointmentServiceDefinition);
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
        AppointmentServiceDefinition appointmentServiceDefinition = new AppointmentServiceDefinition();
        appointmentServiceDefinition.setUuid("uuid");
        appointmentServiceDefinition.setName("name");

        appointmentServiceService.voidAppointmentService(appointmentServiceDefinition, voidReason);

        Mockito.verify(appointmentServiceDao, times(1)).save(captor.capture());
        assertEquals(captor.getValue().getVoided(), true);
        assertNotNull(captor.getValue().getDateVoided());
        assertEquals(captor.getValue().getVoidedBy(), authenticatedUser);
        assertEquals(captor.getValue().getVoidReason(), voidReason);
    }


    @Test
    public void shouldVoidTheAppointmentServiceAlongWithServiceAvailability() throws Exception {
        String voidReason = "voidReason";
        AppointmentServiceDefinition appointmentServiceDefinition = new AppointmentServiceDefinition();
        appointmentServiceDefinition.setUuid("uuid");
        appointmentServiceDefinition.setName("name");
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
        appointmentServiceDefinition.setWeeklyAvailability(weeklyAvailability);


        appointmentServiceService.voidAppointmentService(appointmentServiceDefinition, voidReason);

        Mockito.verify(appointmentServiceDao, times(1)).save(captor.capture());
        assertEquals(true, captor.getValue().getVoided());
        assertNotNull(captor.getValue().getDateVoided());
        assertEquals(authenticatedUser, captor.getValue().getVoidedBy());
        assertEquals(voidReason, captor.getValue().getVoidReason());
        List toSort = new ArrayList<>(captor.getValue().getWeeklyAvailability(true));
        Collections.sort(toSort, new Comparator<ServiceWeeklyAvailability>() {
            public int compare(ServiceWeeklyAvailability o1, ServiceWeeklyAvailability o2) {

                return (o1.getId() > o2.getId() ? 1 : -1);
            }
        });
        Iterator<ServiceWeeklyAvailability> iterator = toSort.iterator();
        ServiceWeeklyAvailability firstWeeklyAvailability = iterator.next();
        ServiceWeeklyAvailability secondWeeklyAvailability = iterator.next();

        assertEquals( 1, firstWeeklyAvailability.getId(), 0);
        assertEquals( true, firstWeeklyAvailability.getVoided());
        assertEquals( voidReason, firstWeeklyAvailability.getVoidReason());
        assertEquals( authenticatedUser, firstWeeklyAvailability.getVoidedBy());
        assertNotNull(firstWeeklyAvailability.getVoidedBy());

        assertEquals( 2, secondWeeklyAvailability.getId(), 0);
        assertEquals( true, secondWeeklyAvailability.getVoided());
        assertEquals( voidReason, secondWeeklyAvailability.getVoidReason());
        assertEquals( authenticatedUser, secondWeeklyAvailability.getVoidedBy());
        assertNotNull(secondWeeklyAvailability.getVoidedBy());
    }

    @Test
    public void shouldVoidTheAppointmentServiceAlongWithServiceTypes() throws Exception {
        String voidReason = "voidReason";
        AppointmentServiceDefinition appointmentServiceDefinition = new AppointmentServiceDefinition();
        appointmentServiceDefinition.setUuid("uuid");
        appointmentServiceDefinition.setName("name");
        AppointmentServiceType appointmentServiceType1 = new AppointmentServiceType();
        AppointmentServiceType appointmentServiceType2 = new AppointmentServiceType();
        appointmentServiceType1.setId(1);
        appointmentServiceType1.setName("type1");
        appointmentServiceType2.setId(2);
        appointmentServiceType2.setName("type2");

        Set<AppointmentServiceType> serviceTypes = new LinkedHashSet<>();
        serviceTypes.add(appointmentServiceType1);
        serviceTypes.add(appointmentServiceType2);

        appointmentServiceDefinition.setServiceTypes(serviceTypes);


        appointmentServiceService.voidAppointmentService(appointmentServiceDefinition, voidReason);

        Mockito.verify(appointmentServiceDao, times(1)).save(captor.capture());
        assertEquals(true, captor.getValue().getVoided());
        assertNotNull(captor.getValue().getDateVoided());
        assertEquals(authenticatedUser, captor.getValue().getVoidedBy());
        assertEquals(voidReason, captor.getValue().getVoidReason());

        serviceTypes = captor.getValue().getServiceTypes(true);
        List toSort = new ArrayList<>(serviceTypes);
        Collections.sort(toSort, new Comparator<AppointmentServiceType>() {
            public int compare(AppointmentServiceType o1, AppointmentServiceType o2) {

                return (o1.getId() > o2.getId() ? 1 : -1);
            }
        });
        Iterator<AppointmentServiceType> iterator = toSort.iterator();
        AppointmentServiceType serviceType1 = iterator.next();
        AppointmentServiceType serviceType2 = iterator.next();

        assertEquals( 1, serviceType1.getId().intValue());
        assertEquals( true, serviceType1.getVoided());
        assertEquals( voidReason, serviceType1.getVoidReason());
        assertEquals( authenticatedUser, serviceType1.getVoidedBy());
        assertNotNull(serviceType1.getVoidedBy());

        assertEquals( 2, serviceType2.getId().intValue());
        assertEquals( true, serviceType2.getVoided());
        assertEquals( voidReason, serviceType2.getVoidReason());
        assertEquals( authenticatedUser, serviceType2.getVoidedBy());
        assertNotNull(serviceType2.getVoidedBy());
    }

    @Test
    public void shouldValidateTheAppointmentServiceAndThrowAnExceptionWhenThereIsNonVoidedAppointmentServiceWithTheSameName() throws Exception {
        String serviceName = "serviceName";
        AppointmentServiceDefinition existingAppointmentServiceDefinition = new AppointmentServiceDefinition();
        existingAppointmentServiceDefinition.setUuid("uuid");
        existingAppointmentServiceDefinition.setName(serviceName);
        AppointmentServiceType appointmentServiceType = new AppointmentServiceType();
        appointmentServiceType.setId(1);
        appointmentServiceType.setName("type1");
        LinkedHashSet<AppointmentServiceType> serviceTypes = new LinkedHashSet<>();
        serviceTypes.add(appointmentServiceType);
        existingAppointmentServiceDefinition.setServiceTypes(serviceTypes);
        when(appointmentServiceDao.getNonVoidedAppointmentServiceByName(serviceName)).thenReturn(existingAppointmentServiceDefinition);

        expectedException.expect(RuntimeException.class);
        expectedException.expectMessage("The service 'serviceName' is already present");
        AppointmentServiceDefinition appointmentServiceDefinition = new AppointmentServiceDefinition();
        appointmentServiceDefinition.setName(serviceName);
        appointmentServiceDefinition.setUuid("otherUuid");
    
        appointmentServiceService.save(appointmentServiceDefinition);
    }

    @Test
    public void shouldThrowAnExceptionWhenAppointmentServiceHasFutureAppointments() throws Exception {
        String voidReason = "voidReason";
        AppointmentServiceDefinition appointmentServiceDefinition = new AppointmentServiceDefinition();
        appointmentServiceDefinition.setUuid("uuid");
        appointmentServiceDefinition.setName("name");

        ArrayList<Appointment> appointments = new ArrayList<>();
        Appointment appointment = new Appointment();
        appointment.setUuid("appointmentUuid");
        appointments.add(appointment);
        when(appointmentsService.getAllFutureAppointmentsForService(appointmentServiceDefinition)).thenReturn(appointments);

        expectedException.expect(RuntimeException.class);
        expectedException.expectMessage("Please cancel all future appointments for this service to proceed. After deleting this service, you will not be able to see any appointments for it");

        appointmentServiceService.voidAppointmentService(appointmentServiceDefinition, voidReason);

        Mockito.verify(appointmentsService, times(1)).getAllFutureAppointmentsForService(appointmentServiceDefinition);
        Mockito.verify(appointmentServiceDao, times(0)).save(captor.capture());
    }

    @Test
    public void shouldGetAllNonVoidedAppointmentService() throws Exception {
        boolean includeVoided = false;
        AppointmentServiceDefinition appointmentServiceDefinition = new AppointmentServiceDefinition();
        appointmentServiceDefinition.setUuid("service1Uuid");
        appointmentServiceDefinition.setAppointmentServiceId(1);
        AppointmentServiceType appointmentServiceType1 = new AppointmentServiceType();
        appointmentServiceType1.setName("serviceType1");
        appointmentServiceType1.setDuration(15);
        appointmentServiceType1.setUuid("serviceTypeUuid1");
        appointmentServiceType1.setAppointmentServiceDefinition(appointmentServiceDefinition);
        appointmentServiceType1.setId(1);
        AppointmentServiceType appointmentServiceType2 = new AppointmentServiceType();
        appointmentServiceType2.setName("serviceType2");
        appointmentServiceType2.setDuration(15);
        appointmentServiceType2.setUuid("serviceTypeUuid2");
        appointmentServiceType2.setAppointmentServiceDefinition(appointmentServiceDefinition);
        appointmentServiceType2.setId(2);
        appointmentServiceType2.setVoided(true);
        Set<AppointmentServiceType> appointmentServiceTypes = new TreeSet<>();
        appointmentServiceTypes.add(appointmentServiceType1);
        appointmentServiceTypes.add(appointmentServiceType2);
        appointmentServiceDefinition.setServiceTypes(appointmentServiceTypes);
        when(appointmentServiceDao.getAppointmentServiceByUuid("service1Uuid")).thenReturn(appointmentServiceDefinition);

        AppointmentServiceDefinition returnedAppointmentServiceDefinition = appointmentServiceService.getAppointmentServiceByUuid("service1Uuid");

        assertEquals("service1Uuid", returnedAppointmentServiceDefinition.getUuid());
        Set<AppointmentServiceType> returnedServiceTypes = returnedAppointmentServiceDefinition.getServiceTypes();
        assertNotNull(returnedServiceTypes);
        assertEquals(1, returnedServiceTypes.size());
        assertEquals(2, returnedAppointmentServiceDefinition.getServiceTypes(true).size());
        assertEquals("serviceTypeUuid1", returnedServiceTypes.iterator().next().getUuid());
    }

    @Test
    public void shouldGetAppointmentServiceTypeByUuid() throws Exception {
        String serviceTypeUuid = "uuid";
        appointmentServiceService.getAppointmentServiceTypeByUuid(serviceTypeUuid);

        Mockito.verify(appointmentServiceDao, times(1)).getAppointmentServiceTypeByUuid(serviceTypeUuid);
    }

    @Test
    public void shouldGetAppointmentsForAServiceAndDateTimeRange() throws Exception {
        AppointmentServiceDefinition appointmentServiceDefinition = new AppointmentServiceDefinition();
        Date startDateTime = DateUtil.convertToLocalDateFromUTC("2108-08-14T18:30:00.0Z");
        Date endDateTime = DateUtil.convertToLocalDateFromUTC("2108-08-15T18:29:29.0Z");
        appointmentServiceService.calculateCurrentLoad(appointmentServiceDefinition,
                startDateTime, endDateTime);
        AppointmentStatus[] includeStatus = new AppointmentStatus[]{AppointmentStatus.CheckedIn, AppointmentStatus.Completed, AppointmentStatus.Scheduled};

        Mockito.verify(appointmentsService, times(1)).getAppointmentsForService(appointmentServiceDefinition, startDateTime, endDateTime,
                Arrays.asList(includeStatus));
    }
}
