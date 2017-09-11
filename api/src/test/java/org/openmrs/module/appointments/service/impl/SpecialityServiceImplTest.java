package org.openmrs.module.appointments.service.impl;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.openmrs.module.appointments.dao.SpecialityDao;
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

    @Test
    public void shouldGetSpecialityByUuid() throws Exception {
        String specialityUuid = "specialityUuid";
        Speciality speciality = new Speciality();
        speciality.setUuid(specialityUuid);
        when(specialityDao.getSpecialityByUuid(specialityUuid)).thenReturn(speciality);
        Speciality response = specialityService.getSpecialityByUuid(specialityUuid);
        verify(specialityDao, times(1)).getSpecialityByUuid(specialityUuid);
        assertEquals(speciality, response);
    }

    @Test
    public void shouldGetAllSpecialities() throws Exception {
        List<Speciality> specialities = Collections.singletonList(new Speciality());
        when(specialityDao.getAllSpecialities()).thenReturn(specialities);
        List<Speciality> response = specialityService.getAllSpecialities();
        verify(specialityDao, times(1)).getAllSpecialities();
        assertEquals(specialities, response);
    }

}