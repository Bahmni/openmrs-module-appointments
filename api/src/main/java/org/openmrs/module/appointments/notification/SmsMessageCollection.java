package org.openmrs.module.appointments.notification;

import java.util.List;

public class SmsMessageCollection {
    private List<SmsMessage> messages;

    public List<SmsMessage> getMessages() {
        return messages;
    }

    public void setMessages(List<SmsMessage> messages) {
        this.messages = messages;
    }
}
