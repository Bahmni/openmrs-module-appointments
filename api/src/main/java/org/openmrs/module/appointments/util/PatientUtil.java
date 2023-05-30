package org.openmrs.module.appointments.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.openmrs.Patient;
import org.openmrs.PersonAttribute;

public class PatientUtil {

    private static final String IDENTIFIER_NAME = "identifierName";
    private static final String IDENTIFIER = "identifier";
    private static final String TELEPHONE_CONTACT = "Telephone contact";

    public static Map<String, Object> patientMap(Patient patient) {
        Map<String, Object> map = new HashMap<>();
        try {
            if (Objects.nonNull(patient)) {
                if (Objects.nonNull(patient.getPersonName())) {
                    map.put("name", patient.getPersonName().getFullName());
                }
                map.put("uuid", patient.getUuid());

                List<Map<String, String>> identifierList = new ArrayList<>();
                patient.getIdentifiers().stream()
                        .filter(Objects::nonNull)
                        .forEach(patientIdentifier -> {
                            Map<String, String> identifierMap = new HashMap<>();
                            identifierMap.put(IDENTIFIER_NAME, patientIdentifier.getIdentifierType().getName());
                            identifierMap.put(IDENTIFIER, patientIdentifier.getIdentifier());
                            identifierList.add(identifierMap);
                        });
                map.put("identifiers", identifierList);
                map.put("gender", patient.getGender());

                PersonAttribute patientPhoneAttribute = patient.getPerson().getAttribute(TELEPHONE_CONTACT);
                String phoneNumber = Objects.nonNull(patientPhoneAttribute) ? patientPhoneAttribute.getValue() : "";
                map.put("phoneNumber", phoneNumber);
                map.put("dob", patient.getBirthdate());
                map.put("age", patient.getAge());
            }
        } catch (Exception e) {
            e.printStackTrace();
            // Log the exception here
        }

        return map;
    }

}
