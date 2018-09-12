package org.openmrs.module.appointments.web.controller;


import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.openmrs.module.appointments.model.Speciality;
import org.openmrs.module.appointments.service.SpecialityService;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.powermock.api.mockito.PowerMockito.when;

public class SpecialityControllerTest {

    @Mock
    private SpecialityService specialityService;

    @InjectMocks
    private SpecialityController controller;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();



    @Before
    public void setUp() throws Exception {
        initMocks(this);
    }

    @Test
    public void shouldGetAllSpecialities() throws Exception {
        ArrayList<Speciality> specialities = new ArrayList<>();
        Speciality speciality1 = new Speciality();
        speciality1.setUuid("uuid1");
        speciality1.setName("speciality1");
        specialities.add(speciality1);

        Speciality speciality2 = new Speciality();
        speciality2.setUuid("uuid1");
        speciality2.setName("speciality2");
        specialities.add(speciality2);


        when(specialityService.getAllSpecialities()).thenReturn(specialities);
        List allSpecialities = controller.getAllSpecialities();
        assertEquals(2, allSpecialities.size());
    }
}
