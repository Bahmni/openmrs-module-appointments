package org.openmrs.module.appointments.service.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Patient;
import org.openmrs.PatientIdentifier;
import org.openmrs.api.PatientService;
import org.openmrs.api.context.Context;
import org.openmrs.module.appointments.model.AdhocTeleconsultationResponse;
import org.openmrs.module.appointments.notification.NotificationResult;

import java.text.MessageFormat;
import java.util.List;
import java.util.Random;

public class TeleconsultationAppointmentService {

    private final static String PROP_TC_SERVER = "bahmni.appointment.teleConsultation.serverUrlPattern";
    private final static String ADHOC_TC_ID = "bahmni.adhoc.teleConsultation.id";
    private final static String DEFAULT_TC_SERVER_URL_PATTERN = "https://meet.jit.si/{0}";
    private Log log = LogFactory.getLog(this.getClass());

    private PatientService patientService;
    private PatientAppointmentNotifierService patientAppointmentNotifierService;

    public void setPatientService(PatientService patientService) {
        this.patientService = patientService;
    }

    public void setPatientAppointmentNotifierService(PatientAppointmentNotifierService patientAppointmentNotifierService) {
        this.patientAppointmentNotifierService = patientAppointmentNotifierService;
    }

    public String generateTeleconsultationLink(String uuid) {
        String tcServerUrl = Context.getAdministrationService().getGlobalProperty(PROP_TC_SERVER);
        if ((tcServerUrl == null) || "".equals(tcServerUrl)) {
            tcServerUrl = DEFAULT_TC_SERVER_URL_PATTERN;
        }
        return new MessageFormat(tcServerUrl).format(new Object[] {uuid} );
    }

    public AdhocTeleconsultationResponse generateAdhocTeleconsultationLink(String patientUuid, String provider) {
        String identifierType = Context.getAdministrationService().getGlobalProperty(ADHOC_TC_ID);
        Patient patient = patientService.getPatientByUuid(patientUuid);
        PatientIdentifier identifier = patient.getIdentifiers().stream().filter(pi ->
                        identifierType.equals(pi.getIdentifierType().getName()))
                .findAny()
                .orElse(null);
        String teleConsultationId = (identifier != null) ? identifier.getIdentifier() : generateRandomID();
        String link = generateTeleconsultationLink(teleConsultationId);
        AdhocTeleconsultationResponse response = new AdhocTeleconsultationResponse();
        response.setUuid(teleConsultationId);
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

    private void notifyUpdates(AdhocTeleconsultationResponse response, Patient patient, String provider, String link) {
        List<NotificationResult> notificationResults = patientAppointmentNotifierService.notifyAll(patient, provider, link);
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
