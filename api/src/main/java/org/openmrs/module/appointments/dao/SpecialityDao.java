package org.openmrs.module.appointments.dao;

import org.openmrs.module.appointments.model.Speciality;

import java.util.List;

public interface SpecialityDao {
    Speciality getSpecialityByUuid(String uuid);
    public List<Speciality> getAllSpecialities();
}
