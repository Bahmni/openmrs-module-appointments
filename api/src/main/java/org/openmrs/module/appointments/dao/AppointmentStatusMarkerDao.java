package org.openmrs.module.appointments.dao;

import org.openmrs.Patient;
import org.openmrs.module.appointments.model.AppointmentServiceDefinition;
import org.openmrs.module.appointments.service.AppointmentsService;

import java.util.Date;
import java.util.List;

public interface AppointmentStatusMarkerDao {

    List<String> getAllAppointmentsNotConsultedUuidList(Date startDateTime, Date endDateTime);

    List<String> getPatientFutureAppointmentUuidList(Patient patient, AppointmentServiceDefinition appointmentServiceDefinition, Date startDate, Date endDate);

    void markAppoinmentAsComplete(String uuid);

    void markAllMissedAppointments(String concatenatedUuidList);
}
