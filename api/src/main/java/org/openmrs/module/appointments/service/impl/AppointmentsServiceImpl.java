package org.openmrs.module.appointments.service.impl;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.openmrs.api.APIException;
import org.openmrs.module.appointments.dao.AppointmentAuditDao;
import org.openmrs.module.appointments.dao.AppointmentDao;
import org.openmrs.module.appointments.model.*;
import org.openmrs.module.appointments.service.AppointmentsService;
import org.openmrs.module.appointments.validator.AppointmentStatusChangeValidator;
import org.openmrs.module.appointments.validator.AppointmentValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AppointmentsServiceImpl implements AppointmentsService {

    @Autowired
    AppointmentDao appointmentDao;

    @Autowired
    List<AppointmentStatusChangeValidator> statusChangeValidators;

    @Autowired
    List<AppointmentValidator> appointmentValidators;

    @Autowired
    AppointmentAuditDao appointmentAuditDao;

    @Override
    public Appointment save(Appointment appointment) {
        if(!CollectionUtils.isEmpty(appointmentValidators)){
            List<String> errors = new ArrayList<>();
            for(AppointmentValidator validator: appointmentValidators){
                validator.validate(appointment, errors);
            }
            if(!errors.isEmpty()) {
                String message = StringUtils.join(errors, "\n");
                throw new APIException(message);
            }
        }
        appointmentDao.save(appointment);
        return appointment;
    }

    @Override
    public List<Appointment> getAllAppointments(Date forDate) {
        List<Appointment> appointments = appointmentDao.getAllAppointments(forDate);
        return appointments.stream().filter(appointment -> !isServiceOrServiceTypeVoided(appointment)).collect(Collectors.toList());
    }
    
    private boolean isServiceOrServiceTypeVoided(Appointment appointment){
        return (appointment.getService() != null && appointment.getService().getVoided()) ||
               (appointment.getServiceType() != null && appointment.getServiceType().getVoided());
    }

    @Override
    public List<Appointment> search(Appointment appointment) {
        List<Appointment> appointments = appointmentDao.search(appointment);
        return appointments.stream().filter(searchedAppointment -> !isServiceOrServiceTypeVoided(searchedAppointment)).collect(Collectors.toList());
    }

    @Override
    public List<Appointment> getAllFutureAppointmentsForService(AppointmentService appointmentService) {
        return appointmentDao.getAllFutureAppointmentsForService(appointmentService);
    }

    @Override
    public List<Appointment> getAllFutureAppointmentsForServiceType(AppointmentServiceType appointmentServiceType) {
        return appointmentDao.getAllFutureAppointmentsForServiceType(appointmentServiceType);
    }

    @Override
    public List<Appointment> getAppointmentsForService(AppointmentService appointmentService, Date startDate, Date endDate, List<AppointmentStatus> appointmentStatusList) {
        return appointmentDao.getAppointmentsForService(appointmentService, startDate, endDate, appointmentStatusList);
    }

    @Override
    public Appointment getAppointmentByUuid(String uuid) {
        Appointment appointment = appointmentDao.getAppointmentByUuid(uuid);
        return appointment;
    }

    @Override
    public void changeStatus(Appointment appointment, String status, Date onDate) throws APIException{
        List<String> errors = new ArrayList<>();
        AppointmentStatus appointmentStatus = AppointmentStatus.valueOf(status);
        validateStatusChange(appointment, appointmentStatus, errors);
        if(errors.isEmpty()) {
            appointment.setStatus(appointmentStatus);
            createStatusChangeEventInAppointmentAudit(appointment, appointmentStatus, onDate);
            appointmentDao.save(appointment);
        }
        else {
            String message = StringUtils.join(errors, "\n");
            throw new APIException(message);
        }
    }

    @Override
    public List<Appointment> getAllAppointmentsInDateRange(Date startDate, Date endDate) {
        List<Appointment> appointments = appointmentDao.getAllAppointmentsInDateRange(startDate, endDate);
        return appointments.stream().filter(appointment -> !isServiceOrServiceTypeVoided(appointment)).collect(Collectors.toList());
    }

    private void createStatusChangeEventInAppointmentAudit(Appointment appointment, AppointmentStatus appointmentStatus,
            Date onDate) {
        AppointmentAudit appointmentAuditEvent = new AppointmentAudit();
        appointmentAuditEvent.setAppointment(appointment);
        appointmentAuditEvent.setStatus(appointmentStatus);
        if(onDate != null)
            appointmentAuditEvent.setNotes(onDate.toInstant().toString());
        appointmentAuditDao.save(appointmentAuditEvent);
    }

    private void validateStatusChange(Appointment appointment, AppointmentStatus status, List<String> errors) {
        if(!CollectionUtils.isEmpty(statusChangeValidators)) {
            for (AppointmentStatusChangeValidator validator : statusChangeValidators) {
                validator.validate(appointment, status, errors);
            }
        }
    }
}
