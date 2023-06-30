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
import org.openmrs.Location;
import org.openmrs.LocationTag;
import org.openmrs.Patient;
import org.openmrs.api.context.Context;
import org.openmrs.module.appointments.connection.OpenmrsLogin;
import org.openmrs.module.appointments.model.Appointment;
import org.openmrs.module.appointments.model.AppointmentProvider;
import org.openmrs.module.appointments.model.SMSRequest;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.openmrs.module.appointments.properties.AppointmentProperties;

import static org.openmrs.module.appointments.util.DateUtil.convertUTCToGivenFormat;

public class SMSService {

    public final static String HELPDESK_TEMPLATE = "sms.helpdeskTemplate";
    private final static String APPOINTMENT_REMINDER_SMS_TEMPLATE = "sms.appointmentReminderSMSTemplate";
    private final static String APPOINTMENT_BOOKING_SMS_TEMPLATE = "sms.appointmentBookingSMSTemplate";
    private final static String APPOINTMENT_PROVIDER_TEMPLATE = "sms.providersTemplate";
    private final static String APPOINTMENT_TELECONSULTATION_LINK_TEMPLATE = "sms.teleconsultationLinkTemplate";
    private final static String RECURRING_APPOINTMENT_BOOKING_SMS_TEMPLATE= "sms.recurringAppointmentTemplate";
    private final static String SMS_TIMEZONE = "bahmni.sms.timezone";
    private final static String SMS_DATEFORMAT = "bahmni.sms.dateformat";
    private final static String SMS_URL = "bahmni.sms.url";
    private static Logger logger = LogManager.getLogger(SMSService.class);
    private OpenmrsLogin openmrsLogin;
    private AppointmentVisitLocation appointmentVisitLocation;

    public void setOpenmrsLogin(OpenmrsLogin openmrsLogin) {
        this.openmrsLogin = openmrsLogin;
    }

    public void setAppointmentVisitLocation(AppointmentVisitLocation appointmentVisitLocation) {
        this.appointmentVisitLocation = appointmentVisitLocation;
    }

    public String generateMessage(Appointment appointment, String smsTemplate) {
        Map<String, Object> arguments;
        if (appointment.isRecurring()){
            arguments = createArgumentsForRecurringAppointment(appointment);
        }
        else
            arguments=createArguments(appointment);
        List<AppointmentProvider> appointmentProviderList = new ArrayList<>();
        if (appointment.getProviders() != null) {
            appointmentProviderList.addAll(appointment.getProviders());
        }
        List<String> providers = new ArrayList<>();

        for (AppointmentProvider appointmentProvider : appointmentProviderList) {
            providers.add(appointmentProvider.getProvider().getName());
        }

        String smsTemplateMessage = templateMessage(smsTemplate, arguments);
        if (!providers.isEmpty()) {
            arguments.put("providername", org.springframework.util.StringUtils.collectionToCommaDelimitedString(providers));
            smsTemplateMessage += templateMessage(APPOINTMENT_PROVIDER_TEMPLATE, arguments);
        }
        if (appointment.getAppointmentKind().getValue().equals("Virtual")) {
            smsTemplateMessage += templateMessage(APPOINTMENT_TELECONSULTATION_LINK_TEMPLATE, arguments);
        }
        String helpdeskTemplate = templateMessage(HELPDESK_TEMPLATE, arguments);
        return smsTemplateMessage + helpdeskTemplate;
    }


    public String templateMessage(String smsTemplate, Map<String, Object> arguments) {
        String template = Context.getAdministrationService().getGlobalProperty(smsTemplate);
        String formattedMessage = StringUtils.isBlank(template) ? Context.getMessageSourceService().getMessage(smsTemplate, null, new Locale("en")) : template;

        Pattern pattern = Pattern.compile("\\{([^}]*)\\}");
        Matcher matcher = pattern.matcher(formattedMessage);
        while (matcher.find()) {
            String placeholder = matcher.group(1);
            String modifiedPlaceholder = placeholder.toLowerCase().replaceAll("\\s", "");
            Object value = arguments.get(modifiedPlaceholder);
            placeholder= String.format("{%s}", placeholder);
            formattedMessage = formattedMessage.replace(placeholder, String.valueOf(value));
            }
        return formattedMessage.replace("\\n", System.lineSeparator());
    }

