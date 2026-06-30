package org.openmrs.module.appointments.conflicts.impl;

import org.openmrs.Provider;
import org.openmrs.module.appointments.conflicts.AppointmentConflict;
import org.openmrs.module.appointments.model.Appointment;
import org.openmrs.module.appointments.model.AppointmentConflictType;
import org.openmrs.module.appointments.model.AppointmentProvider;
import org.openmrs.module.appointments.model.AppointmentUnavailability;
import org.openmrs.module.appointments.search.param.AppointmentUnavailabilitySearchParams;
import org.openmrs.module.appointments.service.AppointmentUnavailabilityService;
import org.openmrs.module.appointments.util.DateUtil;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static org.openmrs.module.appointments.model.AppointmentConflictType.UNAVAILABILITY;

public class AppointmentUnavailabilityConflict implements AppointmentConflict {

    private AppointmentUnavailabilityService appointmentUnavailabilityService;

    public void setAppointmentUnavailabilityService(AppointmentUnavailabilityService appointmentUnavailabilityService) {
        this.appointmentUnavailabilityService = appointmentUnavailabilityService;
    }

    @Override
    public AppointmentConflictType getType() {
        return UNAVAILABILITY;
    }

    @Override
    public List<Appointment> getConflicts(List<Appointment> appointments) {
        return appointments.stream()
                .filter(this::hasConflict)
                .collect(Collectors.toList());
    }

    private boolean hasConflict(Appointment appointment) {
        ZoneId zone = DateUtil.getLocationZone(appointment.getLocation()).orElse(ZoneId.systemDefault());
        ZonedDateTime apptStart = appointment.getStartDateTime().toInstant().atZone(zone);
        ZonedDateTime apptEnd = appointment.getEndDateTime().toInstant().atZone(zone);
        List<AppointmentUnavailability> unavailabilities = getUnavailabilitiesForAppointment(apptStart, apptEnd);
        return unavailabilities.stream().anyMatch(unavailability -> isConflict(appointment, unavailability, apptStart, apptEnd));
    }

    private List<AppointmentUnavailability> getUnavailabilitiesForAppointment(ZonedDateTime apptStart, ZonedDateTime apptEnd) {
        AppointmentUnavailabilitySearchParams params = new AppointmentUnavailabilitySearchParams();
        params.setStartDate(apptStart.toLocalDate().toString());
        params.setEndDate(apptEnd.toLocalDate().toString());
        return appointmentUnavailabilityService.getAll(params);
    }

    private boolean isConflict(Appointment appointment, AppointmentUnavailability unavailability, ZonedDateTime apptStart, ZonedDateTime apptEnd) {
        if (!overlaps(apptStart, apptEnd, unavailability)) return false;
        if (unavailability.getLocation() != null && !Objects.equals(unavailability.getLocation(), appointment.getLocation())) return false;
        if (unavailability.getService() != null && !Objects.equals(unavailability.getService(), appointment.getService())) return false;
        if (unavailability.getProvider() != null && !hasMatchingProvider(appointment, unavailability.getProvider())) return false;
        return true;
    }

    private boolean overlaps(ZonedDateTime apptStart, ZonedDateTime apptEnd, AppointmentUnavailability unavailability) {
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
