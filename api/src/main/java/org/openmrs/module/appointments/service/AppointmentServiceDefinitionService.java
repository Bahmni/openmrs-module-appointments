package org.openmrs.module.appointments.service;


import org.openmrs.annotation.Authorized;
import org.openmrs.module.appointments.model.AppointmentServiceDefinition;
import org.openmrs.module.appointments.model.AppointmentServiceType;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

import static org.openmrs.module.appointments.constants.PrivilegeConstants.MANAGE_APPOINTMENTS_SERVICE;
import static org.openmrs.module.appointments.constants.PrivilegeConstants.VIEW_APPOINTMENTS_SERVICE;

public interface AppointmentServiceDefinitionService {

    @Transactional
    @Authorized({MANAGE_APPOINTMENTS_SERVICE})
    AppointmentServiceDefinition save(AppointmentServiceDefinition appointmentServiceDefinition);

    @Transactional
    @Authorized({VIEW_APPOINTMENTS_SERVICE, MANAGE_APPOINTMENTS_SERVICE})
    List<AppointmentServiceDefinition> getAllAppointmentServices(boolean includeVoided);

    @Transactional
    @Authorized({VIEW_APPOINTMENTS_SERVICE, MANAGE_APPOINTMENTS_SERVICE})
    AppointmentServiceDefinition getAppointmentServiceByUuid(String uuid);

    @Transactional
    @Authorized({MANAGE_APPOINTMENTS_SERVICE})
    AppointmentServiceDefinition voidAppointmentService(AppointmentServiceDefinition appointmentServiceDefinition, String voidReason);

    @Transactional
    @Authorized({VIEW_APPOINTMENTS_SERVICE, MANAGE_APPOINTMENTS_SERVICE})
    AppointmentServiceType getAppointmentServiceTypeByUuid(String serviceTypeUuid);

    @Transactional
    @Authorized({VIEW_APPOINTMENTS_SERVICE, MANAGE_APPOINTMENTS_SERVICE})
    Integer calculateCurrentLoad(AppointmentServiceDefinition appointmentServiceDefinition, Date startDateTime, Date endDateTime);
}

