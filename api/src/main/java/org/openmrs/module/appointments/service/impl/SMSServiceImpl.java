package org.openmrs.module.appointments.service.impl;


import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.openmrs.api.context.Context;
import org.openmrs.module.appointments.model.SMSRequest;
import org.openmrs.module.appointments.service.SMSService;
import org.springframework.stereotype.Service;

import java.text.MessageFormat;
import java.util.Date;
import java.util.Locale;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class SMSServiceImpl implements SMSService {
    private final static String APPOINMTMENT_REMINDER_SMS_TEMPLATE = "bahmni.appointmentReminderSMSTemplate";
    private final static String SMS_TIMEZONE = "bahmni.sms.timezone";
    private final static String SMS_DATEFORMAT = "bahmni.sms.dateformat";
    private final static String SMS_URL = "bahmni.sms.url";
    private static Logger logger = LogManager.getLogger(SMSServiceImpl.class);

    public SMSServiceImpl() {
    }

    @Override
    public String getAppointmentMessage(String name,String familyName ,int id, Date appointmentDate, long time, String service, String provider, String helpDeskPhone) {
        String smsTemplate = Context.getAdministrationService().getGlobalProperty(APPOINMTMENT_REMINDER_SMS_TEMPLATE);
        Object[] arguments = {name + " " + familyName,id,appointmentDate,time,service,provider,helpDeskPhone};
        if (StringUtils.isBlank(smsTemplate)) {
            return Context.getMessageSourceService().getMessage(APPOINMTMENT_REMINDER_SMS_TEMPLATE, arguments, new Locale("en")).replace("\\n", System.lineSeparator());
        } else {
            return new MessageFormat(smsTemplate).format(arguments).replace("\\n", System.lineSeparator());
        }
    }

    @Override
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