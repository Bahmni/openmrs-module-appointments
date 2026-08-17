/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at https://www.bahmni.org/license/mplv2hd.
 *
 * Copyright (C) 2026 OpenMRS Inc.
 */

package org.openmrs.module.appointments.search.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.bahmni.search.model.PaginationResponse;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class SearchResponseMeta {

    private final long timestamp;
    private final Long totalCount;
    private final PaginationResponse pagination;

    public SearchResponseMeta() {
        this.timestamp = System.currentTimeMillis();
        this.totalCount = null;
        this.pagination = new PaginationResponse(null, null);
    }

    public SearchResponseMeta(PaginationResponse pagination, Long totalCount) {
        this.timestamp = System.currentTimeMillis();
        this.pagination = pagination;
        this.totalCount = totalCount;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public Long getTotalCount() {
        return totalCount;
    }

    public PaginationResponse getPagination() {
        return pagination;
    }
}
