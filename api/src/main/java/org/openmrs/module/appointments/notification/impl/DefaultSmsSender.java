package org.openmrs.module.appointments.notification.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.AdministrationService;
import org.openmrs.module.appointments.notification.SmsSender;

import java.util.List;

public class DefaultSmsSender implements SmsSender {

    private Log log = LogFactory.getLog(this.getClass());

    private AdministrationService administrationService;

    public DefaultSmsSender(AdministrationService administrationService) {
        this.administrationService = administrationService;
    }

    @Override
    public void send(String message, List<String> to) {
        //Write your implementation based on your SMS API
    }
}
