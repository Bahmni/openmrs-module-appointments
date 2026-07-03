package org.openmrs.module.appointments.scheduler.tasks;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.openmrs.GlobalProperty;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.context.Context;
import org.openmrs.module.appointments.model.Appointment;
import org.openmrs.module.appointments.model.AppointmentStatus;
import org.openmrs.module.appointments.service.AppointmentsService;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.mockStatic;

@RunWith(MockitoJUnitRunner.Silent.class)
public class MarkAppointmentAsMissedTaskTest {

    @Mock
    private AppointmentsService appointmentsService;

    @Mock
    private AdministrationService administrationService;

    private MarkAppointmentAsMissedTask markAppointmentAsMissedTask;

    private GlobalProperty globalProperty;

    private MockedStatic<Context> mockedContext;

    @Before
    public void setUp() throws Exception {
        mockedContext = mockStatic(Context.class);
        mockedContext.when(() -> Context.getService(AppointmentsService.class)).thenReturn(appointmentsService);
        mockedContext.when(() -> Context.getService(AdministrationService.class)).thenReturn(administrationService);
        String schedulerMarksMissed = "SchedulerMarksMissed";
        GlobalProperty missedGlobalProperty = new GlobalProperty(schedulerMarksMissed, "true");
        when(administrationService.getGlobalPropertyObject(schedulerMarksMissed)).thenReturn(missedGlobalProperty);
        markAppointmentAsMissedTask = new MarkAppointmentAsMissedTask();
    }

    @After
    public void tearDown() {
        if (mockedContext != null) {
            mockedContext.close();
        }
    }

    @Test
    public void shouldNotMarkAppointmentAsMissedWhenSchedulerIsTurnedOff() {
        String schedulerMarksMissed = "SchedulerMarksMissed";
        globalProperty = new GlobalProperty(schedulerMarksMissed, "false");
        when(administrationService.getGlobalPropertyObject(schedulerMarksMissed)).thenReturn(globalProperty);
        markAppointmentAsMissedTask.execute();
        Mockito.verify(appointmentsService, never()).changeStatus(any(Appointment.class), any(String.class), any(Date.class));
    }

    @Test
    public void executeShouldMarkCheckedInAppointmentsAsMissedWhenCompleteSchedulerIsTurnedOff() throws Exception {
        String schedulerMarksComplete = "SchedulerMarksComplete";
        globalProperty = new GlobalProperty(schedulerMarksComplete, "false");
        when(administrationService.getGlobalPropertyObject(schedulerMarksComplete)).thenReturn(globalProperty);
        List<Appointment> appointments = new ArrayList<>();
        Appointment appointment = new Appointment();
        appointments.add(appointment);
        appointment.setStatus(AppointmentStatus.CheckedIn);
        when(appointmentsService.getAllAppointmentsInDateRange(nullable(Date.class), any(Date.class))).thenReturn(appointments);
        markAppointmentAsMissedTask.execute();

        String missedStatus = AppointmentStatus.Missed.toString();
        Mockito.verify(appointmentsService, times(1)).changeStatus(eq(appointment), eq(missedStatus), any(Date.class));
    }
    @Test
    public void shouldMarkScheduledAppointmentsAsMissedWhenCompleteSchedulerIsTurnedOff() {
        String schedulerMarksComplete = "SchedulerMarksComplete";
        globalProperty = new GlobalProperty(schedulerMarksComplete, "false");
        when(administrationService.getGlobalPropertyObject(schedulerMarksComplete)).thenReturn(globalProperty);
        List<Appointment> appointments = new ArrayList<>();
        Appointment appointment = new Appointment();
        appointments.add(appointment);
        appointment.setStatus(AppointmentStatus.Scheduled);
        when(appointmentsService.getAllAppointmentsInDateRange(nullable(Date.class), any(Date.class))).thenReturn(appointments);
        markAppointmentAsMissedTask.execute();

        String missedStatus = AppointmentStatus.Missed.toString();
        Mockito.verify(appointmentsService, times(1)).changeStatus(eq(appointment), eq(missedStatus), any(Date.class));
    }

    @Test
    public void shouldNotMarkCheckedInAppointmentAsMissedWhenCompleteSchedulerIsTurnedOn() {
        String schedulerMarksComplete = "SchedulerMarksComplete";
        globalProperty = new GlobalProperty(schedulerMarksComplete, "true");
        when(administrationService.getGlobalPropertyObject(schedulerMarksComplete)).thenReturn(globalProperty);
        List<Appointment> appointments = new ArrayList<>();
        Appointment appointment = new Appointment();
        appointments.add(appointment);
        appointment.setStatus(AppointmentStatus.CheckedIn);
        when(appointmentsService.getAllAppointmentsInDateRange(any(Date.class), any(Date.class))).thenReturn(appointments);
        markAppointmentAsMissedTask.execute();

        String missedStatus = AppointmentStatus.Missed.toString();
        Mockito.verify(appointmentsService, times(0)).changeStatus(eq(appointment), eq(missedStatus), any(Date.class));
    }

    @Test
    public void shouldNotMarkCancelledAppointmentAsMissed() {
        String schedulerMarksComplete = "SchedulerMarksComplete";
        globalProperty = new GlobalProperty(schedulerMarksComplete, "true");
        when(administrationService.getGlobalPropertyObject(schedulerMarksComplete)).thenReturn(globalProperty);
        List<Appointment> appointments = new ArrayList<>();
        Appointment appointment = new Appointment();
        appointments.add(appointment);
        appointment.setStatus(AppointmentStatus.Cancelled);
        when(appointmentsService.getAllAppointmentsInDateRange(any(Date.class), any(Date.class))).thenReturn(appointments);
        markAppointmentAsMissedTask.execute();

        String missedStatus = AppointmentStatus.Missed.toString();
        Mockito.verify(appointmentsService, times(0)).changeStatus(eq(appointment), eq(missedStatus), any(Date.class));
    }
}