package org.openmrs.module.appointments.web.service.impl;

import org.openmrs.module.appointments.model.Appointment;
import org.openmrs.module.appointments.model.AppointmentRecurringPattern;
import org.openmrs.module.appointments.web.contract.AppointmentRequest;
import org.openmrs.module.appointments.web.mapper.AppointmentMapper;
import org.openmrs.module.appointments.web.service.AbstractRecurringAppointmentsGenerationService;

import java.util.*;
import java.util.stream.Collectors;

public class WeeklyRecurringAppointmentsGenerationService extends AbstractRecurringAppointmentsGenerationService {

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

    public Date getEndDate() {
        if (appointmentRecurringPattern.getEndDate() != null) {
            return appointmentRecurringPattern.getEndDate();
        }
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(appointmentRequest.getStartDateTime());
        int startDayCode = calendar.get(Calendar.DAY_OF_WEEK);
        List<Integer> selectedDaysPerWeek = getSortedSelectedDayCodes(startDayCode);
        int daysToBeAdded = 0;
        if (appointmentRecurringPattern.getFrequency() % selectedDaysPerWeek.size() != 0) {
            daysToBeAdded = 1;
            int selectedDayCode = selectedDaysPerWeek.get(appointmentRecurringPattern.getFrequency() % selectedDaysPerWeek.size() - 1);
            if (startDayCode > selectedDayCode) {
                daysToBeAdded += Calendar.DAY_OF_WEEK - (startDayCode - selectedDayCode);
            } else {
                daysToBeAdded += selectedDayCode - startDayCode;
            }
        }
        calendar.add(Calendar.DAY_OF_WEEK, (Calendar.DAY_OF_WEEK * appointmentRecurringPattern.getPeriod() *
                (appointmentRecurringPattern.getFrequency() / selectedDaysPerWeek.size()) - 1) + daysToBeAdded);
        return calendar.getTime();
    }

    private List<Integer> getSortedSelectedDayCodes(int startDayCode) {
        List<Integer> selectedDaysPerWeek = new ArrayList<>();
        for (int dayCode : getSelectedDayCodes(appointmentRecurringPattern.getDaysOfWeek())) {
            if (dayCode >= startDayCode) selectedDaysPerWeek.add(dayCode);
        }
        for (int dayCode : getSelectedDayCodes(appointmentRecurringPattern.getDaysOfWeek())) {
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
        startCalender.setTime(appointmentRequest.getStartDateTime());
        endCalender.setTime(appointmentRequest.getEndDateTime());
        List<Integer> selectedDayCodes = getSelectedDayCodes(appointmentRecurringPattern.getDaysOfWeek());
        return createAppointments(endDate, appointments, startCalender, endCalender, selectedDayCodes);
    }

    private List<Appointment> createAppointments(Date endDate, List<Appointment> appointments, Calendar startCalender, Calendar endCalender, List<Integer> selectedDayCodes) {
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

    @Override
    public List<Appointment> addAppointments() {
        List<Appointment> appointments = appointmentRecurringPattern.getAppointments().stream().collect(Collectors.toList());
        Collections.sort(appointments, Comparator.comparing(Appointment::getDateFromStartDateTime));
        appointmentRequest.setStartDateTime(appointments.get(appointments.size() - 1).getStartDateTime());
        appointmentRequest.setEndDateTime(appointments.get(appointments.size() - 1).getEndDateTime());
        if (appointmentRecurringPattern.getEndDate() == null)
            appointmentRecurringPattern.setFrequency(appointmentRequest.getRecurringPattern().getFrequency() - appointmentRecurringPattern.getFrequency() + 1);
        else appointmentRecurringPattern.setEndDate(appointmentRequest.getRecurringPattern().getEndDate());
        Date endDate = getEndDate();
        Calendar startCalender = Calendar.getInstance();
        Calendar endCalender = Calendar.getInstance();
        startCalender.setTime(appointmentRequest.getStartDateTime());
        endCalender.setTime(appointmentRequest.getEndDateTime());
        List<Integer> selectedDayCodes = getSelectedDayCodes(appointmentRecurringPattern.getDaysOfWeek());
        List<Integer> selectedDaysPerWeek = getSortedSelectedDayCodes(startCalender.get(Calendar.DAY_OF_WEEK));
        List<Integer> updatedSelectedDayCodes = getSelectedDayCodesForExtend(selectedDaysPerWeek, startCalender.get(Calendar.DAY_OF_WEEK));
        int noOfExtradayCodes = appointments.size() % selectedDaysPerWeek.size();
        updateCalendarInstancesToNextSelectedDay(startCalender, endCalender, selectedDaysPerWeek.get(1));
        appointmentRequest.setUuid(null);
        if (noOfExtradayCodes != 0) {
            getAppointmentsForAWeek(appointmentRequest, appointments, startCalender, endCalender, updatedSelectedDayCodes, endDate);
            startCalender.add(Calendar.DATE, Calendar.DAY_OF_WEEK * (appointmentRecurringPattern.getPeriod()) - 1);
            endCalender.add(Calendar.DATE, Calendar.DAY_OF_WEEK * (appointmentRecurringPattern.getPeriod()) - 1);
        }
        return createAppointments(endDate, appointments, startCalender, endCalender, selectedDayCodes);
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
