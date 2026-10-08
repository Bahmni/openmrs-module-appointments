/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at https://www.bahmni.org/license/mplv2hd.
 *
 * Copyright (C) 2026 OpenMRS Inc.
 */

package org.openmrs.module.appointments.search.builder;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.From;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Copied from org.bahmni.module:search-commons:2.0.0 (org.bahmni.search.builder), whose release is compiled against
 * javax.persistence and therefore cannot be used with the jakarta.persistence criteria API of OpenMRS Platform 3.0.
 * Remove this copy once search-commons publishes a jakarta.persistence release.
 */
public class QueryContext<T> {

    public final CriteriaBuilder criteriaBuilder;
    public final Root<T> root;
    public final List<Predicate> predicates;
    public final Map<String, From<?, ?>> joinCache = new HashMap<>();

    public QueryContext(CriteriaBuilder criteriaBuilder, Root<T> root, List<Predicate> predicates) {
        this.criteriaBuilder = criteriaBuilder;
        this.root = root;
        this.predicates = predicates;
    }
}
