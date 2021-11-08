package org.openmrs.module.appointments.notification.impl;

import com.squareup.okhttp.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.ObjectWriter;
import org.openmrs.api.AdministrationService;
import org.openmrs.module.appointments.notification.SmsMessage;
import org.openmrs.module.appointments.notification.SmsMessageCollection;
import org.openmrs.module.appointments.notification.SmsSender;

import javax.mail.Session;
import java.util.ArrayList;
import java.util.List;

public class DefaultSmsSender implements SmsSender {

    private static final String SMS_PROPERTIES_FILENAME = "sms-config.properties";
    private Log log = LogFactory.getLog(this.getClass());
    private volatile Session session = null;

    private AdministrationService administrationService;

    public DefaultSmsSender(AdministrationService administrationService) {
        this.administrationService = administrationService;
    }

    @Override
    public void send(String message, String[] to) {
        OkHttpClient client = new OkHttpClient();

        MediaType mediaType = MediaType.parse("application/json");
        List<SmsMessage> messages = new ArrayList<>();

        for (int i=0; i<to.length; i++) {
            SmsMessage smsMessage = new SmsMessage();
            smsMessage.setBody(message);
            smsMessage.setFrom("Test");
//            smsMessage.setFrom(this.administrationService.getGlobalProperty("sms.from", ""));
            smsMessage.setTo(to[i]);
            messages.add(smsMessage);
        }
        SmsMessageCollection requestBody = new SmsMessageCollection();
        requestBody.setMessages(messages);

        ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
        String json = null;
        try {
            json = ow.writeValueAsString(requestBody);
        } catch (Exception e) {
            log.error("Error occurred while parsing sms request body", e);
            throw new RuntimeException("Error occurred while parsing sms request body", e);
        }


        RequestBody body = RequestBody.create(mediaType, json);
        Request request = new Request.Builder()
                .url("https://clicksend.p.rapidapi.com/sms/send")
                .post(body)
                .addHeader("content-type", "application/json")
                .addHeader("authorization", "Basic dmlzaGFsQGJlZWh5di5jb206QmVlaHl2QDEyMw==")
                .addHeader("x-rapidapi-host", "clicksend.p.rapidapi.com")
                .addHeader("x-rapidapi-key", "4e7aae4376mshbe508e06ca2e243p1d5560jsn6d3ec60bbd3e")
                .build();

//        Request request = new Request.Builder()
//                .url("https://clicksend.p.rapidapi.com/sms/send")
//                .post(body)
//                .addHeader("content-type", "application/json")
//                .addHeader("authorization", this.administrationService.getGlobalProperty("sms.auth", ""))
//                .addHeader("x-rapidapi-host", this.administrationService.getGlobalProperty("sms.host", "clicksend.p.rapidapi.com"))
//                .addHeader("x-rapidapi-key", this.administrationService.getGlobalProperty("sms.key", ""))
//                .build();

        try {
            Response response = client.newCall(request).execute();
        } catch (Exception e) {
            log.error("Error occurred while sending sms", e);
            throw new RuntimeException("Error occurred while sending sms", e);
        }
    }
}
