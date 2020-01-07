package org.openmrs.module.appointments.helper;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.openmrs.api.APIException;
import org.openmrs.module.appointments.model.Appointment;
import org.openmrs.module.appointments.model.AppointmentAudit;
import org.openmrs.module.appointments.model.AppointmentStatus;
import org.openmrs.module.appointments.validator.AppointmentStatusChangeValidator;
import org.openmrs.module.appointments.validator.AppointmentValidator;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Component
public class AppointmentServiceHelper {

    public void checkAndAssignAppointmentNumber(Appointment appointment) {
        if (appointment.getAppointmentNumber() == null) {
            appointment.setAppointmentNumber(generateAppointmentNumber(appointment));
        }
    }

    //TODO: extract this out to a pluggable strategy
    private String generateAppointmentNumber(Appointment appointment) {
        return "0000";
    }

    public AppointmentAudit getAppointmentAuditEvent(Appointment appointment,String notes) {
        AppointmentAudit appointmentAuditEvent = new AppointmentAudit();
        appointmentAuditEvent.setAppointment(appointment);
        appointmentAuditEvent.setStatus(appointment.getStatus());
        appointmentAuditEvent.setNotes(notes);
        return appointmentAuditEvent;
    }

    //TODO refactor throwing of IOExeption. Its forcing everywhere the exception to be caught and rethrown
    public String getAppointmentAsJsonString(Appointment appointment) throws IOException {
        Map appointmentJson = new HashMap<String,String>();
        String serviceUuid = appointment.getService().getUuid();
        appointmentJson.put("serviceUuid", serviceUuid);
        String serviceTypeUuid = appointment.getServiceType() != null ? appointment.getServiceType().getUuid() : null;
        appointmentJson.put("serviceTypeUuid", serviceTypeUuid);
        //TODO: Should check appointment.getProviders() instead
        String providerUuid = appointment.getProvider() != null ? appointment.getProvider().getUuid() : null;
        appointmentJson.put("providerUuid", providerUuid);
        String locationUuid = appointment.getLocation() != null ? appointment.getLocation().getUuid() : null;
        appointmentJson.put("locationUuid", locationUuid);
        appointmentJson.put("startDateTime", appointment.getStartDateTime().toInstant().toString());
        appointmentJson.put("endDateTime", appointment.getEndDateTime().toInstant().toString());
        appointmentJson.put("appointmentKind", appointment.getAppointmentKind().name());
        appointmentJson.put("appointmentNotes", appointment.getComments());
        ObjectMapper mapperObj = new ObjectMapper();
        return String.format("%s", mapperObj.writeValueAsString(appointmentJson));
    }

    private void validateAppointment(Appointment appointment, List<AppointmentValidator> appointmentValidators,
                                     List<String> errors) {
        if(!CollectionUtils.isEmpty(appointmentValidators) && Objects.nonNull(appointment)) {
            for (AppointmentValidator validator : appointmentValidators) {
                validator.validate(appointment, errors);
            }
        }
    }

    private void validateStatusChange(Appointment appointment, AppointmentStatus status,
                                      List<String> errors,
                                      List<AppointmentStatusChangeValidator> statusChangeValidators) {
        if (!CollectionUtils.isEmpty(statusChangeValidators)) {
            for (AppointmentStatusChangeValidator validator : statusChangeValidators) {
                validator.validate(appointment, status, errors);
            }
        }
    }

    public void validate(Appointment appointment, List<AppointmentValidator> appointmentValidators) {
        List<String> errors = new ArrayList<>();
        validateAppointment(appointment, appointmentValidators, errors);
        if (!errors.isEmpty()) {
            String message = StringUtils.join(errors, "\n");
            throw new APIException(message);
        }
    }

    public void validateStatusChangeAndGetErrors(Appointment appointment,
                                     AppointmentStatus appointmentStatus,
                                     List<AppointmentStatusChangeValidator> statusChangeValidators) {
        List<String> errors = new ArrayList<>();
        validateStatusChange(appointment, appointmentStatus, errors, statusChangeValidators);
        if (!errors.isEmpty()) {
            String message = StringUtils.join(errors, "\n");
            throw new APIException(message);
        }
    }
}
