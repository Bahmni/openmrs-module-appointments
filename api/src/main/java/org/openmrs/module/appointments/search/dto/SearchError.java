/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at https://www.bahmni.org/license/mplv2hd.
 *
 * Copyright (C) 2026 OpenMRS Inc.
 */

package org.openmrs.module.appointments.search.dto;

import java.util.Collections;
import java.util.List;

public class SearchError {

    private final int status;
    private final List<String> messages;

    public SearchError(int status, List<String> messages) {
        this.status = status;
        this.messages = Collections.unmodifiableList(messages);
    }

    public SearchError(int status, String message) {
        this(status, Collections.singletonList(message));
    }

    public int getStatus() {
        return status;
    }

    public List<String> getMessages() {
        return messages;
    }
}
