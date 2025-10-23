package org.openmrs.module.appointments.service.impl;

import org.openmrs.api.context.Context;
import org.openmrs.module.appointments.dao.AppointmentServiceDao;
import org.openmrs.module.appointments.model.Appointment;
import org.openmrs.module.appointments.model.AppointmentServiceAttribute;
import org.openmrs.module.appointments.model.AppointmentServiceDefinition;
import org.openmrs.module.appointments.model.AppointmentServiceType;
import org.openmrs.module.appointments.model.AppointmentStatus;
import org.openmrs.module.appointments.model.ServiceWeeklyAvailability;
import org.openmrs.module.appointments.service.AppointmentServiceAttributeService;
import org.openmrs.module.appointments.service.AppointmentServiceDefinitionService;
import org.openmrs.module.appointments.service.AppointmentsService;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
@Transactional
public class AppointmentServiceDefinitionServiceImpl implements AppointmentServiceDefinitionService {

    AppointmentServiceDao appointmentServiceDao;

    AppointmentsService appointmentsService;

    AppointmentServiceAttributeService appointmentServiceAttributeService;

    public void setAppointmentServiceDao(AppointmentServiceDao appointmentServiceDao) {
        this.appointmentServiceDao = appointmentServiceDao;
    }

    public void setAppointmentsService(AppointmentsService appointmentsService) {
        this.appointmentsService = appointmentsService;
    }

    public void setAppointmentServiceAttributeService(AppointmentServiceAttributeService appointmentServiceAttributeService) {
        this.appointmentServiceAttributeService = appointmentServiceAttributeService;
    }

    @Override
    public AppointmentServiceDefinition save(AppointmentServiceDefinition appointmentServiceDefinition) {
        AppointmentServiceDefinition service = appointmentServiceDao.getNonVoidedAppointmentServiceByName(appointmentServiceDefinition.getName());
        if(service != null && !service.getUuid().equals(appointmentServiceDefinition.getUuid())) {
            throw new RuntimeException("The service '" + appointmentServiceDefinition.getName() + "' is already present");
        }
        return appointmentServiceDao.save(appointmentServiceDefinition);
    }

    @Override
    public List<AppointmentServiceDefinition> getAllAppointmentServices(boolean includeVoided) {
        List<AppointmentServiceDefinition> appointmentServiceDefinitions = appointmentServiceDao.getAllAppointmentServices(includeVoided);
        for (AppointmentServiceDefinition service : appointmentServiceDefinitions) {
            List<AppointmentServiceAttribute> attributes = appointmentServiceAttributeService.getAttributesByService(service.getAppointmentServiceId(), false);
            service.setAttributes(new HashSet<>(attributes));
        }
        return appointmentServiceDefinitions;
    }

    @Override
    public AppointmentServiceDefinition getAppointmentServiceByUuid(String uuid) {
        AppointmentServiceDefinition appointmentServiceDefinition = appointmentServiceDao.getAppointmentServiceByUuid(uuid);
        if (appointmentServiceDefinition != null) {
            List<AppointmentServiceAttribute> attributes = appointmentServiceAttributeService.getAttributesByService(appointmentServiceDefinition.getAppointmentServiceId(), false);
            appointmentServiceDefinition.setAttributes(new HashSet<>(attributes));
        }
        return appointmentServiceDefinition;
    }

    @Override
    public AppointmentServiceDefinition voidAppointmentService(AppointmentServiceDefinition appointmentServiceDefinition, String voidReason) {
        List<Appointment> allFutureAppointmentsForService = appointmentsService.getAllFutureAppointmentsForService(appointmentServiceDefinition);
        if (allFutureAppointmentsForService.size() > 0) {
            throw new RuntimeException("Please cancel all future appointments for this service to proceed. After deleting this service, you will not be able to see any appointments for it");
        }
        setVoidInfoForAppointmentService(appointmentServiceDefinition, voidReason);
        return appointmentServiceDao.save(appointmentServiceDefinition);
    }

    @Override
    public AppointmentServiceType getAppointmentServiceTypeByUuid(String serviceTypeUuid) {
        return appointmentServiceDao.getAppointmentServiceTypeByUuid(serviceTypeUuid);
    }

    @Override
    public Integer calculateCurrentLoad(AppointmentServiceDefinition appointmentServiceDefinition, Date startDateTime, Date endDateTime) {
        AppointmentStatus[] includeStatus = new AppointmentStatus[]{AppointmentStatus.CheckedIn, AppointmentStatus.Completed, AppointmentStatus.Scheduled};
        List<Appointment> appointmentsForService = appointmentsService
                .getAppointmentsForService(appointmentServiceDefinition, startDateTime, endDateTime, Arrays.asList(includeStatus));
        return appointmentsForService.size();
    }

    private void setVoidInfoForAppointmentService(AppointmentServiceDefinition appointmentServiceDefinition, String voidReason) {
        setVoidInfoForService(appointmentServiceDefinition, voidReason);
        setVoidInfoForWeeklyAvailability(appointmentServiceDefinition, voidReason);
        setVoidInfoForServiceTypes(appointmentServiceDefinition, voidReason);
    }

    private void setVoidInfoForService(AppointmentServiceDefinition appointmentServiceDefinition, String voidReason) {
        appointmentServiceDefinition.setVoided(true);
        appointmentServiceDefinition.setDateVoided(new Date());
        appointmentServiceDefinition.setVoidedBy(Context.getAuthenticatedUser());
        appointmentServiceDefinition.setVoidReason(voidReason);
    }

    private void setVoidInfoForServiceTypes(AppointmentServiceDefinition appointmentServiceDefinition, String voidReason) {
        for (AppointmentServiceType appointmentServiceType : appointmentServiceDefinition.getServiceTypes()) {
            appointmentServiceType.setVoided(true);
            appointmentServiceType.setDateVoided(new Date());
            appointmentServiceType.setVoidedBy(Context.getAuthenticatedUser());
            appointmentServiceType.setVoidReason(voidReason);
        }
    }

    private void setVoidInfoForWeeklyAvailability(AppointmentServiceDefinition appointmentServiceDefinition, String voidReason) {
        for (ServiceWeeklyAvailability serviceWeeklyAvailability : appointmentServiceDefinition.getWeeklyAvailability()) {
            serviceWeeklyAvailability.setVoided(true);
            serviceWeeklyAvailability.setDateVoided(new Date());
            serviceWeeklyAvailability.setVoidedBy(Context.getAuthenticatedUser());
            serviceWeeklyAvailability.setVoidReason(voidReason);
        }
    }

}
