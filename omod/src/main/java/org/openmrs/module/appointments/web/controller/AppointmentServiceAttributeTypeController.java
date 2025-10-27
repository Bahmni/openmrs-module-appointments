package org.openmrs.module.appointments.web.controller;

import org.openmrs.module.appointments.model.AppointmentServiceAttributeType;
import org.openmrs.module.appointments.service.AppointmentServiceAttributeTypeService;
import org.openmrs.module.appointments.web.contract.AppointmentServiceAttributeTypeResponse;
import org.openmrs.module.appointments.web.mapper.AppointmentServiceMapper;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.v1_0.controller.BaseRestController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
@RequestMapping(value = "/rest/" + RestConstants.VERSION_1 + "/appointment-service-attribute-types")
public class AppointmentServiceAttributeTypeController extends BaseRestController {

    @Autowired
    private AppointmentServiceAttributeTypeService appointmentServiceAttributeTypeService;

    @Autowired
    private AppointmentServiceMapper appointmentServiceMapper;

    @RequestMapping(method = RequestMethod.GET)
    @ResponseBody
    public List<AppointmentServiceAttributeTypeResponse> getAllAttributeTypes(
            @RequestParam(value = "includeRetired", required = false, defaultValue = "false") Boolean includeRetired) {
        List<AppointmentServiceAttributeType> attributeTypes = appointmentServiceAttributeTypeService.getAllAttributeTypes(includeRetired);
        return appointmentServiceMapper.constructAttributeTypeListResponse(attributeTypes);
    }

    @RequestMapping(value = "/{uuid}", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<AppointmentServiceAttributeTypeResponse> getAttributeTypeByUuid(@PathVariable("uuid") String uuid) {
        AppointmentServiceAttributeType attributeType = appointmentServiceAttributeTypeService.getAttributeTypeByUuid(uuid);
        if (attributeType == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        AppointmentServiceAttributeTypeResponse response = appointmentServiceMapper.constructAttributeTypeResponse(attributeType);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
