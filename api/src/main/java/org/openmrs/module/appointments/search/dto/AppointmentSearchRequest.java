/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at https://www.bahmni.org/license/mplv2hd.
 *
 * Copyright (C) 2026 OpenMRS Inc.
 */

package org.openmrs.module.appointments.search.dto;

import org.bahmni.search.model.SearchCondition;


public class AppointmentSearchRequest {

    private String entity;
    private SearchCondition criteria;

    public String getEntity() {
        return entity;
    }

    public void setEntity(String entity) {
        this.entity = entity;
    }

    public SearchCondition getCriteria() {
        return criteria;
    }

    public void setCriteria(SearchCondition criteria) {
        this.criteria = criteria;
    }
}
