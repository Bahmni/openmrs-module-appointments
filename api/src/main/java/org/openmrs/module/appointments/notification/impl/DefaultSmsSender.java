package org.openmrs.module.appointments.notification.impl;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.AdministrationService;
import org.openmrs.module.appointments.notification.SmsSender;
import org.openmrs.util.OpenmrsUtil;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;

public class DefaultSmsSender implements SmsSender {

    private Log log = LogFactory.getLog(this.getClass());

    private static final String SMS_PROPERTIES_FILENAME = "sms-config.properties";
    private static Properties smsSessionProperties = null;

    SimpleDateFormat fmtTime = new SimpleDateFormat("HH:mm a");
    SimpleDateFormat fmtDateTele = new SimpleDateFormat("dd/MM/yyyy");

    private AdministrationService administrationService;

    public DefaultSmsSender(AdministrationService administrationService) {
        this.administrationService = administrationService;
    }

    @Override
    public void send(String recipientMobileNumber, String patientName, String smsType, String serviceName, Date startDateTime, String teleLink) {
        HttpURLConnection uc = null;
        try {
        	getSession();

        	String password = "";
            String addOnParams = "";
            String entityId = "";
            String templateId = "";
            String msgText = "";

            recipientMobileNumber = recipientMobileNumber.replace("+", "");
            int recipientPhoneLengthRequired = Integer.parseInt(smsSessionProperties.getProperty("sms.phonenumber.length"));
            if (recipientMobileNumber.length()>recipientPhoneLengthRequired) {
            	int indexStart = recipientMobileNumber.length() - recipientPhoneLengthRequired;
            	recipientMobileNumber = recipientMobileNumber.substring(indexStart);
            }
            String dateSuffixText = getDateWithSuffix(startDateTime.getDate());
            String dateApptPattern = String.format("MMMM '%s', yyyy", dateSuffixText);
            SimpleDateFormat fmtDateAppt = new SimpleDateFormat(dateApptPattern);

            String requestUrl = smsSessionProperties.getProperty("sms.url");
            String apiKey = smsSessionProperties.getProperty("sms.key.username") + "=" + URLEncoder.encode(smsSessionProperties.getProperty("sms.username"), "UTF-8");
            String sender = "&" + smsSessionProperties.getProperty("sms.key.sender") + "=" + URLEncoder.encode(smsSessionProperties.getProperty("sms.sender"), "UTF-8");
            String number = "&" + smsSessionProperties.getProperty("sms.key.phonenumber") + "=" + URLEncoder.encode(recipientMobileNumber, "UTF-8");

            if (StringUtils.isNotBlank(smsSessionProperties.getProperty("sms.key.password"))) {
                password = "&" + smsSessionProperties.getProperty("sms.key.password") + "=" + URLEncoder.encode(smsSessionProperties.getProperty("sms.password") , "UTF-8");
            }
            if (StringUtils.isNotBlank(smsSessionProperties.getProperty("sms.key.entity.identifier"))) {
                entityId = "&" + smsSessionProperties.getProperty("sms.key.entity.identifier") + "=" + URLEncoder.encode(smsSessionProperties.getProperty("sms.entity.identifier"), "UTF-8");
            }
            if (StringUtils.isNotBlank(smsSessionProperties.getProperty("sms.key.template.identifier")) && "Virtual".equalsIgnoreCase(smsType)) {
                templateId = "&" + smsSessionProperties.getProperty("sms.key.template.identifier") + "=" + 
                    URLEncoder.encode(smsSessionProperties.getProperty("sms.appointment.teleconsultation.template.identifier"), "UTF-8");
                // Dear %s,\nTele-Consultation booked for %s %s\nVideo link: %s Bahmni Hospital(IPLit)
                msgText = String.format(smsSessionProperties.getProperty("sms.appointment.teleconsultation.message.template"), patientName,
                    fmtDateTele.format(startDateTime), fmtTime.format(startDateTime), teleLink);
            }
            if (StringUtils.isNotBlank(smsSessionProperties.getProperty("sms.key.template.identifier")) && "WalkIn".equalsIgnoreCase(smsType)) {
                templateId = "&" + smsSessionProperties.getProperty("sms.key.template.identifier") + "=" + 
                    URLEncoder.encode(smsSessionProperties.getProperty("sms.appointment.walkin.template.identifier"), "UTF-8");
                // Dear %s,\n Your appointment is booked for %s on %s at %s by Bahmni Hospital (powered by IPLit)
                msgText = String.format(smsSessionProperties.getProperty("sms.appointment.walkin.message.template"), patientName,
                    serviceName, fmtDateAppt.format(startDateTime), fmtTime.format(startDateTime));
            }
            if (StringUtils.isNotBlank(smsSessionProperties.getProperty("sms.params.addons"))) {
                addOnParams = "&" + smsSessionProperties.getProperty("sms.params.addons");
            }
            String message = "&" + smsSessionProperties.getProperty("sms.key.message") + "=" + URLEncoder.encode(msgText, "UTF-8");
            requestUrl += apiKey + message + sender + number + password + entityId + templateId + addOnParams;

            URL url = new URL(requestUrl);
            uc = (HttpURLConnection)url.openConnection();
            String smsResponse = uc.getResponseMessage();
        } catch (Exception e) {
            throw new RuntimeException("Error occurred while sending sms", e);
        } finally {
            if (uc!=null) {
                uc.disconnect();
                uc = null;
            }
        }
    }

