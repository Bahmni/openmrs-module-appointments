package org.openmrs.module.appointments.dao;

import org.bahmni.search.model.SearchCondition;
import org.openmrs.module.appointments.model.Appointment;

import java.util.List;

public interface AppointmentSearchDao {

    List<Appointment> search(SearchCondition criteria);
}
