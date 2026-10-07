package org.openmrs.module.appointments.dao;

import org.openmrs.module.appointments.model.AppointmentServiceAttributeType;

import java.util.List;

public interface AppointmentServiceAttributeTypeDao {
    List<AppointmentServiceAttributeType> getAllAttributeTypes(boolean includeRetired);

    AppointmentServiceAttributeType getAttributeTypeByUuid(String uuid);

    AppointmentServiceAttributeType getAttributeTypeById(Integer id);

    AppointmentServiceAttributeType save(AppointmentServiceAttributeType attributeType);
}
