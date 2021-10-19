package org.openmrs.module.appointments.web.controller;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.context.Context;
import org.openmrs.api.context.UserContext;
import org.openmrs.module.appointments.service.impl.TeleconsultationAppointmentService;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.UUID;

@Controller
@RequestMapping(value = "/rest/" + RestConstants.VERSION_1 + "/adhocTeleconsultation")
public class AdhocTeleconsultationController {

    private Log log = LogFactory.getLog(this.getClass());

    private TeleconsultationAppointmentService teleconsultationAppointmentService;

    @Autowired
    public AdhocTeleconsultationController(TeleconsultationAppointmentService teleconsultationAppointmentService) {
        this.teleconsultationAppointmentService = teleconsultationAppointmentService;
    }

    @RequestMapping(value = "/generateAdhocTeleconsultationLink", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<String> generateAdhocTeleconsultationLink(@RequestParam(value = "patientUuid", required = true) String patientUuid, @RequestParam(value = "provider", required = true) String provider) {
        return new ResponseEntity<>(teleconsultationAppointmentService.generateAdhocTeleconsultationLink(UUID.randomUUID().toString(), patientUuid, provider), HttpStatus.OK);
    }
}
