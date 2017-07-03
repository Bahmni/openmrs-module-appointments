package org.openmrs.module.appointments.service;

import org.openmrs.module.appointments.model.Speciality;

import java.util.List;

public interface SpecialityService {
    Speciality getSpecialityByUuid(String uuid);
    List<Speciality> getAllSpecialities();
}
