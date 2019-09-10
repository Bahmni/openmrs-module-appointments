package org.openmrs.module.appointments.web.mapper;

import org.apache.commons.collections.CollectionUtils;
import org.openmrs.api.APIException;
import org.openmrs.api.PatientService;
import org.openmrs.module.appointments.model.Appointment;
import org.openmrs.module.appointments.model.AppointmentProvider;
import org.openmrs.module.appointments.model.AppointmentProviderResponse;
import org.openmrs.module.appointments.model.AppointmentRecurringPattern;
import org.openmrs.module.appointments.model.AppointmentStatus;
import org.openmrs.module.appointments.service.AppointmentsService;
import org.openmrs.module.appointments.web.contract.RecurringAppointmentRequest;
import org.openmrs.module.appointments.web.service.impl.RecurringAppointmentsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.TimeZone;
import java.util.stream.Collectors;

@Component
@Qualifier("allAppointmentRecurringPatternUpdateService")
public class AllAppointmentRecurringPatternUpdateService implements AppointmentRecurringPatternUpdateService {

    @Autowired
    PatientService patientService;

    @Autowired
    private AppointmentsService appointmentsService;

    @Autowired
    private AppointmentMapper appointmentMapper;

    @Autowired
    private RecurringPatternMapper recurringPatternMapper;

    @Autowired
    RecurringAppointmentsService recurringAppointmentsService;

    @Override
    public AppointmentRecurringPattern getUpdatedRecurringPattern(RecurringAppointmentRequest recurringAppointmentRequest) {
        String clientTimeZone = recurringAppointmentRequest.getTimeZone();
        String serverTimeZone = Calendar.getInstance().getTimeZone().getID();
        if (!org.springframework.util.StringUtils.hasText(clientTimeZone)) {
            throw new APIException("Time Zone is missing");
        }
        Appointment appointment = getAppointment(recurringAppointmentRequest);
        appointmentMapper.mapAppointmentRequestToAppointment(recurringAppointmentRequest.getAppointmentRequest(), appointment);
        List<Appointment> newSetOfAppointments = recurringAppointmentsService
                .getUpdatedSetOfAppointments(appointment.getAppointmentRecurringPattern(), recurringAppointmentRequest);
        AppointmentRecurringPattern updatedAppointmentRecurringPattern =
                recurringPatternMapper.fromRequest(recurringAppointmentRequest.getRecurringPattern());
        if (newSetOfAppointments.size() != 0) {
            updatedAppointmentRecurringPattern.setAppointments(new HashSet<>(newSetOfAppointments));
        }
        appointment.setAppointmentRecurringPattern(updatedAppointmentRecurringPattern);
        TimeZone.setDefault(TimeZone.getTimeZone(clientTimeZone));
        updateMetadataForAllAppointments(appointment, clientTimeZone);
        TimeZone.setDefault(TimeZone.getTimeZone(serverTimeZone));
        return updatedAppointmentRecurringPattern;
    }

    private Appointment getAppointment(RecurringAppointmentRequest recurringAppointmentRequest) {
        Appointment appointment = appointmentsService
                .getAppointmentByUuid(recurringAppointmentRequest.getAppointmentRequest().getUuid());
        if (Objects.isNull(appointment)) {
            throw new APIException("Invalid appointment for edit");
        }
        return appointment;
    }

    private void updateMetadataForAllAppointments(Appointment appointment, String clientTimeZone) {
        String serverTimeZone = Calendar.getInstance().getTimeZone().getID();
        AppointmentRecurringPattern appointmentRecurringPattern = appointment.getAppointmentRecurringPattern();
        appointmentRecurringPattern
                .setAppointments(appointment.getAppointmentRecurringPattern().getAppointments()
                        .stream()
                        .map(app -> {
                            if (app.isFutureAppointment() && app.getStatus() == AppointmentStatus.Scheduled) {
                                TimeZone.setDefault(TimeZone.getTimeZone(clientTimeZone));
                                updateMetadata(app, appointment);
                                TimeZone.setDefault(TimeZone.getTimeZone(serverTimeZone));
                            }
                            return app;
                        }).collect(Collectors.toSet()));
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
}
