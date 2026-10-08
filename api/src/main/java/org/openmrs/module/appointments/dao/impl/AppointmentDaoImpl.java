package org.openmrs.module.appointments.dao.impl;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.Hibernate;
import org.hibernate.SessionFactory;
import org.openmrs.api.db.hibernate.HibernateUtil;
import org.openmrs.module.appointments.dao.AppointmentDao;
import org.openmrs.module.appointments.model.Appointment;
import org.openmrs.module.appointments.model.AppointmentSearchRequestModel;
import org.openmrs.module.appointments.model.AppointmentServiceDefinition;
import org.openmrs.module.appointments.model.AppointmentStatus;
import org.openmrs.module.appointments.model.AppointmentServiceType;
import org.openmrs.module.appointments.model.AppointmentSearchRequest;
import org.openmrs.module.appointments.model.AppointmentPriority;
import org.openmrs.module.appointments.util.DateUtil;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.From;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.metamodel.Attribute;
import jakarta.persistence.metamodel.SingularAttribute;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class AppointmentDaoImpl implements AppointmentDao {

    private static final int APPOINTMENT_SEARCH_DEFAULT_LIMIT = 50;
    private SessionFactory sessionFactory;

    public void setSessionFactory(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    @Override
    public List<Appointment> getAllAppointments(Date forDate) {
        Query query = new Query();
        query.predicates.add(query.cb.equal(query.root.get("voided"), false));
        addNonVoidedPatientCriteria(query);
        if (forDate != null) {
            Date maxDate = new Date(forDate.getTime() + TimeUnit.DAYS.toMillis(1));
            query.predicates.add(query.cb.greaterThanOrEqualTo(query.root.get("startDateTime"), forDate));
            query.predicates.add(query.cb.lessThan(query.root.get("endDateTime"), maxDate));
        }
        return query.list();
    }

    @Override
    public List<Appointment> getAllAppointmentsReminder(String hours) {
        Query query = new Query();
        addNonVoidedPatientCriteria(query);
        if (hours != null) {
            Date minDate = new Date(System.currentTimeMillis() + TimeUnit.HOURS.toMillis(Integer.valueOf(hours)));
            Date maxDate = new Date(minDate.getTime() + TimeUnit.HOURS.toMillis(1));
            query.predicates.add(query.cb.greaterThanOrEqualTo(query.root.get("startDateTime"), minDate));
            query.predicates.add(query.cb.lessThan(query.root.get("startDateTime"), maxDate));
        }
        query.predicates.add(query.cb.notEqual(query.root.get("status"), AppointmentStatus.Cancelled));
        return query.list();
    }

    @Transactional
    @Override
    public void save(Appointment appointment) {
        HibernateUtil.saveOrUpdate(sessionFactory.getCurrentSession(), appointment);
    }

    @Override
    public List<Appointment> search(Appointment appointment) {
        Query query = new Query();
        addExampleCriteria(query, query.root, appointment, "uuid");

        if (appointment.getPatient() != null) addExampleCriteria(query, query.root.join("patient"), appointment.getPatient());

        if (appointment.getLocation() != null) addExampleCriteria(query, query.root.join("location"), appointment.getLocation());

        if (appointment.getService() != null) addExampleCriteria(query, query.root.join("service"), appointment.getService());

        if (appointment.getProvider() != null) addExampleCriteria(query, query.root.join("provider"), appointment.getProvider());

        return query.list();
    }

    @Override
    public List<Appointment> search(AppointmentSearchRequestModel searchQuery) {
        Query query = new Query();
        addSearchCriteria(query, searchQuery);
        return query.list();
    }

    @Override
    public List<Appointment> getAllFutureAppointmentsForService(AppointmentServiceDefinition appointmentServiceDefinition) {
        Query query = new Query();
        query.predicates.add(query.cb.equal(query.root.get("service"), appointmentServiceDefinition));
        query.predicates.add(query.cb.greaterThan(query.root.get("endDateTime"), new Date()));
        query.predicates.add(query.cb.equal(query.root.get("voided"), false));
        addNonVoidedPatientCriteria(query);
        query.predicates.add(query.cb.notEqual(query.root.get("status"), AppointmentStatus.Cancelled));
        return query.list();
    }

    @Override
    public List<Appointment> getAllFutureAppointmentsForServiceType(AppointmentServiceType appointmentServiceType) {
        Query query = new Query();
        query.predicates.add(query.cb.equal(query.root.get("serviceType"), appointmentServiceType));
        query.predicates.add(query.cb.greaterThan(query.root.get("endDateTime"), new Date()));
        query.predicates.add(query.cb.equal(query.root.get("voided"), false));
        addNonVoidedPatientCriteria(query);
        query.predicates.add(query.cb.notEqual(query.root.get("status"), AppointmentStatus.Cancelled));
        return query.list();
    }

    @Override
    public List<Appointment> getAppointmentsForService(AppointmentServiceDefinition appointmentServiceDefinition, Date startDate, Date endDate, List<AppointmentStatus> appointmentStatusFilterList) {
        Query query = new Query();
        Join<Appointment, ?> serviceType = query.root.join("serviceType", JoinType.LEFT);
        query.predicates.add(query.cb.or(query.root.get("serviceType").isNull(), query.cb.equal(serviceType.get("voided"), false)));
        query.predicates.add(query.cb.equal(query.root.get("voided"), false));
        addNonVoidedPatientCriteria(query);
        query.predicates.add(query.cb.greaterThanOrEqualTo(query.root.get("startDateTime"), startDate));
        query.predicates.add(query.cb.lessThanOrEqualTo(query.root.get("startDateTime"), endDate));
        addExampleCriteria(query, query.root.join("service"), appointmentServiceDefinition);
        if (appointmentStatusFilterList != null && !appointmentStatusFilterList.isEmpty()) {
            query.predicates.add(query.root.get("status").in(appointmentStatusFilterList));
        }
        return query.list();

    }

    @Override
    public Appointment getAppointmentByUuid(String uuid) {
        return sessionFactory.getCurrentSession()
                .createQuery("from Appointment appointment where appointment.uuid = :uuid", Appointment.class)
                .setParameter("uuid", uuid)
                .uniqueResult();
    }

    @Override
    public List<Appointment> getAllAppointmentsInDateRange(Date startDate, Date endDate) {
        Query query = new Query();
        query.predicates.add(query.cb.equal(query.root.get("voided"), false));
        addNonVoidedPatientCriteria(query);
        if (startDate != null) {
            query.predicates.add(query.cb.greaterThanOrEqualTo(query.root.get("startDateTime"), startDate));
        }
        if (endDate != null) {
            query.predicates.add(query.cb.lessThan(query.root.get("endDateTime"), endDate));
        }
        return query.list();
    }

    @Override
    public List<Appointment> search(AppointmentSearchRequest appointmentSearchRequest) {
        Query query = new Query();

        query.predicates.add(query.cb.equal(query.root.get("voided"), false));
        query.criteriaQuery.orderBy(query.cb.asc(query.root.get("startDateTime")));
        setDateCriteria(appointmentSearchRequest, query);
        setPatientCriteria(appointmentSearchRequest, query);
        setProviderCriteria(appointmentSearchRequest, query);
        setStatusCriteria(appointmentSearchRequest, query);
        setAppointmentNumberCriteria(appointmentSearchRequest, query);


        return query.list(getLimit(appointmentSearchRequest));
    }

    private void setProviderCriteria(AppointmentSearchRequest appointmentSearchRequest, Query query) {
        if (StringUtils.isNotEmpty(appointmentSearchRequest.getProviderUuid())) {
            Join<?, ?> provider = query.root.join("providers").join("provider");
            query.predicates.add(query.cb.equal(provider.get("uuid"), appointmentSearchRequest.getProviderUuid()));
        }
    }

    private void setPatientCriteria(AppointmentSearchRequest appointmentSearchRequest, Query query) {
        Join<Appointment, ?> patient = addNonVoidedPatientCriteria(query);
        if (StringUtils.isNotEmpty(appointmentSearchRequest.getPatientUuid())) {
            query.predicates.add(query.cb.equal(patient.get("uuid"), appointmentSearchRequest.getPatientUuid()));
        }
    }

    private void setDateCriteria(AppointmentSearchRequest appointmentSearchRequest, Query query) {
        if (appointmentSearchRequest.getStartDate() != null) {
            query.predicates.add(query.cb.greaterThanOrEqualTo(query.root.get("startDateTime"), appointmentSearchRequest.getStartDate()));
        }
        if (appointmentSearchRequest.getEndDate() != null) {
            query.predicates.add(query.cb.lessThanOrEqualTo(query.root.get("startDateTime"), appointmentSearchRequest.getEndDate()));
        }
    }

    private Integer getLimit(AppointmentSearchRequest appointmentSearchRequest) {
        if (appointmentSearchRequest.getLimit() > 0) {
            return appointmentSearchRequest.getLimit();
        } else if (appointmentSearchRequest.getEndDate() == null) {
            return APPOINTMENT_SEARCH_DEFAULT_LIMIT;
        }
        return null;
    }

    private void setStatusCriteria(AppointmentSearchRequest appointmentSearchRequest, Query query) {
        if(appointmentSearchRequest.getStatus() != null) {
            query.predicates.add(query.cb.equal(query.root.get("status"), appointmentSearchRequest.getStatus()));
        }
    }

    @Override
    public List<Appointment> getAppointmentsForPatient(Integer patientId) {

        Query query = new Query();
        Join<Appointment, ?> patient = query.root.join("patient");
        query.predicates.add(query.cb.equal(patient.get("patientId"), patientId));
        query.predicates.add(query.cb.equal(query.root.get("voided"), false));
        query.predicates.add(query.cb.equal(patient.get("voided"), false));
        query.predicates.add(query.cb.equal(patient.get("personVoided"), false));
        query.predicates.add(query.cb.greaterThanOrEqualTo(query.root.get("startDateTime"), DateUtil.getStartOfDay()));

        return query.list();
    }

    @Override
    public List<Appointment> getAppointmentsWithoutDates(AppointmentSearchRequestModel searchQuery, Integer limit) {
        Query query = new Query();
        addSearchCriteria(query, searchQuery);
        query.predicates.add(query.root.get("startDateTime").isNull());
        query.predicates.add(query.root.get("endDateTime").isNull());
        query.criteriaQuery.orderBy(query.cb.asc(query.root.get("dateCreated")));
        return query.list(limit);
    }

    private void addSearchCriteria(Query query, AppointmentSearchRequestModel searchQuery) {
        Join<Appointment, ?> patient = addNonVoidedPatientCriteria(query);
        Join<Appointment, ?> service = query.root.join("service");

        if (searchQuery != null) {
            if (searchQuery.getPatientUuids() != null && !searchQuery.getPatientUuids().isEmpty()) {
                query.predicates.add(anyEqual(query.cb, patient.get("uuid"), searchQuery.getPatientUuids()));
            }

            if (searchQuery.getServiceUuids() != null && !searchQuery.getServiceUuids().isEmpty()) {
                query.predicates.add(anyEqual(query.cb, service.get("uuid"), searchQuery.getServiceUuids()));
            }

            if (searchQuery.getServiceTypeUuids() != null && !searchQuery.getServiceTypeUuids().isEmpty()) {
                Join<Appointment, ?> serviceType = query.root.join("serviceType");
                query.predicates.add(anyEqual(query.cb, serviceType.get("uuid"), searchQuery.getServiceTypeUuids()));
            }

            if (searchQuery.getStatus() != null) {
                query.predicates.add(query.cb.equal(query.root.get("status"), AppointmentStatus.valueOf(searchQuery.getStatus())));
            }

            if (searchQuery.getProviderUuids() != null && !searchQuery.getProviderUuids().isEmpty()) {
                Join<?, ?> provider = query.root.join("providers").join("provider");
                query.predicates.add(anyEqual(query.cb, provider.get("uuid"), searchQuery.getProviderUuids()));
            }

            if (searchQuery.getLocationUuids() != null && !searchQuery.getLocationUuids().isEmpty()) {
                Join<Appointment, ?> location = query.root.join("location");
                query.predicates.add(anyEqual(query.cb, location.get("uuid"), searchQuery.getLocationUuids()));
            }

            if (searchQuery.getPriorities() != null && !searchQuery.getPriorities().isEmpty()) {
                query.predicates.add(anyEqual(query.cb, query.root.get("priority"), searchQuery.getPriorities().stream()
                        .map(AppointmentPriority::valueOf).collect(Collectors.toList())));
            }
        }
    }

    private void setAppointmentNumberCriteria(AppointmentSearchRequest appointmentSearchRequest, Query query) {
        if (StringUtils.isNotEmpty(appointmentSearchRequest.getAppointmentNumber())) {
            query.predicates.add(query.cb.equal(query.root.get("appointmentNumber"), appointmentSearchRequest.getAppointmentNumber()));
        }
    }

    @Override
    public List<Appointment> getAppointmentsByUuids(List<String> uuids) {
        if (uuids == null || uuids.isEmpty()) {
            return Collections.emptyList();
        }
        Query query = new Query();
        query.predicates.add(query.root.get("uuid").in(uuids));
        query.predicates.add(query.cb.equal(query.root.get("voided"), false));
        return query.list();
    }

    private Join<Appointment, ?> addNonVoidedPatientCriteria(Query query) {
        Join<Appointment, ?> patient = query.root.join("patient");
        query.predicates.add(query.cb.equal(patient.get("voided"), false));
        query.predicates.add(query.cb.equal(patient.get("personVoided"), false));
        return patient;
    }

    private Predicate anyEqual(CriteriaBuilder cb, jakarta.persistence.criteria.Path<?> path, List<?> values) {
        return cb.or(values.stream().map(value -> cb.equal(path, value)).toArray(Predicate[]::new));
    }

    /**
     * Replacement for the removed Hibernate {@code Example.create(example)} query-by-example criterion: adds an equality
     * restriction for every non-null basic (non-identifier, non-association) property of the example entity.
     */
    private void addExampleCriteria(Query query, From<?, ?> from, Object example, String... excludedProperties) {
        Object entity = Hibernate.unproxy(example);
        List<String> excluded = Arrays.asList(excludedProperties);
        for (SingularAttribute<?, ?> attribute : sessionFactory.getMetamodel().entity(Hibernate.getClass(entity)).getSingularAttributes()) {
            if (attribute.isId() || attribute.isVersion()
                    || attribute.getPersistentAttributeType() != Attribute.PersistentAttributeType.BASIC
                    || excluded.contains(attribute.getName())) {
                continue;
            }
            Object value = getPropertyValue(entity, attribute.getJavaMember());
            if (value != null) {
                query.predicates.add(query.cb.equal(from.get(attribute.getName()), value));
            }
        }
    }

    private Object getPropertyValue(Object entity, Member member) {
        try {
            if (member instanceof Method) {
                Method method = (Method) member;
                method.setAccessible(true);
                return method.invoke(entity);
            }
            Field field = (Field) member;
            field.setAccessible(true);
            return field.get(entity);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Unable to read property " + member.getName() + " of " + entity.getClass(), e);
        }
    }

    private class Query {

        private final CriteriaBuilder cb = sessionFactory.getCurrentSession().getCriteriaBuilder();

        private final CriteriaQuery<Appointment> criteriaQuery = cb.createQuery(Appointment.class);

        private final Root<Appointment> root = criteriaQuery.from(Appointment.class);

        private final List<Predicate> predicates = new ArrayList<>();

        private List<Appointment> list() {
            return list(null);
        }

        private List<Appointment> list(Integer maxResults) {
            criteriaQuery.where(predicates.toArray(new Predicate[0]));
            TypedQuery<Appointment> typedQuery = sessionFactory.getCurrentSession().createQuery(criteriaQuery);
            if (maxResults != null) {
                typedQuery.setMaxResults(maxResults);
            }
            return typedQuery.getResultList();
        }
    }

}
