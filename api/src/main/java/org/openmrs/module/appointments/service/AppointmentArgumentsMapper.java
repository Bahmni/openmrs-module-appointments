package org.openmrs.module.appointments.service;

import org.openmrs.Location;
import org.openmrs.LocationTag;
import org.openmrs.Patient;
import org.openmrs.api.context.Context;
import org.openmrs.module.appointments.model.Appointment;
import org.openmrs.module.appointments.model.AppointmentProvider;
import org.springframework.stereotype.Component;

import java.util.*;

import static org.bahmni.module.communication.util.DateUtil.convertUTCToGivenFormat;

public interface AppointmentArgumentsMapper {
    Map<String, String> createArgumentsMapForRecurringAppointmentBooking(Appointment appointment);

    List<String> getProvidersNameInString(Appointment appointment);
    Map<String, String> createArgumentsMapForAppointmentBooking(Appointment appointment);
}
