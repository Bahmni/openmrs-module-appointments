package org.openmrs.module.appointments.service.impl;

import org.openmrs.api.context.Context;
import org.openmrs.module.appointments.dao.AppointmentServiceAttributeDao;
import org.openmrs.module.appointments.model.AppointmentServiceAttribute;
import org.openmrs.module.appointments.service.AppointmentServiceAttributeService;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

@Transactional
public class AppointmentServiceAttributeServiceImpl implements AppointmentServiceAttributeService {

    private AppointmentServiceAttributeDao appointmentServiceAttributeDao;

    public void setAppointmentServiceAttributeDao(AppointmentServiceAttributeDao appointmentServiceAttributeDao) {
        this.appointmentServiceAttributeDao = appointmentServiceAttributeDao;
    }

    @Override
    public List<AppointmentServiceAttribute> getAttributesByService(Integer serviceId, boolean includeVoided) {
        return appointmentServiceAttributeDao.getAttributesByService(serviceId, includeVoided);
    }

    @Override
    public AppointmentServiceAttribute getAttributeByUuid(String uuid) {
        return appointmentServiceAttributeDao.getAttributeByUuid(uuid);
    }

    @Override
    public AppointmentServiceAttribute save(AppointmentServiceAttribute attribute) {
        return appointmentServiceAttributeDao.save(attribute);
    }

    @Override
    public AppointmentServiceAttribute voidAttribute(AppointmentServiceAttribute attribute, String voidReason) {
        attribute.setVoided(true);
        attribute.setVoidReason(voidReason);
        attribute.setVoidedBy(Context.getAuthenticatedUser());
        attribute.setDateVoided(new Date());
        return appointmentServiceAttributeDao.save(attribute);
    }
}
