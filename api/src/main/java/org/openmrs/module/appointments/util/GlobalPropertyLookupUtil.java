package org.openmrs.module.appointments.util;

import org.openmrs.api.context.Context;
import org.openmrs.util.PrivilegeConstants;

import java.util.function.Supplier;

public final class GlobalPropertyLookupUtil {

    private GlobalPropertyLookupUtil() {
    }

    /**
     * Since OpenMRS 2.6.11, reading a global property requires the Get Global
     * Properties privilege. This proxies that privilege for the duration of
     * {@code globalPropertyRead}, so user-facing request paths work for users
     * who hold only appointment privileges.
     */
    public static <T> T withGlobalPropertyPrivilege(Supplier<T> globalPropertyRead) {
        Context.addProxyPrivilege(PrivilegeConstants.GET_GLOBAL_PROPERTIES);
        try {
            return globalPropertyRead.get();
        } finally {
            Context.removeProxyPrivilege(PrivilegeConstants.GET_GLOBAL_PROPERTIES);
        }
    }
}
