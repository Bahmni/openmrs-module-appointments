package org.openmrs.module.appointments.conflicts.impl;

import org.openmrs.module.appointments.conflicts.AppointmentConflict;
import org.openmrs.module.appointments.model.Appointment;
import org.openmrs.module.appointments.model.AppointmentConflictType;
import org.openmrs.module.appointments.model.AppointmentServiceDefinition;
import org.openmrs.module.appointments.model.ServiceWeeklyAvailability;
import org.openmrs.module.appointments.util.DateUtil;

import java.sql.Time;
import java.text.SimpleDateFormat;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static org.openmrs.module.appointments.model.AppointmentConflictType.SERVICE_UNAVAILABLE;
import static org.openmrs.module.appointments.util.DateUtil.getEpochTime;

public class AppointmentServiceUnavailabilityConflict implements AppointmentConflict {

    private static final String DAY_OF_WEEK_PATTERN = "EEEE";
    private final SimpleDateFormat DayFormat = new SimpleDateFormat(DAY_OF_WEEK_PATTERN);

    @Override
    public AppointmentConflictType getType() {
        return SERVICE_UNAVAILABLE;
    }

    @Override
    public List<Appointment> getConflicts(List<Appointment> appointments) {
     List<Appointment> conflictingAppointments = new ArrayList<>();
        for (Appointment appointment : appointments) {
            AppointmentServiceDefinition appointmentServiceDefinition = appointment.getService();
            boolean isConflicting = checkConflicts(appointment, appointmentServiceDefinition);
            if (isConflicting)
                conflictingAppointments.add(appointment);
        }
        return conflictingAppointments;
    }

    private boolean checkConflicts(Appointment appointment, AppointmentServiceDefinition service) {
        ZoneId zone = DateUtil.getLocationZone(appointment.getLocation()).orElse(null);
        Set<ServiceWeeklyAvailability> weeklyAvailableDays = service.getWeeklyAvailability();
        if (isObjectPresent(weeklyAvailableDays)) {
            String appointmentDay = zone != null
                    ? appointment.getStartDateTime().toInstant().atZone(zone).getDayOfWeek().toString()
                    : DayFormat.format(appointment.getStartDateTime());
            List<ServiceWeeklyAvailability> dayAvailabilities = weeklyAvailableDays.stream()
                    .filter(day -> day.isSameDay(appointmentDay)).collect(Collectors.toList());
            if (!dayAvailabilities.isEmpty())
                return dayAvailabilities.stream().allMatch(availableDay ->
                        checkTimeAvailability(appointment, availableDay.getStartTime().getTime(), availableDay.getEndTime().getTime(), zone));
            return true;
        }
        Time serviceStartTime = service.getStartTime();
        Time serviceEndTime = service.getEndTime();
        long serviceStartMillis = serviceStartTime != null ? serviceStartTime.getTime() : DateUtil.getStartOfDay().getTime();
        long serviceEndMillis = serviceEndTime != null ? serviceEndTime.getTime() : DateUtil.getEndOfDay().getTime();
        return checkTimeAvailability(appointment, serviceStartMillis, serviceEndMillis, zone);
    }

    private boolean isObjectPresent(Collection<?> object) {
        return Objects.nonNull(object) && !object.isEmpty();
    }

    private boolean checkTimeAvailability(Appointment appointment, long serviceStartTime, long serviceEndTime, ZoneId zone) {
        long appointmentStartMs = zone != null
                ? getEpochTime(appointment.getStartDateTime().getTime(), zone)
                : getEpochTime(appointment.getStartDateTime().getTime());
        long appointmentEndMs = zone != null
                ? getEpochTime(appointment.getEndDateTime().getTime(), zone)
                : getEpochTime(appointment.getEndDateTime().getTime());
        long serviceStartMs = getEpochTime(serviceStartTime);
        long serviceEndMs = getEpochTime(serviceEndTime);
        return (appointmentStartMs >= appointmentEndMs)
                || (appointmentStartMs < serviceStartMs)
                || (appointmentEndMs > serviceEndMs);
    }
}
