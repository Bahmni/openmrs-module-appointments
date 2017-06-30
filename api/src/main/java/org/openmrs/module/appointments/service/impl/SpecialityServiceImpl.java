package org.openmrs.module.appointments.service.impl;

import org.openmrs.module.appointments.dao.SpecialityDao;
import org.openmrs.module.appointments.model.Speciality;
import org.openmrs.module.appointments.service.SpecialityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SpecialityServiceImpl implements SpecialityService {
    @Autowired
    SpecialityDao specialityDao;

    public Speciality getSpecialityByUuid(String uuid) {
        return specialityDao.getSpecialityByUuid(uuid);
    }
}
