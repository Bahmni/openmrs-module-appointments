package org.openmrs.module.appointments.service;

import org.openmrs.annotation.Authorized;
import org.openmrs.module.appointments.model.Speciality;

import java.util.List;

public interface SpecialityService {

    @Authorized({"View Appointments"})
    Speciality getSpecialityByUuid(String uuid);

    @Authorized({"View Appointments"})
    List<Speciality> getAllSpecialities();
}
