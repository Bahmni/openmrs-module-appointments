package org.openmrs.module.appointments.service.impl;


import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.bahmni.webclients.ClientCookies;
import org.openmrs.api.context.Context;
import org.openmrs.module.appointments.connection.OpenmrsLogin;
import org.openmrs.module.appointments.model.SMSRequest;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import com.fasterxml.jackson.databind.ObjectMapper;

import static org.openmrs.module.appointments.util.DateUtil.convertUTCToGivenFormat;

public class SMSService {

    private OpenmrsLogin openmrsLogin;
    private final static String APPOINMTMENT_REMINDER_SMS_TEMPLATE = "bahmni.appointmentReminderSMSTemplate";
    private final static String APPOINMTMENT_REMINDER_SMS_TEMPLATE_NO_PROVIDER = "bahmni.appointmentReminderSMSTemplateWithoutProvider";
    private final static String SMS_TIMEZONE = "bahmni.sms.timezone";
    private final static String SMS_DATEFORMAT = "bahmni.sms.dateformat";
    private final static String SMS_URL = "bahmni.sms.url";
    private static Logger logger = LogManager.getLogger(SMSService.class);

    public void setOpenmrsLogin(OpenmrsLogin openmrsLogin) {
        this.openmrsLogin = openmrsLogin;
    }

    public String getAppointmentMessage(String name, String familyName, int id, Date appointmentDate, String service, List<String> providers) {
        String smsTemplate = Context.getAdministrationService().getGlobalProperty(APPOINMTMENT_REMINDER_SMS_TEMPLATE);
        String smsTemplateNoProvider = Context.getAdministrationService().getGlobalProperty(APPOINMTMENT_REMINDER_SMS_TEMPLATE_NO_PROVIDER);
        String smsTimeZone = Context.getMessageSourceService().getMessage(SMS_TIMEZONE, null, new Locale("en"));
        String smsDateFormat = Context.getMessageSourceService().getMessage(SMS_DATEFORMAT, null, new Locale("en"));
        String date =convertUTCToGivenFormat(appointmentDate, smsDateFormat, smsTimeZone);
        String helpdeskNumber = Context.getAdministrationService().getGlobalPropertyObject("clinic.helpDeskNumber").getPropertyValue();
        String clinicName=Context.getAdministrationService().getGlobalPropertyObject("clinic.clinicName").getPropertyValue();

        List<Object> arguments = new ArrayList<>();
        arguments.add(name + " " + familyName);
        arguments.add(id);
        arguments.add(date);
        arguments.add(org.springframework.util.StringUtils.collectionToCommaDelimitedString(providers));
        arguments.add(service);
        arguments.add(clinicName);
        arguments.add(helpdeskNumber);

        String message;
        if (StringUtils.isBlank(smsTemplate)) {
            if (providers.isEmpty()) {
                arguments.remove(3);
                message = Context.getMessageSourceService().getMessage(APPOINMTMENT_REMINDER_SMS_TEMPLATE_NO_PROVIDER, arguments.toArray(), new Locale("en")).replace("\\n", System.lineSeparator());
            } else {
                message = Context.getMessageSourceService().getMessage(APPOINMTMENT_REMINDER_SMS_TEMPLATE, arguments.toArray(), new Locale("en")).replace("\\n", System.lineSeparator());
            }
        } else {
            if (providers.isEmpty()) {
                arguments.remove(3);
                message=new MessageFormat(smsTemplateNoProvider).format(arguments.toArray()).replace("\\n", System.lineSeparator());
            } else {
                message =new MessageFormat(smsTemplate).format(arguments.toArray()).replace("\\n", System.lineSeparator());
            }
        }
        return message;
    }

    public Object sendSMS(String phoneNumber, String message) {
        try {
            SMSRequest smsRequest = new SMSRequest();
            smsRequest.setPhoneNumber(phoneNumber);
            smsRequest.setMessage(message);

            ObjectMapper Obj = new ObjectMapper();
            String jsonObject = Obj.writeValueAsString(smsRequest);
            StringEntity params = new StringEntity(jsonObject);

            HttpPost request = new HttpPost(Context.getMessageSourceService().getMessage(SMS_URL, null, new Locale("en")));
            request.addHeader("content-type", "application/json");
            request.setEntity(params);
            openmrsLogin.getConnection();
            ClientCookies clientCookies = openmrsLogin.getCookies();
            request.setHeader("Cookie","reporting_session=" +clientCookies.entrySet().iterator().next().getValue());

            CloseableHttpClient httpClient = HttpClients.createDefault();
            HttpResponse response = httpClient.execute(request);
            httpClient.close();
            return response.getStatusLine();
        } catch (Exception e) {
            logger.error("Exception occured in sending sms ", e);
            throw new RuntimeException("Exception occured in sending sms ", e);
        }
    }
}