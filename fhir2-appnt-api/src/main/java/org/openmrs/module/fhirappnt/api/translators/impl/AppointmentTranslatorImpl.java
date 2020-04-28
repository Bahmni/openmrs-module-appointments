package org.openmrs.module.fhirappnt.api.translators.impl;
import lombok.AccessLevel;
import lombok.Setter;
import org.hl7.fhir.r4.model.Appointment;
import org.openmrs.Patient;
import org.openmrs.Provider;
import org.openmrs.module.appointments.model.AppointmentProvider;
import org.openmrs.module.appointments.model.AppointmentStatus;
import org.openmrs.module.fhir2.api.translators.AppointmentTranslator;
import org.openmrs.module.fhirappnt.api.translators.AppointmentParticipantTranslator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

@Component
@Setter(AccessLevel.PACKAGE)
public class AppointmentTranslatorImpl implements AppointmentTranslator<org.openmrs.module.appointments.model.Appointment> {

    @Autowired
    private AppointmentParticipantTranslator<Patient> patientTranslator;

    @Autowired
    private AppointmentParticipantTranslator<Provider> providerTranslator;

    @Autowired
    private AppointmentParticipantTranslator<AppointmentProvider> appointmentParticipantTranslator;

    @Override
    public Appointment toFhirResource(org.openmrs.module.appointments.model.Appointment appointment) {
        Appointment fhirAppointment  = new Appointment();
        fhirAppointment.setId(appointment.getUuid());
        fhirAppointment.setStart(appointment.getStartDateTime());
        fhirAppointment.setEnd(appointment.getEndDateTime());
        fhirAppointment.addParticipant(patientTranslator.toFhirResource(appointment.getPatient()));
        if (appointment.getProvider() != null){
            fhirAppointment.addParticipant(providerTranslator.toFhirResource(appointment.getProvider()));
        }

        if (appointment.getProviders() != null) {
            for (AppointmentProvider provider : appointment.getProviders()) {
                fhirAppointment.addParticipant(appointmentParticipantTranslator.toFhirResource(provider));
            }
        }

        switch (appointment.getStatus()) {
            case Requested :
                fhirAppointment.setStatus(Appointment.AppointmentStatus.PROPOSED);
                break;
            case Scheduled:
                fhirAppointment.setStatus(Appointment.AppointmentStatus.BOOKED);
                break;
            case CheckedIn:
                fhirAppointment.setStatus(Appointment.AppointmentStatus.CHECKEDIN);
                break;
            case Completed:
                fhirAppointment.setStatus(Appointment.AppointmentStatus.FULFILLED);
                break;
            case Missed:
                fhirAppointment.setStatus(Appointment.AppointmentStatus.NOSHOW);
                break;
            case Cancelled:
                fhirAppointment.setStatus(Appointment.AppointmentStatus.CANCELLED);
                break;
        }

        fhirAppointment.setComment(appointment.getComments());
        fhirAppointment.setCreated(appointment.getDateCreated());
        fhirAppointment.getMeta().setLastUpdated(appointment.getDateChanged());

        return fhirAppointment;
    }

    @Override
    public org.openmrs.module.appointments.model.Appointment toOpenmrsType(Appointment appointment) {
        return toOpenmrsType(new org.openmrs.module.appointments.model.Appointment(), appointment);
    }

    @Override
    public org.openmrs.module.appointments.model.Appointment toOpenmrsType(org.openmrs.module.appointments.model.Appointment appointment, Appointment fhirAppointment) {
        appointment.setUuid(fhirAppointment.getId());
        appointment.setStartDateTime(fhirAppointment.getStart());
        appointment.setEndDateTime(fhirAppointment.getEnd());

        Set<AppointmentProvider> providers = new HashSet<>();

        if (fhirAppointment.hasParticipant()) {
            for (Appointment.AppointmentParticipantComponent participantComponent : fhirAppointment.getParticipant()) {
                if (participantComponent.getType().get(0).getCoding().get(0).getCode() == "Patient") {
                    appointment.setPatient(patientTranslator.toOpenmrsType(participantComponent));
                } else if (participantComponent.getType().get(0).getCoding().get(0).getCode() == "Practitioner") {
                    appointment.setProvider(providerTranslator.toOpenmrsType(participantComponent));
                } else {
                    AppointmentProvider provider = appointmentParticipantTranslator.toOpenmrsType(participantComponent);
                    provider.setAppointment(appointment);
                    providers.add(provider);
                    appointment.setProviders(providers);
                }
            }
        }

        if (fhirAppointment.hasStatus()) {
            switch (fhirAppointment.getStatus()) {
                case PROPOSED:
                    appointment.setStatus(AppointmentStatus.Requested);
                    break;
                case BOOKED:
                    appointment.setStatus(AppointmentStatus.Scheduled);
                    break;
                case CHECKEDIN:
                    appointment.setStatus(AppointmentStatus.CheckedIn);
                    break;
                case FULFILLED:
                    appointment.setStatus(AppointmentStatus.Completed);
                    break;
                case NOSHOW:
                    appointment.setStatus(AppointmentStatus.Missed);
                    break;
                case CANCELLED:
                    appointment.setStatus(AppointmentStatus.Cancelled);
                    break;
            }
        }
        appointment.setComments(fhirAppointment.getComment());

        return appointment;
    }
}