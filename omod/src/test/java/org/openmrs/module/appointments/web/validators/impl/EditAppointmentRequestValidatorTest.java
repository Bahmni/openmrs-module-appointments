package org.openmrs.module.appointments.web.validators.impl;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.openmrs.Patient;
import org.openmrs.api.PatientService;
import org.openmrs.module.appointments.model.Appointment;
import org.openmrs.module.appointments.model.AppointmentServiceDefinition;
import org.openmrs.module.appointments.service.AppointmentServiceDefinitionService;
import org.openmrs.module.appointments.service.AppointmentsService;
import org.openmrs.module.appointments.web.contract.AppointmentRequest;
import org.openmrs.module.appointments.web.contract.RecurringAppointmentRequest;
import org.openmrs.module.appointments.web.validators.Validator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;

@RunWith(MockitoJUnitRunner.class)
public class EditAppointmentRequestValidatorTest {

    @Mock
    private Validator<Patient> patientValidator;

    @Mock
    private Validator<AppointmentServiceDefinition> serviceValidator;

    @Mock
    private Validator<Appointment> appointmentValidator;

    @Mock
    private AppointmentServiceDefinitionService appointmentServiceDefinitionService;

    @Mock
    private PatientService patientService;

    @Mock
    private AppointmentsService appointmentsService;

    @InjectMocks
    Validator<RecurringAppointmentRequest> editAppointmentRequestValidator =
            new EditAppointmentRequestValidator(patientValidator, serviceValidator, appointmentValidator);

    @Test
    public void shouldReturnTrueWhenPatientAndAppointmentAndAppointmentServiceAreValid() {
        RecurringAppointmentRequest recurringAppointmentRequest = mock(RecurringAppointmentRequest.class);
        AppointmentRequest appointmentRequest = mock(AppointmentRequest.class);
        final Patient mockPatient = mock(Patient.class);
        final String patientUuid = "patientUuid";
        final String serviceUuid = "serviceUuid";
        final String appointmentUuid = "appointmentUuid";
        final AppointmentServiceDefinition mockService = mock(AppointmentServiceDefinition.class);
        final Appointment mockAppointment = mock(Appointment.class);
        when(recurringAppointmentRequest.getAppointmentRequest()).thenReturn(appointmentRequest);
        when(recurringAppointmentRequest.getAppointmentRequest().getServiceUuid()).thenReturn(serviceUuid);
        when(recurringAppointmentRequest.getAppointmentRequest().getPatientUuid()).thenReturn(patientUuid);
        when(recurringAppointmentRequest.getAppointmentRequest().getUuid()).thenReturn(appointmentUuid);
        when(patientService.getPatientByUuid(patientUuid)).thenReturn(mockPatient);
        when(appointmentServiceDefinitionService.getAppointmentServiceByUuid(serviceUuid))
                .thenReturn(mockService);
        when(appointmentsService.getAppointmentByUuid(appointmentUuid)).thenReturn(mockAppointment);

        when(patientValidator.validate(mockPatient)).thenReturn(true);
        when(appointmentValidator.validate(mockAppointment)).thenReturn(true);
        when(serviceValidator.validate(mockService)).thenReturn(true);

        final boolean isValid = editAppointmentRequestValidator.validate(recurringAppointmentRequest);

        assertTrue(isValid);
        verify(patientService, times(1)).getPatientByUuid(patientUuid);
        verify(appointmentServiceDefinitionService, times(1)).getAppointmentServiceByUuid(serviceUuid);
        verify(appointmentsService, times(1)).getAppointmentByUuid(appointmentUuid);
        verify(patientValidator, times(1)).validate(mockPatient);
        verify(appointmentValidator, times(1)).validate(mockAppointment);
        verify(serviceValidator, times(1)).validate(mockService);

    }

    @Test
    public void shouldReturnFalseWhenPatientIsInvalidAndAppointmentAndAppointmentServiceAreValid() {
        RecurringAppointmentRequest recurringAppointmentRequest = mock(RecurringAppointmentRequest.class);
        AppointmentRequest appointmentRequest = mock(AppointmentRequest.class);
        final Patient mockPatient = mock(Patient.class);
        final String patientUuid = "patientUuid";
        final String serviceUuid = "serviceUuid";
        final String appointmentUuid = "appointmentUuid";
        final AppointmentServiceDefinition mockService = mock(AppointmentServiceDefinition.class);
        final Appointment mockAppointment = mock(Appointment.class);
        when(recurringAppointmentRequest.getAppointmentRequest()).thenReturn(appointmentRequest);
        when(recurringAppointmentRequest.getAppointmentRequest().getServiceUuid()).thenReturn(serviceUuid);
        when(recurringAppointmentRequest.getAppointmentRequest().getPatientUuid()).thenReturn(patientUuid);
        when(recurringAppointmentRequest.getAppointmentRequest().getUuid()).thenReturn(appointmentUuid);
        when(patientService.getPatientByUuid(patientUuid)).thenReturn(mockPatient);
        when(appointmentServiceDefinitionService.getAppointmentServiceByUuid(serviceUuid))
                .thenReturn(mockService);
        when(appointmentsService.getAppointmentByUuid(appointmentUuid)).thenReturn(mockAppointment);

        when(patientValidator.validate(mockPatient)).thenReturn(false);
        when(appointmentValidator.validate(mockAppointment)).thenReturn(true);
        when(serviceValidator.validate(mockService)).thenReturn(true);

        final boolean isValid = editAppointmentRequestValidator.validate(recurringAppointmentRequest);

        assertFalse(isValid);
        verify(patientService, times(1)).getPatientByUuid(patientUuid);
        verify(appointmentServiceDefinitionService, times(1)).getAppointmentServiceByUuid(serviceUuid);
        verify(appointmentsService, times(1)).getAppointmentByUuid(appointmentUuid);
        verify(patientValidator, times(1)).validate(mockPatient);
    }

