package org.openmrs.module.appointments.events.publisher;

import org.openmrs.module.appointments.events.AppointmentEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.stereotype.Component;

@Component
public class AppointmentEventPublisher implements ApplicationEventPublisherAware {
    private ApplicationEventPublisher eventPublisher;

    @Override
    public void setApplicationEventPublisher(ApplicationEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    public void publishEvent(AppointmentEvent event) {
        this.eventPublisher.publishEvent(event);
    }

    public void publish(Object event) {
        this.eventPublisher.publishEvent(event);
    }
}