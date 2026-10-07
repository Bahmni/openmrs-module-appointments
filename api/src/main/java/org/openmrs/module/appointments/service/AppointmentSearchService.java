package org.openmrs.module.appointments.service;

import org.openmrs.module.appointments.search.dto.AppointmentSearchResponse;
import org.openmrs.module.appointments.search.dto.AppointmentSearchRequest;
import org.openmrs.annotation.Authorized;
import org.openmrs.module.appointments.constants.PrivilegeConstants;

public interface AppointmentSearchService {

    @Authorized(value = {
            PrivilegeConstants.VIEW_APPOINTMENTS,
    }, requireAll = true)
    AppointmentSearchResponse search(AppointmentSearchRequest request);
}

