package org.openmrs.module.fhirappnt.api.translators.impl;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import org.exparity.hamcrest.date.DateMatchers;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.mockito.runners.MockitoJUnitRunner;
import org.openmrs.module.appointments.model.Appointment;
import org.openmrs.module.appointments.model.AppointmentStatus;

import java.util.Date;

@RunWith(MockitoJUnitRunner.class)
public class AppointmentTranslatorImplTest {

    private static String APPOINTMENT_UUID = "162298AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";

    private AppointmentTranslatorImpl appointmentTranslator;

    private Appointment appointment;

    @Before
    public void setUp() {
        appointmentTranslator = new AppointmentTranslatorImpl();
        appointment = new Appointment();
    }

    @Test
    public void toFhirResourceShouldTranslateUuidToId() {
        appointment.setUuid(APPOINTMENT_UUID);
        org.hl7.fhir.r4.model.Appointment result = appointmentTranslator.toFhirResource(appointment);
        assertThat(result, notNullValue());
        assertThat(result.getId(), equalTo(APPOINTMENT_UUID));
    }

    @Test
    public void toFhirTypeShouldMapStatusRequestedToProposed() {
        appointment.setStatus(AppointmentStatus.Requested);

        org.hl7.fhir.r4.model.Appointment result = appointmentTranslator.toFhirResource(appointment);
        assertThat(result, notNullValue());
        assertThat(result.getStatus(), equalTo(org.hl7.fhir.r4.model.Appointment.AppointmentStatus.PROPOSED));
    }

    @Test
    public void toFhirTypeShouldMapStatusScheduledToBooked() {
        appointment.setStatus(AppointmentStatus.Scheduled);

        org.hl7.fhir.r4.model.Appointment result = appointmentTranslator.toFhirResource(appointment);
        assertThat(result, notNullValue());
        assertThat(result.getStatus(), equalTo(org.hl7.fhir.r4.model.Appointment.AppointmentStatus.BOOKED));
    }

    @Test
    public void toFhirTypeShouldMapStatusCheckedInToCheckedIn() {
        appointment.setStatus(AppointmentStatus.CheckedIn);

        org.hl7.fhir.r4.model.Appointment result = appointmentTranslator.toFhirResource(appointment);
        assertThat(result, notNullValue());
        assertThat(result.getStatus(), equalTo(org.hl7.fhir.r4.model.Appointment.AppointmentStatus.CHECKEDIN));
    }

    @Test
    public void toFhirTypeShouldMapStatusCompletedToFulfilled() {
        appointment.setStatus(AppointmentStatus.Completed);

        org.hl7.fhir.r4.model.Appointment result = appointmentTranslator.toFhirResource(appointment);
        assertThat(result, notNullValue());
        assertThat(result.getStatus(), equalTo(org.hl7.fhir.r4.model.Appointment.AppointmentStatus.FULFILLED));
    }

    @Test
    public void toFhirTypeShouldMapStatusMissedToNoShow() {
        appointment.setStatus(AppointmentStatus.Missed);

        org.hl7.fhir.r4.model.Appointment result = appointmentTranslator.toFhirResource(appointment);
        assertThat(result, notNullValue());
        assertThat(result.getStatus(), equalTo(org.hl7.fhir.r4.model.Appointment.AppointmentStatus.NOSHOW));
    }

    @Test
    public void toFhirTypeShouldMapStatusCancelledToCancelled() {
        appointment.setStatus(AppointmentStatus.Cancelled);

        org.hl7.fhir.r4.model.Appointment result = appointmentTranslator.toFhirResource(appointment);
        assertThat(result, notNullValue());
        assertThat(result.getStatus(), equalTo(org.hl7.fhir.r4.model.Appointment.AppointmentStatus.CANCELLED));
    }

    @Test
    public void toFhirTypeShouldMapStartDateTimeToStart() {
        appointment.setStartDateTime(new Date());

        org.hl7.fhir.r4.model.Appointment result = appointmentTranslator.toFhirResource(appointment);
        assertThat(result, notNullValue());
        assertThat(result.getStart(), DateMatchers.sameDay(new Date()));
    }

    @Test
    public void toFhirTypeShouldMapEndDateTimeToEnd() {
        appointment.setEndDateTime(new Date());

        org.hl7.fhir.r4.model.Appointment result = appointmentTranslator.toFhirResource(appointment);
        assertThat(result, notNullValue());
        assertThat(result.getEnd(), DateMatchers.sameDay(new Date()));
    }

    @Test
    public void toFhirTypeShouldMapDateCreatedToDateCreated() {
        appointment.setDateCreated(new Date());

        org.hl7.fhir.r4.model.Appointment result = appointmentTranslator.toFhirResource(appointment);
        assertThat(result, notNullValue());
        assertThat(result.getCreated(), DateMatchers.sameDay(new Date()));
    }

    @Test
    public void toFhirTypeShouldDateChangedToLastDateUpdated() {
        appointment.setDateChanged(new Date());

        org.hl7.fhir.r4.model.Appointment result = appointmentTranslator.toFhirResource(appointment);
        assertThat(result, notNullValue());
        assertThat(result.getMeta().getLastUpdated(), DateMatchers.sameDay(new Date()));
    }

