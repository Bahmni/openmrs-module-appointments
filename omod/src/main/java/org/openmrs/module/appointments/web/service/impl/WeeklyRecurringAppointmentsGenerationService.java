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
@Qualifier("weeklyRecurringAppointmentsGenerationService")
public class WeeklyRecurringAppointmentsGenerationService extends AbstractRecurringAppointmentsGenerationService {

    private RecurringAppointmentRequest recurringAppointmentRequest;

    @Autowired
    private AppointmentMapper appointmentMapper;

    @Override
    public List<Appointment> getAppointments(RecurringAppointmentRequest recurringAppointmentRequest) {
        this.recurringAppointmentRequest = recurringAppointmentRequest;
        RecurringPattern recurringPattern = recurringAppointmentRequest.getRecurringPattern();
        Date endDate = getEndDate(recurringPattern.getPeriod(), recurringPattern.getFrequency(),
                recurringPattern.getEndDate(), recurringPattern.getDaysOfWeek());
        return generateAppointments(endDate);
    }

    public Date getEndDate(int period, Integer frequency, Date endDate, List<String> daysOfWeek) {
        if (endDate != null) {
            return endDate;
        }
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(recurringAppointmentRequest.getAppointmentRequest().getStartDateTime());
        int startDayCode = calendar.get(Calendar.DAY_OF_WEEK);
        List<Integer> selectedDaysPerWeek = getSortedSelectedDayCodes(startDayCode, daysOfWeek);
        int daysToBeAdded = 0;
        if (frequency % selectedDaysPerWeek.size() != 0) {
            daysToBeAdded = 1;
            int selectedDayCode = selectedDaysPerWeek.get(frequency % selectedDaysPerWeek.size() - 1);
            if (startDayCode > selectedDayCode) {
                daysToBeAdded += Calendar.DAY_OF_WEEK - (startDayCode - selectedDayCode);
            } else {
                daysToBeAdded += selectedDayCode - startDayCode;
            }
        }
        calendar.add(Calendar.DAY_OF_WEEK, (Calendar.DAY_OF_WEEK * period *
                (frequency / selectedDaysPerWeek.size()) - 1) + daysToBeAdded);
        return calendar.getTime();
    }

    private List<Integer> getSortedSelectedDayCodes(int startDayCode, List<String> daysOfWeek) {
        List<Integer> selectedDaysPerWeek = new ArrayList<>();
        for (int dayCode : getSelectedDayCodes(daysOfWeek)) {
            if (dayCode >= startDayCode) selectedDaysPerWeek.add(dayCode);
        }
        for (int dayCode : getSelectedDayCodes(daysOfWeek)) {
            if (dayCode < startDayCode) {
                selectedDaysPerWeek.add(dayCode);
            } else break;
        }
        return selectedDaysPerWeek;
    }

    private List<Appointment> generateAppointments(Date endDate) {
        List<Appointment> appointments = new ArrayList<>();
        Calendar startCalender = Calendar.getInstance();
        Calendar endCalender = Calendar.getInstance();
        startCalender.setTime(recurringAppointmentRequest.getAppointmentRequest().getStartDateTime());
        endCalender.setTime(recurringAppointmentRequest.getAppointmentRequest().getEndDateTime());
        List<Integer> selectedDayCodes = getSelectedDayCodes(recurringAppointmentRequest.getRecurringPattern().getDaysOfWeek());
        return createAppointments(endDate, appointments, startCalender, endCalender, selectedDayCodes);
    }

    private List<Appointment> createAppointments(Date endDate, List<Appointment> appointments, Calendar startCalender,
                                                 Calendar endCalender, List<Integer> selectedDayCodes) {
        Date currentAppointmentDate = startCalender.getTime();
        while (!currentAppointmentDate.after(endDate)) {
            getAppointmentsForAWeek(recurringAppointmentRequest, appointments, startCalender, endCalender, selectedDayCodes, endDate);
            startCalender.add(Calendar.DATE, Calendar.DAY_OF_WEEK * (recurringAppointmentRequest.getRecurringPattern().getPeriod()));
            endCalender.add(Calendar.DATE, Calendar.DAY_OF_WEEK * (recurringAppointmentRequest.getRecurringPattern().getPeriod()));
            currentAppointmentDate = startCalender.getTime();
        }
        appointments.sort(Comparator.comparing(Appointment::getStartDateTime));
        return appointments;
    }

