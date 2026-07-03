package org.openmrs.module.appointments.web.mapper;

import org.apache.commons.lang.StringUtils;
import org.openmrs.Location;
import org.openmrs.api.LocationService;
import org.openmrs.api.context.Context;
import org.openmrs.module.appointments.model.AppointmentServiceAttribute;
import org.openmrs.module.appointments.model.AppointmentServiceDefinition;
import org.openmrs.module.appointments.model.AppointmentServiceType;
import org.openmrs.module.appointments.model.AppointmentStatus;
import org.openmrs.module.appointments.model.ServiceWeeklyAvailability;
import org.openmrs.module.appointments.model.Speciality;
import org.openmrs.module.appointments.model.AppointmentServiceAttributeType;
import org.openmrs.module.appointments.service.AppointmentServiceAttributeTypeService;
import org.openmrs.module.appointments.service.AppointmentServiceDefinitionService;
import org.openmrs.module.appointments.service.SpecialityService;
import org.openmrs.module.appointments.web.contract.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.sql.Time;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class AppointmentServiceMapper {
    @Autowired
    LocationService locationService;

    @Autowired
    SpecialityService specialityService;

    @Autowired
    AppointmentServiceDefinitionService appointmentServiceDefinitionService;

    @Autowired
    AppointmentServiceAttributeTypeService appointmentServiceAttributeTypeService;

    public AppointmentServiceDefinition fromDescription(AppointmentServiceDescription appointmentServiceDescription) {
        AppointmentServiceDefinition appointmentServiceDefinition;
        if (!StringUtils.isBlank(appointmentServiceDescription.getUuid())) {
            appointmentServiceDefinition = appointmentServiceDefinitionService.getAppointmentServiceByUuid(appointmentServiceDescription.getUuid());
        }else{
            appointmentServiceDefinition = new AppointmentServiceDefinition();
        }
        appointmentServiceDefinition.setName(appointmentServiceDescription.getName());
        appointmentServiceDefinition.setDescription(appointmentServiceDescription.getDescription());
        appointmentServiceDefinition.setDurationMins(appointmentServiceDescription.getDurationMins());
        appointmentServiceDefinition.setStartTime(utcTimeToServerTime(appointmentServiceDescription.getStartTime()));
        appointmentServiceDefinition.setEndTime(utcTimeToServerTime(appointmentServiceDescription.getEndTime()));
        appointmentServiceDefinition.setMaxAppointmentsLimit(appointmentServiceDescription.getMaxAppointmentsLimit());
        appointmentServiceDefinition.setColor(appointmentServiceDescription.getColor());

        String initialAppointmentStatus = appointmentServiceDescription.getInitialAppointmentStatus();
        if (StringUtils.isNotBlank(initialAppointmentStatus)) {
            appointmentServiceDefinition.setInitialAppointmentStatus(AppointmentStatus.valueOf(initialAppointmentStatus));
        }else {
            appointmentServiceDefinition.setInitialAppointmentStatus(null);
        }

        String locationUuid = appointmentServiceDescription.getLocationUuid();
        Location location = locationService.getLocationByUuid(locationUuid);
        appointmentServiceDefinition.setLocation(location);

        String specialityUuid = appointmentServiceDescription.getSpecialityUuid();
        Speciality speciality = specialityService.getSpecialityByUuid(specialityUuid);
        appointmentServiceDefinition.setSpeciality(speciality);

        List<ServiceWeeklyAvailabilityDescription> weeklyAvailabilities = appointmentServiceDescription.getWeeklyAvailability();

        if(weeklyAvailabilities != null) {
            Set<ServiceWeeklyAvailability> availabilityList = weeklyAvailabilities.stream()
                    .map(avb -> constructServiceWeeklyAvailability(avb, appointmentServiceDefinition)).collect(Collectors.toSet());
            appointmentServiceDefinition.setWeeklyAvailability(availabilityList);
        }
        
        if(appointmentServiceDescription.getServiceTypes() != null) {
            appointmentServiceDescription.getServiceTypes()
                    .forEach(serviceType -> constructAppointmentServiceTypes(serviceType, appointmentServiceDefinition));
        }

        if(appointmentServiceDescription.getAttributes() != null) {
            appointmentServiceDescription.getAttributes()
                    .forEach(attribute -> constructAppointmentServiceAttribute(attribute, appointmentServiceDefinition));
        }

        validateAttributeCardinality(appointmentServiceDefinition);

        return appointmentServiceDefinition;
    }

    private void constructAppointmentServiceTypes(AppointmentServiceTypeDescription ast, AppointmentServiceDefinition appointmentServiceDefinition) {
        AppointmentServiceType serviceType;
        Set<AppointmentServiceType> existingServiceTypes = appointmentServiceDefinition.getServiceTypes(true);
        if(ast.getUuid() != null)
            serviceType = getServiceTypeByUuid(existingServiceTypes, ast.getUuid());
        else
            serviceType = new AppointmentServiceType();
        serviceType.setName(ast.getName());
        serviceType.setDuration(ast.getDuration());
        serviceType.setAppointmentServiceDefinition(appointmentServiceDefinition);
        if (ast.getVoided() != null) {
            setVoidedInfo(serviceType, ast.getVoidedReason());
        }
        existingServiceTypes.add(serviceType);
    }

    private void setVoidedInfo(AppointmentServiceType serviceType, String voidReason) {
        serviceType.setVoided(true);
        serviceType.setVoidReason(voidReason);
        serviceType.setDateVoided(new Date());
        serviceType.setVoidedBy(Context.getAuthenticatedUser());
    }

    private void constructAppointmentServiceAttribute(AppointmentServiceAttributeDescription attrDesc, AppointmentServiceDefinition appointmentServiceDefinition) {
        AppointmentServiceAttribute attribute;
        Set<AppointmentServiceAttribute> existingAttributes = appointmentServiceDefinition.getAttributes(true);

        if(attrDesc.getUuid() != null)
            attribute = getAttributeByUuid(existingAttributes, attrDesc.getUuid());
        else
            attribute = new AppointmentServiceAttribute();

        AppointmentServiceAttributeType attributeType = appointmentServiceAttributeTypeService.getAttributeTypeByUuid(attrDesc.getAttributeTypeUuid());
        if (attributeType == null) {
            throw new RuntimeException("Attribute type not found: " + attrDesc.getAttributeTypeUuid());
        }

        attribute.setAttributeType(attributeType);
        attribute.setValueReferenceInternal(attrDesc.getValue());
        attribute.setAppointmentService(appointmentServiceDefinition);

        if (attrDesc.getVoided() != null && attrDesc.getVoided()) {
            setVoidedInfoForAttribute(attribute, attrDesc.getVoidReason());
        }

        existingAttributes.add(attribute);
    }

    private void setVoidedInfoForAttribute(AppointmentServiceAttribute attribute, String voidReason) {
        attribute.setVoided(true);
        attribute.setVoidReason(voidReason);
        attribute.setDateVoided(new Date());
        attribute.setVoidedBy(Context.getAuthenticatedUser());
    }

    private void validateAttributeCardinality(AppointmentServiceDefinition appointmentServiceDefinition) {
        List<AppointmentServiceAttributeType> allAttributeTypes = appointmentServiceAttributeTypeService.getAllAttributeTypes(false);

        for (AppointmentServiceAttributeType attributeType : allAttributeTypes) {
            Set<AppointmentServiceAttribute> existingAttributes = appointmentServiceDefinition.getAttributes(true);
            long count = existingAttributes.stream()
                    .filter(attr -> !attr.getVoided())
                    .filter(attr -> attr.getAttributeType().getUuid().equals(attributeType.getUuid()))
                    .count();

            Integer minOccurs = attributeType.getMinOccurs();
            if (minOccurs != null && minOccurs > 0) {
                if (count < minOccurs) {
                    throw new RuntimeException("Attribute '" + attributeType.getName() +
                            "' requires at least " + minOccurs + " occurrence(s), but only " + count + " provided");
                }
            }

            Integer maxOccurs = attributeType.getMaxOccurs();
            if (maxOccurs != null && maxOccurs > 0) {
                if (count > maxOccurs) {
                    throw new RuntimeException("Attribute '" + attributeType.getName() +
                            "' allows at most " + maxOccurs + " occurrence(s), but " + count + " provided");
                }
            }
        }
    }

    private AppointmentServiceAttribute getAttributeByUuid(Set<AppointmentServiceAttribute> attributes, String attributeUuid) {
        return attributes.stream()
                .filter(attr -> attr.getUuid().equals(attributeUuid)).findAny().get();
    }

    private ServiceWeeklyAvailability constructServiceWeeklyAvailability(ServiceWeeklyAvailabilityDescription avb, AppointmentServiceDefinition appointmentServiceDefinition) {
        ServiceWeeklyAvailability availability;
        if(avb.getUuid() != null)
            availability = getAvailabilityByUuid(appointmentServiceDefinition.getWeeklyAvailability(), avb.getUuid());
        else
            availability = new ServiceWeeklyAvailability();
        availability.setDayOfWeek(avb.getDayOfWeek());
        availability.setStartTime(utcTimeToServerTime(avb.getStartTime()));
        availability.setEndTime(utcTimeToServerTime(avb.getEndTime()));
        availability.setMaxAppointmentsLimit(avb.getMaxAppointmentsLimit());
        availability.setService(appointmentServiceDefinition);
        availability.setVoided(avb.isVoided());

        return availability;
    }

    private ServiceWeeklyAvailability getAvailabilityByUuid(Set<ServiceWeeklyAvailability> weeklyAvailabilities, String availabilityUuid) {
           return weeklyAvailabilities.stream()
                    .filter(avb -> avb.getUuid().equals(availabilityUuid)).findAny().get();
    }

    private AppointmentServiceType getServiceTypeByUuid(Set<AppointmentServiceType> serviceTypes, String serviceTypeUuid) {
        return serviceTypes.stream()
                .filter(avb -> avb.getUuid().equals(serviceTypeUuid)).findAny().get();
    }

    public List<AppointmentServiceDefaultResponse> constructDefaultResponseForServiceList(List<AppointmentServiceDefinition> appointmentServiceDefinitions) {
        return appointmentServiceDefinitions.stream().map(as -> this.mapToDefaultResponse(as, new AppointmentServiceDefaultResponse())).collect(Collectors.toList());
    }

    private Map constructServiceTypeResponse(AppointmentServiceType serviceType) {
        Map serviceTypeMap = new HashMap();
        serviceTypeMap.put("name", serviceType.getName());
        serviceTypeMap.put("duration",serviceType.getDuration());
        serviceTypeMap.put("uuid", serviceType.getUuid());
        return serviceTypeMap;
    }

    public List<AppointmentServiceFullResponse> constructFullResponseForServiceList(List<AppointmentServiceDefinition> appointmentServiceDefinitions) {
        return appointmentServiceDefinitions.stream().map(as -> this.constructResponse(as)).collect(Collectors.toList());
    }

    public AppointmentServiceFullResponse constructResponse(AppointmentServiceDefinition service) {
        AppointmentServiceFullResponse response = new AppointmentServiceFullResponse();
        mapToDefaultResponse(service, response);
        Set<ServiceWeeklyAvailability> serviceWeeklyAvailability = service.getWeeklyAvailability();
        if(serviceWeeklyAvailability != null) {
            response.setWeeklyAvailability(serviceWeeklyAvailability.stream().map(availability -> this.constructAvailabilityResponse(availability)).collect(Collectors.toList()));
        }
        Set<AppointmentServiceType> serviceTypes = service.getServiceTypes();
        if(serviceTypes != null) {
            response.setServiceTypes(serviceTypes.stream().map(serviceType -> this.constructServiceTypeResponse(serviceType)).collect(Collectors.toList()));
        }
        return response;
    }

    public AppointmentServiceDefaultResponse constructDefaultResponse(AppointmentServiceDefinition appointmentServiceDefinition){
        return mapToDefaultResponse(appointmentServiceDefinition, new AppointmentServiceDefaultResponse());
    }
    
    private AppointmentServiceDefaultResponse mapToDefaultResponse(AppointmentServiceDefinition as, AppointmentServiceDefaultResponse asResponse) {
        asResponse.setUuid(as.getUuid());
        asResponse.setAppointmentServiceId(as.getAppointmentServiceId());
        asResponse.setName(as.getName());
        asResponse.setStartTime(convertTimeToString(as.getStartTime()));
        asResponse.setEndTime(convertTimeToString(as.getEndTime()));
        asResponse.setDescription(as.getDescription());
        asResponse.setDurationMins(as.getDurationMins());
        asResponse.setMaxAppointmentsLimit(as.getMaxAppointmentsLimit());
        asResponse.setColor(as.getColor());

        AppointmentStatus initialAppointmentStatus = as.getInitialAppointmentStatus();
        if (null != initialAppointmentStatus){
            asResponse.setInitialAppointmentStatus(initialAppointmentStatus.name());
        }

        Map specialityMap = new HashMap();
        Speciality speciality = as.getSpeciality();
        if(speciality != null) {
            specialityMap.put("name", speciality.getName());
            specialityMap.put("uuid", speciality.getUuid());
        }
        asResponse.setSpeciality(specialityMap);

        Map locationMap = new HashMap();
        Location location = as.getLocation();
        if(location != null) {
            locationMap.put("name", location.getName());
            locationMap.put("uuid", location.getUuid());
        }
        asResponse.setLocation(locationMap);

        Set<AppointmentServiceAttribute> attributes = as.getAttributes();
        if(attributes != null && !attributes.isEmpty()) {
            List<AppointmentServiceAttributeResponse> attributeResponses = attributes.stream()
                    .filter(attr -> !attr.getVoided())
                    .map(attr -> constructAttributeResponse(attr))
                    .collect(Collectors.toList());
            asResponse.setAttributes(attributeResponses);
        }

        return asResponse;
    }

    private Map constructAvailabilityResponse(ServiceWeeklyAvailability availability) {
        Map availabilityMap = new HashMap();
        availabilityMap.put("dayOfWeek",availability.getDayOfWeek());
        availabilityMap.put("startTime", convertTimeToString(availability.getStartTime()));
        availabilityMap.put("endTime", convertTimeToString(availability.getEndTime()));
        availabilityMap.put("maxAppointmentsLimit", availability.getMaxAppointmentsLimit());
        availabilityMap.put("uuid", availability.getUuid());
        return availabilityMap;
    }

    private String convertTimeToString(Time time) {
        if (time == null) {
            return new String();
        }

        // Use today's date for the returned time so that hour is adjusted considering daylight saving time
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, time.getHours());
        calendar.set(Calendar.MINUTE, time.getMinutes());
        calendar.set(Calendar.SECOND, time.getSeconds());
        calendar.set(Calendar.MILLISECOND, 0);

       return calendar.toInstant().toString();
    }

    private Time utcTimeToServerTime(Time time) {
        if (time == null) {
            return null;
        }
        Calendar serviceEndTimeUtc = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        serviceEndTimeUtc.set(Calendar.HOUR_OF_DAY, time.getHours());
        serviceEndTimeUtc.set(Calendar.MINUTE, time.getMinutes());
        serviceEndTimeUtc.set(Calendar.SECOND, time.getSeconds());
        serviceEndTimeUtc.set(Calendar.MILLISECOND, 0);
        return new Time(serviceEndTimeUtc.getTime().getHours(), serviceEndTimeUtc.getTime().getMinutes(), serviceEndTimeUtc.getTime().getSeconds());
    }

    private AppointmentServiceAttributeResponse constructAttributeResponse(AppointmentServiceAttribute attribute) {
        AppointmentServiceAttributeResponse response = new AppointmentServiceAttributeResponse();
        response.setUuid(attribute.getUuid());
        response.setAttributeType(attribute.getAttributeType().getName());
        response.setAttributeTypeUuid(attribute.getAttributeType().getUuid());

        try {
            Object value = attribute.getAttributeType().getDatatypeClassname() != null
                    ? attribute.getValue()
                    : attribute.getValueReference();
            response.setValue(value);
        } catch (Exception e) {
            response.setValue(attribute.getValueReference());
        }

        return response;
    }

    public AppointmentServiceAttributeTypeResponse constructAttributeTypeResponse(AppointmentServiceAttributeType attributeType) {
        AppointmentServiceAttributeTypeResponse response = new AppointmentServiceAttributeTypeResponse();
        response.setUuid(attributeType.getUuid());
        response.setName(attributeType.getName());
        response.setDescription(attributeType.getDescription());
        response.setDatatype(attributeType.getDatatypeClassname());
        response.setMinOccurs(attributeType.getMinOccurs());
        response.setMaxOccurs(attributeType.getMaxOccurs());
        response.setRetired(attributeType.getRetired());
        return response;
    }

    public List<AppointmentServiceAttributeTypeResponse> constructAttributeTypeListResponse(List<AppointmentServiceAttributeType> attributeTypes) {
        return attributeTypes.stream()
                .map(this::constructAttributeTypeResponse)
                .collect(Collectors.toList());
    }
}
