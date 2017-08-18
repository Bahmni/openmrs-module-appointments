package org.openmrs.module.appointments.service;


import org.openmrs.module.appointments.model.AppointmentService;
import org.openmrs.module.appointments.model.AppointmentServiceType;

import java.sql.Time;
import java.util.Date;
import java.util.List;
import java.util.Map;

public interface AppointmentServiceService {

    AppointmentService save(AppointmentService appointmentService);

    List<AppointmentService> getAllAppointmentServices(boolean includeVoided);

    AppointmentService getAppointmentServiceByUuid(String uuid);

    AppointmentService voidAppointmentService(AppointmentService appointmentService, String voidReason);

    AppointmentServiceType getAppointmentServiceTypeByUuid(String serviceTypeUuid);

	Integer calculateCurrentLoad(AppointmentService appointmentService, Date startDateTime, Date endDateTime);
}

