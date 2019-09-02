package com.qcadoo.mes.technologies.dto;

import com.google.common.collect.Lists;

import java.util.List;

public class ModifyTechnologyResult {

    List<String> createdTechnologies = Lists.newArrayList();

    List<String> notCreatedTechnologies = Lists.newArrayList();

    public void addCreatedTechnology(String number) {
        createdTechnologies.add(number);
    }

    public void addNotCreatedTechnologies(String number) {
        notCreatedTechnologies.add(number);
    }

    public List<String> getCreatedTechnologies() {
        return createdTechnologies;
    }

    public void setCreatedTechnologies(List<String> createdTechnologies) {
        this.createdTechnologies = createdTechnologies;
    }

    public List<String> getNotCreatedTechnologies() {
        return notCreatedTechnologies;
    }

    public void setNotCreatedTechnologies(List<String> notCreatedTechnologies) {
        this.notCreatedTechnologies = notCreatedTechnologies;
    }
}
