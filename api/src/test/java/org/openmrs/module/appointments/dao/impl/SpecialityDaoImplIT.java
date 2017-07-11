package org.openmrs.module.appointments.dao.impl;

import java.util.List;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.module.appointments.BaseIntegrationTest;
import org.openmrs.module.appointments.model.Speciality;
import org.springframework.beans.factory.annotation.Autowired;

public class SpecialityDaoImplIT  extends BaseIntegrationTest {

    @Autowired
    SpecialityDaoImpl specialityDao;

    @Before
    public void setUp() throws Exception {
        executeDataSet("specialityTestData.xml");
    }

    @Test
    public void testGetSpecialityByUuid() throws Exception {
        String specialityUuid = "c36006e5-9fbb-4f20-866b-0ece245615a1";
        Speciality speciality = specialityDao.getSpecialityByUuid(specialityUuid);
        assertEquals("Ortho", speciality.getName());
    }

    @Test
    public void testGetAllSpecialities() throws Exception {
        List<Speciality> specialities = specialityDao.getAllSpecialities();
        assertEquals(1,specialities.size());
        Speciality speciality = specialities.get(0);
        assertEquals("Ortho", speciality.getName());
    }
}