    private Properties getSession() {
        if (smsSessionProperties == null) {
            Properties sessionProperties = smsSessionPropertiesFromPath();
            if (sessionProperties == null) {
                log.warn("Could not load sms properties from application data directory file. Loading from OMRS settings.");
                sessionProperties = smsSessionPropertiesFromOMRS();
            }
            smsSessionProperties = sessionProperties;
        }
        return smsSessionProperties;
    }

    /**
     * To be used as fallback. SMS properties are visible in openmrs settings.
     * @param as
     * @return
     */
    private Properties smsSessionPropertiesFromOMRS() {
        Properties p = new Properties();
        p.put("sms.key.username", administrationService.getGlobalProperty("sms.key.username", ""));
        p.put("sms.key.password", administrationService.getGlobalProperty("sms.key.password", ""));
        p.put("sms.key.phonenumber", administrationService.getGlobalProperty("sms.key.phonenumber", ""));
        p.put("sms.key.message", administrationService.getGlobalProperty("sms.key.message", ""));
        p.put("sms.key.sender", administrationService.getGlobalProperty("sms.key.sender", ""));
        p.put("sms.key.entity.identifier", administrationService.getGlobalProperty("sms.key.entity.identifier", ""));
        p.put("sms.key.template.identifier", administrationService.getGlobalProperty("sms.key.template.identifier", ""));

        p.put("sms.url", administrationService.getGlobalProperty("sms.url", ""));
        p.put("sms.username", administrationService.getGlobalProperty("sms.username", ""));
        p.put("sms.password", administrationService.getGlobalProperty("sms.password", ""));
        p.put("sms.sender", administrationService.getGlobalProperty("sms.sender", ""));
        p.put("sms.entity.identifier", administrationService.getGlobalProperty("sms.entity.identifier", ""));
        p.put("sms.appointment.walkin.template.identifier", administrationService.getGlobalProperty("sms.appointment.walkin.template.identifier", ""));
        p.put("sms.appointment.teleconsultation.template.identifier", administrationService.getGlobalProperty("sms.appointment.teleconsultation.template.identifier", ""));
        p.put("sms.appointment.walkin.message.template", administrationService.getGlobalProperty("sms.appointment.walkin.message.template", ""));
        p.put("sms.appointment.teleconsultation.message.template", administrationService.getGlobalProperty("sms.appointment.teleconsultation.message.template", ""));
        p.put("sms.params.addons", administrationService.getGlobalProperty("sms.params.addons", ""));
        return p;
    }

    private Properties smsSessionPropertiesFromPath() {
        Path propertyFilePath = Paths.get(OpenmrsUtil.getApplicationDataDirectory(), SMS_PROPERTIES_FILENAME);
        if (Files.exists(propertyFilePath)) {
            Properties properties = new Properties();
            try {
                log.info("Reading properties from: " + propertyFilePath);
                properties.load(Files.newInputStream(propertyFilePath));
                return properties;
            } catch (IOException e) {
                log.error("Could not load sms properties from: " + propertyFilePath, e);
            }
        } else {
            log.warn("No sms configuration defined at " + propertyFilePath);
        }
        return null;
    }

    private String getDateWithSuffix(int date) {
        switch (date) {
        case 1:
        case 21:
        case 31:
            return "" + date + "st";

        case 2:
        case 22:
            return "" + date + "nd";

        case 3:
        case 23:
            return "" + date + "rd";

        default:
            return "" + date + "th";
        }
    }

}
