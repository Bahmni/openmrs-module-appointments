package org.openmrs.module.appointments.dao;

import org.openmrs.module.appointments.model.AppointmentUnavailability;
import org.openmrs.module.appointments.model.AppointmentUnavailabilitySearchParams;

import java.util.List;

public interface AppointmentUnavailabilityDao {

    AppointmentUnavailability save(AppointmentUnavailability appointmentUnavailability);

    AppointmentUnavailability getByUuid(String uuid);

    List<AppointmentUnavailability> getAll(AppointmentUnavailabilitySearchParams searchParams);
}
