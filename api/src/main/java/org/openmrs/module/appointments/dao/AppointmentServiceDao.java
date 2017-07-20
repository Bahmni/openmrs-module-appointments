package org.openmrs.module.appointments.dao;


import org.openmrs.module.appointments.model.AppointmentService;

import java.util.List;

public interface AppointmentServiceDao {

    List<AppointmentService> getAllAppointmentServices(boolean includeVoided);

    AppointmentService save(AppointmentService appointmentService);

    AppointmentService getAppointmentServiceByUuid(String uuid);
}
