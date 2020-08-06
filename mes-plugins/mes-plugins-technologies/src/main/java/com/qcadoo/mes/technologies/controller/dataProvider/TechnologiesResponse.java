package com.qcadoo.mes.technologies.controller.dataProvider;

import com.google.common.collect.Lists;

import java.util.List;

public class TechnologiesResponse {

    private List<TechnologyDto> technologies = Lists.newArrayList();

    public List<TechnologyDto> getTechnologies() {
        return technologies;
    }

    public void setTechnologies(List<TechnologyDto> technologies) {
        this.technologies = technologies;
    }
}
