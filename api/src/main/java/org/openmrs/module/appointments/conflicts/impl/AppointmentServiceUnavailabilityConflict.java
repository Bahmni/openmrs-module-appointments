package org.openmrs.module.appointments.conflicts.impl;

import org.openmrs.module.appointments.conflicts.AppointmentConflict;
import org.openmrs.module.appointments.model.AppointmentConflictType;
import org.openmrs.module.appointments.model.Appointment;
import org.openmrs.module.appointments.model.AppointmentServiceDefinition;
import org.openmrs.module.appointments.model.ServiceWeeklyAvailability;

import java.sql.Time;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import static org.openmrs.module.appointments.model.AppointmentConflictType.SERVICE_UNAVAILABLE;
import static org.openmrs.module.appointments.util.DateUtil.getEpochTime;

public class AppointmentServiceUnavailabilityConflict implements AppointmentConflict {

    private final static String DAY_OF_WEEK_PATTERN = "EEEE";
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

    private boolean checkConflicts(Appointment appointment, AppointmentServiceDefinition appointmentServiceDefinition) {
        Set<ServiceWeeklyAvailability> weeklyAvailableDays = appointmentServiceDefinition.getWeeklyAvailability();
        if (isObjectPresent(weeklyAvailableDays)) {
            String appointmentDay = DayFormat.format(appointment.getStartDateTime());
            Optional<ServiceWeeklyAvailability> dayAvailability = weeklyAvailableDays.stream()
                    .filter(day -> day.isSameDay(appointmentDay)).findFirst();
            if (dayAvailability.isPresent()) {
                ServiceWeeklyAvailability availableDay = dayAvailability.get();
                return checkTimeAvailability(appointment, availableDay.getStartTime(), availableDay.getEndTime());
            }
            return true;
        }
        return checkTimeAvailability(appointment,
                appointmentServiceDefinition.getStartTime(), appointmentServiceDefinition.getEndTime());

    }

    private boolean isObjectPresent(Collection<?> object) {
        return Objects.nonNull(object) && !object.isEmpty();
    }

    private boolean checkTimeAvailability(Appointment appointment, Time serviceStartTime, Time serviceEndTime) {
        long appointmentStartTimeMilliSeconds = getEpochTime(appointment.getStartDateTime().getTime());
        long appointmentEndTimeMilliSeconds = getEpochTime(appointment.getEndDateTime().getTime());
        long serviceStartTimeMilliSeconds = getEpochTime(serviceStartTime.getTime());
        long serviceEndTimeMilliSeconds = getEpochTime(serviceEndTime.getTime());
        boolean isConflict = (appointmentStartTimeMilliSeconds >= appointmentEndTimeMilliSeconds)
                || ((appointmentStartTimeMilliSeconds < serviceStartTimeMilliSeconds)
                || (appointmentEndTimeMilliSeconds > serviceEndTimeMilliSeconds));
        return isConflict;
    }
}
