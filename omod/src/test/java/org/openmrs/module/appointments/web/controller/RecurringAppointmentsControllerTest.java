package org.openmrs.module.appointments.web.controller;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.openmrs.module.appointments.model.Appointment;
import org.openmrs.module.appointments.model.AppointmentRecurringPattern;
import org.openmrs.module.appointments.service.AppointmentRecurringPatternService;
import org.openmrs.module.appointments.web.contract.AppointmentRequest;
import org.openmrs.module.appointments.web.contract.RecurringPattern;
import org.openmrs.module.appointments.web.helper.RecurringPatternHelper;
import org.openmrs.module.appointments.web.mapper.AbstractAppointmentRecurringPatternMapper;
import org.openmrs.module.appointments.web.mapper.AppointmentMapper;
import org.springframework.beans.factory.annotation.Qualifier;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;

import static org.mockito.Mockito.times;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.powermock.api.mockito.PowerMockito.when;

public class RecurringAppointmentsControllerTest {

    @InjectMocks
    private RecurringAppointmentsController recurringAppointmentsController;

    @Mock
    private AppointmentRecurringPatternService appointmentRecurringPatternService;

    @Mock
    private RecurringPatternHelper recurringPatternHelper;

    @Mock
    @Qualifier("allAppointmentRecurringPatternMapper")
    private AbstractAppointmentRecurringPatternMapper allAppointmentRecurringPatternMapper;

    @Mock
    private AppointmentMapper appointmentMapper;

    @Before
    public void setUp() throws Exception {
        initMocks(this);
    }

    @Test
    public void shouldSaveAnAppointmentWhenRecurringPatternAlsoPresentsInRequest() {
        AppointmentRequest appointmentRequest = new AppointmentRequest();
        RecurringPattern recurringPattern = new RecurringPattern();
        recurringPattern.setType("sometype");
        recurringPattern.setPeriod(1);
        recurringPattern.setFrequency(2);
        appointmentRequest.setStartDateTime(new Date());
        appointmentRequest.setUuid("someUuid");
        appointmentRequest.setServiceUuid("someServiceUuid");
        appointmentRequest.setRecurringPattern(recurringPattern);
        Appointment appointmentOne = new Appointment();
        appointmentOne.setUuid("appointmentUuid");
        Appointment appointmentTwo = new Appointment();
        appointmentTwo.setUuid("appointmentUuid");
        List<Appointment> appointments = Arrays.asList(appointmentOne, appointmentTwo);
        AppointmentRecurringPattern appointmentRecurringPattern = new AppointmentRecurringPattern();
        appointmentRecurringPattern.setAppointments(new HashSet<>(Arrays.asList(appointmentOne, appointmentTwo)));
        when(allAppointmentRecurringPatternMapper.fromRequest(recurringPattern)).thenReturn(appointmentRecurringPattern);
        when(appointmentRecurringPatternService.validateAndSave(appointmentRecurringPattern)).thenReturn(appointments);
        when(recurringPatternHelper.generateRecurringAppointments(appointmentRecurringPattern, appointmentRequest)).thenReturn(new ArrayList<>());
        when(appointmentMapper.constructResponse(Arrays.asList(appointmentOne, appointmentTwo))).thenReturn(Collections.emptyList());
        appointmentRecurringPattern.setAppointments(Collections.emptySet());

        recurringAppointmentsController.save(appointmentRequest);

        Mockito.verify(allAppointmentRecurringPatternMapper, times(1)).fromRequest(recurringPattern);
        Mockito.verify(appointmentRecurringPatternService, times(1)).validateAndSave(appointmentRecurringPattern);
        Mockito.verify(recurringPatternHelper, times(1)).generateRecurringAppointments(appointmentRecurringPattern, appointmentRequest);
        Mockito.verify(appointmentMapper, times(1)).constructResponse(Collections.emptyList());
    }
}