    public String getAppointmentReminderMessage(Appointment appointment) {
        return generateMessage(appointment, APPOINTMENT_REMINDER_SMS_TEMPLATE);
    }

    public String getAppointmentBookingMessage(Appointment appointment) {
        String template = appointment.isRecurring() ? RECURRING_APPOINTMENT_BOOKING_SMS_TEMPLATE : APPOINTMENT_BOOKING_SMS_TEMPLATE;
        return generateMessage(appointment, template);
    }

    public Map<String, Object> createArgumentsForRecurringAppointment(Appointment appointment) {
        Map<String, Object> arguments = createArguments(appointment);
        arguments.put("recurringperiod",appointment.getAppointmentRecurringPattern().getPeriod());
        arguments.put("recurringtype",appointment.getAppointmentRecurringPattern().getType());
        arguments.put("recurringdays",appointment.getAppointmentRecurringPattern().getDaysOfWeek());
        arguments.put("recurringfrequency",appointment.getAppointmentRecurringPattern().getFrequency());

        return arguments;
    }
    public Map<String, Object> createArguments(Appointment appointment) {

        Map<String, Object> arguments = new HashMap<>();
        Patient patient = appointment.getPatient();
        String givenName = patient.getGivenName();
        String familyName = patient.getFamilyName();
        String identifier = patient.getPatientIdentifier().getIdentifier();
        Date appointmentDate = appointment.getStartDateTime();
        String service = appointment.getService().getName();
        String teleLink=appointment.getTeleHealthVideoLink();
        String smsTimeZone = Context.getMessageSourceService().getMessage(SMS_TIMEZONE, null, new Locale("en"));
        String smsDateFormat = Context.getMessageSourceService().getMessage(SMS_DATEFORMAT, null, new Locale("en"));
        String date = convertUTCToGivenFormat(appointmentDate, smsDateFormat, smsTimeZone);
        String helpdeskNumber = Context.getAdministrationService().getGlobalPropertyObject("clinic.helpDeskNumber").getPropertyValue();
        String clinicName = getClinicName(appointment);

        arguments.put("patientname", givenName + " " + familyName);
        arguments.put("identifier", identifier);
        arguments.put("date", date);
        arguments.put("service", service);
        arguments.put("clinicname", clinicName);
        arguments.put("teleconsultationlink", teleLink);
        arguments.put("helpdesknumber", helpdeskNumber);

        return arguments;
    }

    public String getClinicName(Appointment appointment) {
        Location location = appointment.getLocation();
        LocationTag visitLocationTag = Context.getLocationService().getLocationTagByName("Visit Location");
        List<Location> locations = Context.getLocationService().getLocationsHavingAnyTag(Collections.singletonList(visitLocationTag));
        String clinicName = (visitLocationTag != null && !locations.isEmpty()) ? locations.get(0).getName() : "xxxxx";

        if (location != null) {
            String clinicNameFromVisitLocation = appointmentVisitLocation.getClinicName(location.getUuid());

            if (StringUtils.isNotEmpty(clinicNameFromVisitLocation)) {
                clinicName = clinicNameFromVisitLocation;
            }
        }
        return clinicName;
    }


    public Object sendSMS(String phoneNumber, String message) {
        try {
            SMSRequest smsRequest = new SMSRequest();
            smsRequest.setPhoneNumber(phoneNumber);
            smsRequest.setMessage(message);

            ObjectMapper Obj = new ObjectMapper();
            String jsonObject = Obj.writeValueAsString(smsRequest);
            StringEntity params = new StringEntity(jsonObject);
            String smsUrl = StringUtils.isBlank(AppointmentProperties.getProperty("sms.uri")) ? SMS_URL : AppointmentProperties.getProperty("sms.uri");
            HttpPost request = new HttpPost(Context.getMessageSourceService().getMessage(smsUrl, null, new Locale("en")));
            request.addHeader("content-type", "application/json");
            request.setEntity(params);
            openmrsLogin.getConnection();
            ClientCookies clientCookies = openmrsLogin.getCookies();
            request.setHeader("Cookie", "reporting_session=" + clientCookies.entrySet().iterator().next().getValue());

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