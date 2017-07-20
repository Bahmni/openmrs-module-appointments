package org.openmrs.module.appointments.service.impl;

import org.openmrs.module.appointments.dao.AppointmentServiceDao;
import org.openmrs.module.appointments.model.AppointmentService;
import org.openmrs.module.appointments.service.AppointmentServiceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AppointmentServiceServiceImpl implements AppointmentServiceService {

    @Autowired
    AppointmentServiceDao appointmentServiceDao;

    @Override
    public AppointmentService save(AppointmentService appointmentService) {
        return appointmentServiceDao.save(appointmentService);
    }

    @Override
    public List<AppointmentService> getAllAppointmentServices(boolean includeVoided) {
        return appointmentServiceDao.getAllAppointmentServices(includeVoided);
    }

    @Override
    public AppointmentService getAppointmentServiceByUuid(String uuid) {
        return appointmentServiceDao.getAppointmentServiceByUuid(uuid);
    }

}
