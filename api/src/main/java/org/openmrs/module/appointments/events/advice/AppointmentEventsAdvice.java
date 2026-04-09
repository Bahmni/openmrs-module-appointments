package org.openmrs.module.appointments.events.advice;

import com.google.common.collect.Sets;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bahmni.module.eventoutbox.EMREvent;
import org.openmrs.api.context.Context;
import org.openmrs.module.appointments.events.publisher.AppointmentEventPublisher;
import org.openmrs.module.appointments.model.Appointment;
import org.openmrs.module.appointments.events.AppointmentBookingEvent;
import org.openmrs.module.appointments.events.AppointmentEventType;
import org.springframework.aop.AfterReturningAdvice;
import org.springframework.aop.MethodBeforeAdvice;
import org.springframework.lang.Nullable;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import static org.openmrs.module.appointments.constants.AppointmentsEventRecordsConstants.CATEGORY;
import static org.openmrs.module.appointments.constants.AppointmentsEventRecordsConstants.RAISE_EVENT_GLOBAL_PROPERTY;
import static org.openmrs.module.appointments.constants.AppointmentsEventRecordsConstants.URL_PATTERN_GLOBAL_PROPERTY;
import static org.openmrs.module.appointments.constants.AppointmentsEventRecordsConstants.DEFAULT_URL_PATTERN;
import static org.openmrs.module.appointments.events.AppointmentEventType.BAHMNI_APPOINTMENT_CREATED;
import static org.openmrs.module.appointments.events.AppointmentEventType.BAHMNI_APPOINTMENT_UPDATED;

public class AppointmentEventsAdvice implements AfterReturningAdvice, MethodBeforeAdvice {

	private final Logger log = LogManager.getLogger(AppointmentEventsAdvice.class);
	private final AppointmentEventPublisher eventPublisher;
	private final ThreadLocal<Map<String,Integer>> threadLocal = new ThreadLocal<>();
	private final String APPOINTMENT_ID_KEY = "appointmentId";
	private final Set<String> adviceMethodNames = Sets.newHashSet("validateAndSave");
	private final Set<String> emrMethodNames = Sets.newHashSet("validateAndSave", "changeStatus", "undoStatusChange");
	private static final Set<String> VOID_RETURN_METHODS = Sets.newHashSet("changeStatus", "undoStatusChange");
	private static final String TITLE = "Appointment";

	public AppointmentEventsAdvice() {
		this.eventPublisher=Context.getRegisteredComponent("appointmentEventPublisher",AppointmentEventPublisher.class);
	}

	@Override
	public void afterReturning(Object returnValue, Method method, Object[] arguments, Object target) {
		// Existing block — publishes internal AppointmentBookingEvent (for SMS etc.) — DO NOT MODIFY
		if (adviceMethodNames.contains(method.getName())) {
			Map<String, Integer> patientInfo = threadLocal.get();
			if (patientInfo != null) {
				AppointmentEventType eventType = patientInfo.get(APPOINTMENT_ID_KEY) == null ? BAHMNI_APPOINTMENT_CREATED : BAHMNI_APPOINTMENT_UPDATED;
				threadLocal.remove();

				Appointment appointment = (Appointment) returnValue;
				AppointmentBookingEvent appointmentEvent =new AppointmentBookingEvent(eventType,appointment);
				eventPublisher.publishEvent(appointmentEvent);
				log.info("Successfully published event with uuid : {}" , appointmentEvent.payloadId);
			}
		}

		// EMR outbox block — publishes EMREvent for external consumers (replaces atom feed)
		if (emrMethodNames.contains(method.getName())) {
			boolean shouldPublish = Boolean.parseBoolean(
					Context.getAdministrationService().getGlobalProperty(RAISE_EVENT_GLOBAL_PROPERTY, "true"));
			if (shouldPublish) {
				Appointment appointment = VOID_RETURN_METHODS.contains(method.getName())
						? (Appointment) arguments[0]
						: (Appointment) returnValue;
				if (appointment != null) {
					String content = Context.getAdministrationService()
							.getGlobalProperty(URL_PATTERN_GLOBAL_PROPERTY, DEFAULT_URL_PATTERN)
							.replace("{uuid}", appointment.getUuid());
					EMREvent<Appointment> emrEvent = new EMREvent<>(appointment, CATEGORY, TITLE, null, content);
					eventPublisher.publishEMREvent(emrEvent);
					log.info("Successfully published EMR event for appointment uuid : {} " , appointment.getUuid());
				}
			}
		}
	}

	@Override
	public void before(Method method, Object[] objects, @Nullable Object o) {
		if (adviceMethodNames.contains(method.getName())) {
			Appointment appointment = null;

			Object firstArg = objects[0];
			if (firstArg instanceof Supplier<?>) {
				Object result = ((Supplier<?>) firstArg).get();
				if (result instanceof Appointment) {
					appointment = (Appointment) result;
				}
			} else if (firstArg instanceof Appointment) {
				appointment = (Appointment) firstArg;
			}

			if (appointment == null) {
				return;
			}

			Map<String, Integer> appointmentInfo = new HashMap<>(1);
			appointmentInfo.put(APPOINTMENT_ID_KEY, appointment.getId());
			threadLocal.set(appointmentInfo);
		}
	}
}
