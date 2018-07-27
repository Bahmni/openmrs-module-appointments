package org.openmrs.module.appointments.service;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.mockito.runners.MockitoJUnitRunner;
import org.openmrs.Patient;
import org.openmrs.api.APIAuthenticationException;
import org.openmrs.api.APIException;
import org.openmrs.api.context.Context;

import org.openmrs.module.appointments.dao.AppointmentAuditDao;
import org.openmrs.module.appointments.model.*;
import org.openmrs.module.appointments.util.DateUtil;
import org.openmrs.web.test.BaseModuleWebContextSensitiveTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Date;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.when;

@RunWith(SpringJUnit4ClassRunner.class)
@org.springframework.test.context.ContextConfiguration(locations = {"classpath:TestingApplicationContext.xml"}, inheritLocations = true)
public class AppointmentsServiceTest extends BaseModuleWebContextSensitiveTest {
    private String adminUser;
    private String adminUserPassword;
    private String manageUser;
    private String manageUserPassword;
    private String readOnlyUser;
    private String readOnlyUserPassword;
    private String noPrivilegeUser;
    private String noPrivilegeUserPassword;

    @Autowired
    AppointmentsService appointmentsService;

    @Mock
    private AppointmentAuditDao appointmentAuditDao;

    @Before
    public void init() throws Exception {
        adminUser = "super-user";
        adminUserPassword = "P@ssw0rd";
        manageUser = "manage-user";
        manageUserPassword = "P@ssw0rd";
        readOnlyUser = "read-only-user";
        readOnlyUserPassword = "P@ssw0rd";
        noPrivilegeUser = "no-privilege-user";
        noPrivilegeUserPassword = "P@ssw0rd";
        executeDataSet("userRolesandPrivileges.xml");
    }

    @Test
    public void shouldSaveAppointmentsOnlyIfUserHasManagePrivilege() throws Exception {
        Context.authenticate(manageUser, manageUserPassword);
        Appointment appointment = new Appointment();
        appointment.setPatient(new Patient());
        appointment.setService(new AppointmentService());
        Date startDateTime = DateUtil.convertToDate("2108-08-15T10:00:00.0Z", DateUtil.DateFormatType.UTC);
        Date endDateTime = DateUtil.convertToDate("2108-08-15T10:30:00.0Z", DateUtil.DateFormatType.UTC);
        appointment.setStartDateTime(startDateTime);
        appointment.setEndDateTime(endDateTime);
        appointment.setAppointmentKind(AppointmentKind.Scheduled);
        assertNotNull(appointmentsService.validateAndSave(appointment));
    }

    @Test(expected = APIAuthenticationException.class)
    public void shouldNotSaveAppointmentsIfUserHasNoPrivilege() throws Exception {
        Context.authenticate(noPrivilegeUser, noPrivilegeUserPassword);
        assertNotNull(appointmentsService.validateAndSave(new Appointment()));
    }

    @Test(expected = APIAuthenticationException.class)
    public void shouldNotSaveAppointmentIfUserHasReadOnlyPrivilege() throws Exception {
        Context.authenticate(readOnlyUser, readOnlyUserPassword);
        assertNotNull(appointmentsService.validateAndSave(new Appointment()));
    }

    @Test
    public void shouldGetAllAppointmentsIfUserHasReadOnlyPriviliege() throws Exception {
        Context.authenticate(readOnlyUser, readOnlyUserPassword);
        assertNotNull(appointmentsService.getAllAppointments(null));
    }

    @Test(expected = APIAuthenticationException.class)
    public void shouldNotGetAllAppointemntsIfUserDoesNotHaveAnyPrivilege() throws Exception {
        Context.authenticate(noPrivilegeUser, noPrivilegeUserPassword);
        assertNotNull(appointmentsService.getAllAppointments(null));
    }

    @Test
    public void shouldBeAbleToSearchAppointmentsIfUserHasReadOnlyPrivilege() throws Exception {
        Context.authenticate(readOnlyUser, readOnlyUserPassword);
        assertNotNull(appointmentsService.search(new Appointment()));
    }

    @Test(expected = APIAuthenticationException.class)
    public void shouldNotBeAbleToSearchAppointmentsIfUserHasNoPrivilege() throws Exception {
        Context.authenticate(noPrivilegeUser, noPrivilegeUserPassword);
        assertNotNull(appointmentsService.search(new Appointment()));
    }

    @Test
    public void shouldGetAllFutureAppointmentsIfuserHasReadOnlyPrivilege() throws Exception {
        Context.authenticate(manageUser, manageUserPassword);
        AppointmentService appointmentService = new AppointmentService();
        appointmentService.setId(1);
        assertNotNull(appointmentsService.getAllFutureAppointmentsForService(appointmentService));
    }

