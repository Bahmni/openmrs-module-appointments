package org.openmrs.module.appointments.service;


import org.openmrs.module.appointments.model.AppointmentService;
import org.openmrs.module.appointments.model.AppointmentServiceType;

import java.util.List;

public interface AppointmentServiceService {

    AppointmentService save(AppointmentService appointmentService);

    List<AppointmentService> getAllAppointmentServices(boolean includeVoided);

    AppointmentService getAppointmentServiceByUuid(String uuid);

    AppointmentService voidAppointmentService(AppointmentService appointmentService, String voidReason);

    AppointmentServiceType getAppointmentServiceTypeByUuid(String serviceTypeUuid);
}

