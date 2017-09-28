package org.openmrs.module.appointments.service;


import org.openmrs.annotation.Authorized;
import org.openmrs.module.appointments.model.AppointmentService;
import org.openmrs.module.appointments.model.AppointmentServiceType;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Time;
import java.util.Date;
import java.util.List;
import java.util.Map;

public interface AppointmentServiceService {

    @Transactional
    @Authorized({"Manage Appointment Services"})
    AppointmentService save(AppointmentService appointmentService);

    @Transactional
    @Authorized({"View Appointment Services"})
    List<AppointmentService> getAllAppointmentServices(boolean includeVoided);

    @Transactional
    @Authorized({"View Appointment Services"})
    AppointmentService getAppointmentServiceByUuid(String uuid);

    @Transactional
    @Authorized({"Manage Appointment Services"})
    AppointmentService voidAppointmentService(AppointmentService appointmentService, String voidReason);

    @Transactional
    @Authorized({"View Appointment Services"})
    AppointmentServiceType getAppointmentServiceTypeByUuid(String serviceTypeUuid);

    @Transactional
    @Authorized({"View Appointment Services"})
	Integer calculateCurrentLoad(AppointmentService appointmentService, Date startDateTime, Date endDateTime);
}

