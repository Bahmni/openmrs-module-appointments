package org.openmrs.module.appointments.service.impl;

import org.openmrs.Location;
import org.openmrs.LocationTag;
import org.openmrs.Patient;
import org.openmrs.api.context.Context;
import org.openmrs.module.appointments.model.Appointment;
import org.openmrs.module.appointments.model.AppointmentProvider;
import org.openmrs.module.appointments.service.AppointmentArgumentsMapper;


import java.util.ArrayList;
import java.util.Map;
import java.util.List;
import java.util.HashMap;
import java.util.Date;
import java.util.Locale;
import java.util.Collections;


import static org.openmrs.module.appointments.util.DateUtil.convertUTCToGivenFormat;

public class AppointmentArgumentsMapperImpl implements AppointmentArgumentsMapper {

    @Override
    public Map<String, String> createArgumentsMapForRecurringAppointmentBooking(Appointment appointment) {
        Map<String, String> arguments = createArgumentsMapForAppointmentBooking(appointment);
        arguments.put("recurringperiod", appointment.getAppointmentRecurringPattern().getPeriod().toString());
        arguments.put("recurringtype", appointment.getAppointmentRecurringPattern().getType().toString());
        String daysOfWeek=appointment.getAppointmentRecurringPattern().getDaysOfWeek() != null? appointment.getAppointmentRecurringPattern().getDaysOfWeek().toString():"";
        arguments.put("recurringdays",daysOfWeek);
        if (appointment.getAppointmentRecurringPattern().getFrequency() != null)
            arguments.put("recurringfrequency", appointment.getAppointmentRecurringPattern().getFrequency().toString());

        return arguments;
    }

    @Override
    public List<String> getProvidersNameInString(Appointment appointment) {
        List<AppointmentProvider> appointmentProviderList = new ArrayList<>();
        if (appointment.getProviders() != null) {
            appointmentProviderList.addAll(appointment.getProviders());
        }
        List<String> providersName = new ArrayList<>();
        for (AppointmentProvider appointmentProvider : appointmentProviderList) {
            providersName.add(appointmentProvider.getProvider().getName());
        }
        return providersName;
    }

    @Override
    public Map<String, String> createArgumentsMapForAppointmentBooking(Appointment appointment) {
        Map<String, String> arguments = new HashMap<>();
        Patient patient = Context.getPatientService().getPatientByUuid(appointment.getPatient().getUuid());
        String givenName = patient.getGivenName();
        String familyName = patient.getFamilyName();
        String identifier = patient.getPatientIdentifier().getIdentifier();
        Date appointmentDate = appointment.getStartDateTime();
        String appointmentKind = appointment.getAppointmentKind().getValue();
        String service = appointment.getService().getName();
        String teleLink = appointment.getTeleHealthVideoLink();
        String smsTimeZone = Context.getMessageSourceService().getMessage(Context.getAdministrationService().getGlobalProperty("sms.timezone"), null, new Locale("en"));
        String smsDateFormat = Context.getMessageSourceService().getMessage(Context.getAdministrationService().getGlobalProperty("sms.dateformat"), null, new Locale("en"));
        String date = convertUTCToGivenFormat(appointmentDate, smsDateFormat, smsTimeZone);
        String helpdeskNumber = Context.getAdministrationService().getGlobalPropertyObject("clinic.helpDeskNumber").getPropertyValue();
        String facilityName = getFacilityName(appointment.getLocation());
        arguments.put("patientname", givenName + " " + familyName);
        arguments.put("identifier", identifier);
        arguments.put("gender", appointment.getPatient().getGender());
        arguments.put("date", date);
        arguments.put("service", service);
        arguments.put("facilityname", facilityName);
        arguments.put("teleconsultationlink", teleLink);
        arguments.put("helpdesknumber", helpdeskNumber);
        arguments.put("appointmentKind", appointmentKind);
        return arguments;
    }

    public String getFacilityName(Location location) {
        if(location==null){
            LocationTag visitLocationTag = Context.getLocationService().getLocationTagByName("Visit Location");
            List<Location> locations = Context.getLocationService().getLocationsHavingAnyTag(
                    Collections.singletonList(visitLocationTag));
            return (visitLocationTag != null && !locations.isEmpty()) ? locations.get(0).getName() : "xxxxx";
        }
        Location facilityLocation = getParentVisitLocationUuid(location);
        return facilityLocation.getName();
    }

    private Location getParentVisitLocationUuid(Location location) {
            if(isVisitLocation(location)) {
            return location.getParentLocation() != null ? getParentVisitLocationUuid(location.getParentLocation()) : location;
        } else {
            return location.getParentLocation() != null ? getParentVisitLocationUuid(location.getParentLocation()) : null;
        }
    }

    private Boolean isVisitLocation(Location location) {
        return (location.getTags().size() > 0 && location.getTags().stream().filter(tag -> tag.getName().equalsIgnoreCase("Visit Location")).count() != 0);
    }
}
