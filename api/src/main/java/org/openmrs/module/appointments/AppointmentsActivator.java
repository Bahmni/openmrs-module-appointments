/**
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */
package org.openmrs.module.appointments;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.context.Context;
import org.openmrs.module.BaseModuleActivator;
import org.openmrs.module.appointments.properties.AppointmentProperties;

import java.util.List;

/**
 * This class contains the logic that is run every time this module is either started or shutdown
 */
public class AppointmentsActivator extends BaseModuleActivator {
	
	private Log log = LogFactory.getLog(this.getClass());

	@Override
	public void willStart() {
		log.info("Appointments Module Starting");
		AppointmentProperties.load();
	}

	@Override
	public void started() {
		for (AppointmentsActivatorComponent c : getAppointmentsActivatorComponents()) {
			c.started();
		}
		log.info("Appointments Module Started");
	}

	@Override
	public void willStop() {
		log.info("Appointments Module Stopping");
		for (AppointmentsActivatorComponent c : getAppointmentsActivatorComponents()) {
			c.willStop();
		}
	}

	@Override
	public void stopped() {
		log.info("Appointments Module Stopped");
	}

	protected List<AppointmentsActivatorComponent> getAppointmentsActivatorComponents() {
		return Context.getRegisteredComponents(AppointmentsActivatorComponent.class);
	}
	
}
