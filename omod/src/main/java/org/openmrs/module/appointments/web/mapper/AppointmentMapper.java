package org.openmrs.module.appointments.web.mapper;

import java.util.*;

import org.apache.commons.lang.StringUtils;
import org.openmrs.Location;
import org.openmrs.Patient;
import org.openmrs.Provider;
import org.openmrs.api.LocationService;
import org.openmrs.api.PatientService;
import org.openmrs.api.ProviderService;
import org.openmrs.module.appointments.model.*;
import org.openmrs.module.appointments.service.AppointmentServiceDefinitionService;
import org.openmrs.module.appointments.service.AppointmentsService;
import org.openmrs.module.appointments.web.contract.AppointmentDefaultResponse;
import org.openmrs.module.appointments.web.contract.AppointmentPayload;
import org.openmrs.module.appointments.web.contract.AppointmentProviderDetail;
import org.openmrs.module.appointments.web.contract.AppointmentQuery;
import org.openmrs.module.appointments.web.extension.AppointmentResponseExtension;
import org.openmrs.module.webservices.rest.web.response.ConversionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class AppointmentMapper {
    @Autowired
    LocationService locationService;

    @Autowired
    ProviderService providerService;

    @Autowired
    PatientService patientService;

    @Autowired
    AppointmentServiceDefinitionService appointmentServiceDefinitionService;

    @Autowired
    AppointmentServiceMapper appointmentServiceMapper;

    @Autowired
    AppointmentsService appointmentsService;

    @Autowired(required = false)
    AppointmentResponseExtension appointmentResponseExtension;

    public List<AppointmentDefaultResponse> constructResponse(List<Appointment> appointments) {
        return appointments.stream().map(as -> this.mapToDefaultResponse(as, new AppointmentDefaultResponse())).collect(Collectors.toList());
    }

    public AppointmentDefaultResponse constructResponse(Appointment appointment) {
        return this.mapToDefaultResponse(appointment, new AppointmentDefaultResponse());
    }

    public Appointment getAppointmentFromPayload(AppointmentPayload appointmentPayload) {
        Appointment appointment;
        if (!StringUtils.isBlank(appointmentPayload.getUuid())) {
            appointment = appointmentsService.getAppointmentByUuid(appointmentPayload.getUuid());
        } else {
            appointment = new Appointment();
            appointment.setPatient(patientService.getPatientByUuid(appointmentPayload.getPatientUuid()));
        }
        AppointmentServiceDefinition appointmentServiceDefinition = appointmentServiceDefinitionService.getAppointmentServiceByUuid(appointmentPayload.getServiceUuid());
        AppointmentServiceType appointmentServiceType = null;
        if(appointmentPayload.getServiceTypeUuid() != null) {
            appointmentServiceType = getServiceTypeByUuid(appointmentServiceDefinition.getServiceTypes(true), appointmentPayload.getServiceTypeUuid());
        }
        appointment.setServiceType(appointmentServiceType);
        appointment.setService(appointmentServiceDefinition);
        appointment.setProvider(providerService.getProviderByUuid(appointmentPayload.getProviderUuid()));
        appointment.setLocation(locationService.getLocationByUuid(appointmentPayload.getLocationUuid()));
        appointment.setStartDateTime(appointmentPayload.getStartDateTime());
        appointment.setEndDateTime(appointmentPayload.getEndDateTime());
        appointment.setAppointmentKind(AppointmentKind.valueOf(appointmentPayload.getAppointmentKind()));
        appointment.setComments(appointmentPayload.getComments());
        mapProvidersForAppointment(appointment, appointmentPayload.getProviders());
        return appointment;
    }

    private void mapProvidersForAppointment(Appointment appointment, List<AppointmentProviderDetail> newProviders) {
        Set<AppointmentProvider> existingProviders = appointment.getProviders();

        if (existingProviders != null) {
            for (AppointmentProvider appointmentProvider : existingProviders) {
                boolean exists = newProviders == null ? false :
                        newProviders.stream().anyMatch(p -> p.getUuid().equals(appointmentProvider.getProvider().getUuid()));
                if (!exists) {
                    appointmentProvider.setResponse(AppointmentProviderResponse.CANCELLED);
                    appointmentProvider.setVoided(true);
                    appointmentProvider.setVoidReason(AppointmentProviderResponse.CANCELLED.toString());
                }
            }
        }

        if (newProviders != null && !newProviders.isEmpty()) {
            if (appointment.getProviders() == null ) {
                appointment.setProviders(new HashSet<>());
            }
            for (AppointmentProviderDetail providerDetail : newProviders) {
                List<AppointmentProvider> providers = appointment.getProviders().stream().filter(p -> p.getProvider().getUuid().equals(providerDetail.getUuid())).collect(Collectors.toList());
                if (providers.isEmpty()) {
                    AppointmentProvider newAppointmentProvider = createNewAppointmentProvider(providerDetail);
                    newAppointmentProvider.setAppointment(appointment);
                    appointment.getProviders().add(newAppointmentProvider);
                } else {
                    providers.forEach(p -> {
                        p.setResponse(withResponse(providerDetail.getResponse()));
                    });
                }
            }
        }

    }

    private AppointmentProvider createNewAppointmentProvider(AppointmentProviderDetail providerDetail) {
        Provider provider = providerService.getProviderByUuid(providerDetail.getUuid());
        if (provider == null) {
            throw new ConversionException("Bad Request. No such provider.");
        }
        AppointmentProvider appointmentProvider = new AppointmentProvider();
        appointmentProvider.setProvider(provider);
        appointmentProvider.setResponse(withResponse(providerDetail.getResponse()));
        appointmentProvider.setComments(providerDetail.getComments());
        return appointmentProvider;
    }

    private AppointmentProviderResponse withResponse(String response) {
        String namedEnum = StringUtils.isEmpty(response) ? AppointmentProviderResponse.ACCEPTED.toString()  : response.toUpperCase();
        //TODO: validation if not valid enum string
        return AppointmentProviderResponse.valueOf(namedEnum);
    }


    private AppointmentServiceType getServiceTypeByUuid(Set<AppointmentServiceType> serviceTypes, String serviceTypeUuid) {
        return serviceTypes.stream()
                .filter(avb -> avb.getUuid().equals(serviceTypeUuid)).findAny().get();
    }

    public Appointment mapQueryToAppointment(AppointmentQuery searchQuery) {
        Appointment appointment = new Appointment();
        appointment.setService(
                appointmentServiceDefinitionService.getAppointmentServiceByUuid(searchQuery.getServiceUuid()));
        appointment.setPatient(patientService.getPatientByUuid(searchQuery.getPatientUuid()));
        appointment.setProvider(providerService.getProviderByUuid(searchQuery.getProviderUuid()));
        appointment.setLocation(locationService.getLocationByUuid(searchQuery.getLocationUuid()));
        if (searchQuery.getStatus() != null) {
            appointment.setStatus(AppointmentStatus.valueOf(searchQuery.getStatus()));
        }
        return appointment;
    }

    private AppointmentDefaultResponse mapToDefaultResponse(Appointment a, AppointmentDefaultResponse response) {
        response.setUuid(a.getUuid());
        response.setAppointmentNumber(a.getAppointmentNumber());
        response.setPatient(createPatientMap(a.getPatient()));
        response.setService(appointmentServiceMapper.constructDefaultResponse(a.getService()));
        response.setServiceType(createServiceTypeMap(a.getServiceType()));
        response.setProvider(createProviderMap(a.getProvider()));
        response.setLocation(createLocationMap(a.getLocation()));
        response.setStartDateTime(a.getStartDateTime());
        response.setEndDateTime(a.getEndDateTime());
        response.setAppointmentKind(a.getAppointmentKind().name());
        response.setStatus(a.getStatus().name());
        response.setComments(a.getComments());
        if(appointmentResponseExtension!=null)
            response.setAdditionalInfo(appointmentResponseExtension.run(a));
        response.setProviders(mapAppointmentProviders(a.getProviders()));
        return response;
    }

    private List<AppointmentProviderDetail> mapAppointmentProviders(Set<AppointmentProvider> providers) {
        List<AppointmentProviderDetail> providerDetailList = new ArrayList<>();
        if (providers != null) {
            for (AppointmentProvider apptProviderAssociation : providers) {
                AppointmentProviderDetail apptProvider = new AppointmentProviderDetail();
                apptProvider.setUuid(apptProviderAssociation.getProvider().getUuid());
                apptProvider.setComments(apptProviderAssociation.getComments());
                apptProvider.setResponse(apptProviderAssociation.getResponse().toString());
                apptProvider.setName(apptProviderAssociation.getProvider().getName());
                providerDetailList.add(apptProvider);
            }
        }
        return providerDetailList;
    }

    private Map createServiceTypeMap(AppointmentServiceType s) {
        Map serviceTypeMap = null;
        if (s != null) {
            serviceTypeMap = new HashMap();
            serviceTypeMap.put("name", s.getName());
            serviceTypeMap.put("uuid", s.getUuid());
            serviceTypeMap.put("duration", s.getDuration());
        }
        return serviceTypeMap;
    }
    
    private Map createProviderMap(Provider p) {
        Map providerMap = null;
        if (p != null) {
            providerMap = new HashMap();
            providerMap.put("name", p.getName());
            providerMap.put("uuid", p.getUuid());
        }
        return providerMap;
    }

    private Map createLocationMap(Location l) {
        Map locationMap = null;
        if (l != null) {
            locationMap = new HashMap();
            locationMap.put("name", l.getName());
            locationMap.put("uuid", l.getUuid());
        }
        return locationMap;
    }

    private Map createPatientMap(Patient p) {
        Map map = new HashMap();
        map.put("name", p.getPersonName().getFullName());
        map.put("uuid", p.getUuid());
        map.put("identifier", p.getPatientIdentifier().getIdentifier());
        return map;
    }

    public AppointmentProvider mapAppointmentProvider(AppointmentProviderDetail providerResponse) {
        AppointmentProvider appointmentProvider = new AppointmentProvider();
        appointmentProvider.setProvider(providerService.getProviderByUuid(providerResponse.getUuid()));
        appointmentProvider.setResponse(withResponse(providerResponse.getResponse()));
        appointmentProvider.setComments(providerResponse.getComments());
        return appointmentProvider;
    }
}
