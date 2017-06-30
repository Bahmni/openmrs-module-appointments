package org.openmrs.module.appointments.service;

import org.openmrs.module.appointments.model.Speciality;

public interface SpecialityService {
    Speciality getSpecialityByUuid(String uuid);
}
