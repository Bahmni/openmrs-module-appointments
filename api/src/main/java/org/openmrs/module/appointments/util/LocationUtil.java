package org.openmrs.module.appointments.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Location;

import java.time.DateTimeException;
import java.time.ZoneId;
import java.util.Optional;

public class LocationUtil {

    private static final Log log = LogFactory.getLog(LocationUtil.class);
    private static final String TIMEZONE_ATTRIBUTE = "timeZone";

    public static Optional<ZoneId> getLocationZone(Location location) {
        if (location == null) return Optional.empty();
        return location.getActiveAttributes().stream()
                .filter(attr -> TIMEZONE_ATTRIBUTE.equalsIgnoreCase(attr.getAttributeType().getName()))
                .map(attr -> {
                    String zoneId = String.valueOf(attr.getValue());
                    try {
                        return Optional.of(ZoneId.of(zoneId));
                    } catch (DateTimeException e) {
                        log.error("Invalid timezone '" + zoneId
                                + "' configured for location '" + location.getName() + "'", e);
                        return Optional.<ZoneId>empty();
                    }
                })
                .filter(Optional::isPresent)
                .map(Optional::get)
                .findFirst();
    }
}
