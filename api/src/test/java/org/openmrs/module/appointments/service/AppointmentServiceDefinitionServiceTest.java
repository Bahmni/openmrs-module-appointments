package org.openmrs.module.appointments.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openmrs.api.APIAuthenticationException;
import org.openmrs.api.context.Context;
import org.openmrs.module.appointments.model.AppointmentServiceDefinition;
import org.openmrs.test.jupiter.BaseModuleContextSensitiveTest;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

@org.springframework.test.context.ContextConfiguration(locations = {"classpath:TestingApplicationContext.xml"}, inheritLocations = true)
public class AppointmentServiceDefinitionServiceTest extends BaseModuleContextSensitiveTest {
    private String adminUser;
    private String manageUser;
    private String readOnlyUser;
    private String noPrivilegeUser;
    private String password;

    @Autowired
    AppointmentServiceDefinitionService appointmentServiceDefinitionService;

    @BeforeEach
    public void init() throws Exception {
        adminUser = "super-user";
        manageUser = "manage-user";
        readOnlyUser = "read-only-user";
        noPrivilegeUser = "no-privilege-user";
        password = "P@ssw0rd";
        executeDataSet("userRolesandPrivileges.xml");
        // the role privilege cache loads roles on a background thread with its own session, which cannot see this
        // test's uncommitted rows, so commit them (tearDown deletes them again)
        getConnection().commit();
    }

    @AfterEach
    public void tearDown() {
        deleteAllData();
    }

    @Test
    public void shouldBeAbleToSaveServiceIfUserHasManageServicesPrivilege() throws Exception {
        Context.authenticate(adminUser, password);
        AppointmentServiceDefinition appointmentServiceDefinition = new AppointmentServiceDefinition();
        appointmentServiceDefinition.setName("service");
        assertNotNull(appointmentServiceDefinitionService.save(appointmentServiceDefinition));
    }

    @Test
    public void shouldNotBeAbleToSaveServiceIfUserDoesNotHaveManageServicesPrivilege() throws Exception {
        assertThrows(APIAuthenticationException.class, () -> {
            Context.authenticate(manageUser, password);
            AppointmentServiceDefinition appointmentServiceDefinition = new AppointmentServiceDefinition();
            appointmentServiceDefinition.setName("service");
            assertNotNull(appointmentServiceDefinitionService.save(appointmentServiceDefinition));
        });
    }

    @Test
    public void shouldGetAllAppointmentServicesIfUserHasViewServicesPrivilege() throws Exception {
        Context.authenticate(manageUser, password);
        assertNotNull(appointmentServiceDefinitionService.getAllAppointmentServices(false));
    }

    @Test
    public void shouldNotGetAllAppointmentServicesIfUserHasNoPrivilege() throws Exception {
        assertThrows(APIAuthenticationException.class, () -> {
            Context.authenticate(noPrivilegeUser, password);
            assertNotNull(appointmentServiceDefinitionService.getAllAppointmentServices(false));
        });
    }

    @Test
    public void shouldGetAppointmentServiceByUuidIfUserHasViewServicesPrivilege() throws Exception {
        Context.authenticate(readOnlyUser, password);
        assertEquals(null, appointmentServiceDefinitionService.getAppointmentServiceByUuid("uuid"));
    }

    @Test
    public void shouldNotGetAppointmentServiceByUuidIfUserHasNoPrivilege() throws Exception {
        assertThrows(APIAuthenticationException.class, () -> {
            Context.authenticate(noPrivilegeUser, password);
            assertEquals(null, appointmentServiceDefinitionService.getAppointmentServiceByUuid("uuid"));
        });
    }

    @Test
    public void shouldBeAbleToDeleteServiceIfUserHasManageServicesPrivilege() throws Exception {
        executeDataSet("appointmentServicesTestData.xml");
        Context.authenticate(adminUser, password);
        AppointmentServiceDefinition appointmentServiceDefinition = new AppointmentServiceDefinition();
        appointmentServiceDefinition.setId(1);
        assertNotNull(appointmentServiceDefinitionService.voidAppointmentService(appointmentServiceDefinition, null));
    }

    @Test
    public void shouldNotBeAbleToDeleteServiceIfUserDoesNotHaveManageServicesPrivilege() throws Exception {
        assertThrows(APIAuthenticationException.class, () -> {
            Context.authenticate(readOnlyUser, password);
            AppointmentServiceDefinition appointmentServiceDefinition = new AppointmentServiceDefinition();
            appointmentServiceDefinition.setId(1);
            assertNotNull(appointmentServiceDefinitionService.voidAppointmentService(appointmentServiceDefinition, null));
        });
    }

    @Test
    public void shouldGetAppointmentServiceTypeByUuidIfUserHasViewServicesPrivilege() throws Exception {
        Context.authenticate(readOnlyUser, password);
        assertEquals(null, appointmentServiceDefinitionService.getAppointmentServiceTypeByUuid("serviceTypeUuid"));

    }

    @Test
    public void shouldNotGetAppointmentServiceTypeByUuidIfUserHasNoPrivilege() throws Exception {
        assertThrows(APIAuthenticationException.class, () -> {
            Context.authenticate(noPrivilegeUser, password);
            assertEquals(null, appointmentServiceDefinitionService.getAppointmentServiceTypeByUuid("serviceTypeUuid"));
        });
    }

    @Test
    public void shouldCalculateCurrentLoadIfUserHasViewServicesPrivilege() throws Exception {
        Context.authenticate(adminUser, password);
        AppointmentServiceDefinition appointmentServiceDefinition = new AppointmentServiceDefinition();
        appointmentServiceDefinition.setId(1);
        assertNotNull(appointmentServiceDefinitionService.calculateCurrentLoad(appointmentServiceDefinition, null, null));
    }

    @Test
    public void shouldNotCalculateCurrentLoadIfUserHasNoPrivilege() throws Exception {
        assertThrows(APIAuthenticationException.class, () -> {
            Context.authenticate(noPrivilegeUser, password);
            AppointmentServiceDefinition appointmentServiceDefinition = new AppointmentServiceDefinition();
            appointmentServiceDefinition.setId(1);
            assertNotNull(appointmentServiceDefinitionService.calculateCurrentLoad(appointmentServiceDefinition, null, null));
        });
    }
}
