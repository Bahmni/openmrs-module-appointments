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

import java.util.*;
import java.util.stream.Collectors;

@Component
public abstract class AbstractRecurringAppointmentsGenerationService {

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
        List<Appointment> appointments = getSortedActiveAppointments(appointmentRecurringPattern);
        if (appointmentRecurringPattern.getEndDate() == null) appointmentRecurringPattern.setFrequency(appointmentRecurringPattern.getFrequency() - recurringAppointmentRequest.getRecurringPattern().getFrequency());
        else appointmentRecurringPattern.setEndDate(recurringAppointmentRequest.getRecurringPattern().getEndDate());
        recurringAppointmentRequest.getAppointmentRequest().setStartDateTime(appointments.get(0).getStartDateTime());
        recurringAppointmentRequest.getAppointmentRequest().setEndDateTime(appointments.get(0).getEndDateTime());
        Calendar startCalender = Calendar.getInstance();
        Date currentDate = startCalender.getTime();
        List<Integer> removableAppointments = new ArrayList<>();
        removableAppointments = getRemovableAppointments(appointmentRecurringPattern, recurringAppointmentRequest,
                appointments, currentDate, removableAppointments);
        checkAppointmentStatus(appointments, removableAppointments);
        for (int i = 0; i < removableAppointments.size(); i++) {
            appointments.get(removableAppointments.get(i)).setVoided(true);
        }
        return getSortedActiveAppointments(appointmentRecurringPattern);
    }

    private List<Appointment> getSortedActiveAppointments(AppointmentRecurringPattern appointmentRecurringPattern) {
        List<Appointment> appointments = appointmentRecurringPattern.getActiveAppointments().stream().collect(Collectors.toList());
        Collections.sort(appointments, Comparator.comparing(Appointment::getDateFromStartDateTime));
        return appointments;
    }

    private void checkAppointmentStatus(List<Appointment> appointments, List<Integer> removableAppointments) {
        for (int index = 0; index < removableAppointments.size(); index++) {
            if (appointments.get(removableAppointments.get(index)).getStatus().equals(AppointmentStatus.CheckedIn))
                throw new APIException("Changes cannot be made as the appointments are already Checked-In");
            if (appointments.get(removableAppointments.get(index)).getStatus().equals(AppointmentStatus.Missed))
                throw new APIException("Changes cannot be made as the appointments are already Missed");
        }
    }

    private List<Integer> getRemovableAppointments(AppointmentRecurringPattern appointmentRecurringPattern,
                                                   RecurringAppointmentRequest recurringAppointmentRequest,
                                                   List<Appointment> appointments, Date currentDate,
                                                   List<Integer> removableAppointments) {
        if (appointmentRecurringPattern.getEndDate() == null) {
            if (appointments.get(recurringAppointmentRequest.getRecurringPattern().getFrequency()).getStartDateTime().before(currentDate))
                throw new APIException(String.format("Changes cannot be made as the appointments are from past date"));
            for (int index = recurringAppointmentRequest.getRecurringPattern().getFrequency(); index < appointments.size(); index++)
                removableAppointments.add(index);
        } else{
            if(currentDate.after(recurringAppointmentRequest.getRecurringPattern().getEndDate()))
                throw new APIException(String.format("Changes cannot be made as the appointments are from past date"));
            for (int index = 0; index < appointments.size(); index++) {
                if (appointments.get(index).getStartDateTime().after(appointmentRecurringPattern.getEndDate())) {
                    removableAppointments.add(index);
                }
            }
        }
        return removableAppointments;
    }
}
