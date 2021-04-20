package org.openmrs.module.appointments.telehealth;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class User {
    private String email;
    private String username;
    private String password;
    private String id;
    private String role;
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private String authPhoneNumber;
    private Boolean viewAllQueues;
    private String doctorClientVersion;
    private String notifPhoneNumber;
    private Boolean enableNotif;
    private String token;
    private String smsVerificationCode;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
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

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getAuthPhoneNumber() {
        return authPhoneNumber;
    }

    public void setAuthPhoneNumber(String authPhoneNumber) {
        this.authPhoneNumber = authPhoneNumber;
    }

    public Boolean getViewAllQueues() {
        return viewAllQueues;
    }

    public void setViewAllQueues(Boolean viewAllQueues) {
        this.viewAllQueues = viewAllQueues;
    }

    public String getDoctorClientVersion() {
        return doctorClientVersion;
    }

    public void setDoctorClientVersion(String doctorClientVersion) {
        this.doctorClientVersion = doctorClientVersion;
    }

    public String getNotifPhoneNumber() {
        return notifPhoneNumber;
    }

    public void setNotifPhoneNumber(String notifPhoneNumber) {
        this.notifPhoneNumber = notifPhoneNumber;
    }

    public Boolean getEnableNotif() {
        return enableNotif;
    }

    public void setEnableNotif(Boolean enableNotif) {
        this.enableNotif = enableNotif;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getSmsVerificationCode() {
        return smsVerificationCode;
    }

    public void setSmsVerificationCode(String smsVerificationCode) {
        this.smsVerificationCode = smsVerificationCode;
    }
}
