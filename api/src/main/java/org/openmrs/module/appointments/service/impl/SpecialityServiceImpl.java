package org.openmrs.module.appointments.service.impl;

import org.openmrs.module.appointments.dao.SpecialityDao;
import org.openmrs.module.appointments.model.Speciality;
import org.openmrs.module.appointments.service.SpecialityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SpecialityServiceImpl implements SpecialityService {
    @Autowired
    SpecialityDao specialityDao;

    @Override
    public Speciality getSpecialityByUuid(String uuid) {
        return specialityDao.getSpecialityByUuid(uuid);
    }

    @Override
    public List<Speciality> getAllSpecialities() {
        return specialityDao.getAllSpecialities();
    }
}
