package org.openmrs.module.appointments.service.impl;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;
import org.openmrs.User;
import org.openmrs.api.context.Context;
import org.openmrs.module.appointments.dao.AppointmentServiceAttributeTypeDao;
import org.openmrs.module.appointments.model.AppointmentServiceAttributeType;

import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class AppointmentServiceAttributeTypeServiceImplTest {

    @Mock
    private AppointmentServiceAttributeTypeDao appointmentServiceAttributeTypeDao;

    @InjectMocks
    private AppointmentServiceAttributeTypeServiceImpl attributeTypeService;

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void shouldGetAttributeTypeByUuid() {
        String uuid = "attr-type-uuid";
        AppointmentServiceAttributeType attributeType = new AppointmentServiceAttributeType();
        attributeType.setUuid(uuid);

        when(appointmentServiceAttributeTypeDao.getAttributeTypeByUuid(uuid)).thenReturn(attributeType);

        AppointmentServiceAttributeType result = attributeTypeService.getAttributeTypeByUuid(uuid);

        verify(appointmentServiceAttributeTypeDao, times(1)).getAttributeTypeByUuid(uuid);
        assertEquals(attributeType, result);
    }

    @Test
    public void shouldGetAllAttributeTypes() {
        AppointmentServiceAttributeType attributeType = new AppointmentServiceAttributeType();
        attributeType.setName("consultation_fee");

        List<AppointmentServiceAttributeType> attributeTypes = Collections.singletonList(attributeType);
        when(appointmentServiceAttributeTypeDao.getAllAttributeTypes(false)).thenReturn(attributeTypes);

        List<AppointmentServiceAttributeType> result = attributeTypeService.getAllAttributeTypes(false);

        verify(appointmentServiceAttributeTypeDao, times(1)).getAllAttributeTypes(false);
        assertEquals(attributeTypes, result);
    }

    @Test
    public void shouldSaveAttributeType() {
        AppointmentServiceAttributeType attributeType = new AppointmentServiceAttributeType();
        attributeType.setName("consultation_fee");

        attributeTypeService.save(attributeType);

        verify(appointmentServiceAttributeTypeDao, times(1)).save(attributeType);
    }

    @Test
    public void shouldRetireAttributeType() {
        try (MockedStatic<Context> mockedContext = mockStatic(Context.class)) {
            User authenticatedUser = new User();
            mockedContext.when(Context::getAuthenticatedUser).thenReturn(authenticatedUser);

            AppointmentServiceAttributeType attributeType = new AppointmentServiceAttributeType();
            attributeType.setRetired(false);

            attributeTypeService.retire(attributeType, "No longer needed");

            verify(appointmentServiceAttributeTypeDao, times(1)).save(attributeType);
            assertTrue(attributeType.getRetired());
        }
    }
}
