package org.openmrs.module.appointments.telehealth;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class Invite {

    private String invitedBy;
    private Long createdAt;
    private Long updatedAt;
    private String id;
    private String firstName;
    private String lastName;
    private String gender;
    private String phoneNumber;
    private String emailAddress;
    private String status;
    private Long scheduledFor;
    private String type;
    private String patientLanguage;
    private String doctorLanguage;
    private String guestEmailAddress;
    private String guestPhoneNumber;
    private String queue;
    //private Doctor doctor;
    private String patientInvite;
    private String translationOrganization;
    private String translator;
    private String translatorRequestInvite;
    private String translatorInvite;
    private String guestInvite;
    private String patientURL;
    private String doctorURL;


    public String getInvitedBy() {
        return invitedBy;
    }

    public void setInvitedBy(String invitedBy) {
        this.invitedBy = invitedBy;
    }

    public Long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Long createdAt) {
        this.createdAt = createdAt;
    }

    public Long getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Long updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getEmailAddress() {
        return emailAddress;
    }

    public void setEmailAddress(String emailAddress) {
        this.emailAddress = emailAddress;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Long getScheduledFor() {
        return scheduledFor;
    }

    public void setScheduledFor(Long scheduledFor) {
        this.scheduledFor = scheduledFor;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getPatientLanguage() {
        return patientLanguage;
    }

    public void setPatientLanguage(String patientLanguage) {
        this.patientLanguage = patientLanguage;
    }

    public String getDoctorLanguage() {
        return doctorLanguage;
    }

    public void setDoctorLanguage(String doctorLanguage) {
        this.doctorLanguage = doctorLanguage;
    }

    public String getGuestEmailAddress() {
        return guestEmailAddress;
    }

    public void setGuestEmailAddress(String guestEmailAddress) {
        this.guestEmailAddress = guestEmailAddress;
    }

    public String getGuestPhoneNumber() {
        return guestPhoneNumber;
    }

    public void setGuestPhoneNumber(String guestPhoneNumber) {
        this.guestPhoneNumber = guestPhoneNumber;
    }

    public String getQueue() {
        return queue;
    }

    public void setQueue(String queue) {
        this.queue = queue;
    }

    /*public Doctor getDoctor() {
        return doctor;
    }

    public void setDoctor(Doctor doctor) {
        this.doctor = doctor;
    }*/

    public String getPatientInvite() {
        return patientInvite;
    }

    public void setPatientInvite(String patientInvite) {
        this.patientInvite = patientInvite;
    }

    public String getTranslationOrganization() {
        return translationOrganization;
    }

    public void setTranslationOrganization(String translationOrganization) {
        this.translationOrganization = translationOrganization;
    }

    public String getTranslator() {
        return translator;
    }

    public void setTranslator(String translator) {
        this.translator = translator;
    }

    public String getTranslatorRequestInvite() {
        return translatorRequestInvite;
    }

    public void setTranslatorRequestInvite(String translatorRequestInvite) {
        this.translatorRequestInvite = translatorRequestInvite;
    }

    public String getTranslatorInvite() {
        return translatorInvite;
    }

    public void setTranslatorInvite(String translatorInvite) {
        this.translatorInvite = translatorInvite;
    }

    public String getGuestInvite() {
        return guestInvite;
    }

    public void setGuestInvite(String guestInvite) {
        this.guestInvite = guestInvite;
    }

    public String getPatientURL() {
        return patientURL;
    }

    public void setPatientURL(String patientURL) {
        this.patientURL = patientURL;
    }

    public String getDoctorURL() {
        return doctorURL;
    }

    public void setDoctorURL(String doctorURL) {
        this.doctorURL = doctorURL;
    }

    public class Doctor{
        private String id;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }
    }
}
