package org.openmrs.module.appointments.telehealth;

import org.openmrs.Patient;
import org.openmrs.PersonAttribute;
import org.openmrs.module.appointments.model.Appointment;

public class TeleHealthUtils {

    public static InvitationRequest createInvitation(Appointment appointment){
        Patient patient = appointment.getPatient();

        InvitationRequest invitationRequest = new InvitationRequest();
        invitationRequest.setId(appointment.getExternalId());
        PersonAttribute paEmail = patient.getAttribute("Email");
        invitationRequest.setEmailAddress(paEmail.getValue()!=null?paEmail.getValue():"luis.oliveira@emea.merckleinc.com"); //TODO Mandatory
        PersonAttribute paPhoneNumber = patient.getAttribute("Telephone Number");
        invitationRequest.setPhoneNumber(paPhoneNumber.getValue()!=null?paPhoneNumber.getValue():"123456789"); //TODO Mandatory
        invitationRequest.setScheduledFor(appointment.getStartDateTime());
        invitationRequest.setLanguage("");
        invitationRequest.setDoctorLanguage("");
        invitationRequest.setDoctorEmail("olivier.b@iabsis.com"); // How to get
        invitationRequest.setFirstName(patient.getGivenName());
        invitationRequest.setLastName(patient.getFamilyName());
        invitationRequest.setGender(patient.getGender() == "M" ? "male" : "female");
        invitationRequest.setIsPatientInvite(true);
        invitationRequest.setSendInvite(true);

        return invitationRequest;
    }
}
