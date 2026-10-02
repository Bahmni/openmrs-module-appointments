/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at https://www.bahmni.org/license/mplv2hd.
 *
 * Copyright (C) 2026 OpenMRS Inc.
 */

package org.openmrs.module.appointments.search.builder;

import org.bahmni.search.model.ConditionOperator;
import org.bahmni.search.model.FieldComparator;

import jakarta.persistence.criteria.Predicate;

/**
 * Copied from org.bahmni.module:search-commons:2.0.0 (org.bahmni.search.builder), whose release is compiled against
 * javax.persistence and therefore cannot be used with the jakarta.persistence criteria API of OpenMRS Platform 3.0.
 * Remove this copy once search-commons publishes a jakarta.persistence release.
 */
@FunctionalInterface
public interface SearchFieldPredicate {

    Predicate build(QueryContext<?> queryContext,
                    String fieldName,
                    FieldComparator comparator,
                    String value,
                    ConditionOperator operator);
}
