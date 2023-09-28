package org.openmrs.module.appointments.service;

import org.openmrs.module.appointments.model.Appointment;

import java.util.*;


public interface AppointmentArgumentsMapper {
    Map<String, String> createArgumentsMapForRecurringAppointmentBooking(Appointment appointment);

    List<String> getProvidersNameInString(Appointment appointment);
    Map<String, String> createArgumentsMapForAppointmentBooking(Appointment appointment);
}
