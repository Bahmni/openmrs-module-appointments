package org.openmrs.module.appointments.search.builder;

import org.openmrs.module.appointments.model.Appointment;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class QueryContext {

    final CriteriaBuilder criteriaBuilder;
    final Root<Appointment> appointmentRoot;
    final List<Predicate> predicates;
    final Map<String, From<?, ?>> joinCache = new HashMap<>();

    public QueryContext(CriteriaBuilder criteriaBuilder, Root<Appointment> appointmentRoot,
                        List<Predicate> predicates) {
        this.criteriaBuilder = criteriaBuilder;
        this.appointmentRoot = appointmentRoot;
        this.predicates = predicates;
    }
}
