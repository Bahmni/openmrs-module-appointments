package org.openmrs.module.appointments.model;

import org.openmrs.BaseOpenmrsData;
import org.openmrs.Location;
import org.openmrs.customdatatype.Customizable;
import org.openmrs.customdatatype.CustomValueDescriptor;

import java.io.Serializable;
import java.sql.Time;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class AppointmentServiceDefinition extends BaseOpenmrsData implements Serializable, Customizable<AppointmentServiceAttribute> {

    private Integer appointmentServiceId;
    private String name;
    private String description;
    private Speciality speciality;
    private Time startTime;
    private Time endTime;
    private Integer maxAppointmentsLimit;
    private Integer durationMins;
    private Location location;
    private String  color;
    private AppointmentStatus initialAppointmentStatus;
    private Set<ServiceWeeklyAvailability> weeklyAvailability;
    private Set<AppointmentServiceType> serviceTypes;
    private Set<AppointmentServiceAttribute> attributes;

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public Integer getDurationMins() {
        return durationMins;
    }

    public void setDurationMins(Integer durationMins) {
        this.durationMins = durationMins;
    }

    public Integer getMaxAppointmentsLimit() {
        return maxAppointmentsLimit;
    }

    public void setMaxAppointmentsLimit(Integer maxAppointmentsLimit) {
        this.maxAppointmentsLimit = maxAppointmentsLimit;
    }

    public Time getEndTime() {
        return endTime;
    }

    public void setEndTime(Time endTime) {
        this.endTime = endTime;
    }

    public Time getStartTime() {
        return startTime;
    }

    public void setStartTime(Time startTime) {
        this.startTime = startTime;
    }

    public Speciality getSpeciality() {
        return speciality;
    }

    public void setSpeciality(Speciality speciality) {
        this.speciality = speciality;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getAppointmentServiceId() {
        return appointmentServiceId;
    }

    public void setAppointmentServiceId(Integer appointmentServiceId) {
        this.appointmentServiceId = appointmentServiceId;
    }

    @Override
    public Integer getId() {
        return getAppointmentServiceId();
    }

    @Override
    public void setId(Integer id) {
        setAppointmentServiceId(id);
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Set<ServiceWeeklyAvailability> getWeeklyAvailability(boolean includeVoided) {
        if (this.weeklyAvailability == null) {
            this.weeklyAvailability = new LinkedHashSet<>();
        }
        if(includeVoided)
            return this.weeklyAvailability;

        Set<ServiceWeeklyAvailability> nonVoided = new LinkedHashSet<>(this.weeklyAvailability);
        nonVoided.removeIf(ServiceWeeklyAvailability::getVoided);
        return nonVoided;
    }

    public Set<ServiceWeeklyAvailability> getWeeklyAvailability() {
        return getWeeklyAvailability(false);
    }

    public void setWeeklyAvailability(
            Set<ServiceWeeklyAvailability> availability) {
        if (this.weeklyAvailability == null) {
            this.weeklyAvailability = new HashSet<>();
        }
            this.weeklyAvailability.clear();
            this.weeklyAvailability.addAll(availability);
    }

    public Set<AppointmentServiceType> getServiceTypes(boolean includeVoided) {
        if (this.serviceTypes == null) {
            this.serviceTypes = new LinkedHashSet<>();
        }
        if(includeVoided)
            return this.serviceTypes;

        Set<AppointmentServiceType> nonVoided = new LinkedHashSet<>(this.serviceTypes);
        nonVoided.removeIf(AppointmentServiceType::getVoided);
        return nonVoided;
    }

    public Set<AppointmentServiceType> getServiceTypes() {
        return getServiceTypes(false);
    }

    public void setServiceTypes(Set<AppointmentServiceType> serviceTypes) {
        if (this.serviceTypes == null) {
            this.serviceTypes = new HashSet<>();
        }
        this.serviceTypes.clear();
        this.serviceTypes.addAll(serviceTypes);
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public AppointmentStatus getInitialAppointmentStatus() {
        return initialAppointmentStatus;
    }

    public void setInitialAppointmentStatus(AppointmentStatus initialAppointmentStatus) {
        this.initialAppointmentStatus = initialAppointmentStatus;
    }

    public Set<AppointmentServiceAttribute> getAttributes(boolean includeVoided) {
        if (this.attributes == null) {
            this.attributes = new LinkedHashSet<>();
        }
        if(includeVoided)
            return this.attributes;

        Set<AppointmentServiceAttribute> nonVoided = new LinkedHashSet<>(this.attributes);
        nonVoided.removeIf(AppointmentServiceAttribute::getVoided);
        return nonVoided;
    }

    public Set<AppointmentServiceAttribute> getAttributes() {
        return getAttributes(false);
    }

    public void setAttributes(Set<AppointmentServiceAttribute> attributes) {
        if (this.attributes == null) {
            this.attributes = new HashSet<>();
        }
        this.attributes.clear();
        this.attributes.addAll(attributes);
    }

    @Override
    public Collection<AppointmentServiceAttribute> getActiveAttributes() {
        return getAttributes(false);
    }

    @Override
    public List<AppointmentServiceAttribute> getActiveAttributes(CustomValueDescriptor ofType) {
        List<AppointmentServiceAttribute> result = new ArrayList<>();
        for (AppointmentServiceAttribute attr : getActiveAttributes()) {
            if (attr.getAttributeType().equals(ofType)) {
                result.add(attr);
            }
        }
        return result;
    }

    @Override
    public void addAttribute(AppointmentServiceAttribute attribute) {
        if (this.attributes == null) {
            this.attributes = new LinkedHashSet<>();
        }
        attribute.setAppointmentService(this);
        this.attributes.add(attribute);
    }
}
