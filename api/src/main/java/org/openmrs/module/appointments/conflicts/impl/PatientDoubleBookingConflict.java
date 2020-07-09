package org.openmrs.module.appointments.conflicts.impl;

import org.apache.commons.collections.CollectionUtils;
import org.openmrs.module.appointments.conflicts.AppointmentConflict;
import org.openmrs.module.appointments.model.AppointmentConflictType;
import org.openmrs.module.appointments.dao.AppointmentDao;
import org.openmrs.module.appointments.model.Appointment;
import org.openmrs.module.appointments.model.AppointmentStatus;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import static org.openmrs.module.appointments.model.AppointmentConflictType.PATIENT_DOUBLE_BOOKING;

public class PatientDoubleBookingConflict implements AppointmentConflict {

    private AppointmentDao appointmentDao;

    public void setAppointmentDao(AppointmentDao appointmentDao) {
        this.appointmentDao = appointmentDao;
    }

    @Override
    public AppointmentConflictType getType() {
        return PATIENT_DOUBLE_BOOKING;
    }

    @Override
    public List<Appointment> getConflicts(List<Appointment> appointments) {
        List<Appointment> conflictingAppointments = new ArrayList<>();
        if(CollectionUtils.isNotEmpty(appointments)) {
            List<Appointment> patientAppointments = getPatientAppointments(appointments.get(0).getPatient().getPatientId());
            for (Appointment appointment : appointments) {
                conflictingAppointments.addAll(getConflictingAppointments(appointment, patientAppointments));
            }
        }
        return conflictingAppointments;
    }

    private List<Appointment> getConflictingAppointments(Appointment appointment, List<Appointment> patientAppointments) {
        return patientAppointments.stream()
                .filter(patientAppointment -> isConflicting(appointment, patientAppointment))
                .collect(Collectors.toList());
    }

    private boolean isConflicting(Appointment appointment, Appointment patientAppointment) {
        return !patientAppointment.isSameAppointment(appointment)
                && !patientAppointment.getVoided()
                && patientAppointment.isFutureAppointment()
                && isAppointmentOverlapping(patientAppointment, appointment)
                && patientAppointment.getStatus() != AppointmentStatus.Cancelled;
    }

    private boolean isAppointmentOverlapping(Appointment patientAppointment, Appointment appointment) {
        Date startTime = patientAppointment.getStartDateTime();
        Date endTime = patientAppointment.getEndDateTime();
        return appointment.getStartDateTime().before(endTime) && appointment.getEndDateTime().after(startTime);
    }

    private List<Appointment> getPatientAppointments(Integer patientId) {
        return appointmentDao.getAppointmentsForPatient(patientId);
    }
}
