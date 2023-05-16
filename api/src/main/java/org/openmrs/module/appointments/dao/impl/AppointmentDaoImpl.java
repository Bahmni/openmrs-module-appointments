package org.openmrs.module.appointments.dao.impl;

import org.apache.commons.lang.StringUtils;
import org.hibernate.Criteria;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Example;
import org.hibernate.Session;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Property;
import org.hibernate.criterion.Conjunction;
import org.hibernate.criterion.Disjunction;

import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.hibernate.sql.JoinType;
import org.openmrs.module.appointments.dao.AppointmentDao;
import org.openmrs.module.appointments.model.Appointment;
import org.openmrs.module.appointments.model.AppointmentSearchRequest;
import org.openmrs.module.appointments.model.AppointmentServiceDefinition;
import org.openmrs.module.appointments.model.AppointmentServiceType;
import org.openmrs.module.appointments.model.AppointmentStatus;
import org.openmrs.module.appointments.util.DateUtil;
import org.springframework.transaction.annotation.Transactional;
import org.openmrs.Patient;
import org.openmrs.Visit;


import java.util.ArrayList;
import java.util.Arrays;
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
    public List<Appointment> getAllAppointments(Date forDate, String status) {
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

        if (StringUtils.isNotEmpty(status)) {
            criteria.add(Restrictions.eq("status", AppointmentStatus.valueOf(status))); //TODO: we may need to explore Optional construct to help validate against missing enum values
        }
        return criteria.list();
    }

    @Override
    public List<Appointment> getPendingAppointments(Date forDate) {
        /**
         * SELECT * FROM patient_appointment
            where start_date_time >= '2023-01-10T00:00:00.000' and start_date_time <= '2023-01-11T00:00:00.000' and patient_id not in
            (SELECT patient_id FROM visit where date_started >= '2023-01-10T00:00:00.000' and date_started <= '2023-01-11T00:00:00.000');
         */
        Session session = sessionFactory.getCurrentSession();
        Criteria criteria = session.createCriteria(Appointment.class);
        Date maxDate = new Date(forDate.getTime() + TimeUnit.DAYS.toMillis(1));
        criteria.add(Restrictions.ge("startDateTime", forDate));
        criteria.add(Restrictions.lt("startDateTime", maxDate));
        DetachedCriteria subquery = DetachedCriteria.forClass(Visit.class, "sub_v");
        // subquery.add(Restrictions.or(Restrictions.and(Restrictions.le("sub_v.startDatetime", forDate), Restrictions.or(Restrictions.ge("sub_v.stopDatetime", forDate), Restrictions.isNull("sub_v.stopDatetime"))), Restrictions.between("sub_v.startDatetime", forDate, maxDate)));
        subquery.add(Restrictions.between("sub_v.startDatetime", forDate, maxDate));
        subquery.setProjection(Projections.property("sub_v.patient"));
        criteria.add(Property.forName("patient").notIn(subquery));
        List<Appointment> results = criteria.list();
        // Appointment fofo = new Appointment();
        // fofo.getPatient();
        // Visit v = new Visit();
        // v.getStopDatetime();
        // v.getStartDatetime();
        return(results);
    }

    @Override
    public List<Appointment> getHonouredAppointments(Date forDate) {
        /**
         * SELECT * FROM patient_appointment
            where start_date_time >= '2023-01-10T00:00:00.000' and start_date_time <= '2023-01-11T00:00:00.000' and patient_id in
            (SELECT patient_id FROM visit where date_started >= '2023-01-10T00:00:00.000' and date_started <= '2023-01-11T00:00:00.000');
         */
        Session session = sessionFactory.getCurrentSession();
        Criteria criteria = session.createCriteria(Appointment.class);
        Date maxDate = new Date(forDate.getTime() + TimeUnit.DAYS.toMillis(1));
        criteria.add(Restrictions.ge("startDateTime", forDate));
        criteria.add(Restrictions.lt("startDateTime", maxDate));
        DetachedCriteria subquery = DetachedCriteria.forClass(Visit.class, "sub_v");
        // subquery.add(Restrictions.or(Restrictions.and(Restrictions.le("sub_v.startDatetime", forDate), Restrictions.or(Restrictions.ge("sub_v.stopDatetime", forDate), Restrictions.isNull("sub_v.stopDatetime"))), Restrictions.between("sub_v.startDatetime", forDate, maxDate)));
        subquery.add(Restrictions.between("sub_v.startDatetime", forDate, maxDate));
        subquery.setProjection(Projections.property("sub_v.patient"));
        criteria.add(Property.forName("patient").in(subquery));
        List<Appointment> results = criteria.list();
        // Appointment fofo = new Appointment();
        // fofo.getPatient();
        // Visit v = new Visit();
        // v.getStopDatetime();
        // v.getStartDatetime();
        return(results);
    }

    @Override
    public List<Appointment> getCameEarlyAppointments(Date forDate) {
        /**
         * SELECT * FROM patient_appointment
            where start_date_time >= '2023-01-10T00:00:00.000' and start_date_time <= '2023-01-11T00:00:00.000' and patient_id not in
            (SELECT patient_id FROM visit where (date_started <= '2023-01-10T00:00:00.000' and (date_stopped >= '2023-01-10T00:00:00.000' or date_stopped is null)) or (date_started >= '2023-01-10T00:00:00.000' and date_started <= '2023-01-11T00:00:00.000'));
         */
        Session session = sessionFactory.getCurrentSession();
        Criteria criteria = session.createCriteria(Appointment.class);
        Date maxDate = new Date(forDate.getTime() + TimeUnit.DAYS.toMillis(1));
        criteria.add(Restrictions.ge("startDateTime", forDate));
        criteria.add(Restrictions.lt("startDateTime", maxDate));
        DetachedCriteria subquery = DetachedCriteria.forClass(Visit.class, "sub_v");

        //subquery.add(Restrictions.or(Restrictions.isNull("sub_v.stopDatetime"), Restrictions.and(Restrictions.ge("sub_v.startDatetime", forDate), Restrictions.lt("sub_v.startDatetime", maxDate))));
        Conjunction conjunction = Restrictions.conjunction();
        conjunction.add(Restrictions.le("sub_v.startDatetime", forDate));
        conjunction.add(Restrictions.or(Restrictions.ge("sub_v.stopDatetime", forDate), Restrictions.isNull("sub_v.stopDatetime")));
        subquery.add(conjunction);

        Disjunction disjunction = Restrictions.disjunction();
        disjunction.add(Restrictions.between("sub_v.startDatetime", forDate, maxDate));
        subquery.add(disjunction);

        subquery.setProjection(Projections.property("sub_v.patient"));
        criteria.add(Property.forName("patient").in(subquery));
        List<Appointment> results = criteria.list();
        // Appointment fofo = new Appointment();
        // fofo.getPatient();
        // Visit v = new Visit();
        // v.getStopDatetime();
        // v.getStartDatetime();
        return(results);
    }

    @Override
    public List<Appointment> getRescheduledAppointments(Date forDate) {
        /**
         * Added new status Rescheduled
         * AppointmentStatus.java - L4
         * */
        Session session = sessionFactory.getCurrentSession();
        Criteria criteria = session.createCriteria(Appointment.class);
        Date maxDate = new Date(forDate.getTime() + TimeUnit.DAYS.toMillis(1));
        criteria.add(Restrictions.ge("startDateTime", forDate));
        criteria.add(Restrictions.lt("startDateTime", maxDate));
        // criteria.add(Restrictions.eq("status", "rescheduled"));
        criteria.add(Restrictions.eq("status", AppointmentStatus.Rescheduled));
        List<Appointment> results = criteria.list();
        // Appointment fofo = new Appointment();
        // fofo.getPatient();
        // Visit v = new Visit();
        // v.getStopDatetime();
        // v.getStartDatetime();
        return(results);
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

        if(appointment.getPatient()!=null) criteria.createCriteria("patient").add(
                Example.create(appointment.getPatient()));

        if(appointment.getLocation()!=null) criteria.createCriteria("location").add(
                Example.create(appointment.getLocation()));

        if(appointment.getService()!=null) criteria.createCriteria("service").add(
                Example.create(appointment.getService()));

        if(appointment.getProvider()!=null) criteria.createCriteria("provider").add(
                Example.create(appointment.getProvider()));

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
        return  criteria.list();

    }

    @Override
    public Appointment getAppointmentByUuid(String uuid) {
        Criteria criteria = sessionFactory.getCurrentSession().createCriteria(Appointment.class, "appointment");
        criteria.add(Restrictions.eq("uuid", uuid));
        return (Appointment) criteria.uniqueResult();
    }

    @Override
    public Appointment getAppointmentById(Integer id) {
        Criteria criteria = sessionFactory.getCurrentSession().createCriteria(Appointment.class, "appointment");
        criteria.add(Restrictions.eq("id", id));
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
    public List<Appointment> getAllCameEarlyAppointments(Date forDate) {
        Criteria criteria = sessionFactory.getCurrentSession().createCriteria(Appointment.class);
        criteria.add(Restrictions.eq("voided", false));
        criteria.createAlias("patient", "patient");
        criteria.add(Restrictions.eq("patient.voided", false));
        criteria.add(Restrictions.eq("patient.personVoided", false));
        if (forDate != null) {
            Date maxDate = new Date(forDate.getTime() + TimeUnit.DAYS.toMillis(1));
            criteria.add(Restrictions.ge("startDateTime", forDate));
            criteria.add(Restrictions.lt("endDateTime", maxDate));
            criteria.add(Restrictions.isNotNull("dateHonored"));
            criteria.add(Restrictions.lt("dateHonored", forDate));
        }
        return criteria.list();
    }

    /**
     * SELECT p.* FROM patient_appointment p
     * where start_date_time >= '2023-05-10T00:00:00.000' and start_date_time <= '2023-05-11T23:00:00.000'
     * and p.patient_id in (select v.patient_id from visit v inner join encounter e on (v.visit_id = e.visit_id) #and encounter_id in (11))
     * where (v.date_started >= '2023-05-10T00:00:00.000' and v.date_started <= '2023-05-11T23:00:00.000') and (v.date_stopped >= '2023-05-10T00:00:00.000' and v.date_stopped <= '2023-05-11T23:00:00.000'))
     */
    @Override
    public List<Appointment> getCompletedAppointments(Date forDate) {
        Date endOfDay = new Date(forDate.getTime() + TimeUnit.DAYS.toMillis(1));

        Criteria criteria = sessionFactory.getCurrentSession().createCriteria(Appointment.class);
        criteria.add(Restrictions.ge("startDateTime", forDate));
        criteria.add(Restrictions.lt("startDateTime", endOfDay));
        DetachedCriteria subQuery = DetachedCriteria.forClass(Visit.class, "sub_v");
        subQuery.createAlias("sub_v.encounters", "enc");
        subQuery.createAlias("enc.encounterType", "encType");
        subQuery.add(Restrictions.ge("sub_v.startDatetime", forDate));
        subQuery.add(Restrictions.lt("sub_v.stopDatetime", endOfDay));
        /**HIV Consultation - a0034eee-1940-4e35-847f-97537a35d05e, CWC Consultation - bcc6da85-72f2-4291-b206-789b8186a021,MCH Mother Consultation - c6d09e05-1f25-4164-8860-9f32c5a02df0, TB FollowUp - fbf0bfce-e9f4-45bb-935a-59195d8a0e35, ART Refill - e87aa2ad-6886-422e-9dfd-064e3bfe3aad,
         IPT FollowUp - aadeafbe-a3b1-4c57-bc76-8461b778ebd6, PrEP Consultation - c4a2be28-6673-4c36-b886-ea89b0a42116, PrEP Monthly refill - 291c0828-a216-11e9-a2a3-2a2ae2dbcce4, KP Clinic visit form - 92e03f22-9686-11e9-bc42-526af7764f64, Cervical cancer screening - 3fefa230-ea10-45c7-b62b-b3b8eb7274bb,
         VMMC Client Follow up - 2504e865-638e-4a63-bf08-7e8f03a376f3, VMMC Immediate Post-Operation Assessment - 6632e66c-9ae5-11ec-b909-0242ac120002 */
        subQuery.add(Restrictions.in("encType.uuid", Arrays.asList("a0034eee-1940-4e35-847f-97537a35d05e", "bcc6da85-72f2-4291-b206-789b8186a021", "c6d09e05-1f25-4164-8860-9f32c5a02df0", "fbf0bfce-e9f4-45bb-935a-59195d8a0e35", "e87aa2ad-6886-422e-9dfd-064e3bfe3aad",
                "aadeafbe-a3b1-4c57-bc76-8461b778ebd6", "c4a2be28-6673-4c36-b886-ea89b0a42116", "291c0828-a216-11e9-a2a3-2a2ae2dbcce4", "92e03f22-9686-11e9-bc42-526af7764f64", "3fefa230-ea10-45c7-b62b-b3b8eb7274bb", "2504e865-638e-4a63-bf08-7e8f03a376f3", "6632e66c-9ae5-11ec-b909-0242ac120002")));
        subQuery.setProjection(Projections.property("sub_v.patient"));
        criteria.add(Property.forName("patient").in(subQuery));
        List<Appointment> appointments = criteria.list();

        return appointments;
    }
}
