package org.openmrs.module.appointments.service;

import java.util.List;

import org.openmrs.annotation.Authorized;
import org.openmrs.module.appointments.model.Speciality;
import org.springframework.transaction.annotation.Transactional;

public interface SpecialityService {

    @Transactional
    @Authorized({"View Appointments"})
    Speciality getSpecialityByUuid(String uuid);

    @Transactional
    @Authorized({"View Appointments"})
    List<Speciality> getAllSpecialities();
    
    @Transactional
    @Authorized({"Manage Appointment Specialities"})
    Speciality save(Speciality speciality);
}
