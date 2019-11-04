package org.openmrs.module.appointments.web.validators;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.openmrs.module.appointments.web.contract.RecurringPattern;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;

public class RecurringPatternValidatorTest {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();
    private RecurringPatternValidator recurringPatternValidator = new RecurringPatternValidator();
    private Errors errors;
    private RecurringPattern recurringPattern;

    private RecurringPattern getRecurringPattern() {
        RecurringPattern recurringPattern = new RecurringPattern();
        recurringPattern.setType("DAY");
        recurringPattern.setPeriod(1);
        recurringPattern.setFrequency(1);
        return recurringPattern;
    }

    @Before
    public void setUp() throws Exception {
        recurringPattern = getRecurringPattern();
        errors = new BeanPropertyBindingResult(recurringPattern, "recurringPattern");
    }

    private String getExceptionMessage() {
        return "type should be DAY/WEEK\n" +
                "period and frequency/endDate are mandatory if type is DAY\n" +
                "daysOfWeek, period and frequency/endDate are mandatory if type is WEEK";
    }


    @Test
    public void shouldAddToErrorsWhenTypeIsNullInRecurringPattern() {
        recurringPattern.setType(null);
        errors = new BeanPropertyBindingResult(recurringPattern, "recurringPattern");

        recurringPatternValidator.validate(recurringPattern, errors);

        assertEquals(errors.getAllErrors().size(), 1);
        assertEquals(getExceptionMessage(), errors.getAllErrors().get(0).getDefaultMessage());
        assertEquals("invalid.recurringPattern", errors.getAllErrors().get(0).getCodes()[0]);
    }

    @Test
    public void shouldAddToErrorsWhenTypeIsEmptyInRecurringPattern() {
        recurringPattern.setType("");

        recurringPatternValidator.validate(recurringPattern, errors);

        assertEquals(errors.getAllErrors().size(), 1);
        assertEquals(getExceptionMessage(), errors.getAllErrors().get(0).getDefaultMessage());
        assertEquals("invalid.recurringPattern", errors.getAllErrors().get(0).getCodes()[0]);
    }

    @Test
    public void shouldAddToErrorsWhenInvalidType() {
        recurringPattern.setType("TEXT");
        recurringPattern.setDaysOfWeek(null);

        recurringPatternValidator.validate(recurringPattern, errors);

        assertEquals(errors.getAllErrors().size(), 1);
        assertEquals(getExceptionMessage(), errors.getAllErrors().get(0).getDefaultMessage());
        assertEquals("invalid.recurringPattern", errors.getAllErrors().get(0).getCodes()[0]);
    }

    @Test
    public void shouldAddToErrorsWhenPeriodIsLessThanOneInRecurringPattern() {
        recurringPattern.setPeriod(0);

        recurringPatternValidator.validate(recurringPattern, errors);

        assertEquals(errors.getAllErrors().size(), 1);
        assertEquals(getExceptionMessage(), errors.getAllErrors().get(0).getDefaultMessage());
        assertEquals("invalid.recurringPattern", errors.getAllErrors().get(0).getCodes()[0]);
    }

    @Test
    public void shouldAddToErrorsWhenFrequencyIsLessThanOneInRecurringPattern() {
        recurringPattern.setFrequency(0);

        recurringPatternValidator.validate(recurringPattern, errors);

        assertEquals(errors.getAllErrors().size(), 1);
        assertEquals(getExceptionMessage(), errors.getAllErrors().get(0).getDefaultMessage());
        assertEquals("invalid.recurringPattern", errors.getAllErrors().get(0).getCodes()[0]);
    }

    @Test
    public void shouldAddToErrorsWhenFrequencyAndEndDateAreNullInRecurringPattern() {
        recurringPattern.setFrequency(null);

        recurringPatternValidator.validate(recurringPattern, errors);

        assertEquals(errors.getAllErrors().size(), 1);
        assertEquals(getExceptionMessage(), errors.getAllErrors().get(0).getDefaultMessage());
        assertEquals("invalid.recurringPattern", errors.getAllErrors().get(0).getCodes()[0]);
    }

