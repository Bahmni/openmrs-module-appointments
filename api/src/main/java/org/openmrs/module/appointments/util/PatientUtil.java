package org.openmrs.module.appointments.util;

import java.util.HashMap;
import java.util.Map;

import org.openmrs.Patient;
import org.openmrs.PatientIdentifier;
import org.openmrs.PersonAttribute;

public class PatientUtil {

    public static Map patientMap(Patient p) {
        Map map = new HashMap();
        map.put("name", p.getPersonName().getFullName());
        map.put("uuid", p.getUuid());
        for (PatientIdentifier patientIdentifier : p.getIdentifiers()) {
            if (patientIdentifier.getIdentifierType().getName().equalsIgnoreCase("Unique Patient Number")) {
                map.put("identifier", patientIdentifier.getIdentifier());
            } else if (patientIdentifier.getIdentifierType().getName().equalsIgnoreCase("Patient Clinic Number")) {
                map.put("identifier", patientIdentifier.getIdentifier());
            }
        }
        map.put("gender", p.getGender());
        PersonAttribute patientPhoneAttribute = p.getPerson().getAttribute("Telephone contact");
        if (patientPhoneAttribute != null) {
            String phone = patientPhoneAttribute.getValue();
            map.put("phoneNumber", phone);
        } else {
            map.put("phoneNumber", "");
        }
        map.put("dob", p.getBirthdate());
        map.put("age", p.getAge());
        return map;
    }
}
