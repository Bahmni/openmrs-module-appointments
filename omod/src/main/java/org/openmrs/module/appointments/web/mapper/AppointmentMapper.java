package org.openmrs.module.appointments.web.mapper;

import org.apache.commons.lang.StringUtils;
import org.openmrs.Location;
import org.openmrs.Patient;
import org.openmrs.Provider;
import org.openmrs.api.LocationService;
import org.openmrs.api.PatientService;
import org.openmrs.api.ProviderService;
import org.openmrs.module.appointments.model.Appointment;
import org.openmrs.module.appointments.model.AppointmentKind;
import org.openmrs.module.appointments.model.AppointmentProvider;
import org.openmrs.module.appointments.model.AppointmentProviderResponse;
import org.openmrs.module.appointments.model.AppointmentServiceDefinition;
import org.openmrs.module.appointments.model.AppointmentServiceType;
import org.openmrs.module.appointments.model.AppointmentStatus;
import org.openmrs.module.appointments.service.AppointmentServiceDefinitionService;
import org.openmrs.module.appointments.service.AppointmentsService;
import org.openmrs.module.appointments.web.contract.AppointmentDefaultResponse;
import org.openmrs.module.appointments.web.contract.AppointmentProviderDetail;
import org.openmrs.module.appointments.web.contract.AppointmentQuery;
import org.openmrs.module.appointments.web.contract.AppointmentRequest;
import org.openmrs.module.appointments.web.extension.AppointmentResponseExtension;
import org.openmrs.module.webservices.rest.web.response.ConversionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
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

    public Appointment fromRequest(AppointmentRequest appointmentRequest) {
        Appointment appointment;
        if (!StringUtils.isBlank(appointmentRequest.getUuid())) {
            appointment = appointmentsService.getAppointmentByUuid(appointmentRequest.getUuid());
        } else {
            appointment = new Appointment();
            appointment.setPatient(patientService.getPatientByUuid(appointmentRequest.getPatientUuid()));
        }
        mapAppointmentRequestToAppointment(appointmentRequest, appointment);
        return appointment;
    }

    public Appointment fromRequestClonedAppointment(AppointmentRequest appointmentRequest) {
        Appointment appointment = new Appointment();
        appointment.setUuid(appointmentRequest.getUuid());
        appointment.setPatient(patientService.getPatientByUuid(appointmentRequest.getPatientUuid()));
        mapAppointmentRequestToAppointment(appointmentRequest, appointment);
        return appointment;
    }

    public void mapAppointmentRequestToAppointment(AppointmentRequest appointmentRequest, Appointment appointment) {
        AppointmentServiceDefinition appointmentServiceDefinition = appointmentServiceDefinitionService.getAppointmentServiceByUuid(appointmentRequest.getServiceUuid());
        AppointmentServiceType appointmentServiceType = null;
        if (appointmentRequest.getServiceTypeUuid() != null) {
            appointmentServiceType = getServiceTypeByUuid(appointmentServiceDefinition.getServiceTypes(true), appointmentRequest.getServiceTypeUuid());
        }
        appointment.setServiceType(appointmentServiceType);
        appointment.setService(appointmentServiceDefinition);
        //appointment.setProvider(identifyAppointmentProvider(appointmentRequest.getProviderUuid()));
        appointment.setLocation(identifyAppointmentLocation(appointmentRequest.getLocationUuid()));
        appointment.setStartDateTime(appointmentRequest.getStartDateTime());
        appointment.setEndDateTime(appointmentRequest.getEndDateTime());
        appointment.setAppointmentKind(AppointmentKind.valueOf(appointmentRequest.getAppointmentKind()));
        appointment.setComments(appointmentRequest.getComments());
        mapProvidersForAppointment(appointment, appointmentRequest.getProviders());
    }

    private Provider identifyAppointmentProvider(String providerUuid) {
        return providerService.getProviderByUuid(providerUuid);
    }

    private Location identifyAppointmentLocation(String locationUuid) {
        return locationService.getLocationByUuid(locationUuid);
    }

    private void mapProvidersForAppointment(Appointment appointment, List<AppointmentProviderDetail> newProviders) {
        Set<AppointmentProvider> existingProviders = appointment.getProviders();

        if (existingProviders != null) {
            for (AppointmentProvider appointmentProvider : existingProviders) {
                boolean exists = newProviders != null && newProviders.stream().anyMatch(p -> p.getUuid().equals(appointmentProvider.getProvider().getUuid()));
                if (!exists) {
                    appointmentProvider.setResponse(AppointmentProviderResponse.CANCELLED);
                    appointmentProvider.setVoided(true);
                    appointmentProvider.setVoidReason(AppointmentProviderResponse.CANCELLED.toString());
                }
            }
        }

        if (newProviders != null && !newProviders.isEmpty()) {
            if (appointment.getProviders() == null) {
                appointment.setProviders(new HashSet<>());
            }
            for (AppointmentProviderDetail providerDetail : newProviders) {
                List<AppointmentProvider> providers = appointment.getProviders().stream().filter(p -> p.getProvider().getUuid().equals(providerDetail.getUuid())).collect(Collectors.toList());
                if (providers.isEmpty()) {
                    AppointmentProvider newAppointmentProvider = createNewAppointmentProvider(providerDetail);
                    newAppointmentProvider.setAppointment(appointment);
                    appointment.getProviders().add(newAppointmentProvider);
                } else {
                    providers.forEach(existingAppointmentProvider -> {
                        //TODO: if currentUser is same person as provider, set ACCEPTED
                        existingAppointmentProvider.setResponse(mapProviderResponse(providerDetail.getResponse()));
                        existingAppointmentProvider.setVoided(Boolean.FALSE);
                        existingAppointmentProvider.setVoidReason(null);
                    });
                }
            }
        }

    }

    private AppointmentProvider createNewAppointmentProvider(AppointmentProviderDetail providerDetail) {
        Provider provider = identifyAppointmentProvider(providerDetail.getUuid());
        if (provider == null) {
            throw new ConversionException("Bad Request. No such provider.");
        }
        AppointmentProvider appointmentProvider = new AppointmentProvider();
        appointmentProvider.setProvider(provider);
        appointmentProvider.setResponse(mapProviderResponse(providerDetail.getResponse()));
        appointmentProvider.setComments(providerDetail.getComments());
        return appointmentProvider;
    }

    public AppointmentProviderResponse mapProviderResponse(String response) {
        String namedEnum = StringUtils.isEmpty(response) ? AppointmentProviderResponse.ACCEPTED.toString() : response.toUpperCase();
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
        appointment.setProvider(identifyAppointmentProvider(searchQuery.getProviderUuid()));
        appointment.setLocation(identifyAppointmentLocation(searchQuery.getLocationUuid()));
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
        //response.setProvider(createProviderMap(a.getProvider()));
        response.setLocation(createLocationMap(a.getLocation()));
        response.setStartDateTime(a.getStartDateTime());
        response.setEndDateTime(a.getEndDateTime());
        response.setAppointmentKind(a.getAppointmentKind().name());
        response.setStatus(a.getStatus().name());
        response.setComments(a.getComments());
        if (appointmentResponseExtension != null)
            response.setAdditionalInfo(appointmentResponseExtension.run(a));
        response.setProviders(mapAppointmentProviders(a.getProviders()));
        response.setRecurring(a.isRecurring());
        return response;
    }

    private List<AppointmentProviderDetail> mapAppointmentProviders(Set<AppointmentProvider> providers) {
        List<AppointmentProviderDetail> providerList = new ArrayList<>();
        providers = getNonVoidedProviders(providers);
        if (providers != null) {
            for (AppointmentProvider apptProviderAssociation : providers) {
                AppointmentProviderDetail providerDetail = new AppointmentProviderDetail();
                providerDetail.setUuid(apptProviderAssociation.getProvider().getUuid());
                providerDetail.setComments(apptProviderAssociation.getComments());
                providerDetail.setResponse(apptProviderAssociation.getResponse().toString());
                providerDetail.setName(apptProviderAssociation.getProvider().getName());
                providerList.add(providerDetail);
            }
        }
        return providerList;
    }

    private Set<AppointmentProvider> getNonVoidedProviders(Set<AppointmentProvider> providers) {
        if (providers == null || providers.isEmpty()) {
            return providers;
        }
        providers = providers
                .stream()
                .filter(provider -> provider.getVoided() != Boolean.TRUE).collect(Collectors.toSet());
        return providers;
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

    public AppointmentProvider mapAppointmentProvider(AppointmentProviderDetail providerDetail) {
        AppointmentProvider appointmentProvider = new AppointmentProvider();
        appointmentProvider.setProvider(identifyAppointmentProvider(providerDetail.getUuid()));
        appointmentProvider.setResponse(mapProviderResponse(providerDetail.getResponse()));
        appointmentProvider.setComments(providerDetail.getComments());
        return appointmentProvider;
    }

    public Map<String, List<AppointmentDefaultResponse>> constructConflictResponse(Map<Enum, List<Appointment>> conflicts) {
        Map<String, List<AppointmentDefaultResponse>> conflictsResponse = new HashMap<>();
        for (Map.Entry<Enum, List<Appointment>> entry : conflicts.entrySet()) {
            conflictsResponse.put(entry.getKey().name(), constructResponse(entry.getValue()));
        }
        return conflictsResponse;
    }
}
