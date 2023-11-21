package org.openmrs.module.appointments.events.advice;

import com.google.common.collect.Sets;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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
import static org.openmrs.module.appointments.events.AppointmentEventType.BAHMNI_APPOINTMENT_CREATED;
import static org.openmrs.module.appointments.events.AppointmentEventType.BAHMNI_APPOINTMENT_UPDATED;

public class AppointmentEventsAdvice implements AfterReturningAdvice, MethodBeforeAdvice {

	private final Logger log = LogManager.getLogger(AppointmentEventsAdvice.class);
	private final AppointmentEventPublisher eventPublisher;
	private final ThreadLocal<Map<String,Integer>> threadLocal = new ThreadLocal<>();
	private final String APPOINTMENT_ID_KEY = "appointmentId";
	private final Set<String> adviceMethodNames = Sets.newHashSet("validateAndSave");

	public AppointmentEventsAdvice() {
		this.eventPublisher=Context.getRegisteredComponent("appointmentEventPublisher",AppointmentEventPublisher.class);
	}

	@Override
	public void afterReturning(Object returnValue, Method method, Object[] arguments, Object target) {
		if (adviceMethodNames.contains(method.getName())) {
			Map<String, Integer> patientInfo = threadLocal.get();
			if (patientInfo != null) {
				AppointmentEventType eventType = patientInfo.get(APPOINTMENT_ID_KEY) == null ? BAHMNI_APPOINTMENT_CREATED : BAHMNI_APPOINTMENT_UPDATED;
				threadLocal.remove();

				Appointment appointment = (Appointment) returnValue;
				AppointmentBookingEvent appointmentEvent =new AppointmentBookingEvent(eventType,appointment);
				eventPublisher.publishEvent(appointmentEvent);
				log.info("Successfully published event with uuid : " + appointmentEvent.payloadId);
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
