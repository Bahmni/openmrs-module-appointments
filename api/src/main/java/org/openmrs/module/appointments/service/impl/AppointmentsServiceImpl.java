package org.openmrs.module.appointments.service.impl;

import java.util.Date;
import java.util.stream.Collectors;
import org.openmrs.module.appointments.dao.AppointmentDao;
import org.openmrs.module.appointments.model.Appointment;
import org.openmrs.module.appointments.model.AppointmentService;
import org.openmrs.module.appointments.model.AppointmentServiceType;
import org.openmrs.module.appointments.service.AppointmentsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AppointmentsServiceImpl implements AppointmentsService {

    @Autowired
    AppointmentDao appointmentDao;

    @Override
    public Appointment save(Appointment appointment) {
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
    public List<Appointment> Search(Appointment appointment) {
        return appointmentDao.search(appointment);
    }

    @Override
    public List<Appointment> getAllFutureAppointmentsForService(AppointmentService appointmentService) {
        return appointmentDao.getAllFutureAppointmentsForService(appointmentService);
    }

    @Override
    public List<Appointment> getAllFutureAppointmentsForServiceType(AppointmentServiceType appointmentServiceType) {
        return appointmentDao.getAllFutureAppointmentsForServiceType(appointmentServiceType);
    }
}
