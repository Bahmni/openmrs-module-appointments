package org.openmrs.module.fhirappnt.api;

import lombok.NoArgsConstructor;
import org.openmrs.module.fhir2.FhirConstants;

@NoArgsConstructor
public class AppointmentFhirConstants extends FhirConstants {

    public static final String APPOINTMENT_SPECIALITY_VALUESET_URI = HL7_FHIR_VALUE_SET_PREFIX
            + "/c80-practice-codes";

    public static final String APPOINTMENT_PARTICIPANT_TYPE = HL7_FHIR_VALUE_SET_PREFIX
            + "/encounter-participant-type";

}
