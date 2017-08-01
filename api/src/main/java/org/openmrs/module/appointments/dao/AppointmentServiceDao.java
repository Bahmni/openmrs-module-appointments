package org.openmrs.module.appointments.dao;


import org.openmrs.module.appointments.model.AppointmentService;
import org.openmrs.module.appointments.model.AppointmentServiceType;

import java.util.List;

public interface AppointmentServiceDao {

    List<AppointmentService> getAllAppointmentServices(boolean includeVoided);

    AppointmentService save(AppointmentService appointmentService);

    AppointmentService getAppointmentServiceByUuid(String uuid);

    AppointmentService getNonVoidedAppointmentServiceByName(String serviceName);

    AppointmentServiceType getAppointmentServiceTypeByUuid(String uuid);
}
