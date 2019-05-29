package org.openmrs.module.appointments.web.service.impl;

import org.openmrs.module.appointments.model.Appointment;
import org.openmrs.module.appointments.model.AppointmentRecurringPattern;
import org.openmrs.module.appointments.web.contract.AppointmentRequest;
import org.openmrs.module.appointments.web.mapper.AppointmentMapper;
import org.openmrs.module.appointments.web.service.RecurringAppointmentsGenerationService;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

public class WeeklyRecurringAppointmentsGenerationService implements RecurringAppointmentsGenerationService {

    private AppointmentRecurringPattern appointmentRecurringPattern;
    private AppointmentRequest appointmentRequest;
    private AppointmentMapper appointmentMapper;

    public WeeklyRecurringAppointmentsGenerationService(AppointmentRecurringPattern appointmentRecurringPattern,
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
        calendar.add(Calendar.DAY_OF_WEEK, (Calendar.DAY_OF_WEEK * appointmentRecurringPattern.getPeriod() * appointmentRecurringPattern.getFrequency()) - 1);
        return calendar.getTime();
    }

    private List<Appointment> generateAppointments(Date endDate) {
        List<Appointment> appointments = new ArrayList<>();
        Calendar startCalender = Calendar.getInstance();
        Calendar endCalender = Calendar.getInstance();
        startCalender.setTime(appointmentRequest.getStartDateTime());
        endCalender.setTime(appointmentRequest.getEndDateTime());
        List<Integer> selectedDayCodes = getSelectedDayCodes(appointmentRecurringPattern.getDaysOfWeek());
        Date currentAppointmentDate = startCalender.getTime();
        while (!currentAppointmentDate.after(endDate)) {
            getAppointmentsForAWeek(appointmentRequest, appointments, startCalender, endCalender, selectedDayCodes, endDate);
            startCalender.add(Calendar.DATE, Calendar.DAY_OF_WEEK * (appointmentRecurringPattern.getPeriod()));
            endCalender.add(Calendar.DATE, Calendar.DAY_OF_WEEK * (appointmentRecurringPattern.getPeriod()));
            currentAppointmentDate = startCalender.getTime();
        }
        appointments.sort(Comparator.comparing(Appointment::getStartDateTime));
        return appointments;
    }

    private void getAppointmentsForAWeek(AppointmentRequest appointmentRequest, List<Appointment> appointments,
                                         Calendar startCalender, Calendar endCalender, List<Integer> selectedDayCodes, Date endDate) {
        for (int dayCode : selectedDayCodes) {
            Calendar startCalenderInstance = Calendar.getInstance();
            Calendar endCalenderInstance = Calendar.getInstance();
            startCalenderInstance.setTime(startCalender.getTime());
            endCalenderInstance.setTime(endCalender.getTime());

            updateCalendarInstancesToNextSelectedDay(startCalenderInstance, endCalenderInstance, dayCode);
            if (startCalenderInstance.getTime().after(endDate)) {
                continue;
            }
            Appointment appointment = appointmentMapper.fromRequest(appointmentRequest);
            appointment.setStartDateTime(startCalenderInstance.getTime());
            appointment.setEndDateTime(endCalenderInstance.getTime());
            appointments.add(appointment);
        }
    }

    private void updateCalendarInstancesToNextSelectedDay(Calendar startCalender, Calendar endCalender, int dayCode) {
        int startDayCode = startCalender.get(Calendar.DAY_OF_WEEK);
        int daysToBeAdded = dayCode - startDayCode;
        if (dayCode < startDayCode)
            daysToBeAdded += Calendar.DAY_OF_WEEK;
        startCalender.add(Calendar.DAY_OF_WEEK, daysToBeAdded);
        endCalender.add(Calendar.DAY_OF_WEEK, daysToBeAdded);
    }

    private List<Integer> getSelectedDayCodes(String selectedDays) {
        return Arrays.stream(selectedDays.split(",")).map(day -> getDayCodeFromDay(day)).sorted().collect(Collectors.toList());
    }

    private int getDayCodeFromDay(String day) {
        int dayCode = 0;
        switch (day.toUpperCase()) {
            case "SUNDAY":
                dayCode = Calendar.SUNDAY;
                break;
            case "MONDAY":
                dayCode = Calendar.MONDAY;
                break;
            case "TUESDAY":
                dayCode = Calendar.TUESDAY;
                break;
            case "WEDNESDAY":
                dayCode = Calendar.WEDNESDAY;
                break;
            case "THURSDAY":
                dayCode = Calendar.THURSDAY;
                break;
            case "FRIDAY":
                dayCode = Calendar.FRIDAY;
                break;
            case "SATURDAY":
                dayCode = Calendar.SATURDAY;
                break;
            default:
                break;
        }
        return dayCode;
    }

}
