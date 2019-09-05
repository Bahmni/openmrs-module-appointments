package org.openmrs.module.appointments.web.service.impl;

import org.openmrs.module.appointments.model.Appointment;
import org.openmrs.module.appointments.model.AppointmentRecurringPattern;
import org.openmrs.module.appointments.web.contract.AppointmentRequest;
import org.openmrs.module.appointments.web.contract.RecurringPattern;
import org.openmrs.module.appointments.web.mapper.AppointmentMapper;
import org.openmrs.module.appointments.web.service.AbstractRecurringAppointmentsGenerationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class DailyRecurringAppointmentsGenerationService extends AbstractRecurringAppointmentsGenerationService {

    private AppointmentRequest appointmentRequest;

    @Autowired
    private AppointmentMapper appointmentMapper;

    @Override
    public List<Appointment> getAppointments(AppointmentRequest appointmentRequest) {
        this.appointmentRequest = appointmentRequest;
        RecurringPattern recurringPattern = appointmentRequest.getRecurringPattern();
        Date endDate = getEndDate(recurringPattern.getPeriod(), recurringPattern.getFrequency(), recurringPattern.getEndDate());
        return generateAppointments(endDate);
    }

    public Date getEndDate(int period, Integer frequency, Date endDate) {
        if (endDate != null) {
            return endDate;
        }
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(appointmentRequest.getStartDateTime());
        calendar.add(Calendar.DAY_OF_MONTH, period * (frequency - 1));
        return calendar.getTime();
    }

    private List<Appointment> generateAppointments(Date endDate) {
        List<Appointment> appointments = new ArrayList<>();
        Calendar startCalender = Calendar.getInstance();
        Calendar endCalender = Calendar.getInstance();
        startCalender.setTime(appointmentRequest.getStartDateTime());
        endCalender.setTime(appointmentRequest.getEndDateTime());
        return createAppointments(appointments, endDate, startCalender, endCalender, appointmentRequest);
    }

    public List<Appointment> addAppointments(AppointmentRecurringPattern appointmentRecurringPattern,
                                             AppointmentRequest appointmentRequest){
        this.appointmentRequest = appointmentRequest;
        List<Appointment> appointments = appointmentRecurringPattern.getAppointments().stream().collect(Collectors.toList());
        Collections.sort(appointments, Comparator.comparing(Appointment::getDateFromStartDateTime));
        appointmentRequest.setStartDateTime(appointments.get(appointments.size()-1).getStartDateTime());
        appointmentRequest.setEndDateTime(appointments.get(appointments.size()-1).getEndDateTime());
        if (appointmentRecurringPattern.getEndDate() == null) appointmentRecurringPattern.setFrequency(appointmentRequest.getRecurringPattern().getFrequency() - appointmentRecurringPattern.getFrequency() + 1 );
        else appointmentRecurringPattern.setEndDate(appointmentRequest.getRecurringPattern().getEndDate());
        Date endDate = getEndDate(appointmentRecurringPattern.getPeriod(), appointmentRecurringPattern.getFrequency(),
                appointmentRecurringPattern.getEndDate());
        Calendar startCalender = Calendar.getInstance();
        Calendar endCalender = Calendar.getInstance();
        startCalender.setTime(appointmentRequest.getStartDateTime());
        endCalender.setTime(appointmentRequest.getEndDateTime());
        startCalender.add(Calendar.DAY_OF_YEAR, appointmentRecurringPattern.getPeriod());
        endCalender.add(Calendar.DAY_OF_YEAR, appointmentRecurringPattern.getPeriod());
        appointmentRequest.setUuid(null);
        return createAppointments(appointments, endDate, startCalender, endCalender, appointmentRequest);
    }

    private List<Appointment> createAppointments(List<Appointment> appointments, Date endDate, Calendar startCalendar,
                                                 Calendar endCalendar, AppointmentRequest appointmentRequest) {
        Date currentAppointmentDate = appointmentRequest.getStartDateTime();
        while (!currentAppointmentDate.after(endDate)) {
            Appointment appointment = appointmentMapper.fromRequest(appointmentRequest);
            appointment.setStartDateTime(startCalendar.getTime());
            appointment.setEndDateTime(endCalendar.getTime());
            startCalendar.add(Calendar.DAY_OF_YEAR, appointmentRequest.getRecurringPattern().getPeriod());
            endCalendar.add(Calendar.DAY_OF_YEAR, appointmentRequest.getRecurringPattern().getPeriod());
            appointments.add(appointment);
            currentAppointmentDate = startCalendar.getTime();
        }
        return appointments;
    }
}
