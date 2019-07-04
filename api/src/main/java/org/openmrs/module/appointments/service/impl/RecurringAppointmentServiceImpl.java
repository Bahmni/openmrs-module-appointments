package org.openmrs.module.appointments.service.impl;

import org.apache.commons.lang3.StringUtils;
import org.openmrs.api.APIException;
import org.openmrs.module.appointments.dao.AppointmentDao;
import org.openmrs.module.appointments.dao.AppointmentRecurringPatternDao;
import org.openmrs.module.appointments.helper.AppointmentServiceHelper;
import org.openmrs.module.appointments.model.Appointment;
import org.openmrs.module.appointments.model.AppointmentAudit;
import org.openmrs.module.appointments.model.AppointmentRecurringPattern;
import org.openmrs.module.appointments.model.AppointmentStatus;
import org.openmrs.module.appointments.service.RecurringAppointmentService;
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

@Transactional
public class RecurringAppointmentServiceImpl implements RecurringAppointmentService {


    AppointmentRecurringPatternDao appointmentRecurringPatternDao;

    List<AppointmentStatusChangeValidator> statusChangeValidators;

    AppointmentServiceHelper appointmentServiceHelper;

    List<AppointmentValidator> appointmentValidators;

    List<AppointmentValidator> editAppointmentValidators;

    AppointmentDao appointmentDao;

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
    public List<Appointment> validateAndSave(AppointmentRecurringPattern appointmentRecurringPattern,
                                             List<Appointment> appointments) {
        if (appointments == null || appointments.isEmpty()) {
            return appointments;
        }
        appointments.forEach(appointment -> {
            appointmentServiceHelper.validate(appointment, appointmentValidators);
            appointmentServiceHelper.checkAndAssignAppointmentNumber(appointment);
            setAppointmentAudit(appointment);
            appointment.setAppointmentRecurringPattern(appointmentRecurringPattern);
        });
        appointmentRecurringPattern.setAppointments(new HashSet<>(appointments));
        appointmentRecurringPatternDao.save(appointmentRecurringPattern);
        return appointments;
    }

    @Override
    public List<Appointment> validateAndUpdate(Appointment appointment, String clientTimeZone) {
        appointmentServiceHelper.validate(appointment, editAppointmentValidators);
        String serverTimeZone = Calendar.getInstance().getTimeZone().getID();
        TimeZone.setDefault(TimeZone.getTimeZone(clientTimeZone));
        List<Appointment> pendingAppointments = getPendingOccurrences(appointment.getUuid(),
                Collections.singletonList(AppointmentStatus.Scheduled));
        TimeZone.setDefault(TimeZone.getTimeZone(serverTimeZone));
        return pendingAppointments
                .stream()
                .map(pendingAppointment -> {
                    TimeZone.setDefault(TimeZone.getTimeZone(clientTimeZone));
                    updateMetadata(pendingAppointment, appointment);
                    TimeZone.setDefault(TimeZone.getTimeZone(serverTimeZone));
                    setAppointmentAudit(pendingAppointment);
                    appointmentDao.save(pendingAppointment);
                    return pendingAppointment;
                })
                .collect(Collectors.toList());
    }

    @Override
    public void changeStatus(Appointment appointment, String status, Date onDate, String clientTimeZone) {
        List<String> errors = new ArrayList<>();
        AppointmentStatus appointmentStatus = AppointmentStatus.valueOf(status);
        appointmentServiceHelper.validateStatusChange(appointment, appointmentStatus, errors, statusChangeValidators);
        if (errors.isEmpty()) {
            String serverTimeZone = Calendar.getInstance().getTimeZone().getID();
            TimeZone.setDefault(TimeZone.getTimeZone(clientTimeZone));
            List<Appointment> pendingAppointments = getPendingOccurrences(appointment.getUuid(),
                    Arrays.asList(AppointmentStatus.Scheduled, AppointmentStatus.CheckedIn));
            TimeZone.setDefault(TimeZone.getTimeZone(serverTimeZone));
            pendingAppointments
                    .stream()
                    .map(pendingAppointment -> {
                        pendingAppointment.setStatus(appointmentStatus);
                        setAppointmentAudit(pendingAppointment);
                        appointmentDao.save(pendingAppointment);
                        return pendingAppointment;
                    })
                    .collect(Collectors.toList());
        } else {
            String message = StringUtils.join(errors, "\n");
            throw new APIException(message);
        }
    }

    private Appointment updateMetadata(Appointment pendingAppointment, Appointment appointment) {
        Date startTime = getStartTime(pendingAppointment, appointment);
        Date endTime = getEndTime(pendingAppointment, appointment);
        pendingAppointment.setStartDateTime(startTime);
        pendingAppointment.setEndDateTime(endTime);
        return pendingAppointment;
    }

    private Date getUpdatedTimeStamp(int endHours, int endMinutes, Date endDateTime) {
        Calendar pendingAppointmentCalender = Calendar.getInstance();
        pendingAppointmentCalender.setTime(endDateTime);
        pendingAppointmentCalender.set(Calendar.HOUR_OF_DAY, endHours);
        pendingAppointmentCalender.set(Calendar.MINUTE, endMinutes);
        pendingAppointmentCalender.set(Calendar.MILLISECOND, 0);
        return pendingAppointmentCalender.getTime();
    }

    private Date getEndTime(Appointment pendingAppointment, Appointment appointment) {
        Calendar endCalender = Calendar.getInstance();
        endCalender.setTime(appointment.getEndDateTime());
        int endHours = endCalender.get(Calendar.HOUR_OF_DAY);
        int endMinutes = endCalender.get(Calendar.MINUTE);
        return getUpdatedTimeStamp(endHours, endMinutes, pendingAppointment.getEndDateTime());
    }

    private Date getStartTime(Appointment pendingAppointment, Appointment appointment) {
        Calendar startCalender = Calendar.getInstance();
        startCalender.setTime(appointment.getStartDateTime());
        int startHours = startCalender.get(Calendar.HOUR_OF_DAY);
        int startMinutes = startCalender.get(Calendar.MINUTE);
        return getUpdatedTimeStamp(startHours, startMinutes, pendingAppointment.getStartDateTime());
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

    private Date getStartOfDay() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, calendar.getMinimum(Calendar.HOUR_OF_DAY));
        calendar.set(Calendar.MINUTE, calendar.getMinimum(Calendar.MINUTE));
        calendar.set(Calendar.SECOND, calendar.getMinimum(Calendar.SECOND));
        calendar.set(Calendar.MILLISECOND, calendar.getMinimum(Calendar.MILLISECOND));
        return calendar.getTime();
    }

    private void setAppointmentAudit(Appointment appointment) {
        try {
            String notes = appointmentServiceHelper.getAppointmentAsJsonString(appointment);
            AppointmentAudit appointmentAudit = appointmentServiceHelper.getAppointmentAuditEvent(appointment, notes);
            if (appointment.getAppointmentAudits() != null) {
                appointment.getAppointmentAudits().addAll(new HashSet<>(Collections.singletonList(appointmentAudit)));
            } else {
                appointment.setAppointmentAudits(new HashSet<>(Collections.singletonList(appointmentAudit)));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
