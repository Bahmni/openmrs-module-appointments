package org.openmrs.module.appointments.dao.impl;

import org.apache.commons.lang.StringUtils;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;
import org.openmrs.Location;
import org.openmrs.Provider;
import org.openmrs.api.LocationService;
import org.openmrs.api.ProviderService;
import org.openmrs.module.appointments.dao.AppointmentUnavailabilityDao;
import org.openmrs.module.appointments.model.AppointmentServiceDefinition;
import org.openmrs.module.appointments.model.AppointmentUnavailability;
import org.openmrs.module.appointments.search.param.AppointmentUnavailabilitySearchParams;
import org.openmrs.module.appointments.service.AppointmentServiceDefinitionService;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class AppointmentUnavailabilityDaoImpl implements AppointmentUnavailabilityDao {

    private SessionFactory sessionFactory;
    private LocationService locationService;
    private ProviderService providerService;
    private AppointmentServiceDefinitionService appointmentServiceDefinitionService;

    public void setSessionFactory(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    public void setLocationService(LocationService locationService) {
        this.locationService = locationService;
    }

    public void setProviderService(ProviderService providerService) {
        this.providerService = providerService;
    }

    public void setAppointmentServiceDefinitionService(AppointmentServiceDefinitionService appointmentServiceDefinitionService) {
        this.appointmentServiceDefinitionService = appointmentServiceDefinitionService;
    }

    @Transactional
    @Override
    public AppointmentUnavailability save(AppointmentUnavailability appointmentUnavailability) {
        Session currentSession = sessionFactory.getCurrentSession();
        currentSession.saveOrUpdate(appointmentUnavailability);
        return appointmentUnavailability;
    }

    @Override
    public AppointmentUnavailability getByUuid(String uuid) {
        Session currentSession = sessionFactory.getCurrentSession();
        Criteria criteria = currentSession.createCriteria(AppointmentUnavailability.class, "appointmentUnavailability");
        criteria.add(Restrictions.eq("uuid", uuid));
        AppointmentUnavailability appointmentUnavailability = (AppointmentUnavailability) criteria.uniqueResult();
        evictObjectFromSession(currentSession, appointmentUnavailability);
        return appointmentUnavailability;
    }

    @Override
    public List<AppointmentUnavailability> getAll(AppointmentUnavailabilitySearchParams searchParams) {
        CriteriaBuilder criteriaBuilder = sessionFactory.getCurrentSession().getCriteriaBuilder();
        CriteriaQuery<AppointmentUnavailability> criteriaQuery = criteriaBuilder.createQuery(AppointmentUnavailability.class);
        Root<AppointmentUnavailability> root = criteriaQuery.from(AppointmentUnavailability.class);

        List<Predicate> predicates = new ArrayList<>();

        if (StringUtils.isNotBlank(searchParams.getLocationUuid())) {
            Location location = locationService.getLocationByUuid(searchParams.getLocationUuid());
            if (location != null) {
                predicates.add(criteriaBuilder.equal(root.get("location"), location));
            }
        }

        if (StringUtils.isNotBlank(searchParams.getServiceUuid())) {
            AppointmentServiceDefinition service = appointmentServiceDefinitionService.getAppointmentServiceByUuid(searchParams.getServiceUuid());
            if (service != null) {
                predicates.add(criteriaBuilder.equal(root.get("service"), service));
            }
        }

        if (StringUtils.isNotBlank(searchParams.getProviderUuid())) {
            Provider provider = providerService.getProviderByUuid(searchParams.getProviderUuid());
            if (provider != null) {
                predicates.add(criteriaBuilder.equal(root.get("provider"), provider));
            }
        }

        if (StringUtils.isNotBlank(searchParams.getStartDate())) {
            Date startDate = java.sql.Date.valueOf(LocalDate.parse(searchParams.getStartDate(), DateTimeFormatter.ofPattern("yyyy-MM-dd")));
            predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("endDate"), startDate));
        }

        if (StringUtils.isNotBlank(searchParams.getEndDate())) {
            Date endDate = java.sql.Date.valueOf(LocalDate.parse(searchParams.getEndDate(), DateTimeFormatter.ofPattern("yyyy-MM-dd")));
            predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("startDate"), endDate));
        }

        if (!searchParams.isIncludeVoided()) {
            predicates.add(criteriaBuilder.equal(root.get("voided"), false));
        }

        if (!predicates.isEmpty()) {
            criteriaQuery.where(criteriaBuilder.and(predicates.toArray(new Predicate[0])));
        }

        criteriaQuery.orderBy(criteriaBuilder.asc(root.get("startDate")), criteriaBuilder.asc(root.get("startTime")));
        TypedQuery<AppointmentUnavailability> query = sessionFactory.getCurrentSession().createQuery(criteriaQuery);

        if (searchParams.getLimit() != null && searchParams.getLimit() > 0) {
            query.setMaxResults(searchParams.getLimit());
        }

        return query.getResultList();
    }

    private void evictObjectFromSession(Session currentSession, AppointmentUnavailability appointmentUnavailability) {
        if (appointmentUnavailability != null) {
            currentSession.evict(appointmentUnavailability);
        }
    }
}
