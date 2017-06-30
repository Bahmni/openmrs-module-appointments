package org.openmrs.module.appointments.dao;

import org.openmrs.module.appointments.model.Speciality;

public interface SpecialityDao {
    Speciality getSpecialityByUuid(String uuid);
}
