package org.openmrs.module.appointments.service;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openmrs.api.APIAuthenticationException;
import org.openmrs.api.context.Context;
import org.openmrs.module.appointments.model.Appointment;
import org.openmrs.module.appointments.model.AppointmentService;
import org.openmrs.web.test.BaseModuleWebContextSensitiveTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(SpringJUnit4ClassRunner.class)
@org.springframework.test.context.ContextConfiguration(locations = {"classpath:TestingApplicationContext.xml"}, inheritLocations = true)
public class AppointmentServiceServiceTest extends BaseModuleWebContextSensitiveTest {
    private String adminUser;
    private String manageUser;
    private String readOnlyUser;
    private String noPrivilegeUser;
    private String password;

    @Autowired
    AppointmentServiceService appointmentServiceService;

    @Before
    public void init() throws Exception {
        adminUser = "super-user";
        manageUser = "manage-user";
        readOnlyUser = "read-only-user";
        noPrivilegeUser = "no-privilege-user";
        password = "P@ssw0rd";
        executeDataSet("userRolesandPrivileges.xml");
    }

    @Test
    public void shouldBeAbleToSaveServiceIfUserHasManageServicesPrivilege() throws Exception {
        Context.authenticate(adminUser, password);
        AppointmentService appointmentService = new AppointmentService();
        appointmentService.setName("service");
        assertNotNull(appointmentServiceService.save(appointmentService));
    }

    @Test(expected = APIAuthenticationException.class)
    public void shouldNotBeAbleToSaveServiceIfUserDoesNotHaveManageServicesPrivilege() throws Exception {
        Context.authenticate(manageUser, password);
        AppointmentService appointmentService = new AppointmentService();
        appointmentService.setName("service");
        assertNotNull(appointmentServiceService.save(appointmentService));
    }

    @Test
    public void shouldGetAllAppointmentServicesIfUserHasViewServicesPrivilege() throws Exception {
        Context.authenticate(manageUser, password);
        assertNotNull(appointmentServiceService.getAllAppointmentServices(false));
    }

    @Test(expected = APIAuthenticationException.class)
    public void shouldNotGetAllAppointmentServicesIfUserHasNoPrivilege() throws Exception {
        Context.authenticate(noPrivilegeUser, password);
        assertNotNull(appointmentServiceService.getAllAppointmentServices(false));
    }

    @Test
    public void shouldGetAppointmentServiceByUuidIfUserHasViewServicesPrivilege() throws Exception {
        Context.authenticate(readOnlyUser, password);
        assertEquals(null, appointmentServiceService.getAppointmentServiceByUuid("uuid"));
    }

    @Test(expected = APIAuthenticationException.class)
    public void shouldNotGetAppointmentServiceByUuidIfUserHasNoPrivilege() throws Exception {
        Context.authenticate(noPrivilegeUser, password);
        assertEquals(null, appointmentServiceService.getAppointmentServiceByUuid("uuid"));
    }

    @Test
    public void shouldBeAbleToDeleteServiceIfUserHasManageServicesPrivilege() throws Exception {
        Context.authenticate(adminUser, password);
        AppointmentService appointmentService = new AppointmentService();
        appointmentService.setId(1);
        assertNotNull(appointmentServiceService.voidAppointmentService(appointmentService, null));
    }

    @Test(expected = APIAuthenticationException.class)
    public void shouldNotBeAbleToDeleteServiceIfUserDoesNotHaveManageServicesPrivilege() throws Exception {
        Context.authenticate(readOnlyUser, password);
        AppointmentService appointmentService = new AppointmentService();
        appointmentService.setId(1);
        assertNotNull(appointmentServiceService.voidAppointmentService(appointmentService, null));
    }

    @Test
    public void shouldGetAppointmentServiceTypeByUuidIfUserHasViewServicesPrivilege() throws Exception {
        Context.authenticate(readOnlyUser, password);
        assertEquals(null, appointmentServiceService.getAppointmentServiceTypeByUuid("serviceTypeUuid"));

    }

    @Test(expected = APIAuthenticationException.class)
    public void shouldNotGetAppointmentServiceTypeByUuidIfUserHasNoPrivilege() throws Exception {
        Context.authenticate(noPrivilegeUser, password);
        assertEquals(null, appointmentServiceService.getAppointmentServiceTypeByUuid("serviceTypeUuid"));
    }

    @Test
    public void shouldCalculateCurrentLoadIfUserHasViewServicesPrivilege() throws Exception {
        Context.authenticate(adminUser, password);
        AppointmentService appointmentService = new AppointmentService();
        appointmentService.setId(1);
        assertNotNull(appointmentServiceService.calculateCurrentLoad(appointmentService, null, null));
    }

    @Test(expected = APIAuthenticationException.class)
    public void shouldNotCalculateCurrentLoadIfUserHasNoPrivilege() throws Exception {
        Context.authenticate(noPrivilegeUser, password);
        AppointmentService appointmentService = new AppointmentService();
        appointmentService.setId(1);
        assertNotNull(appointmentServiceService.calculateCurrentLoad(appointmentService, null, null));
    }
}
