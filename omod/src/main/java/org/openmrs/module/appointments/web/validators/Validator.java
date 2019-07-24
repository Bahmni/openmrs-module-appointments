package org.openmrs.module.appointments.web.validators;

import java.util.ArrayList;
import java.util.List;

public interface Validator<T> {

    boolean validate(T objectToValidate);

    String getError();
}
