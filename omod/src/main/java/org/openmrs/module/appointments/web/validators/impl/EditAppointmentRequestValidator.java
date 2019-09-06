package org.openmrs.module.appointments.web.validators.impl;

import org.apache.commons.lang3.StringUtils;
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
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component("appointmentRequestEditValidator")
public class EditAppointmentRequestValidator implements Validator<RecurringAppointmentRequest> {

    private Validator<Patient> patientValidator;

    private Validator<AppointmentServiceDefinition> serviceValidator;

    private Validator<Appointment> appointmentValidator ;

    public EditAppointmentRequestValidator(){
        this(new PatientValidator(), new AppointmentServiceValidator(),new AppointmentValidator() );
    }

    @Autowired
    private AppointmentServiceDefinitionService appointmentServiceDefinitionService;

    @Autowired
    private PatientService patientService;

    @Autowired
    private AppointmentsService appointmentsService;

    public EditAppointmentRequestValidator(Validator<Patient> patientValidator,
                                           Validator<AppointmentServiceDefinition> appointmentServiceValidator,
                                           Validator<Appointment> appointmentValidator) {
        this.patientValidator =patientValidator;
        this.serviceValidator = appointmentServiceValidator;
        this.appointmentValidator= appointmentValidator;
    }

    @Override
    public boolean validate(RecurringAppointmentRequest recurringAppointmentRequest) {
        AppointmentServiceDefinition appointmentServiceDefinition=
                appointmentServiceDefinitionService.getAppointmentServiceByUuid(recurringAppointmentRequest.getAppointmentRequest().getServiceUuid());
        Patient patient = patientService.getPatientByUuid(recurringAppointmentRequest.getAppointmentRequest().getPatientUuid());
        Appointment appointment= appointmentsService.getAppointmentByUuid(recurringAppointmentRequest.getAppointmentRequest().getUuid());
        return patientValidator.validate(patient) && appointmentValidator.validate(appointment)
                && serviceValidator.validate(appointmentServiceDefinition);


    }

    @Override
    public String getError() {
        List<String> errors = Arrays.asList(patientValidator.getError(),
                appointmentValidator.getError(), serviceValidator.getError());
        return StringUtils.join(errors, "\n");
    }
}
