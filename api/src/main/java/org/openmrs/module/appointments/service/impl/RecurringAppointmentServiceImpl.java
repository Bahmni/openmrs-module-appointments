package org.openmrs.module.appointments.service.impl;

import org.apache.commons.lang3.StringUtils;
import org.openmrs.api.APIException;
import org.openmrs.module.appointments.dao.AppointmentRecurringPatternDao;
import org.openmrs.module.appointments.helper.AppointmentServiceHelper;
import org.openmrs.module.appointments.model.Appointment;
import org.openmrs.module.appointments.model.AppointmentAudit;
import org.openmrs.module.appointments.model.AppointmentRecurringPattern;
import org.openmrs.module.appointments.service.RecurringAppointmentService;
import org.openmrs.module.appointments.validator.AppointmentValidator;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

@Transactional
public class RecurringAppointmentServiceImpl implements RecurringAppointmentService {


    AppointmentRecurringPatternDao appointmentRecurringPatternDao;

    AppointmentServiceHelper appointmentServiceHelper;

    List<AppointmentValidator> appointmentValidators;

    public void setAppointmentRecurringPatternDao(AppointmentRecurringPatternDao appointmentRecurringPatternDao) {
        this.appointmentRecurringPatternDao = appointmentRecurringPatternDao;
    }

    public void setAppointmentServiceHelper(AppointmentServiceHelper appointmentServiceHelper) {
        this.appointmentServiceHelper = appointmentServiceHelper;
    }

    public void setAppointmentValidators(List<AppointmentValidator> appointmentValidators) {
        this.appointmentValidators = appointmentValidators;
    }

    @Override
    public List<Appointment> validateAndSave(AppointmentRecurringPattern appointmentRecurringPattern,
                                             List<Appointment> appointments) {
        if (appointments == null || appointments.isEmpty()) {
            return appointments;
        }
        appointments.forEach(appointment -> {
            validate(appointment);
            appointmentServiceHelper.checkAndAssignAppointmentNumber(appointment);
            setAppointmentAudit(appointment);
            appointment.setAppointmentRecurringPattern(appointmentRecurringPattern);
        });
        appointmentRecurringPattern.setAppointments(new HashSet<>(appointments));
        appointmentRecurringPatternDao.save(appointmentRecurringPattern);
        return appointments;
    }

    private void setAppointmentAudit(Appointment appointment) {
        try {
            String notes = appointmentServiceHelper.getAppointmentAsJsonString(appointment);
            AppointmentAudit appointmentAudit = appointmentServiceHelper.getAppointmentAuditEvent(appointment, notes);
            appointment.setAppointmentAudits(new HashSet<>(Collections.singletonList(appointmentAudit)));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void validate(Appointment appointment) {
        List<String> errors = new ArrayList<>();
        appointmentServiceHelper.validate(appointment, appointmentValidators, errors);
        if (!errors.isEmpty()) {
            String message = StringUtils.join(errors, "\n");
            throw new APIException(message);
        }
    }
}
