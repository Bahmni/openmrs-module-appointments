package org.openmrs.module.appointments.scheduler.tasks;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.openmrs.GlobalProperty;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.context.Context;
import org.openmrs.module.appointments.model.Appointment;
import org.openmrs.module.appointments.service.AppointmentsService;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.times;
import static org.powermock.api.mockito.PowerMockito.when;

@PowerMockIgnore("javax.management.*")
@PrepareForTest(Context.class)
@RunWith(PowerMockRunner.class)
public class ReminderForAppointmentTest {

    @Mock
    private AdministrationService administrationService;

    @Mock
    private AppointmentsService appointmentsService;

    private ReminderForAppointment reminderForAppointment;
    private GlobalProperty globalProperty;

    @Before
    public void setUp() throws Exception {
        PowerMockito.mockStatic(Context.class);
        when(Context.getService(AdministrationService.class)).thenReturn(administrationService);
        when(Context.getService(AppointmentsService.class)).thenReturn(appointmentsService);
        reminderForAppointment = new ReminderForAppointment();
    }

    @Test
    public void executeShouldSendAppointmentRemindersWhenEnabled() throws Exception {
        String globalPropertyName = "sms.enableAppointmentReminderSMSAlert";
        globalProperty = new GlobalProperty(globalPropertyName, "true");
        when(administrationService.getGlobalPropertyObject(globalPropertyName)).thenReturn(globalProperty);
        List<Appointment> appointments = new ArrayList<>();
        Appointment appointment1 = new Appointment();
        Appointment appointment2 = new Appointment();
        appointments.add(appointment1);
        appointments.add(appointment2);
        when(appointmentsService.getAllAppointmentsReminder(any(String.class))).thenReturn(appointments);
        String globalPropertyNameReminderSms = "SchedulerReminderBeforeHours";
        globalProperty = new GlobalProperty(globalPropertyNameReminderSms, "24");
        when(administrationService.getGlobalPropertyObject(globalPropertyNameReminderSms)).thenReturn(globalProperty);

        reminderForAppointment.execute();

        Mockito.verify(appointmentsService, times(1)).sendAppointmentReminderSMS(appointment1);
        Mockito.verify(appointmentsService, times(1)).sendAppointmentReminderSMS(appointment2);
    }

    @Test
    public void executeShouldNotSendAppointmentRemindersWhenDisabled() {
        String globalPropertyName = "sms.enableAppointmentReminderSMSAlert";
        globalProperty = new GlobalProperty(globalPropertyName, "false");
        when(administrationService.getGlobalPropertyObject(globalPropertyName)).thenReturn(globalProperty);

        reminderForAppointment.execute();

        Mockito.verify(appointmentsService, times(0)).sendAppointmentReminderSMS(any(Appointment.class));
    }

}
