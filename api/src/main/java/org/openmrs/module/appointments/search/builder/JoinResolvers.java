/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at https://www.bahmni.org/license/mplv2hd.
 *
 * Copyright (C) 2026 OpenMRS Inc.
 */

package org.openmrs.module.appointments.search.builder;

import jakarta.persistence.criteria.Fetch;
import jakarta.persistence.criteria.From;
import jakarta.persistence.criteria.JoinType;

/**
 * Common helpers used by module-specific {@code JoinResolver}s.
 * <p>
 * Copied from org.bahmni.module:search-commons:2.0.0 (org.bahmni.search.builder), whose release is compiled against
 * javax.persistence and therefore cannot be used with the jakarta.persistence criteria API of OpenMRS Platform 3.0.
 * Remove this copy once search-commons publishes a jakarta.persistence release.
 */
public final class JoinResolvers {

    @SuppressWarnings("unchecked")
    public static From<?, ?> findExistingFetchOrJoin(From<?, ?> parent, String attributeName, JoinType joinType) {
        for (Fetch<?, ?> fetch : parent.getFetches()) {
            if (attributeName.equals(fetch.getAttribute().getName())) {
                return (From<?, ?>) fetch;
            }
        }
        return parent.join(attributeName, joinType);
    }
}
