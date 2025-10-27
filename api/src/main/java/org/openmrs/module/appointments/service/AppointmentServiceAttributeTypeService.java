package org.openmrs.module.appointments.service;

import org.openmrs.annotation.Authorized;
import org.openmrs.module.appointments.model.AppointmentServiceAttributeType;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface AppointmentServiceAttributeTypeService {

    @Transactional(readOnly = true)
    @Authorized({"View Appointments"})
    List<AppointmentServiceAttributeType> getAllAttributeTypes(boolean includeRetired);

    @Transactional(readOnly = true)
    @Authorized({"View Appointments"})
    AppointmentServiceAttributeType getAttributeTypeByUuid(String uuid);

    @Transactional
    @Authorized({"Manage Appointment Services"})
    AppointmentServiceAttributeType save(AppointmentServiceAttributeType attributeType);

    @Transactional
    @Authorized({"Manage Appointment Services"})
    AppointmentServiceAttributeType retire(AppointmentServiceAttributeType attributeType, String retireReason);
}
