package org.openmrs.module.appointments.search.validation;

import org.bahmni.search.validation.SearchCriteriaValidationUtils;
import org.openmrs.module.appointments.search.dto.AppointmentSearchRequest;

public class CriteriaValidator {

    public void validateEntity(String entity, String supportedEntity) {
        SearchCriteriaValidationUtils.validateEntity(entity, supportedEntity);
    }

    public void validateRequest(AppointmentSearchRequest request) {
        SearchCriteriaValidationUtils.validateCriteria(request.getCriteria());
        SearchCriteriaValidationUtils.validateMeta(request.getEntity(), request.getMeta());
    }
}
