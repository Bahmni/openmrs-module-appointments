package org.openmrs.module.appointments.service.impl;

import org.openmrs.Patient;
import org.openmrs.module.appointments.dao.AppointmentStatusMarkerDao;
import org.openmrs.module.appointments.model.AppointmentServiceDefinition;
import org.openmrs.module.appointments.service.AppointmentStatusMarkerService;
import org.openmrs.module.appointments.service.AppointmentsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Objects;

public class AppointmentStatusMarkerServiceImpl implements AppointmentStatusMarkerService {

    private AppointmentStatusMarkerDao appointmentStatusMarkerDao;

    public AppointmentStatusMarkerDao getAppointmentStatusMarkerDao() {
        return appointmentStatusMarkerDao;
    }

    public void setAppointmentStatusMarkerDao(AppointmentStatusMarkerDao appointmentStatusMarkerDao) {
        this.appointmentStatusMarkerDao = appointmentStatusMarkerDao;
    }

    @Override
    public List<String> getAllAppointmentsNotConsultedUuidList(Date startDateTime, Date endDateTime) {
        return appointmentStatusMarkerDao.getAllAppointmentsNotConsultedUuidList(startDateTime, endDateTime);
    }

    @Override
    public List<String> getPatientFutureAppointmentUuidList(Patient patient, AppointmentServiceDefinition appointmentServiceDefinition, Date startDateTime, Date endDateTime) {
        return appointmentStatusMarkerDao.getPatientFutureAppointmentUuidList(patient, appointmentServiceDefinition, startDateTime, endDateTime);
    }

    @Transactional
    @Override
    public void markAppoinmentAsComplete(String uuid) {
        appointmentStatusMarkerDao.markAppoinmentAsComplete(uuid);
    }

    @Transactional
    @Override
    public void markAllMissedAppointments(Date startDateTime, Date endDateTime) {
        List<String> missedAppointmentsUuid = getAllAppointmentsNotConsultedUuidList(startDateTime, endDateTime);

        System.out.println("\n================= markAllMissedAppointments Called ============================\n");

        String concatenatedUuidList = concatenateUuidList(missedAppointmentsUuid);

        if (!Objects.equals(concatenatedUuidList, ""))
            appointmentStatusMarkerDao.markAllMissedAppointments(concatenatedUuidList);

    }

    @Override
    public void markPatientFutureAppointments(Patient patient, AppointmentServiceDefinition appointmentServiceDefinition, Date startDateTime, Date endDateTime) {
        List<String> patientFutureAppointmentsUuid = getPatientFutureAppointmentUuidList(patient, appointmentServiceDefinition, startDateTime, endDateTime);

        patientFutureAppointmentsUuid.forEach(this::markAppoinmentAsComplete);
    }

    @Override
    public Date schedulertStartDateTime(int minusDays) {
        return Date.from((LocalDate.now().minusDays(minusDays).atStartOfDay()).atZone(ZoneId.systemDefault()).toInstant());
  }

    @Override
    public Date schedulerEndDateTime() {
        return Date.from((LocalDate.now().atTime(LocalTime.of(23, 59, 59, 999000000))).atZone(ZoneId.systemDefault()).toInstant());
    }

    public String concatenateUuidList(List<String> uuidList) {
        String concatenatedUuidList = "";

        int index = 0;
        for (String uuid : uuidList) {
            concatenatedUuidList += "'" + uuid + "'";

            if (index != (uuidList.size() - 1)) {
                concatenatedUuidList += ",";
            }
            index++;
        }

        return concatenatedUuidList;
    }
}
