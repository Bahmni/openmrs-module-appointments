package org.openmrs.module.appointments.service.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Location;
import org.openmrs.Provider;
import org.openmrs.api.APIException;
import org.openmrs.api.context.Context;
import org.openmrs.module.appointments.dao.AppointmentUnavailabilityDao;
import org.openmrs.module.appointments.model.AppointmentServiceDefinition;
import org.openmrs.module.appointments.model.AppointmentUnavailability;
import org.openmrs.module.appointments.search.param.AppointmentUnavailabilitySearchParams;
import org.openmrs.module.appointments.service.AppointmentServiceDefinitionService;
import org.openmrs.module.appointments.service.AppointmentUnavailabilityService;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Time;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Transactional
public class AppointmentUnavailabilityServiceImpl implements AppointmentUnavailabilityService {

    private Log log = LogFactory.getLog(this.getClass());

    private final AppointmentUnavailabilityDao appointmentUnavailabilityDao;
    private final AppointmentServiceDefinitionService appointmentServiceDefinitionService;

    public AppointmentUnavailabilityServiceImpl(AppointmentUnavailabilityDao appointmentUnavailabilityDao,
                                                 AppointmentServiceDefinitionService appointmentServiceDefinitionService) {
        this.appointmentUnavailabilityDao = appointmentUnavailabilityDao;
        this.appointmentServiceDefinitionService = appointmentServiceDefinitionService;
    }

    @Override
    public List<AppointmentUnavailability> save(List<AppointmentUnavailability> appointmentUnavailabilities) {
        if (appointmentUnavailabilities == null || appointmentUnavailabilities.isEmpty()) {
            log.error("Save failed: Request must contain at least one unavailability block");
            throw new APIException("Request must contain at least one unavailability block");
        }

        for (int i = 0; i < appointmentUnavailabilities.size(); i++) {
            AppointmentUnavailability unavailability = appointmentUnavailabilities.get(i);
            String indexPrefix = "[" + i + "] ";

            validateUnavailability(unavailability, indexPrefix);
        }

        List<AppointmentUnavailability> savedUnavailabilities = new ArrayList<>();
        for (AppointmentUnavailability unavailability : appointmentUnavailabilities) {
            savedUnavailabilities.add(appointmentUnavailabilityDao.save(unavailability));
        }

        log.info("Successfully saved " + savedUnavailabilities.size() + " appointment unavailability block(s)");
        return savedUnavailabilities;
    }

    private void validateUnavailability(AppointmentUnavailability unavailability, String indexPrefix) {
        Location location = Context.getLocationService().getLocation(unavailability.getLocation().getLocationId());
        if (location == null || location.getRetired()) {
            log.error(indexPrefix + "Validation failed: location is invalid or retired");
            throw new APIException(indexPrefix + "location is invalid or retired");
        }

        if (unavailability.getService() != null) {
            AppointmentServiceDefinition service = appointmentServiceDefinitionService
                    .getAppointmentServiceByUuid(unavailability.getService().getUuid());
            if (service == null || service.getVoided()) {
                log.error(indexPrefix + "Validation failed: service is invalid or voided");
                throw new APIException(indexPrefix + "service is invalid or voided");
            }
            unavailability.setService(service);

            if (service.getLocation() != null && !service.getLocation().equals(location)) {
                log.error(indexPrefix + "Validation failed: Service does not belong to the specified location");
                throw new APIException(indexPrefix + "Service does not belong to the specified location");
            }
        }

        if (unavailability.getProvider() != null) {
            Provider provider = Context.getProviderService().getProvider(unavailability.getProvider().getProviderId());
            if (provider == null || provider.getRetired()) {
                log.error(indexPrefix + "Validation failed: provider is invalid or retired");
                throw new APIException(indexPrefix + "provider is invalid or retired");
            }
        }

        if (!isStartBeforeEnd(unavailability.getStartDate(), unavailability.getStartTime(),
                unavailability.getEndDate(), unavailability.getEndTime())) {
            log.error(indexPrefix + "Validation failed: End date/time must be after start date/time");
            throw new APIException(indexPrefix + "End date/time must be after start date/time");
        }

        Date now = new Date();
        Date endDateTime = combineDateAndTime(unavailability.getEndDate(), unavailability.getEndTime());
        if (endDateTime.before(now)) {
            log.error(indexPrefix + "Validation failed: Cannot create unavailability block that has already ended");
            throw new APIException(indexPrefix + "Cannot create unavailability block that has already ended");
        }
    }

    private Date combineDateAndTime(java.sql.Date date, Time time) {
        LocalDate localDate = date.toLocalDate();
        LocalTime localTime = time.toLocalTime();
        LocalDateTime combined = LocalDateTime.of(localDate, localTime);
        return Date.from(combined.atZone(ZoneId.systemDefault()).toInstant());
    }

    private boolean isStartBeforeEnd(java.sql.Date startDate, Time startTime,
                                      java.sql.Date endDate, Time endTime) {
        return endDate.after(startDate) || (endDate.equals(startDate) && endTime.after(startTime));
    }

    @Override
    public AppointmentUnavailability getByUuid(String uuid) {
        AppointmentUnavailability unavailability = appointmentUnavailabilityDao.getByUuid(uuid);
        if (unavailability == null) {
            log.info("No appointment unavailability found with uuid: " + uuid);
        }
        return unavailability;
    }

    @Override
    public List<AppointmentUnavailability> getAll(AppointmentUnavailabilitySearchParams searchParams) {
        List<AppointmentUnavailability> unavailabilities = appointmentUnavailabilityDao.getAll(searchParams);
        log.info("Retrieved " + unavailabilities.size() + " appointment unavailability block(s)");
        return unavailabilities;
    }

    @Override
    public void voidAppointmentUnavailability(AppointmentUnavailability appointmentUnavailability, String voidReason) {
        appointmentUnavailability.setVoided(true);
        appointmentUnavailability.setVoidReason(voidReason);
        appointmentUnavailability.setVoidedBy(Context.getAuthenticatedUser());
        appointmentUnavailability.setDateVoided(new Date());
        appointmentUnavailabilityDao.save(appointmentUnavailability);
        log.info("Successfully voided appointment unavailability: " + appointmentUnavailability.getUuid());
    }
}
