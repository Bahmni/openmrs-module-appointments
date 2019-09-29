package org.openmrs.module.appointments.conflicts.impl;

import org.apache.commons.collections.CollectionUtils;
import org.openmrs.module.appointments.conflicts.AppointmentConflictType;
import org.openmrs.module.appointments.model.Appointment;
import org.openmrs.module.appointments.service.AppointmentsService;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import static org.openmrs.module.appointments.constants.AppointmentConflictTypeEnum.PATIENT_DOUBLE_BOOKING;

public class PatientDoubleBookingConflict implements AppointmentConflictType {

    private List<Appointment> patientAppointments;

    private AppointmentsService appointmentsService;

    public void setAppointmentsService(AppointmentsService appointmentsService) {
        this.appointmentsService = appointmentsService;
    }

    @Override
    public String getType() {
        return PATIENT_DOUBLE_BOOKING.name();
    }

    @Override
    public List<Appointment> getAppointmentConflicts(Appointment appointment) {
        return checkConflicts(appointment);
    }

    private List<Appointment> checkConflicts(Appointment appointment) {
        getPatientAppointments(appointment);
        return getConflictingAppointments(appointment);
    }

    private List<Appointment> getConflictingAppointments(Appointment appointment) {
        return patientAppointments.stream()
                .filter(patientAppointment -> isConflicting(appointment, patientAppointment))
                .collect(Collectors.toList());
    }

    private boolean isConflicting(Appointment appointment, Appointment patientAppointment) {
        return !appointment.isSameAppointment(patientAppointment)
                && !patientAppointment.getVoided()
                && patientAppointment.isFutureAppointment()
                && isAppointmentOverlapping(patientAppointment, appointment);
    }

    private boolean isAppointmentOverlapping(Appointment patientAppointment, Appointment appointment) {
        Date startTime = patientAppointment.getStartDateTime();
        Date endTime = patientAppointment.getEndDateTime();
        return appointment.getStartDateTime().before(endTime) && appointment.getEndDateTime().after(startTime);
    }

    private void getPatientAppointments(Appointment appointment) {
        if (CollectionUtils.isEmpty(patientAppointments)) {
            patientAppointments = appointmentsService.getAppointmentsForPatient(appointment.getPatient().getPatientId());
        }
    }
}
