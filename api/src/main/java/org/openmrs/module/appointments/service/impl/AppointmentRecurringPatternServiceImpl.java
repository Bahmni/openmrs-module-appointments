package org.openmrs.module.appointments.service.impl;

import org.openmrs.module.appointments.dao.AppointmentDao;
import org.openmrs.module.appointments.dao.AppointmentRecurringPatternDao;
import org.openmrs.module.appointments.helper.AppointmentServiceHelper;
import org.openmrs.module.appointments.model.Appointment;
import org.openmrs.module.appointments.model.AppointmentAudit;
import org.openmrs.module.appointments.model.AppointmentRecurringPattern;
import org.openmrs.module.appointments.model.AppointmentStatus;
import org.openmrs.module.appointments.service.AppointmentRecurringPatternService;
import org.openmrs.module.appointments.validator.AppointmentStatusChangeValidator;
import org.openmrs.module.appointments.validator.AppointmentValidator;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.TimeZone;
import java.util.stream.Collectors;

import static org.openmrs.module.appointments.util.DateUtil.getStartOfDay;

@Transactional
public class AppointmentRecurringPatternServiceImpl implements AppointmentRecurringPatternService {


    private AppointmentRecurringPatternDao appointmentRecurringPatternDao;

    private List<AppointmentStatusChangeValidator> statusChangeValidators;

    private AppointmentServiceHelper appointmentServiceHelper;

    private List<AppointmentValidator> appointmentValidators;

    private List<AppointmentValidator> editAppointmentValidators;

    private AppointmentDao appointmentDao;

    public void setAppointmentRecurringPatternDao(AppointmentRecurringPatternDao appointmentRecurringPatternDao) {
        this.appointmentRecurringPatternDao = appointmentRecurringPatternDao;
    }

    public void setStatusChangeValidators(List<AppointmentStatusChangeValidator> statusChangeValidators) {
        this.statusChangeValidators = statusChangeValidators;
    }

    public void setAppointmentServiceHelper(AppointmentServiceHelper appointmentServiceHelper) {
        this.appointmentServiceHelper = appointmentServiceHelper;
    }

    public void setAppointmentValidators(List<AppointmentValidator> appointmentValidators) {
        this.appointmentValidators = appointmentValidators;
    }

    public void setAppointmentDao(AppointmentDao appointmentDao) {
        this.appointmentDao = appointmentDao;
    }

    public void setEditAppointmentValidators(List<AppointmentValidator> editAppointmentValidators) {
        this.editAppointmentValidators = editAppointmentValidators;
    }

    @Override
    public AppointmentRecurringPattern validateAndSave(AppointmentRecurringPattern appointmentRecurringPattern) {
        List<Appointment> appointments = new ArrayList<>(appointmentRecurringPattern.getAppointments());
         appointmentServiceHelper.validate(appointments.get(0), appointmentValidators);
         updateAppointmentsDetails(appointmentRecurringPattern, appointments);
        appointmentRecurringPatternDao.save(appointmentRecurringPattern);
        return appointmentRecurringPattern;
    }

    @Override
    public AppointmentRecurringPattern update(AppointmentRecurringPattern appointmentRecurringPattern, Appointment editedAppointment) {
        List<Appointment> appointments = new ArrayList<>(appointmentRecurringPattern.getAppointments());
        updateAppointmentsDetails(appointmentRecurringPattern, appointments);
        appointmentServiceHelper.validate(editedAppointment, editAppointmentValidators);
        appointmentRecurringPatternDao.save(appointmentRecurringPattern);
        return appointmentRecurringPattern;
    }

    private void updateAppointmentsDetails(AppointmentRecurringPattern appointmentRecurringPattern, List<Appointment> appointments)  {
        appointments.forEach(appointment -> {
            appointmentServiceHelper.checkAndAssignAppointmentNumber(appointment);
            setAppointmentAudit(appointment);
            appointment.setAppointmentRecurringPattern(appointmentRecurringPattern);
        });
    }

    @Override
    public Appointment update(AppointmentRecurringPattern appointmentRecurringPattern,
                              List<Appointment> updatedAppointments) {
        Appointment editedAppointment = updatedAppointments
                .stream()
                .filter(app -> !app.getVoided())
                .collect(Collectors.toList()).get(0);
        updateAppointmentsDetails(appointmentRecurringPattern, updatedAppointments);
        appointmentServiceHelper.validate(editedAppointment.getRelatedAppointment(), editAppointmentValidators);
        appointmentRecurringPatternDao.save(appointmentRecurringPattern);
        return editedAppointment;
    }

    @Override
    public List<Appointment> changeStatus(Appointment appointment, String status, String clientTimeZone) {
        AppointmentStatus appointmentStatus = AppointmentStatus.valueOf(status);
        appointmentServiceHelper.validateStatusChangeAndGetErrors(appointment, appointmentStatus, statusChangeValidators);
        String serverTimeZone = Calendar.getInstance().getTimeZone().getID();
        TimeZone.setDefault(TimeZone.getTimeZone(clientTimeZone));
        List<Appointment> pendingAppointments = getPendingOccurrences(appointment.getUuid(),
                Arrays.asList(AppointmentStatus.Requested, AppointmentStatus.Scheduled, AppointmentStatus.CheckedIn));
        TimeZone.setDefault(TimeZone.getTimeZone(serverTimeZone));
        pendingAppointments.stream()
                .map(pendingAppointment -> {
                    pendingAppointment.setStatus(appointmentStatus);
                    updateAppointmentAudits(pendingAppointment, null);
                    appointmentDao.save(pendingAppointment);
                    return pendingAppointment;
                })
                .collect(Collectors.toList());
        return pendingAppointments;
    }



    private List<Appointment> getPendingOccurrences(String appointmentUuid, List<AppointmentStatus> applicableStatusList) {
        Date startOfDay = getStartOfDay();
        Appointment appointment = appointmentDao.getAppointmentByUuid(appointmentUuid);
        return appointment.getAppointmentRecurringPattern().getAppointments()
                .stream()
                .filter(appointmentInList -> (appointmentInList.getStartDateTime().after(startOfDay)
                        || startOfDay.equals(appointmentInList.getStartDateTime()))
                        && applicableStatusList.contains(appointmentInList.getStatus()))
                .collect(Collectors.toList());
    }

    private void setAppointmentAudit(Appointment appointment) {
        try {
            String notes = appointmentServiceHelper.getAppointmentAsJsonString(appointment);
            updateAppointmentAudits(appointment, notes);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void updateAppointmentAudits(Appointment appointment, String notes) {
        AppointmentAudit appointmentAudit = appointmentServiceHelper.getAppointmentAuditEvent(appointment, notes);
        if (appointment.getAppointmentAudits() != null) {
            appointment.getAppointmentAudits().addAll(new HashSet<>(Collections.singletonList(appointmentAudit)));
        } else {
            appointment.setAppointmentAudits(new HashSet<>(Collections.singletonList(appointmentAudit)));
        }
    }
}
