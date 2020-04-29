package org.openmrs.module.appointments.web.service;

import org.apache.commons.lang3.tuple.Pair;
import org.openmrs.api.APIException;
import org.openmrs.module.appointments.model.Appointment;
import org.openmrs.module.appointments.model.AppointmentRecurringPattern;
import org.openmrs.module.appointments.model.AppointmentStatus;
import org.openmrs.module.appointments.web.contract.AppointmentRequest;
import org.openmrs.module.appointments.web.contract.RecurringAppointmentRequest;
import org.openmrs.module.appointments.web.mapper.AppointmentMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Objects;

@Component
public abstract class AbstractRecurringAppointmentsService {

    @Autowired
    private AppointmentMapper appointmentMapper;

    public abstract List<Appointment> generateAppointments(RecurringAppointmentRequest recurringAppointmentRequest);

    public abstract List<Appointment> addAppointments(AppointmentRecurringPattern appointmentRecurringPattern,
                                                      RecurringAppointmentRequest recurringAppointmentRequest);

    protected List<Appointment> createAppointments(List<Pair<Date, Date>> appointmentDates,
                                                   AppointmentRequest appointmentRequest) {
        List<Appointment> appointments = new ArrayList<>();
        appointmentDates.forEach(appointmentDate -> {
            Appointment appointment = appointmentMapper.fromRequest(appointmentRequest);
            appointment.setStartDateTime(appointmentDate.getLeft());
            appointment.setEndDateTime(appointmentDate.getRight());
            appointments.add(appointment);
        });
        return appointments;
    }

    protected List<Appointment> sort(List<Appointment> appointments) {
        appointments.sort(Comparator.comparing(Appointment::getStartDateTime));
        return appointments;
    }

    public List<Appointment> removeRecurringAppointments(AppointmentRecurringPattern appointmentRecurringPattern,
                                                         RecurringAppointmentRequest recurringAppointmentRequest) {
        List<Appointment> appointments = sort(new ArrayList<>(appointmentRecurringPattern.getActiveAppointments()));
        Date recurringStartDate = appointments.get(0).getStartDateTime();
        if (Objects.nonNull(recurringAppointmentRequest.getRecurringPattern().getEndDate()) &&
                recurringAppointmentRequest.getRecurringPattern().getEndDate().before(recurringStartDate))
            throw new APIException("End date cannot be before the start date of recurring series");
        if (appointmentRecurringPattern.getEndDate() == null)
            appointmentRecurringPattern.setFrequency(appointmentRecurringPattern.getFrequency() -
                    recurringAppointmentRequest.getRecurringPattern().getFrequency());
        else
            appointmentRecurringPattern.setEndDate(recurringAppointmentRequest.getRecurringPattern().getEndDate());
        recurringAppointmentRequest.getAppointmentRequest().setStartDateTime(appointments.get(0).getStartDateTime());
        recurringAppointmentRequest.getAppointmentRequest().setEndDateTime(appointments.get(0).getEndDateTime());
        Calendar startCalender = Calendar.getInstance();
        Date currentDate = startCalender.getTime();
        List<Integer> removableAppointments = new ArrayList<>();
        removableAppointments = getRemovableAppointments(appointmentRecurringPattern, recurringAppointmentRequest,
                appointments, currentDate, removableAppointments);
        checkAppointmentStatus(appointments, removableAppointments);
        for (Integer removableAppointment : removableAppointments) {
            appointments.get(removableAppointment).setVoided(true);
        }
        return sort(new ArrayList<>(appointmentRecurringPattern.getAppointments()));
    }

    protected void addRemovedAndRelatedAppointments(List<Appointment> appointments,
                                                    AppointmentRecurringPattern appointmentRecurringPattern) {
        appointments.addAll(appointmentRecurringPattern.getRemovedAppointments());
        appointments.addAll(appointmentRecurringPattern.getRelatedAppointments());
    }

    private void checkAppointmentStatus(List<Appointment> appointments, List<Integer> removableAppointments) {
        for (Integer removableAppointment : removableAppointments) {
            if (appointments.get(removableAppointment).getStatus().equals(AppointmentStatus.CheckedIn))
                throw new APIException("Changes cannot be made as the appointments are already Checked-In");
            if (appointments.get(removableAppointment).getStatus().equals(AppointmentStatus.Missed))
                throw new APIException("Changes cannot be made as the appointments are already Missed");
        }
    }

    private List<Integer> getRemovableAppointments(AppointmentRecurringPattern appointmentRecurringPattern,
                                                   RecurringAppointmentRequest recurringAppointmentRequest,
                                                   List<Appointment> appointments, Date currentDate,
                                                   List<Integer> removableAppointments) {
        if (appointmentRecurringPattern.getEndDate() == null) {
            if (appointments.get(recurringAppointmentRequest.getRecurringPattern().getFrequency()).getStartDateTime().before(currentDate))
                throw new APIException("Changes cannot be made as the appointments are from past date");
            for (int index = recurringAppointmentRequest.getRecurringPattern().getFrequency(); index < appointments.size(); index++)
                removableAppointments.add(index);
        } else {
            if (currentDate.after(recurringAppointmentRequest.getRecurringPattern().getEndDate()))
                throw new APIException("Changes cannot be made as the appointments are from past date");
            for (int index = 0; index < appointments.size(); index++) {
                if (appointments.get(index).getStartDateTime().after(appointmentRecurringPattern.getEndDate())) {
                    removableAppointments.add(index);
                }
            }
        }
        return removableAppointments;
    }
}
