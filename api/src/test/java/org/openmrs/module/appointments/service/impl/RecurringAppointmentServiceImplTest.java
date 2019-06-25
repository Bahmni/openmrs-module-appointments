package org.openmrs.module.appointments.service.impl;


import org.apache.commons.lang.time.DateUtils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import org.openmrs.Patient;
import org.openmrs.api.APIException;
import org.openmrs.module.appointments.dao.AppointmentDao;
import org.openmrs.module.appointments.dao.AppointmentRecurringPatternDao;
import org.openmrs.module.appointments.helper.AppointmentServiceHelper;
import org.openmrs.module.appointments.model.Appointment;
import org.openmrs.module.appointments.model.AppointmentAudit;
import org.openmrs.module.appointments.model.AppointmentKind;
import org.openmrs.module.appointments.model.AppointmentRecurringPattern;
import org.openmrs.module.appointments.model.AppointmentStatus;
import org.openmrs.module.appointments.util.AppointmentBuilder;
import org.openmrs.module.appointments.validator.AppointmentValidator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyListOf;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class RecurringAppointmentServiceImplTest {

    @InjectMocks
    private RecurringAppointmentServiceImpl recurringAppointmentService;

    @Mock
    private AppointmentRecurringPatternDao appointmentRecurringPatternDao;

    @Mock
    private AppointmentDao appointmentDao;

    @Mock
    private AppointmentServiceHelper appointmentServiceHelper;

    @Mock
    private Patient patient;

    @Spy
    private List<AppointmentValidator> appointmentValidators = new ArrayList<>();

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Test
    public void shouldSaveRecurringAppointmentsForGivenRecurringPatternAndAppointment() throws IOException {
        AppointmentRecurringPattern appointmentRecurringPattern = new AppointmentRecurringPattern();
        Appointment appointment = new Appointment();
        AppointmentAudit appointmentAudit = new AppointmentAudit();
        List<Appointment> appointments = Collections.singletonList(appointment);
        List<String> errors = new ArrayList<>();

        doNothing().when(appointmentServiceHelper).validate(appointment, appointmentValidators, errors);
        String notes = "Notes";
        doReturn(notes).when(appointmentServiceHelper).getAppointmentAsJsonString(appointment);
        doReturn(appointmentAudit).when(appointmentServiceHelper).getAppointmentAuditEvent(appointment, notes);
        doNothing().when(appointmentRecurringPatternDao).save(appointmentRecurringPattern);

        List<Appointment> appointmentsList = recurringAppointmentService
                .validateAndSave(appointmentRecurringPattern, appointments);

        assertEquals(1, appointmentsList.size());
        verify(appointmentRecurringPatternDao).save(appointmentRecurringPattern);
        verify(appointmentServiceHelper).validate(appointment, appointmentValidators, errors);
        verify(appointmentServiceHelper).getAppointmentAsJsonString(appointment);
        verify(appointmentServiceHelper).getAppointmentAuditEvent(appointment, notes);
        verify(appointmentServiceHelper).checkAndAssignAppointmentNumber(appointment);
        assertEquals(1, appointmentRecurringPattern.getAppointments().size());
        assertEquals(1, appointmentsList.get(0).getAppointmentAudits().size());
    }

    @Test
    public void shouldReturnNullWhenAppointsAreNull() {
        List<Appointment> appointmentsList = recurringAppointmentService.validateAndSave(
                mock(AppointmentRecurringPattern.class), Collections.emptyList());
        assertEquals(0, appointmentsList.size());
        verify(appointmentRecurringPatternDao, never()).save(any(AppointmentRecurringPattern.class));
    }

    @Test
    public void shouldReturnEmptyAppointmentsWithoutSavingWhenAppointmentsAreEmpty() {
        List<Appointment> appointmentsList = recurringAppointmentService.validateAndSave(
                mock(AppointmentRecurringPattern.class), null);
        assertNull(appointmentsList);
        verify(appointmentRecurringPatternDao, never()).save(any(AppointmentRecurringPattern.class));
    }

    @Test
    public void shouldThrowExceptionIfAppointmentValidationFails() throws IOException {
        AppointmentRecurringPattern appointmentRecurringPattern = mock(AppointmentRecurringPattern.class);
        String errorMessage = "Appointment cannot be created without Patient";
        doAnswer(invocation -> {
            Object[] args = invocation.getArguments();
            List<String> errors = (List) args[2];
            errors.add(errorMessage);
            return null;
        }).when(appointmentServiceHelper).validate(any(Appointment.class), anyListOf(AppointmentValidator.class),
                anyListOf(String.class));

        expectedException.expect(APIException.class);
        expectedException.expectMessage(errorMessage);
        recurringAppointmentService.validateAndSave(appointmentRecurringPattern,
                Collections.singletonList(new Appointment()));
        verify(appointmentRecurringPatternDao, never()).save(any(AppointmentRecurringPattern.class));
        verify(appointmentServiceHelper, never()).validate(any(), any(), any());
        verify(appointmentServiceHelper, never()).getAppointmentAsJsonString(any());
        verify(appointmentServiceHelper, never()).getAppointmentAuditEvent(any(), any());
        verify(appointmentServiceHelper, never()).checkAndAssignAppointmentNumber(any());
    }

    @Test
    public void shouldUpdateStartTimeAndEndTimeForPendingOccurrences() throws IOException {
        Calendar startTimeCalendar = Calendar.getInstance();
        Calendar endTimeCalendar = Calendar.getInstance();
        Calendar newStartTimeCalendar = Calendar.getInstance();
        Calendar newEndTimeCalendar = Calendar.getInstance();
        int year = startTimeCalendar.get(Calendar.YEAR);
        int month = startTimeCalendar.get(Calendar.MONTH);
        int day = startTimeCalendar.get(Calendar.DATE);
        startTimeCalendar.set(year, month, day, 10, 00, 00);
        endTimeCalendar.set(year, month, day, 10, 30, 00);
        newStartTimeCalendar.set(year, month, day, 12, 00, 00);
        newStartTimeCalendar.set(Calendar.MILLISECOND, 0);
        newEndTimeCalendar.set(year, month, day, 12, 30, 00);
        newEndTimeCalendar.set(Calendar.MILLISECOND, 0);
        AppointmentRecurringPattern appointmentRecurringPattern = new AppointmentRecurringPattern();
        appointmentRecurringPattern.setType(RecurringAppointmentType.DAY);
        appointmentRecurringPattern.setFrequency(3);
        appointmentRecurringPattern.setPeriod(2);
        Appointment appointmentOne = new AppointmentBuilder()
                .withUuid("uuid1")
                .withPatient(patient)
                .withStatus(AppointmentStatus.Completed)
                .withAppointmentKind(AppointmentKind.Scheduled)
                .withStartDateTime(DateUtils.addDays(startTimeCalendar.getTime(), -2))
                .withEndDateTime(DateUtils.addDays(endTimeCalendar.getTime(), -2))
                .build();
        Appointment oldAppointmentTwo = new AppointmentBuilder()
                .withUuid("uuid2")
                .withPatient(patient)
                .withStatus(AppointmentStatus.Scheduled)
                .withAppointmentKind(AppointmentKind.Scheduled)
                .withStartDateTime(startTimeCalendar.getTime())
                .withEndDateTime(endTimeCalendar.getTime())
                .build();
        oldAppointmentTwo.setAppointmentAudits(new HashSet<>());
        Appointment newAppointmentTwo = new AppointmentBuilder()
                .withUuid("uuid2")
                .withPatient(patient)
                .withStatus(AppointmentStatus.Scheduled)
                .withAppointmentKind(AppointmentKind.Scheduled)
                .withStartDateTime(newStartTimeCalendar.getTime())
                .withEndDateTime(newEndTimeCalendar.getTime())
                .build();
        Appointment appointmentThree = new AppointmentBuilder()
                .withUuid("uuid3")
                .withPatient(patient)
                .withStatus(AppointmentStatus.Scheduled)
                .withAppointmentKind(AppointmentKind.Scheduled)
                .withStartDateTime(DateUtils.addDays(startTimeCalendar.getTime(), 2))
                .withEndDateTime(DateUtils.addDays(endTimeCalendar.getTime(), 2))
                .build();
        appointmentThree.setAppointmentAudits(new HashSet<>());
        Set<Appointment> appointments = new HashSet<>(Arrays.asList(appointmentOne, oldAppointmentTwo, appointmentThree));
        appointmentRecurringPattern.setAppointments(appointments);
        appointmentOne.setAppointmentRecurringPattern(appointmentRecurringPattern);
        oldAppointmentTwo.setAppointmentRecurringPattern(appointmentRecurringPattern);
        appointmentThree.setAppointmentRecurringPattern(appointmentRecurringPattern);
        newAppointmentTwo.setAppointmentRecurringPattern(appointmentRecurringPattern);

        List<Appointment> appointmentList = recurringAppointmentService.update(newAppointmentTwo);

        assertEquals(2, appointmentList.size());
        assertEquals(newStartTimeCalendar.getTime(), appointmentList.get(0).getStartDateTime());
        assertEquals(newEndTimeCalendar.getTime(), appointmentList.get(0).getEndDateTime());
        assertEquals(DateUtils.addDays(newStartTimeCalendar.getTime(), 2), appointmentList.get(1).getStartDateTime());
        assertEquals(DateUtils.addDays(newEndTimeCalendar.getTime(), 2), appointmentList.get(1).getEndDateTime());
        verify(appointmentServiceHelper, times(2))
                .getAppointmentAuditEvent(any(Appointment.class),any(String.class));
        verify(appointmentServiceHelper, times(2))
                .getAppointmentAsJsonString(any(Appointment.class));
        verify(appointmentDao, times(2)).save(any(Appointment.class));
    }
}
