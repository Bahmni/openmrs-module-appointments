package org.openmrs.module.appointments.web.mapper;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.openmrs.api.APIException;
import org.openmrs.api.PatientService;
import org.openmrs.module.appointments.helper.AppointmentServiceHelper;
import org.openmrs.module.appointments.model.Appointment;
import org.openmrs.module.appointments.model.AppointmentAudit;
import org.openmrs.module.appointments.model.AppointmentProvider;
import org.openmrs.module.appointments.model.AppointmentProviderResponse;
import org.openmrs.module.appointments.model.AppointmentRecurringPattern;
import org.openmrs.module.appointments.model.AppointmentStatus;
import org.openmrs.module.appointments.service.AppointmentsService;
import org.openmrs.module.appointments.util.DateUtil;
import org.openmrs.module.appointments.web.contract.RecurringAppointmentRequest;
import org.openmrs.module.appointments.web.service.impl.DailyRecurringAppointmentsGenerationService;
import org.openmrs.module.appointments.web.service.impl.WeeklyRecurringAppointmentsGenerationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TimeZone;
import java.util.stream.Collectors;
@Component
@Qualifier("allAppointmentRecurringPatternMapper")
public class AllAppointmentRecurringPatternMapper extends AbstractAppointmentRecurringPatternMapper {

    @Autowired
    PatientService patientService;

    @Autowired
    private AppointmentsService appointmentsService;

    @Autowired
    private AppointmentServiceHelper appointmentServiceHelper;

    @Autowired
    private AppointmentMapper appointmentMapper;

    @Autowired
    private WeeklyRecurringAppointmentsGenerationService weeklyRecurringAppointmentsGenerationService;

    @Autowired
    private DailyRecurringAppointmentsGenerationService dailyRecurringAppointmentsGenerationService;

    @Override
    public AppointmentRecurringPattern fromRequest(RecurringAppointmentRequest recurringAppointmentRequest) {
        String clientTimeZone = recurringAppointmentRequest.getTimeZone();
        String serverTimeZone = Calendar.getInstance().getTimeZone().getID();
        if (!org.springframework.util.StringUtils.hasText(clientTimeZone)) {
            throw new APIException("Time Zone is missing");
        }
        String appointmentUuid = recurringAppointmentRequest.getAppointmentRequest().getUuid();
        Appointment appointment;
        if (!StringUtils.isBlank(recurringAppointmentRequest.getAppointmentRequest().getUuid())) {
            appointment = appointmentsService.getAppointmentByUuid(recurringAppointmentRequest.getAppointmentRequest().getUuid());
        } else {
            appointment = new Appointment();
            appointment.setPatient(patientService.getPatientByUuid(recurringAppointmentRequest.getAppointmentRequest().getPatientUuid()));
        }
        appointmentMapper.mapAppointmentRequestToAppointment(recurringAppointmentRequest.getAppointmentRequest(), appointment);
        appointment.setUuid(appointmentUuid);
        AppointmentRecurringPattern appointmentRecurringPattern = appointment.getAppointmentRecurringPattern();
        List<Appointment> newSetOfAppointments = getUpdatedSetOfAppointments(recurringAppointmentRequest, appointmentRecurringPattern);
        if (newSetOfAppointments.size() != 0)
            appointmentRecurringPattern.setAppointments(new HashSet<>(newSetOfAppointments));
        appointmentRecurringPattern.setEndDate(recurringAppointmentRequest.getRecurringPattern().getEndDate());
        appointmentRecurringPattern.setFrequency(recurringAppointmentRequest.getRecurringPattern().getFrequency());
        appointment.setAppointmentRecurringPattern(appointmentRecurringPattern);
        TimeZone.setDefault(TimeZone.getTimeZone(clientTimeZone));
        AppointmentRecurringPattern updatedAppointmentRecurringPattern = getUpdatedRecurringPattern(appointment,
                Collections.singletonList(AppointmentStatus.Scheduled), clientTimeZone);
        TimeZone.setDefault(TimeZone.getTimeZone(serverTimeZone));
        return updatedAppointmentRecurringPattern;
    }

    private List<Appointment> getUpdatedSetOfAppointments(RecurringAppointmentRequest recurringAppointmentRequest,
                                                          AppointmentRecurringPattern appointmentRecurringPattern) {
        List<Appointment> newSetOfAppointments = new ArrayList<>();
        if (appointmentRecurringPattern.getEndDate() == null){
            if (recurringAppointmentRequest.getRecurringPattern().getFrequency() > appointmentRecurringPattern.getFrequency()) {
                newSetOfAppointments = addRecurringAppointments(appointmentRecurringPattern, recurringAppointmentRequest);
            }
            if (recurringAppointmentRequest.getRecurringPattern().getFrequency() < appointmentRecurringPattern.getFrequency()) {
                newSetOfAppointments = deleteRecurringAppointments(appointmentRecurringPattern, recurringAppointmentRequest);            }
        }else {
            if (recurringAppointmentRequest.getRecurringPattern().getEndDate().after(appointmentRecurringPattern.getEndDate())) {
                newSetOfAppointments =  addRecurringAppointments(appointmentRecurringPattern, recurringAppointmentRequest);
            }
            if (recurringAppointmentRequest.getRecurringPattern().getEndDate().before(appointmentRecurringPattern.getEndDate())) {
                newSetOfAppointments =  deleteRecurringAppointments(appointmentRecurringPattern, recurringAppointmentRequest);            }
        }
        return newSetOfAppointments;
    }

