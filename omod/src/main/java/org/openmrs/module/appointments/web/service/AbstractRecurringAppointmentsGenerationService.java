package org.openmrs.module.appointments.web.service;
import org.openmrs.api.APIException;
import org.openmrs.module.appointments.model.Appointment;
import org.openmrs.module.appointments.model.AppointmentRecurringPattern;
import org.openmrs.module.appointments.model.AppointmentStatus;
import org.openmrs.module.appointments.web.contract.AppointmentRequest;
import java.util.*;
import java.util.stream.Collectors;
public abstract class AbstractRecurringAppointmentsGenerationService {
    public abstract List<Appointment> getAppointments();

    public abstract List<Appointment> addAppointments();

    public List<Appointment> removeRecurringAppointments(AppointmentRecurringPattern appointmentRecurringPattern, AppointmentRequest appointmentRequest) {
        List<Appointment> appointments = getSortedActiveAppointments(appointmentRecurringPattern);
        if (appointmentRecurringPattern.getEndDate() == null) appointmentRecurringPattern.setFrequency(appointmentRecurringPattern.getFrequency() - appointmentRequest.getRecurringPattern().getFrequency());
        else appointmentRecurringPattern.setEndDate(appointmentRequest.getRecurringPattern().getEndDate());
        appointmentRequest.setStartDateTime(appointments.get(0).getStartDateTime());
        appointmentRequest.setEndDateTime(appointments.get(0).getEndDateTime());
        Calendar startCalender = Calendar.getInstance();
        Date currentDate = startCalender.getTime();
        List<Integer> removableAppointments = new ArrayList<>();
        removableAppointments = getRemovableAppointments(appointmentRecurringPattern, appointmentRequest,
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

    private List<Integer> getRemovableAppointments(AppointmentRecurringPattern appointmentRecurringPattern, AppointmentRequest appointmentRequest, List<Appointment> appointments, Date currentDate, List<Integer> removableAppointments) {
        if (appointmentRecurringPattern.getEndDate() == null) {
            if (appointments.get(appointmentRequest.getRecurringPattern().getFrequency()).getStartDateTime().before(currentDate))
                throw new APIException(String.format("Changes cannot be made as the appointments are from past date"));
            for (int index = appointmentRequest.getRecurringPattern().getFrequency(); index < appointments.size(); index++)
                removableAppointments.add(index);
        } else{
            if(currentDate.after(appointmentRequest.getRecurringPattern().getEndDate()))
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