    @Override
    public List<Appointment> addAppointments(AppointmentRecurringPattern appointmentRecurringPattern,
                                             RecurringAppointmentRequest recurringAppointmentRequest) {
        List<String> daysOfWeek = Arrays.stream(appointmentRecurringPattern.getDaysOfWeek().split(",")).collect(Collectors.toList());
        this.recurringAppointmentRequest = recurringAppointmentRequest;
        List<Appointment> appointments = appointmentRecurringPattern.getAppointments().stream().collect(Collectors.toList());
        Collections.sort(appointments, Comparator.comparing(Appointment::getDateFromStartDateTime));
        recurringAppointmentRequest.getAppointmentRequest().setStartDateTime(appointments.get(appointments.size() - 1).getStartDateTime());
        recurringAppointmentRequest.getAppointmentRequest().setEndDateTime(appointments.get(appointments.size() - 1).getEndDateTime());
        if (appointmentRecurringPattern.getEndDate() == null)
            appointmentRecurringPattern.setFrequency(recurringAppointmentRequest.getRecurringPattern().getFrequency() - appointmentRecurringPattern.getFrequency() + 1);
        else appointmentRecurringPattern.setEndDate(recurringAppointmentRequest.getRecurringPattern().getEndDate());
        Date endDate = getEndDate(appointmentRecurringPattern.getPeriod(), appointmentRecurringPattern.getFrequency(),
                appointmentRecurringPattern.getEndDate(), daysOfWeek);
        Calendar startCalender = Calendar.getInstance();
        Calendar endCalender = Calendar.getInstance();
        startCalender.setTime(recurringAppointmentRequest.getAppointmentRequest().getStartDateTime());
        endCalender.setTime(recurringAppointmentRequest.getAppointmentRequest().getEndDateTime());
        List<Integer> selectedDayCodes = getSelectedDayCodes(daysOfWeek);
        List<Integer> selectedDaysPerWeek = getSortedSelectedDayCodes(startCalender.get(Calendar.DAY_OF_WEEK), daysOfWeek);
        List<Integer> updatedSelectedDayCodes = getSelectedDayCodesForExtend(selectedDaysPerWeek, startCalender.get(Calendar.DAY_OF_WEEK));
        int noOfExtradayCodes = appointments.size() % selectedDaysPerWeek.size();
        updateCalendarInstancesToNextSelectedDay(startCalender, endCalender, selectedDaysPerWeek.get(1));
        recurringAppointmentRequest.getAppointmentRequest().setUuid(null);
        if (noOfExtradayCodes != 0) {
            getAppointmentsForAWeek(recurringAppointmentRequest, appointments, startCalender, endCalender, updatedSelectedDayCodes, endDate);
            startCalender.add(Calendar.DATE, Calendar.DAY_OF_WEEK * (appointmentRecurringPattern.getPeriod()) - 1);
            endCalender.add(Calendar.DATE, Calendar.DAY_OF_WEEK * (appointmentRecurringPattern.getPeriod()) - 1);
        }
        return createAppointments(endDate, appointments, startCalender, endCalender, selectedDayCodes);
    }

    private void getAppointmentsForAWeek(RecurringAppointmentRequest recurringAppointmentRequest, List<Appointment> appointments,
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
            Appointment appointment = appointmentMapper.fromRequest(recurringAppointmentRequest.getAppointmentRequest());
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

    private List<Integer> getSelectedDayCodesForExtend(List<Integer> selectedDaysPerWeek, int dayCode) {
        int dayCodeIndex = 0;
        List<Integer> updatedSelectedDayCodes = new ArrayList<>();
        for (int i = 0; i < selectedDaysPerWeek.size(); i++) {
            if (dayCode == selectedDaysPerWeek.get(i)) {
                dayCodeIndex = i;
                break;
            }
        }
        for (int i = dayCodeIndex + 1; i < selectedDaysPerWeek.size(); i++) {
            updatedSelectedDayCodes.add(selectedDaysPerWeek.get(i));
        }
        return updatedSelectedDayCodes;
    }

    private List<Integer> getSelectedDayCodes(List<String> selectedDays) {
        return selectedDays.stream().map(day -> getDayCodeFromDay(day)).sorted().collect(Collectors.toList());
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
