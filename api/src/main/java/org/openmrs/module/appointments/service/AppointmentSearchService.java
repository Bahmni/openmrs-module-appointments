package org.openmrs.module.appointments.service;

import org.bahmni.search.model.ContextSearchResponse;
import org.bahmni.search.model.SearchRequest;
import org.openmrs.annotation.Authorized;
import org.openmrs.module.appointments.constants.PrivilegeConstants;

public interface AppointmentSearchService {

    @Authorized(value = {
            PrivilegeConstants.VIEW_APPOINTMENTS,
            PrivilegeConstants.MANAGE_APPOINTMENTS,
            PrivilegeConstants.MANAGE_OWN_APPOINTMENTS
    }, requireAll = true)
    ContextSearchResponse search(SearchRequest request);
}
