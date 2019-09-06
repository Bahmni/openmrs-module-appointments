package org.openmrs.module.appointments.web.service.impl;

import org.openmrs.module.appointments.model.Appointment;
import org.openmrs.module.appointments.model.AppointmentRecurringPattern;
import org.openmrs.module.appointments.web.contract.AppointmentRequest;
import org.openmrs.module.appointments.web.contract.RecurringAppointmentRequest;
import org.openmrs.module.appointments.web.contract.RecurringPattern;
import org.openmrs.module.appointments.web.mapper.AppointmentMapper;
import org.openmrs.module.appointments.web.service.AbstractRecurringAppointmentsGenerationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
@Qualifier("dailyRecurringAppointmentsGenerationService")
public class DailyRecurringAppointmentsGenerationService extends AbstractRecurringAppointmentsGenerationService {

    private RecurringAppointmentRequest recurringAppointmentRequest;

    @Autowired
    private AppointmentMapper appointmentMapper;

    @Override
    public List<Appointment> getAppointments(RecurringAppointmentRequest recurringAppointmentRequest) {
        this.recurringAppointmentRequest = recurringAppointmentRequest;
        RecurringPattern recurringPattern = recurringAppointmentRequest.getRecurringPattern();
        Date endDate = getEndDate(recurringPattern.getPeriod(), recurringPattern.getFrequency(), recurringPattern.getEndDate());
        return generateAppointments(endDate);
    }

    public Date getEndDate(int period, Integer frequency, Date endDate) {
        if (endDate != null) {
            return endDate;
        }
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(recurringAppointmentRequest.getAppointmentRequest().getStartDateTime());
        calendar.add(Calendar.DAY_OF_MONTH, period * (frequency - 1));
        return calendar.getTime();
    }

    private List<Appointment> generateAppointments(Date endDate) {
        List<Appointment> appointments = new ArrayList<>();
        Calendar startCalender = Calendar.getInstance();
        Calendar endCalender = Calendar.getInstance();
        startCalender.setTime(recurringAppointmentRequest.getAppointmentRequest().getStartDateTime());
        endCalender.setTime(recurringAppointmentRequest.getAppointmentRequest().getEndDateTime());
        return createAppointments(appointments, endDate, startCalender, endCalender, recurringAppointmentRequest);
    }

    public List<Appointment> addAppointments(AppointmentRecurringPattern appointmentRecurringPattern,
                                             RecurringAppointmentRequest recurringAppointmentRequest){
        this.recurringAppointmentRequest = recurringAppointmentRequest;
        List<Appointment> appointments = appointmentRecurringPattern.getAppointments().stream().collect(Collectors.toList());
        Collections.sort(appointments, Comparator.comparing(Appointment::getDateFromStartDateTime));
        recurringAppointmentRequest.getAppointmentRequest().setStartDateTime(appointments.get(appointments.size()-1).getStartDateTime());
        recurringAppointmentRequest.getAppointmentRequest().setEndDateTime(appointments.get(appointments.size()-1).getEndDateTime());
        if (appointmentRecurringPattern.getEndDate() == null) appointmentRecurringPattern.setFrequency(recurringAppointmentRequest.getRecurringPattern().getFrequency() - appointmentRecurringPattern.getFrequency() + 1 );
        else appointmentRecurringPattern.setEndDate(recurringAppointmentRequest.getRecurringPattern().getEndDate());
        Date endDate = getEndDate(appointmentRecurringPattern.getPeriod(), appointmentRecurringPattern.getFrequency(),
                appointmentRecurringPattern.getEndDate());
        Calendar startCalender = Calendar.getInstance();
        Calendar endCalender = Calendar.getInstance();
        startCalender.setTime(recurringAppointmentRequest.getAppointmentRequest().getStartDateTime());
        endCalender.setTime(recurringAppointmentRequest.getAppointmentRequest().getEndDateTime());
        startCalender.add(Calendar.DAY_OF_YEAR, appointmentRecurringPattern.getPeriod());
        endCalender.add(Calendar.DAY_OF_YEAR, appointmentRecurringPattern.getPeriod());
        recurringAppointmentRequest.getAppointmentRequest().setUuid(null);
        return createAppointments(appointments, endDate, startCalender, endCalender, recurringAppointmentRequest);
    }

    private List<Appointment> createAppointments(List<Appointment> appointments, Date endDate, Calendar startCalendar,
                                                 Calendar endCalendar, RecurringAppointmentRequest recurringAppointmentRequest) {
        Date currentAppointmentDate = recurringAppointmentRequest.getAppointmentRequest().getStartDateTime();
        while (!currentAppointmentDate.after(endDate)) {
            Appointment appointment = appointmentMapper.fromRequest(recurringAppointmentRequest.getAppointmentRequest());
            appointment.setStartDateTime(startCalendar.getTime());
            appointment.setEndDateTime(endCalendar.getTime());
            startCalendar.add(Calendar.DAY_OF_YEAR, recurringAppointmentRequest.getRecurringPattern().getPeriod());
            endCalendar.add(Calendar.DAY_OF_YEAR, recurringAppointmentRequest.getRecurringPattern().getPeriod());
            appointments.add(appointment);
            currentAppointmentDate = startCalendar.getTime();
        }
        return appointments;
    }
}
