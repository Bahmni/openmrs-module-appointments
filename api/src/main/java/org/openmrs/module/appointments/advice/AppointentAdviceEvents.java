package org.openmrs.module.appointments.advice;

import com.google.common.collect.Sets;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bahmni.module.bahmnicommons.api.model.BahmniEventType;
import org.bahmni.module.bahmnicommons.api.eventPublisher.BahmniEventPublisher;
import org.openmrs.api.context.Context;
import org.openmrs.module.appointments.model.Appointment;
import org.openmrs.module.appointments.model.AppointmentEvent;
import org.springframework.aop.AfterReturningAdvice;
import org.springframework.aop.MethodBeforeAdvice;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import static org.bahmni.module.bahmnicommons.api.model.BahmniEventType.BAHMNI_APPOINTMENT_CREATED;
import static org.bahmni.module.bahmnicommons.api.model.BahmniEventType.BAHMNI_APPOINTMENT_UPDATED;

public class AppointentAdviceEvents implements AfterReturningAdvice, MethodBeforeAdvice {
	
	private final Logger log = LogManager.getLogger(AppointentAdviceEvents.class);
	private final BahmniEventPublisher eventPublisher;
	private final ThreadLocal<Map<String,Integer>> threadLocal = new ThreadLocal<>();
	private final String APPOINTMENT_ID_KEY = "appointmentId";
	private final Set<String> adviceMethodNames = Sets.newHashSet("validateAndSave");
	private final AppointmentEvent appointmentEvent;

	public AppointentAdviceEvents() {
		this.eventPublisher = Context.getRegisteredComponent("bahmniEventPublisher", BahmniEventPublisher.class);
		this.appointmentEvent = Context.getRegisteredComponent("appointmentEvent", AppointmentEvent.class);
	}

	@Override
	public void afterReturning(Object returnValue, Method method, Object[] arguments, Object target) {
		if (adviceMethodNames.contains(method.getName())) {
			Map<String, Integer> patientInfo = threadLocal.get();
			if (patientInfo != null) {
				BahmniEventType eventType = patientInfo.get(APPOINTMENT_ID_KEY) == null ? BAHMNI_APPOINTMENT_CREATED : BAHMNI_APPOINTMENT_UPDATED;
				threadLocal.remove();

				Appointment appointment = (Appointment) returnValue;
//
//				Object representation = ConversionUtil.convertToRepresentation(patient, Representation.FULL);
				appointmentEvent.createAppointmentEvent(eventType, appointment);
				eventPublisher.publishEvent(appointmentEvent);

				log.info("Successfully published event with uuid : " + appointment.getUuid());
			}
		}
	}
	@Override
	public void before(Method method, Object[] objects, Object o) {
		if (adviceMethodNames.contains(method.getName())) {
			Appointment appointment = ((Supplier<Appointment>) objects[0]).get();
			Map<String, Integer> appointmentInfo = new HashMap<>(1);
			appointmentInfo.put(APPOINTMENT_ID_KEY, appointment.getId());
			threadLocal.set(appointmentInfo);
		}
	}
}
