package org.openmrs.module.appointments.dao.impl;

import org.apache.commons.lang.StringUtils;
import org.hibernate.Criteria;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Example;

import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.hibernate.criterion.Disjunction;
import org.hibernate.sql.JoinType;
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

import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class AppointmentDaoImpl implements AppointmentDao {

    private static final int APPOINTMENT_SEARCH_DEFAULT_LIMIT = 50;
    private SessionFactory sessionFactory;

    public void setSessionFactory(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    @Override
    public List<Appointment> getAllAppointments(Date forDate) {
        Criteria criteria = sessionFactory.getCurrentSession().createCriteria(Appointment.class);
        criteria.add(Restrictions.eq("voided", false));
        criteria.createAlias("patient", "patient");
        criteria.add(Restrictions.eq("patient.voided", false));
        criteria.add(Restrictions.eq("patient.personVoided", false));
        if (forDate != null) {
            Date maxDate = new Date(forDate.getTime() + TimeUnit.DAYS.toMillis(1));
            criteria.add(Restrictions.ge("startDateTime", forDate));
            criteria.add(Restrictions.lt("endDateTime", maxDate));
        }
        return criteria.list();
    }

    @Override
    public List<Appointment> getAllAppointmentsReminder(String hours) {
        Criteria criteria = sessionFactory.getCurrentSession().createCriteria(Appointment.class);
        criteria.createAlias("patient", "patient");
        criteria.add(Restrictions.eq("patient.voided", false));
        criteria.add(Restrictions.eq("patient.personVoided", false));
        if (hours != null) {
            Date minDate = new Date(System.currentTimeMillis() + TimeUnit.HOURS.toMillis(Integer.valueOf(hours)));
            Date maxDate = new Date(minDate.getTime() + TimeUnit.HOURS.toMillis(1));
            criteria.add(Restrictions.ge("startDateTime", minDate));
            criteria.add(Restrictions.lt("startDateTime", maxDate));
        }
        criteria.add(Restrictions.ne("status", AppointmentStatus.Cancelled));
        return criteria.list();
    }

    @Transactional
    @Override
    public void save(Appointment appointment) {
        sessionFactory.getCurrentSession().saveOrUpdate(appointment);
    }

    @Override
    public List<Appointment> search(Appointment appointment) {
        Criteria criteria = sessionFactory.getCurrentSession().createCriteria(Appointment.class).add(
                Example.create(appointment).excludeProperty("uuid"));

        if (appointment.getPatient() != null) criteria.createCriteria("patient").add(
                Example.create(appointment.getPatient()));

        if (appointment.getLocation() != null) criteria.createCriteria("location").add(
                Example.create(appointment.getLocation()));

        if (appointment.getService() != null) criteria.createCriteria("service").add(
                Example.create(appointment.getService()));

        if (appointment.getProvider() != null) criteria.createCriteria("provider").add(
                Example.create(appointment.getProvider()));

        return criteria.list();
    }

    @Override
    public List<Appointment> search(AppointmentSearchRequestModel searchQuery) {
        Criteria criteria = sessionFactory.getCurrentSession().createCriteria(Appointment.class);
        addSearchCriteria(criteria, searchQuery);
        return criteria.list();
    }

    @Override
    public List<Appointment> getAllFutureAppointmentsForService(AppointmentServiceDefinition appointmentServiceDefinition) {
        Criteria criteria = sessionFactory.getCurrentSession().createCriteria(Appointment.class);
        criteria.add(Restrictions.eq("service", appointmentServiceDefinition));
        criteria.add(Restrictions.gt("endDateTime", new Date()));
        criteria.add(Restrictions.eq("voided", false));
        criteria.createAlias("patient", "patient");
        criteria.add(Restrictions.eq("patient.voided", false));
        criteria.add(Restrictions.eq("patient.personVoided", false));
        criteria.add(Restrictions.ne("status", AppointmentStatus.Cancelled));
        return criteria.list();
    }

    @Override
    public List<Appointment> getAllFutureAppointmentsForServiceType(AppointmentServiceType appointmentServiceType) {
        Criteria criteria = sessionFactory.getCurrentSession().createCriteria(Appointment.class);
        criteria.add(Restrictions.eq("serviceType", appointmentServiceType));
        criteria.add(Restrictions.gt("endDateTime", new Date()));
        criteria.add(Restrictions.eq("voided", false));
        criteria.createAlias("patient", "patient");
        criteria.add(Restrictions.eq("patient.voided", false));
        criteria.add(Restrictions.eq("patient.personVoided", false));
        criteria.add(Restrictions.ne("status", AppointmentStatus.Cancelled));
        return criteria.list();
    }

    @Override
    public List<Appointment> getAppointmentsForService(AppointmentServiceDefinition appointmentServiceDefinition, Date startDate, Date endDate, List<AppointmentStatus> appointmentStatusFilterList) {
        Criteria criteria = sessionFactory.getCurrentSession().createCriteria(Appointment.class);
        criteria.createAlias("serviceType", "serviceType", JoinType.LEFT_OUTER_JOIN);
        criteria.add(Restrictions.or(Restrictions.isNull("serviceType"), Restrictions.eq("serviceType.voided", false)));
        criteria.add(Restrictions.eq("voided", false));
        criteria.createAlias("patient", "patient");
        criteria.add(Restrictions.eq("patient.voided", false));
        criteria.add(Restrictions.eq("patient.personVoided", false));
        criteria.add(Restrictions.ge("startDateTime", startDate));
        criteria.add(Restrictions.le("startDateTime", endDate));
        criteria.createCriteria("service").add(Example.create(appointmentServiceDefinition));
        if (appointmentStatusFilterList != null && !appointmentStatusFilterList.isEmpty()) {
            criteria.add(Restrictions.in("status", appointmentStatusFilterList));
        }
        return criteria.list();

    }

    @Override
    public Appointment getAppointmentByUuid(String uuid) {
        Criteria criteria = sessionFactory.getCurrentSession().createCriteria(Appointment.class, "appointment");
        criteria.add(Restrictions.eq("uuid", uuid));
        return (Appointment) criteria.uniqueResult();
    }

    @Override
    public List<Appointment> getAllAppointmentsInDateRange(Date startDate, Date endDate) {
        Criteria criteria = sessionFactory.getCurrentSession().createCriteria(Appointment.class);
        criteria.add(Restrictions.eq("voided", false));
        criteria.createAlias("patient", "patient");
        criteria.add(Restrictions.eq("patient.voided", false));
        criteria.add(Restrictions.eq("patient.personVoided", false));
        if (startDate != null) {
            criteria.add(Restrictions.ge("startDateTime", startDate));
        }
        if (endDate != null) {
            criteria.add(Restrictions.lt("endDateTime", endDate));
        }
        return criteria.list();
    }

    @Override
    public List<Appointment> search(AppointmentSearchRequest appointmentSearchRequest) {
        Criteria criteria = sessionFactory.getCurrentSession().createCriteria(Appointment.class);

        criteria.add(Restrictions.eq("voided", false));
        criteria.addOrder(Order.asc("startDateTime"));
        setDateCriteria(appointmentSearchRequest, criteria);
        setPatientCriteria(appointmentSearchRequest, criteria);
        setLimitCriteria(appointmentSearchRequest, criteria);
        setProviderCriteria(appointmentSearchRequest, criteria);
        setStatusCriteria(appointmentSearchRequest, criteria);
        setAppointmentNumberCriteria(appointmentSearchRequest, criteria);


        return criteria.list();
    }

    private void setProviderCriteria(AppointmentSearchRequest appointmentSearchRequest, Criteria criteria) {
        if (StringUtils.isNotEmpty(appointmentSearchRequest.getProviderUuid())) {
            criteria.createAlias("providers", "providers");
            criteria.createAlias("providers.provider", "provider");
            criteria.add(Restrictions.eq("provider.uuid", appointmentSearchRequest.getProviderUuid()));
        }
    }

    private void setPatientCriteria(AppointmentSearchRequest appointmentSearchRequest, Criteria criteria) {
        criteria.createAlias("patient", "patient");
        criteria.add(Restrictions.eq("patient.voided", false));
        criteria.add(Restrictions.eq("patient.personVoided", false));
        if (StringUtils.isNotEmpty(appointmentSearchRequest.getPatientUuid())) {
            criteria.add(Restrictions.eq("patient.uuid", appointmentSearchRequest.getPatientUuid()));
        }
    }

    private void setDateCriteria(AppointmentSearchRequest appointmentSearchRequest, Criteria criteria) {
        if (appointmentSearchRequest.getStartDate() != null) {
            criteria.add(Restrictions.ge("startDateTime", appointmentSearchRequest.getStartDate()));
        }
        if (appointmentSearchRequest.getEndDate() != null) {
            criteria.add(Restrictions.le("startDateTime", appointmentSearchRequest.getEndDate()));
        }
    }

    private void setLimitCriteria(AppointmentSearchRequest appointmentSearchRequest, Criteria criteria) {
        if (appointmentSearchRequest.getLimit() > 0) {
            criteria.setMaxResults(appointmentSearchRequest.getLimit());
        } else if (appointmentSearchRequest.getEndDate() == null) {
            criteria.setMaxResults(APPOINTMENT_SEARCH_DEFAULT_LIMIT);
        }
    }

    private void setStatusCriteria(AppointmentSearchRequest appointmentSearchRequest, Criteria criteria) {
        if(appointmentSearchRequest.getStatus() != null) {
            criteria.add(Restrictions.eq("status", appointmentSearchRequest.getStatus()));
        }
    }

    @Override
    public List<Appointment> getAppointmentsForPatient(Integer patientId) {

        Criteria criteria = sessionFactory.getCurrentSession().createCriteria(Appointment.class);
        criteria.createAlias("patient", "patient");
        criteria.add(Restrictions.eq("patient.patientId", patientId));
        criteria.add(Restrictions.eq("voided", false));
        criteria.add(Restrictions.eq("patient.voided", false));
        criteria.add(Restrictions.eq("patient.personVoided", false));
        criteria.add(Restrictions.ge("startDateTime", DateUtil.getStartOfDay()));

        return criteria.list();
    }

    @Override
    public List<Appointment> getAppointmentsWithoutDates(AppointmentSearchRequestModel searchQuery, Integer limit) {
        Criteria criteria = sessionFactory.getCurrentSession().createCriteria(Appointment.class);
        addSearchCriteria(criteria, searchQuery);
        criteria.add(Restrictions.isNull("startDateTime"));
        criteria.add(Restrictions.isNull("endDateTime"));
        criteria.addOrder(Order.asc("dateCreated"));
        if (limit != null) {
            criteria.setMaxResults(limit);
        }
        return criteria.list();
    }

    private void addSearchCriteria(Criteria criteria, AppointmentSearchRequestModel searchQuery) {
        criteria.createAlias("patient", "patient");
        criteria.add(Restrictions.eq("patient.voided", false));
        criteria.add(Restrictions.eq("patient.personVoided", false));
        criteria.createAlias("service", "service");

        if (searchQuery != null) {
            if (searchQuery.getPatientUuids() != null && !searchQuery.getPatientUuids().isEmpty()) {
                Disjunction disjunction = Restrictions.disjunction();
                searchQuery.getPatientUuids().stream()
                        .map(patientUuid -> Restrictions.eq("patient.uuid", patientUuid))
                        .forEach(disjunction::add);
                criteria.add(disjunction);
            }

            if (searchQuery.getServiceUuids() != null && !searchQuery.getServiceUuids().isEmpty()) {
                Disjunction disjunction = Restrictions.disjunction();
                searchQuery.getServiceUuids().stream()
                        .map(serviceUuid -> Restrictions.eq("service.uuid", serviceUuid))
                        .forEach(disjunction::add);
                criteria.add(disjunction);
            }

            if (searchQuery.getServiceTypeUuids() != null && !searchQuery.getServiceTypeUuids().isEmpty()) {
                criteria.createAlias("serviceType", "serviceType");
                Disjunction disjunction = Restrictions.disjunction();
                searchQuery.getServiceTypeUuids().stream()
                        .map(serviceTypeUuid -> Restrictions.eq("serviceType.uuid", serviceTypeUuid))
                        .forEach(disjunction::add);
                criteria.add(disjunction);
            }

            if (searchQuery.getStatus() != null) {
                criteria.add(Restrictions.eq("status", AppointmentStatus.valueOf(searchQuery.getStatus())));
            }

            if (searchQuery.getProviderUuids() != null && !searchQuery.getProviderUuids().isEmpty()) {
                criteria.createAlias("providers", "providers");
                criteria.createAlias("providers.provider", "provider");
                Disjunction disjunction = Restrictions.disjunction();
                searchQuery.getProviderUuids().stream()
                        .map(providerUuid -> Restrictions.eq("provider.uuid", providerUuid))
                        .forEach(disjunction::add);
                criteria.add(disjunction);
            }

            if (searchQuery.getLocationUuids() != null && !searchQuery.getLocationUuids().isEmpty()) {
                criteria.createAlias("location", "location");
                Disjunction disjunction = Restrictions.disjunction();
                searchQuery.getLocationUuids().stream()
                        .map(locationUuid -> Restrictions.eq("location.uuid", locationUuid))
                        .forEach(disjunction::add);
                criteria.add(disjunction);
            }

            if (searchQuery.getPriorities() != null && !searchQuery.getPriorities().isEmpty()) {
                Disjunction disjunction = Restrictions.disjunction();
                searchQuery.getPriorities().stream()
                        .map(priority -> Restrictions.eq("priority", AppointmentPriority.valueOf(priority)))
                        .forEach(disjunction::add);
                criteria.add(disjunction);
            }
        }
    }

    private void setAppointmentNumberCriteria(AppointmentSearchRequest appointmentSearchRequest, Criteria criteria) {
        if (StringUtils.isNotEmpty(appointmentSearchRequest.getAppointmentNumber())) {
            criteria.add(Restrictions.eq("appointmentNumber", appointmentSearchRequest.getAppointmentNumber()));
        }
    }

}
