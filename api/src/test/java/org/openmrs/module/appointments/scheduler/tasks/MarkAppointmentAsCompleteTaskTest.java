package org.openmrs.module.appointments.scheduler.tasks;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.mockito.MockedStatic;
import org.junit.jupiter.api.AfterEach;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class MarkAppointmentAsCompleteTaskTest {

    private MockedStatic<Context> contextMockedStatic;

    @AfterEach
    public void closeStaticMocks() {
        if (contextMockedStatic != null) {
            contextMockedStatic.close();
        }
    }

    @Mock
    private AppointmentsService appointmentsService;

    @Mock
    private AdministrationService administrationService;

    private MarkAppointmentAsCompleteTask markAppointmentAsCompleteTask;
    private GlobalProperty globalProperty;

    @BeforeEach
    public void setUp() throws Exception {
        contextMockedStatic = Mockito.mockStatic(Context.class);
        when(Context.getService(AppointmentsService.class)).thenReturn(appointmentsService);
        when(Context.getService(AdministrationService.class)).thenReturn(administrationService);
        markAppointmentAsCompleteTask = new MarkAppointmentAsCompleteTask();
    }

    @Test
    public void executeShouldMarkCheckedInAppointmentsAsCompletedWhenSchedulerTurnedOn() throws Exception {
        String schedulerMarksComplete = "SchedulerMarksComplete";
        globalProperty = new GlobalProperty(schedulerMarksComplete, "true");
        when(administrationService.getGlobalPropertyObject(schedulerMarksComplete)).thenReturn(globalProperty);
        List<Appointment> appointments = new ArrayList<>();
        Appointment appointment1 = new Appointment();
        Appointment appointment2 = new Appointment();
        appointments.add(appointment1);
        appointments.add(appointment2);
        appointment1.setStatus(AppointmentStatus.CheckedIn);
        when(appointmentsService.getAllAppointmentsInDateRange(nullable(Date.class), any(Date.class))).thenReturn(appointments);
        markAppointmentAsCompleteTask.execute();

        String completedStatus = AppointmentStatus.Completed.toString();
        Mockito.verify(appointmentsService, times(1)).changeStatus(eq(appointment1), eq(completedStatus), any(Date.class));
    }

    @Test
    public void shouldNotMarkAppointmentAsCompleteWhenSchedulerIsTurnedOff() {
        String schedulerMarksComplete = "SchedulerMarksComplete";
        globalProperty = new GlobalProperty(schedulerMarksComplete, "false");
        when(administrationService.getGlobalPropertyObject(schedulerMarksComplete)).thenReturn(globalProperty);
        markAppointmentAsCompleteTask.execute();
        Mockito.verify(appointmentsService, times(0)).changeStatus(any(Appointment.class), any(String.class), any(Date.class));
    }
}