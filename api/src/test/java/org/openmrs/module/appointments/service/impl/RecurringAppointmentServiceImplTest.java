package org.openmrs.module.appointments.service.impl;


import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.openmrs.Patient;
import org.openmrs.module.appointments.dao.AppointmentRecurringPatternDao;
import org.openmrs.module.appointments.model.Appointment;
import org.openmrs.module.appointments.model.AppointmentKind;
import org.openmrs.module.appointments.model.AppointmentRecurringPattern;
import org.openmrs.module.appointments.model.AppointmentServiceDefinition;
import org.openmrs.module.appointments.service.AppointmentsService;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class RecurringAppointmentServiceImplTest {

    private RecurringAppointmentServiceImpl recurringAppointmentServiceImpl;

    @Mock
    private AppointmentsService appointmentsService;

    @Mock
    private AppointmentRecurringPatternDao appointmentRecurringPatternDao;

    @Before
    public void setUp() throws Exception {
        recurringAppointmentServiceImpl = new RecurringAppointmentServiceImpl();
    }

    @Test
    public void shouldSaveRecurringAppointmentsForGivenRecurringPatternAndAppointment() {
        AppointmentRecurringPattern appointmentRecurringPattern = new AppointmentRecurringPattern();
        appointmentRecurringPattern.setFrequency(3);
        appointmentRecurringPattern.setPeriod(1);
        appointmentRecurringPattern.setType(RecurringAppointmentType.DAY);
        Appointment appointment = new Appointment();
        appointment.setPatient(new Patient());
        appointment.setService(new AppointmentServiceDefinition());
        appointment.setStartDateTime(new Date());
        appointment.setEndDateTime(new Date());
        appointment.setAppointmentKind(AppointmentKind.Scheduled);
        List<Appointment> appointments = Collections.singletonList(appointment);
        recurringAppointmentServiceImpl.setAppointmentsService(appointmentsService);
        recurringAppointmentServiceImpl.setAppointmentRecurringPatternDao(appointmentRecurringPatternDao);
        for (Appointment app : appointments) {
            when(appointmentsService.validateAndSave(app)).thenReturn(app);
        }
        doNothing().when(appointmentRecurringPatternDao).save(appointmentRecurringPattern);
        List<Appointment> appointmentsList = recurringAppointmentServiceImpl.saveRecurringAppointments(appointmentRecurringPattern, appointments);

        verify(appointmentRecurringPatternDao, times(1)).save(appointmentRecurringPattern);
        verify(appointmentsService).validateAndSave(any(Appointment.class));
        Assert.assertEquals(1, appointmentsList.size());
    }

    @Test
    public void shouldNotSaveEmptyRecurringAppointmentsForGivenRecurringPatternAndAppointment() {
        AppointmentRecurringPattern appointmentRecurringPattern = new AppointmentRecurringPattern();
        appointmentRecurringPattern.setFrequency(3);
        appointmentRecurringPattern.setPeriod(1);
        appointmentRecurringPattern.setType(RecurringAppointmentType.DAY);

        List<Appointment> appointments = Collections.emptyList();
        recurringAppointmentServiceImpl.setAppointmentsService(appointmentsService);
        recurringAppointmentServiceImpl.setAppointmentRecurringPatternDao(appointmentRecurringPatternDao);

        doNothing().when(appointmentRecurringPatternDao).save(appointmentRecurringPattern);
        List<Appointment> appointmentsList = recurringAppointmentServiceImpl.saveRecurringAppointments(appointmentRecurringPattern, appointments);

        verify(appointmentsService, never()).validateAndSave(any(Appointment.class));
        verify(appointmentRecurringPatternDao, times(1)).save(appointmentRecurringPattern);
        Assert.assertEquals(0, appointmentsList.size());
    }

}
