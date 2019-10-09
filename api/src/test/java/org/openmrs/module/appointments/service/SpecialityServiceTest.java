package org.openmrs.module.appointments.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openmrs.api.APIAuthenticationException;
import org.openmrs.api.context.Context;
import org.openmrs.module.appointments.model.Speciality;
import org.openmrs.web.test.BaseModuleWebContextSensitiveTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@org.springframework.test.context.ContextConfiguration(locations = {"classpath:TestingApplicationContext.xml"}, inheritLocations = true)
public class SpecialityServiceTest extends BaseModuleWebContextSensitiveTest {
    private String adminUser;
    private String manageUser;
    private String readOnlyUser;
    private String noPrivilegeUser;
    private String password;
    
    @Autowired
    SpecialityService specialityService;

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
    public void shouldGetSpecialityByUuidIfUserHasReadOnlyPrivilege() throws Exception {
        Context.authenticate(readOnlyUser, password);
        assertEquals(null, specialityService.getSpecialityByUuid("uuid"));
    }

    @Test(expected = APIAuthenticationException.class)
    public void shouldNotGetSpecialityByUuidIfUserHasNoPrivilege() throws Exception {
        Context.authenticate(noPrivilegeUser, password);
        specialityService.getSpecialityByUuid("uuid");
    }

    @Test
    public void shouldGetAllSpecialitiesIfUserHasReadOnlyPrivilege() throws Exception {
        Context.authenticate(readOnlyUser, password);
        assertNotNull(specialityService.getAllSpecialities());
    }

    @Test(expected = APIAuthenticationException.class)
    public void shouldNotGetAllSpecialitiesIfUserHasNoPrivilege() throws Exception {
        Context.authenticate(noPrivilegeUser, password);
        specialityService.getAllSpecialities();
    }
    
    @Test
    public void shouldBeAbleToSaveSpecialityIfUserHasManageSpecialitiesPrivilege() throws Exception {
        Context.authenticate(adminUser, password);
        Speciality speciality = new Speciality();
        speciality.setName("speciality");
        assertNotNull(specialityService.save(speciality));
    }
    
    @Test(expected = APIAuthenticationException.class)
    public void shouldNotBeAbleToSaveSpecialityIfUserDoesNotHaveManageSpecialitiesPrivilege() throws Exception {
        Context.authenticate(manageUser, password);
        Speciality speciality = new Speciality();
        speciality.setName("speciality");
        specialityService.save(speciality);
    }
}
