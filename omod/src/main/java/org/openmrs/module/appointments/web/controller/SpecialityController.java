package org.openmrs.module.appointments.web.controller;

import org.openmrs.module.appointments.model.Speciality;
import org.openmrs.module.appointments.service.SpecialityService;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.v1_0.controller.BaseRestController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Controller
@RequestMapping(value = "/rest/" + RestConstants.VERSION_1 + "/speciality")
public class SpecialityController extends BaseRestController {

    @Autowired
    private SpecialityService specialityService;

    @RequestMapping(method = RequestMethod.GET, value = "all")
    @ResponseBody
    public List getAllSpecialities()  {
        List<Speciality> allSpecialities = specialityService.getAllSpecialities();
        List specialities = new ArrayList();
        for(Speciality speciality: allSpecialities){
            HashMap specialityResponse = new HashMap<>();
            specialityResponse.put("name", speciality.getName());
            specialityResponse.put("uuid", speciality.getUuid());
            specialities.add(specialityResponse);
        }
        return specialities;
    }
}
