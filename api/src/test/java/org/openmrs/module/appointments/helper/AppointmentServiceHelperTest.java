package org.openmrs.module.appointments.helper;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.openmrs.Patient;
import org.openmrs.module.appointments.model.Appointment;
import org.openmrs.module.appointments.model.AppointmentAudit;
import org.openmrs.module.appointments.model.AppointmentKind;
import org.openmrs.module.appointments.model.AppointmentServiceDefinition;
import org.openmrs.module.appointments.model.AppointmentServiceType;
import org.openmrs.module.appointments.model.AppointmentStatus;
import org.openmrs.module.appointments.util.DateUtil;
import org.openmrs.module.appointments.validator.AppointmentValidator;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class AppointmentServiceHelperTest {

    @InjectMocks
    private AppointmentServiceHelper appointmentServiceHelper;

    @Mock
    private AppointmentValidator appointmentValidator;

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
                anyListOf(String.class));
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
                anyListOf(String.class));
    }

    @Test
    public void shouldAssignAppointmentNumberIfNumberIsNull() {
        Appointment appointment = new Appointment();

        appointmentServiceHelper.checkAndAssignAppointmentNumber(appointment);

        assertEquals("0000", appointment.getAppointmentNumber());
    }

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
                +"\",\"serviceUuid\":\""+ service.getUuid() +"\",\"appointmentNotes\":null}";
        assertEquals(notes, jsonString);
    }
}
