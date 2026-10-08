package org.openmrs.module.appointments.util;

import org.junit.jupiter.api.Test;
import org.openmrs.Location;
import org.openmrs.LocationAttribute;
import org.openmrs.LocationAttributeType;

import java.time.ZoneId;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import org.mockito.Mockito;

public class LocationUtilTest {

    private Location locationWithTimezone(String zoneId) {
        LocationAttributeType attrType = mock(LocationAttributeType.class);
        when(attrType.getName()).thenReturn("timeZone");
        LocationAttribute attr = mock(LocationAttribute.class);
        when(attr.getAttributeType()).thenReturn(attrType);
        when(attr.getValue()).thenReturn(zoneId);
        Location location = mock(Location.class);
        when(location.getActiveAttributes()).thenReturn(Collections.singletonList(attr));
        return location;
    }

    @Test
    public void shouldReturnEmptyWhenLocationIsNull() {
        Optional<ZoneId> result = LocationUtil.getLocationZone(null);
        assertFalse(result.isPresent());
    }

    @Test
    public void shouldReturnEmptyWhenLocationHasNoTimezoneAttribute() {
        Location location = mock(Location.class);
        when(location.getActiveAttributes()).thenReturn(Collections.emptyList());

        Optional<ZoneId> result = LocationUtil.getLocationZone(location);
        assertFalse(result.isPresent());
    }

    @Test
    public void shouldReturnZoneIdWhenLocationHasValidTimezoneAttribute() {
        Location location = locationWithTimezone("Asia/Kolkata");

        Optional<ZoneId> result = LocationUtil.getLocationZone(location);
        assertTrue(result.isPresent());
        assertEquals(ZoneId.of("Asia/Kolkata"), result.get());
    }

    @Test
    public void shouldReturnEmptyWhenTimezoneAttributeValueIsInvalid() {
        Location location = locationWithTimezone("Invalid/Zone");
        when(location.getName()).thenReturn("Test Location");

        Optional<ZoneId> result = LocationUtil.getLocationZone(location);
        assertFalse(result.isPresent());
    }
}
