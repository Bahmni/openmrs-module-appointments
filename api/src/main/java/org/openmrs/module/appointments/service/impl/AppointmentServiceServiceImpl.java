package org.openmrs.module.appointments.service.impl;

import org.openmrs.api.context.Context;
import org.openmrs.module.appointments.dao.AppointmentServiceDao;
import org.openmrs.module.appointments.model.AppointmentService;
import org.openmrs.module.appointments.model.AppointmentServiceType;
import org.openmrs.module.appointments.model.ServiceWeeklyAvailability;
import org.openmrs.module.appointments.service.AppointmentServiceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

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

    @Override
    public AppointmentService voidAppointmentService(AppointmentService appointmentService, String voidReason) {
        setVoidInfoForAppointmentService(appointmentService, voidReason);
        return appointmentServiceDao.save(appointmentService);
    }

    private void setVoidInfoForAppointmentService(AppointmentService appointmentService, String voidReason) {
        setVoidInfoForService(appointmentService, voidReason);
        setVoidInfoForWeeklyAvailability(appointmentService, voidReason);
        setVoidInfoForServiceTypes(appointmentService, voidReason);
    }

    private void setVoidInfoForService(AppointmentService appointmentService, String voidReason) {
        appointmentService.setVoided(true);
        appointmentService.setDateVoided(new Date());
        appointmentService.setVoidedBy(Context.getAuthenticatedUser());
        appointmentService.setVoidReason(voidReason);
    }

    private void setVoidInfoForServiceTypes(AppointmentService appointmentService, String voidReason) {
        for (AppointmentServiceType appointmentServiceType : appointmentService.getServiceTypes()) {
            appointmentServiceType.setVoided(true);
            appointmentServiceType.setDateVoided(new Date());
            appointmentServiceType.setVoidedBy(Context.getAuthenticatedUser());
            appointmentServiceType.setVoidReason(voidReason);
        }
    }

    private void setVoidInfoForWeeklyAvailability(AppointmentService appointmentService, String voidReason) {
        for (ServiceWeeklyAvailability serviceWeeklyAvailability : appointmentService.getWeeklyAvailability()) {
            serviceWeeklyAvailability.setVoided(true);
            serviceWeeklyAvailability.setDateVoided(new Date());
            serviceWeeklyAvailability.setVoidedBy(Context.getAuthenticatedUser());
            serviceWeeklyAvailability.setVoidReason(voidReason);
        }
    }

}
