package org.openmrs.module.appointments.service;

import org.openmrs.annotation.Authorized;
import org.openmrs.module.appointments.model.AppointmentServiceAttribute;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface AppointmentServiceAttributeService {

    @Transactional(readOnly = true)
    @Authorized({"View Appointments"})
    List<AppointmentServiceAttribute> getAttributesByService(Integer serviceId, boolean includeVoided);

    @Transactional(readOnly = true)
    @Authorized({"View Appointments"})
    AppointmentServiceAttribute getAttributeByUuid(String uuid);

    @Transactional
    @Authorized({"Manage Appointment Services"})
    AppointmentServiceAttribute save(AppointmentServiceAttribute attribute);

    @Transactional
    @Authorized({"Manage Appointment Services"})
    AppointmentServiceAttribute voidAttribute(AppointmentServiceAttribute attribute, String voidReason);
}
