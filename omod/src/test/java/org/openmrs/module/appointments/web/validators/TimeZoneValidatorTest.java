package org.openmrs.module.appointments.web.validators;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;

import static org.junit.Assert.assertEquals;

public class TimeZoneValidatorTest {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();
    private TimeZoneValidator timeZoneValidator = new TimeZoneValidator();
    private Errors errors;
    private String timeZone;

    @Before
    public void setup() throws Exception {
        timeZone = null;
        errors = new BeanPropertyBindingResult(timeZone, "timeZone");
    }

    private String getExceptionMessage(){
        return "Time Zone is missing";
    }

    @Test
    public void shouldAddToErrorsWhenTimeZoneIsNull() {
        timeZone = null;
        timeZoneValidator.validate(timeZone, errors);

        assertEquals(errors.getAllErrors().size(), 1);
        assertEquals(getExceptionMessage(), errors.getAllErrors().get(0).getDefaultMessage());
        assertEquals("missing.timeZone", errors.getAllErrors().get(0).getCodes()[0]);
    }

    @Test
    public void shouldNotThrowAnExceptionForValidData() {
        timeZone = "Asia/Calcutta";
        timeZoneValidator.validate(timeZone, errors);

        assertEquals(errors.getAllErrors().size(), 0);
    }
}
