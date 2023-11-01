package org.openmrs.module.appointments.events.eventListener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.bahmni.module.communication.service.CommunicationService;
import org.bahmni.module.communication.service.MessageBuilderService;
import org.openmrs.Patient;
import org.openmrs.PersonAttribute;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.context.Context;
import org.openmrs.module.appointments.events.AppointmentEventType;
import org.openmrs.module.appointments.service.AppointmentArgumentsMapper;
import org.openmrs.module.appointments.model.Appointment;
import org.openmrs.module.appointments.events.AppointmentBookingEvent;
import org.openmrs.module.appointments.events.RecurringAppointmentEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.List;


@Component
public class AppointmentSMSEventListener {

    private final Log log = LogFactory.getLog(this.getClass());

    @Autowired
    AppointmentArgumentsMapper appointmentArgumentsMapper;

    @Async("AppointmentsAsyncThreadExecutor")
    @EventListener
    public void onApplicationEvent(AppointmentBookingEvent event) {
        try {
            Context.openSession();
            Context.setUserContext(event.userContext);
            if (event.eventType == AppointmentEventType.BAHMNI_APPOINTMENT_CREATED) {
                handleAppointmentCreatedEvent(event.getAppointment());
            }
        } catch (Exception e) {
            log.error("Exception occurred during event processing", e);
        } finally {
            Context.closeSession();
        }
    }

    @Async("AppointmentsAsyncThreadExecutor")
    @EventListener
    public void onApplicationEvent(RecurringAppointmentEvent event) {
        try {
            Context.openSession();
            Context.setUserContext(event.userContext);
            if (event.eventType == AppointmentEventType.BAHMNI_RECURRING_APPOINTMENT_CREATED) {
                handleRecurringAppointmentCreatedEvent(event.getAppointmentRecurringPattern().getAppointments().iterator().next());
            }
        } catch (Exception e) {
            log.error("Exception occurred during event processing", e);
        } finally {
            Context.closeSession();
        }
    }

    private void handleAppointmentCreatedEvent(Appointment appointment) {
        String phoneNumber=getPhoneNumber(appointment.getPatient());
        if (!shouldSendEmailForBookingAppointment()||phoneNumber==null)return;
        MessageBuilderService messageBuilderService = Context.getRegisteredComponent("messageBuilderService", MessageBuilderService.class);
        CommunicationService communicationService = Context.getRegisteredComponent("communicationService", CommunicationService.class);
        List<String> providersName = appointmentArgumentsMapper.getProvidersNameInString(appointment);
        String message = messageBuilderService.getAppointmentBookingMessage(appointmentArgumentsMapper.createArgumentsMapForAppointmentBooking(appointment), providersName);
        communicationService.sendSMS(phoneNumber, message);
    }

    private void handleRecurringAppointmentCreatedEvent(Appointment appointment) {
        String phoneNumber=getPhoneNumber(appointment.getPatient());
        if (!shouldSendEmailForBookingAppointment()||phoneNumber==null)return;
        MessageBuilderService messageBuilderService = Context.getRegisteredComponent("messageBuilderService", MessageBuilderService.class);
        CommunicationService communicationService = Context.getRegisteredComponent("communicationService", CommunicationService.class);
        List<String> providersName = appointmentArgumentsMapper.getProvidersNameInString(appointment);
        String message = messageBuilderService.getRecurringAppointmentBookingMessage(appointmentArgumentsMapper.createArgumentsMapForRecurringAppointmentBooking(appointment), providersName);
        communicationService.sendSMS(phoneNumber, message);
    }
    private boolean shouldSendEmailForBookingAppointment() {
        AdministrationService administrationService = Context.getService(AdministrationService.class);
        return Boolean.parseBoolean(administrationService.getGlobalProperty("sms.enableAppointmentBookingSMSAlert", "false"));
    }
    private String getPhoneNumber(Patient patient) {
        PersonAttribute phoneNumber = patient.getAttribute("phoneNumber");
        if (phoneNumber == null) {
            log.info("No mobile number found for the patient. SMS not sent.");
            return null;
        }
        return phoneNumber.getValue();
    }
}


