package org.openmrs.module.appointments.service.impl;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.openmrs.User;
import org.openmrs.module.appointments.dao.SpecialityDao;
import org.openmrs.module.appointments.model.AppointmentServiceDefinition;
import org.openmrs.module.appointments.model.Speciality;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(PowerMockRunner.class)
public class SpecialityServiceImplTest {
    @Mock
    private SpecialityDao specialityDao;

    @InjectMocks
    private SpecialityServiceImpl specialityService;

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);
    }

    User creator;
    Speciality speciality;

    @Test
    public void shouldGetSpecialityByUuid() throws Exception {
        String specialityUuid = "specialityUuid";
        speciality = new Speciality();
        speciality.setId(1);
        speciality.setUuid(specialityUuid);
        creator = new User();
        speciality.setCreator(creator);
        when(specialityDao.getSpecialityByUuid(specialityUuid)).thenReturn(speciality);
        Speciality response = specialityService.getSpecialityByUuid(specialityUuid);
        verify(specialityDao, times(1)).getSpecialityByUuid(specialityUuid);
        assertEquals(speciality, response);
    }

    @Test
    public void shouldGetAllSpecialities() throws Exception {
        Speciality speciality = new Speciality();
        speciality.setId(1);
        speciality.setUuid("specialityUuid");
        creator = new User();
        speciality.setCreator(creator);

        List<Speciality> specialities = Collections.singletonList(speciality);
        when(specialityDao.getAllSpecialities()).thenReturn(specialities);
        List<Speciality> response = specialityService.getAllSpecialities();
        verify(specialityDao, times(1)).getAllSpecialities();
        assertEquals(specialities, response);
        assertEquals(new Integer(1), specialities.get(0).getId());
        assertEquals(creator, specialities.get(0).getCreator());
    }
    
    @Test
    public void shouldCreateSpeciality() throws Exception {
       Speciality speciality = new Speciality();
       speciality.setName("Cardiology");
       specialityService.save(speciality);
       Mockito.verify(specialityDao, times(1)).save(speciality);
    }

}