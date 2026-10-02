package org.openmrs.module.appointments.helper;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.openmrs.Patient;
import org.openmrs.api.APIException;
import org.openmrs.module.appointments.model.*;
import org.openmrs.module.appointments.util.DateUtil;
import org.openmrs.module.appointments.validator.AppointmentStatusChangeValidator;
import org.openmrs.module.appointments.validator.AppointmentValidator;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.Mockito;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;

@ExtendWith(MockitoExtension.class)
public class AppointmentServiceHelperTest {
    @InjectMocks
    private AppointmentServiceHelper appointmentServiceHelper;

    @Mock
    private AppointmentValidator appointmentValidator;

    @Mock
    private AppointmentStatusChangeValidator appointmentStatusChangeValidator;

    @Test
    public void shouldRunDefaultAppointmentValidatorsOnSave(){
        Appointment appointment = new Appointment();
        appointment.setPatient(new Patient());
        appointment.setService(new AppointmentServiceDefinition());
        appointment.setStartDateTime(new Date());
        appointment.setEndDateTime(new Date());
        appointment.setAppointmentKind(AppointmentKind.Scheduled);
        List<String> errors = new ArrayList<>();
        List<AppointmentValidator> appointmentValidators = Collections.singletonList(appointmentValidator);

        appointmentServiceHelper.validate(appointment, appointmentValidators);

        verify(appointmentValidator, times(1)).validate(any(Appointment.class),
                anyList());
    }

    @Test
    public void shouldNotCallValidateWhenValidatorsIsEmpty(){
        Appointment appointment = new Appointment();
        appointment.setPatient(new Patient());
        appointment.setService(new AppointmentServiceDefinition());
        appointment.setStartDateTime(new Date());
        appointment.setEndDateTime(new Date());
        appointment.setAppointmentKind(AppointmentKind.Scheduled);
        List<String> errors = new ArrayList<>();

        appointmentServiceHelper.validate(appointment, null);

        verify(appointmentValidator, never()).validate(any(Appointment.class),
                anyList());
    }

    @Disabled("ignored as it is moved out of appointment service helper and part of Appointment service invoking the appointment number generator")
    @Test
    public void shouldAssignAppointmentNumberIfNumberIsNull() {
        Appointment appointment = new Appointment();

        appointmentServiceHelper.checkAndAssignAppointmentNumber(appointment);

        assertEquals("0000", appointment.getAppointmentNumber());
    }

    @Disabled("ignored as it is moved out of appointment service helper and part of Appointment service invoking the appointment number generator")
    @Test
    public void shouldNotAssignAppointmentNumberIfNumberIsNotNull() {
        Appointment appointment = new Appointment();
        appointment.setAppointmentNumber("1234");

        appointmentServiceHelper.checkAndAssignAppointmentNumber(appointment);

        assertEquals("1234", appointment.getAppointmentNumber());
    }

    @Test
    public void shouldGetAppointmentAuditEvent() {
        Appointment appointment = new Appointment();
        appointment.setStatus(AppointmentStatus.Scheduled);

        AppointmentAudit appointmentAudit =
                appointmentServiceHelper.getAppointmentAuditEvent(appointment, "Notes");

        assertEquals(appointment, appointmentAudit.getAppointment());
        assertEquals("Notes", appointmentAudit.getNotes());
        assertEquals(AppointmentStatus.Scheduled, appointmentAudit.getStatus());

    }

    @Test
    public void shouldGetJsonStringOfAppointment() throws ParseException, IOException {
        Appointment appointment = new Appointment();
        Patient patient = new Patient();
        appointment.setPatient(patient);
        AppointmentServiceDefinition service = new AppointmentServiceDefinition();
        appointment.setService(service);
        AppointmentServiceType serviceType = new AppointmentServiceType();
        appointment.setServiceType(serviceType);
        Date startDateTime = DateUtil.convertToDate("2108-08-15T10:00:00.0Z", DateUtil.DateFormatType.UTC);
        Date endDateTime = DateUtil.convertToDate("2108-08-15T10:30:00.0Z", DateUtil.DateFormatType.UTC);
        appointment.setStartDateTime(startDateTime);
        appointment.setEndDateTime(endDateTime);
        appointment.setAppointmentKind(AppointmentKind.Scheduled);

        String jsonString = appointmentServiceHelper.getAppointmentAsJsonString(appointment);
        String notes = "{\"serviceTypeUuid\":\""+ serviceType.getUuid() +"\",\"startDateTime\":\""+
                startDateTime.toInstant().toString() +"\",\"locationUuid\":null,\"appointmentKind\":\"Scheduled\"," +
                "\"providerUuid\":null,\"endDateTime\":\""+ endDateTime.toInstant().toString()
                +"\",\"priority\":null,\"serviceUuid\":\""+ service.getUuid() +"\",\"appointmentNotes\":null}";
        assertEquals(notes, jsonString);
    }

    @Test
    public void shouldTriggerAppointmentStatusChangeValidator() {
        Appointment appointment = new Appointment();
        appointment.setStatus(AppointmentStatus.Completed);
        List<AppointmentStatusChangeValidator> appointmentValidators = Collections.singletonList(appointmentStatusChangeValidator);
        appointmentServiceHelper.validateStatusChangeAndGetErrors(appointment, AppointmentStatus.CheckedIn, appointmentValidators);
        verify(appointmentStatusChangeValidator, times(1)).validate(any(Appointment.class),
                any(AppointmentStatus.class),
                anyList());
    }

    @Test
    public void shouldThrowExceptionForInapplicableStatusChange() {
        Appointment appointment = new Appointment();
        appointment.setStatus(AppointmentStatus.Completed);
        List<AppointmentStatusChangeValidator> appointmentValidators = Collections.singletonList(appointmentStatusChangeValidator);

        String errorMessage = "Appointment status can not be changed from Completed to CheckedIn";
        doAnswer(invocation -> {
            Object[] args = invocation.getArguments();
            List<String> errors = (List) args[2];
            errors.add(errorMessage);
            return null;
        }).when(appointmentStatusChangeValidator).validate(any(Appointment.class), any(AppointmentStatus.class), anyList());

        APIException exception = assertThrows(APIException.class, () -> {
            appointmentServiceHelper.validateStatusChangeAndGetErrors(appointment, AppointmentStatus.CheckedIn, appointmentValidators);
            verify(appointmentStatusChangeValidator, times(1)).validate(any(Appointment.class),
                    any(AppointmentStatus.class),
                    anyList());
        });
        assertThat(exception.getMessage(), containsString(errorMessage));
    }

    @Test
    public void shouldNotCallValidateWhenAppointmentIsNull(){
        List<AppointmentValidator> appointmentValidators = Collections.singletonList(appointmentValidator);
        appointmentServiceHelper.validate(null, appointmentValidators);

        verify(appointmentValidator, never()).validate(any(Appointment.class),
                anyList());
    }
}
