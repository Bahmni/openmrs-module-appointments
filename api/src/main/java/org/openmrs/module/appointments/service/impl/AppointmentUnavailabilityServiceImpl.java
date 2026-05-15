package org.openmrs.module.appointments.service.impl;

import org.openmrs.Location;
import org.openmrs.Provider;
import org.openmrs.api.context.Context;
import org.openmrs.module.appointments.dao.AppointmentUnavailabilityDao;
import org.openmrs.module.appointments.model.AppointmentServiceDefinition;
import org.openmrs.module.appointments.model.AppointmentUnavailability;
import org.openmrs.module.appointments.service.AppointmentServiceDefinitionService;
import org.openmrs.module.appointments.service.AppointmentUnavailabilityService;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Time;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

@Transactional
public class AppointmentUnavailabilityServiceImpl implements AppointmentUnavailabilityService {

    private AppointmentUnavailabilityDao appointmentUnavailabilityDao;
    private AppointmentServiceDefinitionService appointmentServiceDefinitionService;

    public void setAppointmentUnavailabilityDao(AppointmentUnavailabilityDao appointmentUnavailabilityDao) {
        this.appointmentUnavailabilityDao = appointmentUnavailabilityDao;
    }

    public void setAppointmentServiceDefinitionService(AppointmentServiceDefinitionService appointmentServiceDefinitionService) {
        this.appointmentServiceDefinitionService = appointmentServiceDefinitionService;
    }

    @Override
    public List<AppointmentUnavailability> save(List<AppointmentUnavailability> appointmentUnavailabilities) {
        // Batch-level validation
        if (appointmentUnavailabilities == null || appointmentUnavailabilities.isEmpty()) {
            throw new RuntimeException("Request must contain at least one unavailability block");
        }

        // Validate all elements before persisting any
        for (int i = 0; i < appointmentUnavailabilities.size(); i++) {
            AppointmentUnavailability unavailability = appointmentUnavailabilities.get(i);
            String indexPrefix = "[" + i + "] ";

            validateUnavailability(unavailability, indexPrefix);
        }

        // All validations passed, persist all
        List<AppointmentUnavailability> savedUnavailabilities = new ArrayList<>();
        for (AppointmentUnavailability unavailability : appointmentUnavailabilities) {
            savedUnavailabilities.add(appointmentUnavailabilityDao.save(unavailability));
        }

        return savedUnavailabilities;
    }

    private void validateUnavailability(AppointmentUnavailability unavailability, String indexPrefix) {
        // Rule 1: All date/time fields must not be null
        if (unavailability.getStartDate() == null) {
            throw new RuntimeException(indexPrefix + "startDate is required");
        }
        if (unavailability.getStartTime() == null) {
            throw new RuntimeException(indexPrefix + "startTime is required");
        }
        if (unavailability.getEndDate() == null) {
            throw new RuntimeException(indexPrefix + "endDate is required");
        }
        if (unavailability.getEndTime() == null) {
            throw new RuntimeException(indexPrefix + "endTime is required");
        }

        // Rule 2: Combined start must be strictly before combined end
        if (!isStartBeforeEnd(unavailability.getStartDate(), unavailability.getStartTime(),
                unavailability.getEndDate(), unavailability.getEndTime())) {
            throw new RuntimeException(indexPrefix + "endDate/endTime must be after startDate/startTime");
        }

        // Rule 3: Location must resolve and be non-retired
        if (unavailability.getLocation() == null) {
            throw new RuntimeException(indexPrefix + "location is required");
        }
        Location location = Context.getLocationService().getLocation(unavailability.getLocation().getLocationId());
        if (location == null || location.getRetired()) {
            throw new RuntimeException(indexPrefix + "location is invalid or retired");
        }

        // Rule 4: If service provided, must resolve and be non-voided
        if (unavailability.getService() != null) {
            AppointmentServiceDefinition service = appointmentServiceDefinitionService
                    .getAppointmentServiceByUuid(unavailability.getService().getUuid());
            if (service == null || service.getVoided()) {
                throw new RuntimeException(indexPrefix + "service is invalid or voided");
            }
            unavailability.setService(service);

            // Rule 6: Service-location coherence
            if (service.getLocation() != null && !service.getLocation().equals(location)) {
                throw new RuntimeException(indexPrefix + "Service does not belong to the specified location");
            }
        }

        // Rule 5: If provider provided, must resolve and be non-retired
        if (unavailability.getProvider() != null) {
            Provider provider = Context.getProviderService().getProvider(unavailability.getProvider().getProviderId());
            if (provider == null || provider.getRetired()) {
                throw new RuntimeException(indexPrefix + "provider is invalid or retired");
            }
        }

        // Rule 7: Past blocks - reject if end is before now
        Date now = new Date();
        Date endDateTime = combineDateAndTime(unavailability.getEndDate(), unavailability.getEndTime());
        if (endDateTime.before(now)) {
            throw new RuntimeException(indexPrefix + "Cannot create unavailability block that has already ended");
        }
    }

    private boolean isStartBeforeEnd(java.sql.Date startDate, Time startTime,
                                      java.sql.Date endDate, Time endTime) {
        if (endDate.after(startDate)) {
            return true;
        }
        if (endDate.equals(startDate) && endTime.after(startTime)) {
            return true;
        }
        return false;
    }

    private Date combineDateAndTime(java.sql.Date date, Time time) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);

        Calendar timeCal = Calendar.getInstance();
        timeCal.setTime(time);

        cal.set(Calendar.HOUR_OF_DAY, timeCal.get(Calendar.HOUR_OF_DAY));
        cal.set(Calendar.MINUTE, timeCal.get(Calendar.MINUTE));
        cal.set(Calendar.SECOND, timeCal.get(Calendar.SECOND));
        cal.set(Calendar.MILLISECOND, 0);

        return cal.getTime();
    }

    @Override
    public AppointmentUnavailability getByUuid(String uuid) {
        return appointmentUnavailabilityDao.getByUuid(uuid);
    }

    @Override
    public List<AppointmentUnavailability> getAll(Location location, AppointmentServiceDefinition service,
                                                   Provider provider, Date startDate, Date endDate,
                                                   boolean includeVoided, Integer limit) {
        return appointmentUnavailabilityDao.getAll(location, service, provider, startDate, endDate, includeVoided, limit);
    }

    @Override
    public void voidAppointmentUnavailability(AppointmentUnavailability appointmentUnavailability, String voidReason) {
        appointmentUnavailability.setVoided(true);
        appointmentUnavailability.setVoidReason(voidReason);
        appointmentUnavailability.setVoidedBy(Context.getAuthenticatedUser());
        appointmentUnavailability.setDateVoided(new Date());
        appointmentUnavailabilityDao.save(appointmentUnavailability);
    }
}
