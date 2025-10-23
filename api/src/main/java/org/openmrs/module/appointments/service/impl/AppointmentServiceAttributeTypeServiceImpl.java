package org.openmrs.module.appointments.service.impl;

import org.openmrs.User;
import org.openmrs.api.context.Context;
import org.openmrs.module.appointments.dao.AppointmentServiceAttributeTypeDao;
import org.openmrs.module.appointments.model.AppointmentServiceAttributeType;
import org.openmrs.module.appointments.service.AppointmentServiceAttributeTypeService;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

@Transactional
public class AppointmentServiceAttributeTypeServiceImpl implements AppointmentServiceAttributeTypeService {

    private AppointmentServiceAttributeTypeDao appointmentServiceAttributeTypeDao;

    public void setAppointmentServiceAttributeTypeDao(AppointmentServiceAttributeTypeDao appointmentServiceAttributeTypeDao) {
        this.appointmentServiceAttributeTypeDao = appointmentServiceAttributeTypeDao;
    }

    @Override
    public List<AppointmentServiceAttributeType> getAllAttributeTypes(boolean includeRetired) {
        return appointmentServiceAttributeTypeDao.getAllAttributeTypes(includeRetired);
    }

    @Override
    public AppointmentServiceAttributeType getAttributeTypeByUuid(String uuid) {
        return appointmentServiceAttributeTypeDao.getAttributeTypeByUuid(uuid);
    }

    @Override
    public AppointmentServiceAttributeType save(AppointmentServiceAttributeType attributeType) {
        return appointmentServiceAttributeTypeDao.save(attributeType);
    }

    @Override
    public AppointmentServiceAttributeType retire(AppointmentServiceAttributeType attributeType, String retireReason) {
        attributeType.setRetired(true);
        attributeType.setRetireReason(retireReason);
        attributeType.setRetiredBy(Context.getAuthenticatedUser());
        attributeType.setDateRetired(new Date());
        return appointmentServiceAttributeTypeDao.save(attributeType);
    }
}