    private List<Appointment> addRecurringAppointments(AppointmentRecurringPattern appointmentRecurringPattern,
                                                       RecurringAppointmentRequest recurringAppointmentRequest) {
        List<Appointment> appointments = new ArrayList<>();
            switch (appointmentRecurringPattern.getType()) {
                case WEEK:
                    appointments = weeklyRecurringAppointmentsGenerationService.addAppointments(
                            appointmentRecurringPattern, recurringAppointmentRequest);
                    break;
                case DAY:
                    appointments = dailyRecurringAppointmentsGenerationService.addAppointments(
                            appointmentRecurringPattern, recurringAppointmentRequest);
                    break;
            }
            appointments.forEach(appointment -> {
                appointmentServiceHelper.checkAndAssignAppointmentNumber(appointment);
                setAppointmentAudit(appointment);
            });
        return appointments;
    }

    private List<Appointment> deleteRecurringAppointments(AppointmentRecurringPattern appointmentRecurringPattern,
                                                          RecurringAppointmentRequest recurringAppointmentRequest) {
        List<Appointment> appointments = new ArrayList<>();
            switch (appointmentRecurringPattern.getType()) {
                case WEEK:
                    appointments = weeklyRecurringAppointmentsGenerationService
                            .removeRecurringAppointments(appointmentRecurringPattern, recurringAppointmentRequest);
                    break;
                case DAY:
                    appointments = dailyRecurringAppointmentsGenerationService
                            .removeRecurringAppointments(appointmentRecurringPattern, recurringAppointmentRequest);
                    break;
        }
        return appointments;
    }

    private AppointmentRecurringPattern getUpdatedRecurringPattern(Appointment appointment, List<AppointmentStatus> applicableStatusList, String clientTimeZone) {
        Date startOfDay = DateUtil.getStartOfDay();
        String serverTimeZone = Calendar.getInstance().getTimeZone().getID();
        AppointmentRecurringPattern appointmentRecurringPattern = appointment.getAppointmentRecurringPattern();
        appointmentRecurringPattern
                .setAppointments(appointment.getAppointmentRecurringPattern().getAppointments()
                        .stream()
                        .map(app -> {
                            if (isPendingAppointment(applicableStatusList, startOfDay, app)) {
                                TimeZone.setDefault(TimeZone.getTimeZone(clientTimeZone));
                                updateMetadata(app, appointment);
                                TimeZone.setDefault(TimeZone.getTimeZone(serverTimeZone));
                                setAppointmentAudit(app);
                            }
                            return app;
                        }).collect(Collectors.toSet()));
        return appointmentRecurringPattern;
    }

    private boolean isPendingAppointment(List<AppointmentStatus> applicableStatusList, Date startOfDay, Appointment appointmentInList) {
        return (appointmentInList.getStartDateTime().after(startOfDay)
                || startOfDay.equals(appointmentInList.getStartDateTime()))
                && applicableStatusList.contains(appointmentInList.getStatus());
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

    private Date getStartTime(Appointment pendingAppointment, Appointment appointment) {
        Calendar startCalender = Calendar.getInstance();
        startCalender.setTime(appointment.getStartDateTime());
        int startHours = startCalender.get(Calendar.HOUR_OF_DAY);
        int startMinutes = startCalender.get(Calendar.MINUTE);
        return getUpdatedTimeStamp(startHours, startMinutes, pendingAppointment.getStartDateTime());
    }

    private Date getEndTime(Appointment pendingAppointment, Appointment appointment) {
        Calendar endCalender = Calendar.getInstance();
        endCalender.setTime(appointment.getEndDateTime());
        int endHours = endCalender.get(Calendar.HOUR_OF_DAY);
        int endMinutes = endCalender.get(Calendar.MINUTE);
        return getUpdatedTimeStamp(endHours, endMinutes, pendingAppointment.getEndDateTime());
    }

    private Date getUpdatedTimeStamp(int endHours, int endMinutes, Date endDateTime) {
        Calendar pendingAppointmentCalender = Calendar.getInstance();
        pendingAppointmentCalender.setTime(endDateTime);
        pendingAppointmentCalender.set(Calendar.HOUR_OF_DAY, endHours);
        pendingAppointmentCalender.set(Calendar.MINUTE, endMinutes);
        pendingAppointmentCalender.set(Calendar.MILLISECOND, 0);
        return pendingAppointmentCalender.getTime();
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