    @Test
    public void shouldAddToErrorsWhenEndDateIsNull() {
        recurringPattern.setEndDate(null);
        recurringPattern.setFrequency(0);

        recurringPatternValidator.validate(recurringPattern, errors);

        assertEquals(errors.getAllErrors().size(), 1);
        assertEquals(getExceptionMessage(), errors.getAllErrors().get(0).getDefaultMessage());
        assertEquals("invalid.recurringPattern", errors.getAllErrors().get(0).getCodes()[0]);
    }

    @Test
    public void shouldAddToErrorsWhenWeekTypeWithEmptyDays() {
        recurringPattern.setType("WEEk");
        recurringPattern.setEndDate(null);
        recurringPattern.setFrequency(2);
        recurringPattern.setPeriod(2);
        recurringPattern.setDaysOfWeek(Arrays.asList());

        recurringPatternValidator.validate(recurringPattern, errors);

        assertEquals(errors.getAllErrors().size(), 1);
        assertEquals(getExceptionMessage(), errors.getAllErrors().get(0).getDefaultMessage());
        assertEquals("invalid.recurringPattern", errors.getAllErrors().get(0).getCodes()[0]);
    }

    @Test
    public void shouldAddToErrorsWhenWeekTypeWithNoDays() {
        recurringPattern.setType("WEEk");
        recurringPattern.setEndDate(null);
        recurringPattern.setFrequency(2);
        recurringPattern.setPeriod(2);
        recurringPattern.setDaysOfWeek(null);

        recurringPatternValidator.validate(recurringPattern, errors);

        assertEquals(errors.getAllErrors().size(), 1);
        assertEquals(getExceptionMessage(), errors.getAllErrors().get(0).getDefaultMessage());
        assertEquals("invalid.recurringPattern", errors.getAllErrors().get(0).getCodes()[0]);
    }

    @Test
    public void shouldAddToErrorsWhenWeekTypeWithInvalidDays() {
        recurringPattern.setType("WEEk");
        recurringPattern.setFrequency(2);
        recurringPattern.setPeriod(2);
        recurringPattern.setDaysOfWeek(Arrays.asList("TEXT", "MISC"));

        recurringPatternValidator.validate(recurringPattern, errors);

        assertEquals(errors.getAllErrors().size(), 1);
        assertEquals(getExceptionMessage(), errors.getAllErrors().get(0).getDefaultMessage());
        assertEquals("invalid.recurringPattern", errors.getAllErrors().get(0).getCodes()[0]);
    }

    @Test
    public void shouldAddToErrorsWhenWeekTypeWithNoPeriod() {
        recurringPattern.setType("WEEk");
        recurringPattern.setFrequency(2);
        recurringPattern.setPeriod(0);
        recurringPattern.setDaysOfWeek(Arrays.asList("SUNDAY", "MONDAY"));

        recurringPatternValidator.validate(recurringPattern, errors);

        assertEquals(errors.getAllErrors().size(), 1);
        assertEquals(getExceptionMessage(), errors.getAllErrors().get(0).getDefaultMessage());
        assertEquals("invalid.recurringPattern", errors.getAllErrors().get(0).getCodes()[0]);
    }

    @Test
    public void shouldAddToErrorsWhenWeekTypeWithNoFrequency() {
        recurringPattern.setType("WEEk");
        recurringPattern.setFrequency(0);
        recurringPattern.setPeriod(2);
        recurringPattern.setDaysOfWeek(Arrays.asList("SUNDAY", "MONDAY"));

        recurringPatternValidator.validate(recurringPattern, errors);

        assertEquals(errors.getAllErrors().size(), 1);
        assertEquals(getExceptionMessage(), errors.getAllErrors().get(0).getDefaultMessage());
        assertEquals("invalid.recurringPattern", errors.getAllErrors().get(0).getCodes()[0]);
    }

    @Test
    public void shouldNotThrowAnExceptionForValidData() {
        recurringPatternValidator.validate(recurringPattern, errors);

        assertEquals(errors.getAllErrors().size(), 0);
    }
}
