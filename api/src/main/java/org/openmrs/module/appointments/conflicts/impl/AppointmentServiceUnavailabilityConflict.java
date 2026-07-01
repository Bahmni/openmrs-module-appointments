package org.openmrs.module.appointments.conflicts.impl;

import org.openmrs.Provider;
import org.openmrs.module.appointments.conflicts.AppointmentConflict;
import org.openmrs.module.appointments.model.Appointment;
import org.openmrs.module.appointments.model.AppointmentConflictType;
import org.openmrs.module.appointments.model.AppointmentProvider;
import org.openmrs.module.appointments.model.AppointmentServiceDefinition;
import org.openmrs.module.appointments.model.AppointmentUnavailability;
import org.openmrs.module.appointments.model.ServiceWeeklyAvailability;
import org.openmrs.module.appointments.dao.AppointmentUnavailabilityDao;
import org.openmrs.module.appointments.search.param.AppointmentUnavailabilitySearchParams;
import org.openmrs.module.appointments.util.DateUtil;
import org.openmrs.module.appointments.util.LocationUtil;

import java.sql.Time;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static org.openmrs.module.appointments.model.AppointmentConflictType.SERVICE_UNAVAILABLE;
import static org.openmrs.module.appointments.util.DateUtil.getEpochTime;

public class AppointmentServiceUnavailabilityConflict implements AppointmentConflict {

    private AppointmentUnavailabilityDao appointmentUnavailabilityDao;

    public void setAppointmentUnavailabilityDao(AppointmentUnavailabilityDao appointmentUnavailabilityDao) {
        this.appointmentUnavailabilityDao = appointmentUnavailabilityDao;
    }

    @Override
    public AppointmentConflictType getType() {
        return SERVICE_UNAVAILABLE;
    }

    @Override
    public List<Appointment> getConflicts(List<Appointment> appointments) {
        return appointments.stream()
                .filter(appointment -> isOutsideServiceHours(appointment) || hasUnavailabilityConflict(appointment))
                .collect(Collectors.toList());
    }

    private ZoneId getTimezoneForAppointment(Appointment appointment) {
        if (appointment.getLocation() != null) {
            return LocationUtil.getLocationZone(appointment.getLocation()).orElse(ZoneId.systemDefault());
        }
        AppointmentServiceDefinition service = appointment.getService();
        if (service != null) {
            return LocationUtil.getLocationZone(service.getLocation()).orElse(ZoneId.systemDefault());
        }
        return ZoneId.systemDefault();
    }

    private boolean isOutsideServiceHours(Appointment appointment) {
        AppointmentServiceDefinition service = appointment.getService();
        if (service == null) return false;
        ZoneId zone = getTimezoneForAppointment(appointment);
        Set<ServiceWeeklyAvailability> weeklyAvailableDays = service.getWeeklyAvailability();
        if (weeklyAvailableDays != null && !weeklyAvailableDays.isEmpty()) {
            String appointmentDay = appointment.getStartDateTime().toInstant().atZone(zone).getDayOfWeek().toString();
            List<ServiceWeeklyAvailability> dayAvailabilities = weeklyAvailableDays.stream()
                    .filter(day -> day.isSameDay(appointmentDay)).collect(Collectors.toList());
            if (!dayAvailabilities.isEmpty())
                return dayAvailabilities.stream().allMatch(availableDay ->
                        hasTimeConflict(appointment, availableDay.getStartTime().getTime(), availableDay.getEndTime().getTime(), zone));
            return true;
        }
        Time serviceStartTime = service.getStartTime();
        Time serviceEndTime = service.getEndTime();
        long serviceStartMillis = serviceStartTime != null ? serviceStartTime.getTime() : DateUtil.getStartOfDay().getTime();
        long serviceEndMillis = serviceEndTime != null ? serviceEndTime.getTime() : DateUtil.getEndOfDay().getTime();
        return hasTimeConflict(appointment, serviceStartMillis, serviceEndMillis, zone);
    }

    private boolean hasTimeConflict(Appointment appointment, long serviceStartTime, long serviceEndTime, ZoneId zone) {
        long appointmentStartMs = getEpochTime(appointment.getStartDateTime().getTime(), zone);
        long appointmentEndMs = getEpochTime(appointment.getEndDateTime().getTime(), zone);
        long serviceStartMs = getEpochTime(serviceStartTime);
        long serviceEndMs = getEpochTime(serviceEndTime);
        return (appointmentStartMs >= appointmentEndMs)
                || (appointmentStartMs < serviceStartMs)
                || (appointmentEndMs > serviceEndMs);
    }

    private boolean hasUnavailabilityConflict(Appointment appointment) {
        if (appointmentUnavailabilityDao == null) return false;
        ZoneId zone = getTimezoneForAppointment(appointment);
        ZonedDateTime apptStart = appointment.getStartDateTime().toInstant().atZone(zone);
        ZonedDateTime apptEnd = appointment.getEndDateTime().toInstant().atZone(zone);
        List<AppointmentUnavailability> unavailabilities = fetchUnavailabilities(apptStart, apptEnd);
        return unavailabilities.stream().anyMatch(unavailability -> conflictsWithUnavailability(appointment, unavailability, apptStart, apptEnd));
    }

    private List<AppointmentUnavailability> fetchUnavailabilities(ZonedDateTime apptStart, ZonedDateTime apptEnd) {
        AppointmentUnavailabilitySearchParams params = new AppointmentUnavailabilitySearchParams();
        params.setStartDate(apptStart.toLocalDate().toString());
        params.setEndDate(apptEnd.toLocalDate().toString());
        return appointmentUnavailabilityDao.getAll(params);
    }

    private boolean conflictsWithUnavailability(Appointment appointment, AppointmentUnavailability unavailability, ZonedDateTime apptStart, ZonedDateTime apptEnd) {
        if (!isDateTimeOverlap(apptStart, apptEnd, unavailability)) return false;
        if (unavailability.getLocation() != null && !Objects.equals(unavailability.getLocation(), appointment.getLocation())) return false;
        if (unavailability.getService() != null && !Objects.equals(unavailability.getService(), appointment.getService())) return false;
        if (unavailability.getProvider() != null && !hasMatchingProvider(appointment, unavailability.getProvider())) return false;
        return true;
    }

    private boolean isDateTimeOverlap(ZonedDateTime apptStart, ZonedDateTime apptEnd, AppointmentUnavailability unavailability) {
        LocalDate apptDate = apptStart.toLocalDate();
        LocalTime apptStartTime = apptStart.toLocalTime();
        LocalTime apptEndTime = apptEnd.toLocalTime();

        LocalDate unavailStartDate = unavailability.getStartDate().toLocalDate();
        LocalDate unavailEndDate = unavailability.getEndDate().toLocalDate();
        LocalTime unavailStartTime = unavailability.getStartTime().toLocalTime();
        LocalTime unavailEndTime = unavailability.getEndTime().toLocalTime();

        boolean dateInRange = !apptDate.isBefore(unavailStartDate) && !apptDate.isAfter(unavailEndDate);
        boolean timeOverlaps = apptStartTime.isBefore(unavailEndTime) && apptEndTime.isAfter(unavailStartTime);

        return dateInRange && timeOverlaps;
    }

    private boolean hasMatchingProvider(Appointment appointment, Provider provider) {
        Set<AppointmentProvider> providers = appointment.getProviders();
        return providers != null && providers.stream().anyMatch(ap -> provider.equals(ap.getProvider()));
    }
}
