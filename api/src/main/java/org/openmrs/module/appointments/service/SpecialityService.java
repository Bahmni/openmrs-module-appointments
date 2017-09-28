package org.openmrs.module.appointments.service;

import org.openmrs.annotation.Authorized;
import org.openmrs.module.appointments.model.Speciality;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface SpecialityService {

    @Transactional
    @Authorized({"View Appointments"})
    Speciality getSpecialityByUuid(String uuid);

    @Transactional
    @Authorized({"View Appointments"})
    List<Speciality> getAllSpecialities();
}
