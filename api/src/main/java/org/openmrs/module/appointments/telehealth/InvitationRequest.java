package org.openmrs.module.appointments.telehealth;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.Date;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class InvitationRequest {

    private String id;
    private String emailAddress;
    private String phoneNumber;
    @JsonFormat(pattern = "yyyy-MM-dd' 'HH:mm:ss")
    private Date scheduledFor;
    private String language;
    private String doctorLanguage;
    private String doctorEmail;
    private String firstName;
    private String lastName;
    private String gender;
    private Boolean isPatientInvite;
    private Boolean sendInvite;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getEmailAddress() {
        return emailAddress;
    }

    public void setEmailAddress(String emailAddress) {
        this.emailAddress = emailAddress;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public Date getScheduledFor() {
        return scheduledFor;
    }

    public void setScheduledFor(Date scheduledFor) {
        this.scheduledFor = scheduledFor;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getDoctorLanguage() {
        return doctorLanguage;
    }

    public void setDoctorLanguage(String doctorLanguage) {
        this.doctorLanguage = doctorLanguage;
    }

    public String getDoctorEmail() {
        return doctorEmail;
    }

    public void setDoctorEmail(String doctorEmail) {
        this.doctorEmail = doctorEmail;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public Boolean getIsPatientInvite() {
        return isPatientInvite;
    }

    public void setIsPatientInvite(Boolean isPatientInvite) {
        this.isPatientInvite = isPatientInvite;
    }

    public Boolean getSendInvite() {
        return sendInvite;
    }

    public void setSendInvite(Boolean sendInvite) {
        this.sendInvite = sendInvite;
    }

}
