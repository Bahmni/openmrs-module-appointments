package org.openmrs.module.appointments.web.service.impl;

import org.openmrs.module.appointments.model.Appointment;
import org.openmrs.module.appointments.model.AppointmentRecurringPattern;
import org.openmrs.module.appointments.web.contract.AppointmentRequest;
import org.openmrs.module.appointments.web.mapper.AppointmentMapper;
import org.openmrs.module.appointments.web.service.AbstractRecurringAppointmentsGenerationService;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class DailyRecurringAppointmentsGenerationService extends AbstractRecurringAppointmentsGenerationService {

    private AppointmentRecurringPattern appointmentRecurringPattern;
    private AppointmentRequest appointmentRequest;
    private AppointmentMapper appointmentMapper;

    public DailyRecurringAppointmentsGenerationService(AppointmentRecurringPattern appointmentRecurringPattern,
                                                       AppointmentRequest appointmentRequest,
                                                       AppointmentMapper appointmentMapper) {
        this.appointmentRecurringPattern = appointmentRecurringPattern;
        this.appointmentRequest = appointmentRequest;
        this.appointmentMapper = appointmentMapper;
    }

    @Override
    public List<Appointment> getAppointments() {
        Date endDate = getEndDate();
        return generateAppointments(endDate);
    }

    private Date getEndDate() {
        if (appointmentRecurringPattern.getEndDate() != null) {
            return appointmentRecurringPattern.getEndDate();
        }
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(appointmentRequest.getStartDateTime());
        calendar.add(Calendar.DAY_OF_MONTH, appointmentRecurringPattern.getPeriod() * (appointmentRecurringPattern.getFrequency() - 1));
        return calendar.getTime();
    }

    private List<Appointment> generateAppointments(Date endDate) {
        List<Appointment> appointments = new ArrayList<>();
        Calendar startCalender = Calendar.getInstance();
        Calendar endCalender = Calendar.getInstance();
        startCalender.setTime(appointmentRequest.getStartDateTime());
        endCalender.setTime(appointmentRequest.getEndDateTime());
        Date currentAppointmentDate = appointmentRequest.getStartDateTime();
        while (!currentAppointmentDate.after(endDate)) {
            Appointment appointment = appointmentMapper.fromRequest(appointmentRequest);
            appointment.setStartDateTime(startCalender.getTime());
            appointment.setEndDateTime(endCalender.getTime());
            startCalender.add(Calendar.DAY_OF_YEAR, appointmentRecurringPattern.getPeriod());
            endCalender.add(Calendar.DAY_OF_YEAR, appointmentRecurringPattern.getPeriod());
            appointments.add(appointment);
            currentAppointmentDate = startCalender.getTime();
        }
        return appointments;
    }
}
