package org.openmrs.module.appointments.service;

import org.openmrs.annotation.Authorized;
import org.openmrs.module.appointments.model.AppointmentUnavailability;
import org.openmrs.module.appointments.model.AppointmentUnavailabilitySearchParams;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.openmrs.module.appointments.constants.PrivilegeConstants.ADD_APPOINTMENT_UNAVAILABILITY;
import static org.openmrs.module.appointments.constants.PrivilegeConstants.DELETE_APPOINTMENT_UNAVAILABILITY;
import static org.openmrs.module.appointments.constants.PrivilegeConstants.GET_APPOINTMENT_UNAVAILABILITY;

public interface AppointmentUnavailabilityService {

    @Transactional
    @Authorized({ADD_APPOINTMENT_UNAVAILABILITY})
    List<AppointmentUnavailability> save(List<AppointmentUnavailability> appointmentUnavailabilities);

    @Transactional
    @Authorized({GET_APPOINTMENT_UNAVAILABILITY})
    AppointmentUnavailability getByUuid(String uuid);

    @Transactional
    @Authorized({GET_APPOINTMENT_UNAVAILABILITY})
    List<AppointmentUnavailability> getAll(AppointmentUnavailabilitySearchParams searchParams);

    @Transactional
    @Authorized({DELETE_APPOINTMENT_UNAVAILABILITY})
    void voidAppointmentUnavailability(AppointmentUnavailability appointmentUnavailability, String voidReason);
}
