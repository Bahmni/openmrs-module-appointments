package org.openmrs.module.appointments.telehealth;

public interface TeleHealthService {

    public Boolean authenticate();

    public Invite invite(InvitationRequest invitationRequest);

    public void delete(String invitationID);

}
