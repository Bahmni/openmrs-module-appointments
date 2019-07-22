package org.openmrs.module.appointments.service.impl;

import org.apache.commons.collections.CollectionUtils;
import org.openmrs.module.appointments.dao.AppointmentDao;
import org.openmrs.module.appointments.dao.AppointmentRecurringPatternDao;
import org.openmrs.module.appointments.helper.AppointmentServiceHelper;
import org.openmrs.module.appointments.model.Appointment;
import org.openmrs.module.appointments.model.AppointmentAudit;
import org.openmrs.module.appointments.model.AppointmentProvider;
import org.openmrs.module.appointments.model.AppointmentProviderResponse;
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
import java.util.Set;
import java.util.TimeZone;
import java.util.stream.Collectors;

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
    public List<Appointment> validateAndSave(AppointmentRecurringPattern appointmentRecurringPattern) {
        if (appointmentRecurringPattern.isAppointmentsEmptyOrNull()) {
            return Collections.emptyList();
        }
        List<Appointment> appointments = new ArrayList<>(appointmentRecurringPattern.getAppointments());
        appointmentServiceHelper.validate(appointments.get(0), appointmentValidators);
        appointments.forEach(appointment -> {
            appointmentServiceHelper.checkAndAssignAppointmentNumber(appointment);
            setAppointmentAudit(appointment);
            appointment.setAppointmentRecurringPattern(appointmentRecurringPattern);
        });
        save(appointmentRecurringPattern);
        return appointments;
    }

    public void save(AppointmentRecurringPattern appointmentRecurringPattern) {
        appointmentRecurringPatternDao.save(appointmentRecurringPattern);
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
        AppointmentStatus appointmentStatus = AppointmentStatus.valueOf(status);
        appointmentServiceHelper.validateStatusChangeAndGetErrors(appointment, appointmentStatus, statusChangeValidators);
        String serverTimeZone = Calendar.getInstance().getTimeZone().getID();
        TimeZone.setDefault(TimeZone.getTimeZone(clientTimeZone));
        List<Appointment> pendingAppointments = getPendingOccurrences(appointment.getUuid(),
                Arrays.asList(AppointmentStatus.Scheduled, AppointmentStatus.CheckedIn));
        TimeZone.setDefault(TimeZone.getTimeZone(serverTimeZone));
        pendingAppointments.stream()
                .map(pendingAppointment -> {
                    pendingAppointment.setStatus(appointmentStatus);
                    setAppointmentAuditForStatusChange(pendingAppointment, onDate);
                    appointmentDao.save(pendingAppointment);
                    return pendingAppointment;
                })
                .collect(Collectors.toList());
    }

    @Override
    public AppointmentRecurringPattern validateAndUpdate(AppointmentRecurringPattern appointmentRecurringPattern) {
        return null;
    }

    private void updateMetadata(Appointment pendingAppointment, Appointment appointment) {
        Date startTime = getStartTime(pendingAppointment, appointment);
        Date endTime = getEndTime(pendingAppointment, appointment);
        pendingAppointment.setStartDateTime(startTime);
        pendingAppointment.setEndDateTime(endTime);
        pendingAppointment.setService(appointment.getService());
        pendingAppointment.setServiceType(appointment.getServiceType());
        pendingAppointment.setLocation(appointment.getLocation());
        pendingAppointment.setComments(appointment.getComments());
        List<AppointmentProvider> cloneOfProviders = getDeepCloneOfProviders(appointment.getProviders());
        mapProvidersForAppointment(pendingAppointment, cloneOfProviders);
    }

    private List<AppointmentProvider> getDeepCloneOfProviders(Set<AppointmentProvider> appointmentProviders) {
        if (CollectionUtils.isEmpty(appointmentProviders))
            return new ArrayList<>();
        return appointmentProviders
                .stream()
                .map(AppointmentProvider::new)
                .collect(Collectors.toList());
    }

    private void mapProvidersForAppointment(Appointment appointment, List<AppointmentProvider> newProviders) {
        Set<AppointmentProvider> existingProviders = appointment.getProviders();
        setRemovedProvidersToCancel(newProviders, existingProviders);
        createNewAppointmentProviders(appointment, newProviders);
    }

    private void createNewAppointmentProviders(Appointment appointment, List<AppointmentProvider> newProviders) {
        if (!newProviders.isEmpty()) {
            if (appointment.getProviders() == null) {
                appointment.setProviders(new HashSet<>());
            }
            for (AppointmentProvider newAppointmentProvider : newProviders) {
                List<AppointmentProvider> oldProviders = appointment.getProviders()
                        .stream()
                        .filter(existingProvider -> existingProvider.getProvider()
                                .getUuid()
                                .equals(newAppointmentProvider.getProvider().getUuid()))
                        .collect(Collectors.toList());
                if (oldProviders.isEmpty()) {
                    newAppointmentProvider.setAppointment(appointment);
                    appointment.getProviders().add(newAppointmentProvider);
                } else {
                    oldProviders.forEach(existingAppointmentProvider -> {
                        //TODO: if currentUser is same person as provider, set ACCEPTED
                        existingAppointmentProvider.setResponse(newAppointmentProvider.getResponse());
                        existingAppointmentProvider.setVoided(Boolean.FALSE);
                        existingAppointmentProvider.setVoidReason(null);
                    });
                }
            }
        }
    }

    private void setRemovedProvidersToCancel(List<AppointmentProvider> newProviders,
                                             Set<AppointmentProvider> existingProviders) {
        if (CollectionUtils.isNotEmpty(existingProviders)) {
            for (AppointmentProvider existingAppointmentProvider : existingProviders) {
                boolean appointmentProviderExists = newProviders.stream()
                        .anyMatch(newProvider -> newProvider.getProvider()
                                .getUuid()
                                .equals(existingAppointmentProvider.getProvider().getUuid()));
                if (!appointmentProviderExists) {
                    existingAppointmentProvider.setResponse(AppointmentProviderResponse.CANCELLED);
                    existingAppointmentProvider.setVoided(true);
                    existingAppointmentProvider.setVoidReason(AppointmentProviderResponse.CANCELLED.toString());
                }
            }
        }
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
            updateAppointmentAudits(appointment, notes);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void setAppointmentAuditForStatusChange(Appointment appointment, Date onDate) {
        String notes = onDate != null ? onDate.toInstant().toString() : null;
        updateAppointmentAudits(appointment, notes);
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
