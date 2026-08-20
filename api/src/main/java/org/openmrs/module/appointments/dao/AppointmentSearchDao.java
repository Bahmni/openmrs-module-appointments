package org.openmrs.module.appointments.dao;

import org.bahmni.search.model.SearchCondition;
import org.openmrs.module.appointments.model.Appointment;

import java.util.List;

public interface AppointmentSearchDao {

    List<Integer> findMatchingIds(SearchCondition criteria, Long cursorId,
                                   String sortOrder, String direction, int limit);

    List<Appointment> findByIds(List<Integer> appointmentIds);

    long count(SearchCondition criteria);
}

