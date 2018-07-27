package org.openmrs.module.appointments.dao;


import org.openmrs.module.appointments.model.AppointmentServiceDefinition;
import org.openmrs.module.appointments.model.AppointmentServiceType;

import java.util.List;

public interface AppointmentServiceDao {

    List<AppointmentServiceDefinition> getAllAppointmentServices(boolean includeVoided);

    AppointmentServiceDefinition save(AppointmentServiceDefinition appointmentServiceDefinition);

    AppointmentServiceDefinition getAppointmentServiceByUuid(String uuid);

    AppointmentServiceDefinition getNonVoidedAppointmentServiceByName(String serviceName);

    AppointmentServiceType getAppointmentServiceTypeByUuid(String uuid);
}