    @Test(expected = APIAuthenticationException.class)
    public void shouldNotGetAllFutureAppointmentsForServiceIfUserHasNoPrivilege() throws Exception {
        Context.authenticate(noPrivilegeUser, noPrivilegeUserPassword);
        AppointmentService appointmentService = new AppointmentService();
        appointmentService.setId(1);
        assertNotNull(appointmentsService.getAllFutureAppointmentsForService(appointmentService));
    }

    @Test
    public void shouldGetAllFutureAppointmentsForServiceTypeIfUserHasReadOnlyPrivilege() throws Exception {
        Context.authenticate(adminUser, adminUserPassword);
        AppointmentServiceType appointmentServiceType = new AppointmentServiceType();
        appointmentServiceType.setId(1);
        assertNotNull(appointmentsService.getAllFutureAppointmentsForServiceType(appointmentServiceType));
    }

    @Test(expected = APIAuthenticationException.class)
    public void shouldNotGetAllFutureAppointmentsForServiceTypeIfUserHasNoPrivilege() throws Exception {
        Context.authenticate(noPrivilegeUser, noPrivilegeUserPassword);
        AppointmentServiceType appointmentServiceType = new AppointmentServiceType();
        appointmentServiceType.setId(1);
        assertNotNull(appointmentsService.getAllFutureAppointmentsForServiceType(appointmentServiceType));
    }

    @Test
    public void shouldGetAppointmentsForServiceIfUserHasReadOnlyPrivilege() throws Exception {
        Context.authenticate(manageUser, manageUserPassword);
        AppointmentService appointmentService = new AppointmentService();
        appointmentService.setId(1);
        assertNotNull(appointmentsService.getAppointmentsForService(appointmentService, null, null, null));
    }

    @Test(expected = APIAuthenticationException.class)
    public void shouldNotGetAppointmentsForServiceIfUserHasNoPrivilege() throws Exception {
        Context.authenticate(noPrivilegeUser, noPrivilegeUserPassword);
        AppointmentService appointmentService = new AppointmentService();
        appointmentService.setId(1);
        assertNotNull(appointmentsService.getAppointmentsForService(appointmentService, null, null, null));
    }

    @Test
    public void shouldGetAppointmentByUuidIfUserHasReadOnlyPrivilege() throws Exception {
        Context.authenticate(readOnlyUser, readOnlyUserPassword);
        assertEquals(null, appointmentsService.getAppointmentByUuid("uuid"));
    }

    @Test(expected = APIAuthenticationException.class)
    public void shouldNotGetAppointmentByUuidIfUserHasNoPrivilege() throws Exception {
        Context.authenticate(noPrivilegeUser, noPrivilegeUserPassword);
        assertEquals(null, appointmentsService.getAppointmentByUuid("uuid"));
    }

    @Test
    public void shouldBeAbleToChangeStatusIfUserHasManagePrivilege() throws Exception {
        Context.authenticate(manageUser, manageUserPassword);
        appointmentsService.changeStatus(new Appointment(), "Completed", null);
    }

    @Test(expected = APIAuthenticationException.class)
    public void shouldNotBeAbleToChangeStatusIfUserHasReadOnlyPrivilege() throws Exception {
        Context.authenticate(readOnlyUser, readOnlyUserPassword);
        appointmentsService.changeStatus(new Appointment(), "Completed", null);
    }

    @Test
    public void shouldGetAllAppointmentsInDateRangeIfUserHasReadOnlyPrivilege() throws Exception {
        Context.authenticate(readOnlyUser, readOnlyUserPassword);
        assertNotNull(appointmentsService.getAllAppointmentsInDateRange(null, null));
    }

    @Test(expected = APIAuthenticationException.class)
    public void shouldNotGetAllAppointmentsInDateRangeIfUserHasNoPrivilege() throws Exception {
        Context.authenticate(noPrivilegeUser, readOnlyUserPassword);
        assertNotNull(appointmentsService.getAllAppointmentsInDateRange(null, null));
    }

    @Test(expected = APIException.class)
    public void shouldBeAbleToUndoStatusChangeIfUserHasManagePrivilege() throws Exception {
        Context.authenticate(manageUser, manageUserPassword);
        Appointment appointment = new Appointment();
        appointment.setId(1);
        appointmentsService.undoStatusChange(appointment);
    }

    @Test(expected = APIAuthenticationException.class)
    public void shouldNotBeAbleToUndoStatusChangeIfUserHasReadOnlyPrivilege() throws Exception {
        Context.authenticate(readOnlyUser, manageUserPassword);
        Appointment appointment = new Appointment();
        appointment.setId(1);
        appointmentsService.undoStatusChange(appointment);
    }
}
