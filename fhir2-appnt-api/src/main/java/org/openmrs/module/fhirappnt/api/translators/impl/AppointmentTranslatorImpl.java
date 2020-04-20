package org.openmrs.module.fhirappnt.api.translators.impl;

import lombok.AccessLevel;
import lombok.Setter;
import org.hl7.fhir.r4.model.Appointment;
import org.openmrs.module.appointments.model.AppointmentStatus;
import org.openmrs.module.fhir2.api.translators.AppointmentTranslator;
import org.springframework.stereotype.Component;


@Component
@Setter(AccessLevel.PACKAGE)
public class AppointmentTranslatorImpl implements AppointmentTranslator<org.openmrs.module.appointments.model.Appointment> {

    @Override
    public Appointment toFhirResource(org.openmrs.module.appointments.model.Appointment appointment) {
        Appointment fhirAppointment  = new Appointment();
        fhirAppointment.setId(appointment.getUuid());
        fhirAppointment.setStart(appointment.getStartDateTime());
        fhirAppointment.setEnd(appointment.getEndDateTime());

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
