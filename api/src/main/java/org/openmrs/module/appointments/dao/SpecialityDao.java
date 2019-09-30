package org.openmrs.module.appointments.dao;

import java.util.List;

import org.openmrs.module.appointments.model.Speciality;

public interface SpecialityDao {
    Speciality getSpecialityByUuid(String uuid);
    public List<Speciality> getAllSpecialities();
    Speciality save(Speciality speciality);
}
