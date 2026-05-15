package org.openmrs.module.appointments.dao;

import org.openmrs.Location;
import org.openmrs.Provider;
import org.openmrs.module.appointments.model.AppointmentServiceDefinition;
import org.openmrs.module.appointments.model.AppointmentUnavailability;

import java.util.Date;
import java.util.List;

public interface AppointmentUnavailabilityDao {

    AppointmentUnavailability save(AppointmentUnavailability appointmentUnavailability);

    AppointmentUnavailability getByUuid(String uuid);

    List<AppointmentUnavailability> getAll(Location location, AppointmentServiceDefinition service,
                                           Provider provider, Date startDate, Date endDate,
                                           boolean includeVoided, Integer limit);
}
