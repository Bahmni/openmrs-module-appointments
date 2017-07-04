package org.openmrs.module.appointments.model;

import org.openmrs.BaseOpenmrsData;
import org.openmrs.User;

public class Speciality extends BaseOpenmrsData {
    private Integer specialityId;
    private String name;

    public User getCreator() {
        return creator;
    }

    public void setCreator(User creator) {
        this.creator = creator;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getSpecialityId() {
        return specialityId;
    }

    public void setSpecialityId(Integer specialityId) {
        this.specialityId = specialityId;
    }

    @Override
    public Integer getId() {
        return getSpecialityId();
    }

    @Override
    public void setId(Integer id) {
        setSpecialityId(id);
    }
}
