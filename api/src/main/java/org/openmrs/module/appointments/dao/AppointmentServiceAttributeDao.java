package org.openmrs.module.appointments.dao;

import org.openmrs.module.appointments.model.AppointmentServiceAttribute;

import java.util.List;

public interface AppointmentServiceAttributeDao {
    List<AppointmentServiceAttribute> getAttributesByService(Integer serviceId, boolean includeVoided);

    AppointmentServiceAttribute getAttributeByUuid(String uuid);

    AppointmentServiceAttribute save(AppointmentServiceAttribute attribute);
}
