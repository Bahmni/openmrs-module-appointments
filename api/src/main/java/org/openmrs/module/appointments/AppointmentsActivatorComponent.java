package org.openmrs.module.appointments;

/**
 * Components implementing this interface will be retrieved from the module activator during startup and shutdown
 * and the appropriate methods will be called
 */
public interface AppointmentsActivatorComponent {

    default void started() {}

    default void willStop() {}

}
