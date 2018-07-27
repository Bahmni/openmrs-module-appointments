package org.openmrs.module.appointments.service;


import org.openmrs.annotation.Authorized;
import org.openmrs.module.appointments.model.AppointmentServiceDefinition;
import org.openmrs.module.appointments.model.AppointmentServiceType;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

public interface AppointmentServiceDefinitionService {

    @Transactional
    @Authorized({"Manage Appointment Services"})
    AppointmentServiceDefinition save(AppointmentServiceDefinition appointmentServiceDefinition);

    @Transactional
    @Authorized({"View Appointment Services"})
    List<AppointmentServiceDefinition> getAllAppointmentServices(boolean includeVoided);

    @Transactional
    @Authorized({"View Appointment Services"})
    AppointmentServiceDefinition getAppointmentServiceByUuid(String uuid);

    @Transactional
    @Authorized({"Manage Appointment Services"})
    AppointmentServiceDefinition voidAppointmentService(AppointmentServiceDefinition appointmentServiceDefinition, String voidReason);

    @Transactional
    @Authorized({"View Appointment Services"})
    AppointmentServiceType getAppointmentServiceTypeByUuid(String serviceTypeUuid);

    @Transactional
    @Authorized({"View Appointment Services"})
	Integer calculateCurrentLoad(AppointmentServiceDefinition appointmentServiceDefinition, Date startDateTime, Date endDateTime);
}

