package org.openmrs.module.appointments.service.impl;


import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import org.openmrs.api.APIException;
import org.openmrs.module.appointments.dao.AppointmentRecurringPatternDao;
import org.openmrs.module.appointments.helper.AppointmentServiceHelper;
import org.openmrs.module.appointments.model.Appointment;
import org.openmrs.module.appointments.model.AppointmentAudit;
import org.openmrs.module.appointments.model.AppointmentRecurringPattern;
import org.openmrs.module.appointments.validator.AppointmentValidator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyListOf;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class RecurringAppointmentServiceImplTest {

    @InjectMocks
    private RecurringAppointmentServiceImpl recurringAppointmentService;

    @Mock
    private AppointmentRecurringPatternDao appointmentRecurringPatternDao;

    @Mock
    private AppointmentServiceHelper appointmentServiceHelper;

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
}