    @Test
    public void toFhirTypeShouldCommentsToFhirAppointmentComment() {
        appointment.setComments("Test Appointment");

        org.hl7.fhir.r4.model.Appointment result = appointmentTranslator.toFhirResource(appointment);
        assertThat(result, notNullValue());
        assertThat(result.getComment(), equalTo("Test Appointment"));
    }

    @Test
    public void toOpenmrsTypeShouldTranslateIdToUuid() {
        org.hl7.fhir.r4.model.Appointment appointment = new org.hl7.fhir.r4.model.Appointment();
        appointment.setId(APPOINTMENT_UUID);

        Appointment result = appointmentTranslator.toOpenmrsType(new Appointment(), appointment);
        assertThat(result, notNullValue());
        assertThat(result.getUuid(), equalTo(APPOINTMENT_UUID));
    }

    @Test
    public void toOpenmrsTypeShouldMapStatusProposedToRequested() {
        org.hl7.fhir.r4.model.Appointment appointment = new org.hl7.fhir.r4.model.Appointment();
        appointment.setStatus(org.hl7.fhir.r4.model.Appointment.AppointmentStatus.PROPOSED);

        Appointment result = appointmentTranslator.toOpenmrsType(new Appointment(), appointment);
        assertThat(result, notNullValue());
        assertThat(result.getStatus(), equalTo(AppointmentStatus.Requested));
    }

    @Test
    public void toOpenmrsTypeShouldMapStatusBookedToScheduled() {
        org.hl7.fhir.r4.model.Appointment appointment = new org.hl7.fhir.r4.model.Appointment();
        appointment.setStatus(org.hl7.fhir.r4.model.Appointment.AppointmentStatus.BOOKED);

        Appointment result = appointmentTranslator.toOpenmrsType(new Appointment(), appointment);
        assertThat(result, notNullValue());
        assertThat(result.getStatus(), equalTo(AppointmentStatus.Scheduled));
    }

    @Test
    public void toOpenmrsTypeShouldMapStatusCheckedInToCheckedIn() {
        org.hl7.fhir.r4.model.Appointment appointment = new org.hl7.fhir.r4.model.Appointment();
        appointment.setStatus(org.hl7.fhir.r4.model.Appointment.AppointmentStatus.CHECKEDIN);

        Appointment result = appointmentTranslator.toOpenmrsType(new Appointment(), appointment);
        assertThat(result, notNullValue());
        assertThat(result.getStatus(), equalTo(AppointmentStatus.CheckedIn));
    }

    @Test
    public void toOpenmrsTypeShouldMapStatusNoShowToMissed() {
        org.hl7.fhir.r4.model.Appointment appointment = new org.hl7.fhir.r4.model.Appointment();
        appointment.setStatus(org.hl7.fhir.r4.model.Appointment.AppointmentStatus.NOSHOW);

        Appointment result = appointmentTranslator.toOpenmrsType(new Appointment(), appointment);
        assertThat(result, notNullValue());
        assertThat(result.getStatus(), equalTo(AppointmentStatus.Missed));
    }

    @Test
    public void toOpenmrsTypeShouldMapStatusCancelledToCancelled() {
        org.hl7.fhir.r4.model.Appointment appointment = new org.hl7.fhir.r4.model.Appointment();
        appointment.setStatus(org.hl7.fhir.r4.model.Appointment.AppointmentStatus.CANCELLED);

        Appointment result = appointmentTranslator.toOpenmrsType(new Appointment(), appointment);
        assertThat(result, notNullValue());
        assertThat(result.getStatus(), equalTo(AppointmentStatus.Cancelled));
    }

    @Test
    public void toOpenmrsTypeShouldMapStatusFulfilledToCompleted() {
        org.hl7.fhir.r4.model.Appointment appointment = new org.hl7.fhir.r4.model.Appointment();
        appointment.setStatus(org.hl7.fhir.r4.model.Appointment.AppointmentStatus.FULFILLED);

        Appointment result = appointmentTranslator.toOpenmrsType(new Appointment(), appointment);
        assertThat(result, notNullValue());
        assertThat(result.getStatus(), equalTo(AppointmentStatus.Completed));
    }

    @Test
    public void toOpenmrsTypeShouldMapStartToStartDateTime() {
        org.hl7.fhir.r4.model.Appointment appointment = new org.hl7.fhir.r4.model.Appointment();
        appointment.setStart(new Date());

        Appointment result = appointmentTranslator.toOpenmrsType(new Appointment(), appointment);
        assertThat(result, notNullValue());
        assertThat(result.getStartDateTime(), DateMatchers.sameDay(new Date()));
    }

    @Test
    public void toOpenmrsTypeShouldMapEndToEndDateTime() {
        org.hl7.fhir.r4.model.Appointment appointment = new org.hl7.fhir.r4.model.Appointment();
        appointment.setEnd(new Date());

        Appointment result = appointmentTranslator.toOpenmrsType(new Appointment(), appointment);
        assertThat(result, notNullValue());
        assertThat(result.getEndDateTime(), DateMatchers.sameDay(new Date()));
    }

    @Test
    public void toOpenmrsTypeShouldMapCommentsCorrectly() {
        org.hl7.fhir.r4.model.Appointment appointment = new org.hl7.fhir.r4.model.Appointment();
        appointment.setComment("Test Appointment");

        Appointment result = appointmentTranslator.toOpenmrsType(new Appointment(), appointment);
        assertThat(result, notNullValue());
        assertThat(result.getComments(), equalTo("Test Appointment"));
    }

}
