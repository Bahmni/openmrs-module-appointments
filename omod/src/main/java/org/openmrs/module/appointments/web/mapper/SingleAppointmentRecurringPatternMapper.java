package org.openmrs.module.appointments.web.mapper;

import org.codehaus.jackson.map.ObjectMapper;
import org.openmrs.api.APIException;
import org.openmrs.module.appointments.model.Appointment;
import org.openmrs.module.appointments.model.AppointmentRecurringPattern;
import org.openmrs.module.appointments.service.AppointmentsService;
import org.openmrs.module.appointments.web.contract.AppointmentRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashSet;

@Component
@Qualifier("singleAppointmentRecurringPatternMapper")
public class SingleAppointmentRecurringPatternMapper extends AbstractAppointmentRecurringPatternMapper {

    @Autowired
    private AppointmentsService appointmentsService;

    @Autowired
    private AppointmentMapper appointmentMapper;

    @Override
    public AppointmentRecurringPattern fromRequest(AppointmentRequest appointmentRequest){
        String uuid = appointmentRequest.getUuid();
        Appointment appointment = appointmentsService.getAppointmentByUuid(uuid);
        Appointment newAppointment;
        if(appointment==null)
            throw new APIException("Appointment does not exist.");
        if(appointment.getRelatedAppointment()!=null){
            newAppointment = appointment;
        }
        else {
            newAppointment = getNewAppointmentFrom(appointment);
            appointment.setVoided(true);
            newAppointment.setRelatedAppointment(appointment);
        }
        appointmentMapper.mapAppointmentRequestToAppointment(appointmentRequest, newAppointment);
        final AppointmentRecurringPattern appointmentRecurringPattern = appointment.getAppointmentRecurringPattern();
        appointmentRecurringPattern.getAppointments().add(newAppointment);
        return appointmentRecurringPattern;
    }


    private Appointment getNewAppointmentFrom(Appointment oldAppointment){
        Appointment appointment = new Appointment();
        appointment.setService(oldAppointment.getService());
        appointment.setProviders(oldAppointment.getProviders());
        appointment.setStatus(oldAppointment.getStatus());
        appointment.setEndDateTime(oldAppointment.getEndDateTime());
        appointment.setStartDateTime(oldAppointment.getStartDateTime());
        appointment.setServiceType(oldAppointment.getServiceType());
        appointment.setPatient(oldAppointment.getPatient());
        appointment.setLocation(oldAppointment.getLocation());
        appointment.setAppointmentKind(oldAppointment.getAppointmentKind());
        appointment.setComments(oldAppointment.getComments());
        appointment.setAppointmentAudits(new HashSet<>());
        return appointment;
    }

}