    @Test
    public void shouldReturnFalseWhenAppointmentIsInvalidAndPatientAndAppointmentServiceAreValid() {
        RecurringAppointmentRequest recurringAppointmentRequest = mock(RecurringAppointmentRequest.class);
        AppointmentRequest appointmentRequest = mock(AppointmentRequest.class);
        final Patient mockPatient = mock(Patient.class);
        final String patientUuid = "patientUuid";
        final String serviceUuid = "serviceUuid";
        final String appointmentUuid = "appointmentUuid";
        final AppointmentServiceDefinition mockService = mock(AppointmentServiceDefinition.class);
        final Appointment mockAppointment = mock(Appointment.class);
        when(recurringAppointmentRequest.getAppointmentRequest()).thenReturn(appointmentRequest);
        when(recurringAppointmentRequest.getAppointmentRequest().getServiceUuid()).thenReturn(serviceUuid);
        when(recurringAppointmentRequest.getAppointmentRequest().getPatientUuid()).thenReturn(patientUuid);
        when(recurringAppointmentRequest.getAppointmentRequest().getUuid()).thenReturn(appointmentUuid);
        when(patientService.getPatientByUuid(patientUuid)).thenReturn(mockPatient);
        when(appointmentServiceDefinitionService.getAppointmentServiceByUuid(serviceUuid))
                .thenReturn(mockService);
        when(appointmentsService.getAppointmentByUuid(appointmentUuid)).thenReturn(mockAppointment);

        when(patientValidator.validate(mockPatient)).thenReturn(true);
        when(appointmentValidator.validate(mockAppointment)).thenReturn(false);
        when(serviceValidator.validate(mockService)).thenReturn(true);

        final boolean isValid = editAppointmentRequestValidator.validate(recurringAppointmentRequest);

        assertFalse(isValid);
        verify(patientService, times(1)).getPatientByUuid(patientUuid);
        verify(appointmentServiceDefinitionService, times(1)).getAppointmentServiceByUuid(serviceUuid);
        verify(appointmentsService, times(1)).getAppointmentByUuid(appointmentUuid);
        verify(patientValidator, times(1)).validate(mockPatient);
        verify(appointmentValidator, times(1)).validate(mockAppointment);
    }

    @Test
    public void shouldReturnFalseWhenAppointmentServiceIsInvalidAndPatientAndAppointmentAreValid() {
        RecurringAppointmentRequest recurringAppointmentRequest = mock(RecurringAppointmentRequest.class);
        AppointmentRequest appointmentRequest = mock(AppointmentRequest.class);
        final Patient mockPatient = mock(Patient.class);
        final String patientUuid = "patientUuid";
        final String serviceUuid = "serviceUuid";
        final String appointmentUuid = "appointmentUuid";
        final AppointmentServiceDefinition mockService = mock(AppointmentServiceDefinition.class);
        final Appointment mockAppointment = mock(Appointment.class);
        when(recurringAppointmentRequest.getAppointmentRequest()).thenReturn(appointmentRequest);
        when(recurringAppointmentRequest.getAppointmentRequest().getServiceUuid()).thenReturn(serviceUuid);
        when(recurringAppointmentRequest.getAppointmentRequest().getPatientUuid()).thenReturn(patientUuid);
        when(recurringAppointmentRequest.getAppointmentRequest().getUuid()).thenReturn(appointmentUuid);
        when(patientService.getPatientByUuid(patientUuid)).thenReturn(mockPatient);
        when(appointmentServiceDefinitionService.getAppointmentServiceByUuid(serviceUuid))
                .thenReturn(mockService);
        when(appointmentsService.getAppointmentByUuid(appointmentUuid)).thenReturn(mockAppointment);

        when(patientValidator.validate(mockPatient)).thenReturn(true);
        when(appointmentValidator.validate(mockAppointment)).thenReturn(true);
        when(serviceValidator.validate(mockService)).thenReturn(false);

        final boolean isValid = editAppointmentRequestValidator.validate(recurringAppointmentRequest);

        assertFalse(isValid);
        verify(patientService, times(1)).getPatientByUuid(patientUuid);
        verify(appointmentServiceDefinitionService, times(1)).getAppointmentServiceByUuid(serviceUuid);
        verify(appointmentsService, times(1)).getAppointmentByUuid(appointmentUuid);
        verify(patientValidator, times(1)).validate(mockPatient);
        verify(appointmentValidator, times(1)).validate(mockAppointment);
        verify(serviceValidator, times(1)).validate(mockService);

    }
}
