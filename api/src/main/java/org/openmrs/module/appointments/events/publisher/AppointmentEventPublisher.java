package org.openmrs.module.appointments.events.publisher;

import org.openmrs.module.appointments.events.AppointmentEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

@Component
public class AppointmentEventPublisher implements ApplicationEventPublisherAware {
    private ApplicationEventPublisher eventPublisher;
    public void setApplicationEventPublisher(@NonNull ApplicationEventPublisher applicationEventPublisher) {
        this.eventPublisher = applicationEventPublisher;
    }

    public void publishEvent(AppointmentEvent event) {
        this.eventPublisher.publishEvent(event);
    }
}