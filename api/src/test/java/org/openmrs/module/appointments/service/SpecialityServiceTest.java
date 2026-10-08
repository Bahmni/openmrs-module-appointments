package org.openmrs.module.appointments.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openmrs.api.context.Context;
import org.openmrs.module.appointments.model.Speciality;
import org.openmrs.test.jupiter.BaseModuleContextSensitiveTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import static org.junit.jupiter.api.Assertions.assertThrows;

@org.springframework.test.context.ContextConfiguration(locations = {"classpath:TestingApplicationContext.xml"}, inheritLocations = true)
public class SpecialityServiceTest extends BaseModuleContextSensitiveTest {
    private String adminUser;
    private String manageUser;
    private String readOnlyUser;
    private String noPrivilegeUser;
    private String password;
    
    @Autowired
    SpecialityService specialityService;

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
    public void shouldGetSpecialityByUuidIfUserHasReadOnlyPrivilege() throws Exception {
        Context.authenticate(readOnlyUser, password);
        assertEquals(null, specialityService.getSpecialityByUuid("uuid"));
    }

    @Test
    public void shouldNotGetSpecialityByUuidIfUserHasNoPrivilege() throws Exception {
        assertThrows(AccessDeniedException.class, () -> {
            Context.authenticate(noPrivilegeUser, password);
            specialityService.getSpecialityByUuid("uuid");
        });
    }

    @Test
    public void shouldGetAllSpecialitiesIfUserHasReadOnlyPrivilege() throws Exception {
        Context.authenticate(readOnlyUser, password);
        assertNotNull(specialityService.getAllSpecialities());
    }

    @Test
    public void shouldNotGetAllSpecialitiesIfUserHasNoPrivilege() throws Exception {
        assertThrows(AccessDeniedException.class, () -> {
            Context.authenticate(noPrivilegeUser, password);
            specialityService.getAllSpecialities();
        });
    }
    
    @Test
    public void shouldBeAbleToSaveSpecialityIfUserHasManageSpecialitiesPrivilege() throws Exception {
        Context.authenticate(adminUser, password);
        Speciality speciality = new Speciality();
        speciality.setName("speciality");
        assertNotNull(specialityService.save(speciality));
    }
    
    @Test
    public void shouldNotBeAbleToSaveSpecialityIfUserDoesNotHaveManageSpecialitiesPrivilege() throws Exception {
        assertThrows(AccessDeniedException.class, () -> {
            Context.authenticate(manageUser, password);
            Speciality speciality = new Speciality();
            speciality.setName("speciality");
            specialityService.save(speciality);
        });
    }
}
