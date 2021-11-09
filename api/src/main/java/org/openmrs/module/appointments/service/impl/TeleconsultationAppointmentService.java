package org.openmrs.module.appointments.service.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Patient;
import org.openmrs.PatientIdentifier;
import org.openmrs.api.PatientService;
import org.openmrs.api.context.Context;
import org.openmrs.module.appointments.model.AdhocTeleconsultationResponse;
import org.openmrs.module.appointments.model.Appointment;
import org.openmrs.module.appointments.notification.NotificationResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.text.MessageFormat;
import java.util.List;
import java.util.Random;

@Component
public class TeleconsultationAppointmentService {

    private final static String PROP_TC_SERVER = "bahmni.appointment.teleConsultation.serverUrlPattern";
    private final static String ADHOC_TC_ID = "bahmni.adhoc.teleConsultation.id";
    private final static String DEFAULT_TC_SERVER_URL_PATTERN = "https://meet.jit.si/{0}";
    private Log log = LogFactory.getLog(this.getClass());

    @Autowired
    private PatientService patientService;

    @Autowired
    private PatientAppointmentNotifierService appointmentNotifierService;

    public String generateTeleconsultationLink(Appointment appointment) {
        String tcServerUrl = Context.getAdministrationService().getGlobalProperty(PROP_TC_SERVER);
        if ((tcServerUrl == null) || "".equals(tcServerUrl)) {
            tcServerUrl = DEFAULT_TC_SERVER_URL_PATTERN;
        }
        return new MessageFormat(tcServerUrl).format(new Object[] {appointment.getUuid()} );
    }

    public AdhocTeleconsultationResponse generateAdhocTeleconsultationLink(String patientUuid, String provider) {
        String identifierType = Context.getAdministrationService().getGlobalProperty(ADHOC_TC_ID);
        Patient patient = patientService.getPatientByUuid(patientUuid);
        PatientIdentifier identifier = patient.getIdentifiers().stream().filter(pi ->
                        identifierType.equals(pi.getIdentifierType().getName()))
                .findAny()
                .orElse(null);
        String id = generateRandomID();
        if (identifier != null) {
            id = identifier.getIdentifier();
        }
        String link = getAdhocTeleConsultationLink(id);
        AdhocTeleconsultationResponse response = new AdhocTeleconsultationResponse();
        response.setUuid(id);
        response.setLink(link);
        notifyUpdates(response, patient, provider, link);
        return response;
    }

    private String generateRandomID() {
        Random rnd = new Random();
        int number = rnd.nextInt(999999);
        String id = String.format("%06d", number) + System.currentTimeMillis();
        return id;
    }

    private String getAdhocTeleConsultationLink(String uuid) {
        String tcServerUrl = Context.getAdministrationService().getGlobalProperty(PROP_TC_SERVER);
        if ((tcServerUrl == null) || "".equals(tcServerUrl)) {
            tcServerUrl = DEFAULT_TC_SERVER_URL_PATTERN;
        }
        String link = new MessageFormat(tcServerUrl).format(new Object[] {uuid});
        return link;
    }

    private void notifyUpdates(AdhocTeleconsultationResponse response, Patient patient, String provider, String link) {
        List<NotificationResult> notificationResults = appointmentNotifierService.notifyAll(patient, provider, link);
        if (!notificationResults.isEmpty()) {
            notificationResults.stream().forEach(nr -> {
                String notificationMsg = String.format("Appointment Notification Result - medium: %s, uuid: %s, status: %d, message: %s",
                        nr.getMedium(), nr.getUuid(), nr.getStatus(), nr.getMessage());
                log.info(notificationMsg);
            });
            response.setNotificationResults(notificationResults);
        }
    }
}
