package org.openmrs.module.appointments.service;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openmrs.api.APIAuthenticationException;
import org.openmrs.api.context.Context;
import org.openmrs.module.appointments.model.AppointmentServiceDefinition;
import org.openmrs.web.test.BaseModuleWebContextSensitiveTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(SpringJUnit4ClassRunner.class)
@org.springframework.test.context.ContextConfiguration(locations = {"classpath:TestingApplicationContext.xml"}, inheritLocations = true)
public class AppointmentServiceDefinitionServiceTest extends BaseModuleWebContextSensitiveTest {
    private String adminUser;
    private String manageUser;
    private String readOnlyUser;
    private String noPrivilegeUser;
    private String password;

    @Autowired
    AppointmentServiceDefinitionService appointmentServiceDefinitionService;

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
        AppointmentServiceDefinition appointmentServiceDefinition = new AppointmentServiceDefinition();
        appointmentServiceDefinition.setName("service");
        assertNotNull(appointmentServiceDefinitionService.save(appointmentServiceDefinition));
    }

    @Test(expected = APIAuthenticationException.class)
    public void shouldNotBeAbleToSaveServiceIfUserDoesNotHaveManageServicesPrivilege() throws Exception {
        Context.authenticate(manageUser, password);
        AppointmentServiceDefinition appointmentServiceDefinition = new AppointmentServiceDefinition();
        appointmentServiceDefinition.setName("service");
        assertNotNull(appointmentServiceDefinitionService.save(appointmentServiceDefinition));
    }

    @Test
    public void shouldGetAllAppointmentServicesIfUserHasViewServicesPrivilege() throws Exception {
        Context.authenticate(manageUser, password);
        assertNotNull(appointmentServiceDefinitionService.getAllAppointmentServices(false));
    }

    @Test(expected = APIAuthenticationException.class)
    public void shouldNotGetAllAppointmentServicesIfUserHasNoPrivilege() throws Exception {
        Context.authenticate(noPrivilegeUser, password);
        assertNotNull(appointmentServiceDefinitionService.getAllAppointmentServices(false));
    }

    @Test
    public void shouldGetAppointmentServiceByUuidIfUserHasViewServicesPrivilege() throws Exception {
        Context.authenticate(readOnlyUser, password);
        assertEquals(null, appointmentServiceDefinitionService.getAppointmentServiceByUuid("uuid"));
    }

    @Test(expected = APIAuthenticationException.class)
    public void shouldNotGetAppointmentServiceByUuidIfUserHasNoPrivilege() throws Exception {
        Context.authenticate(noPrivilegeUser, password);
        assertEquals(null, appointmentServiceDefinitionService.getAppointmentServiceByUuid("uuid"));
    }

    @Test
    public void shouldBeAbleToDeleteServiceIfUserHasManageServicesPrivilege() throws Exception {
        Context.authenticate(adminUser, password);
        AppointmentServiceDefinition appointmentServiceDefinition = new AppointmentServiceDefinition();
        appointmentServiceDefinition.setId(1);
        assertNotNull(appointmentServiceDefinitionService.voidAppointmentService(appointmentServiceDefinition, null));
    }

    @Test(expected = APIAuthenticationException.class)
    public void shouldNotBeAbleToDeleteServiceIfUserDoesNotHaveManageServicesPrivilege() throws Exception {
        Context.authenticate(readOnlyUser, password);
        AppointmentServiceDefinition appointmentServiceDefinition = new AppointmentServiceDefinition();
        appointmentServiceDefinition.setId(1);
        assertNotNull(appointmentServiceDefinitionService.voidAppointmentService(appointmentServiceDefinition, null));
    }

    @Test
    public void shouldGetAppointmentServiceTypeByUuidIfUserHasViewServicesPrivilege() throws Exception {
        Context.authenticate(readOnlyUser, password);
        assertEquals(null, appointmentServiceDefinitionService.getAppointmentServiceTypeByUuid("serviceTypeUuid"));

    }

    @Test(expected = APIAuthenticationException.class)
    public void shouldNotGetAppointmentServiceTypeByUuidIfUserHasNoPrivilege() throws Exception {
        Context.authenticate(noPrivilegeUser, password);
        assertEquals(null, appointmentServiceDefinitionService.getAppointmentServiceTypeByUuid("serviceTypeUuid"));
    }

    @Test
    public void shouldCalculateCurrentLoadIfUserHasViewServicesPrivilege() throws Exception {
        Context.authenticate(adminUser, password);
        AppointmentServiceDefinition appointmentServiceDefinition = new AppointmentServiceDefinition();
        appointmentServiceDefinition.setId(1);
        assertNotNull(appointmentServiceDefinitionService.calculateCurrentLoad(appointmentServiceDefinition, null, null));
    }

    @Test(expected = APIAuthenticationException.class)
    public void shouldNotCalculateCurrentLoadIfUserHasNoPrivilege() throws Exception {
        Context.authenticate(noPrivilegeUser, password);
        AppointmentServiceDefinition appointmentServiceDefinition = new AppointmentServiceDefinition();
        appointmentServiceDefinition.setId(1);
        assertNotNull(appointmentServiceDefinitionService.calculateCurrentLoad(appointmentServiceDefinition, null, null));
    }
}
