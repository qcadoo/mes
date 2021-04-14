package com.qcadoo.mes.basic.controllers.dataProvider.responses;

import com.google.common.collect.Lists;
import com.qcadoo.mes.basic.controllers.dataProvider.dto.FactoryDto;

import java.util.List;

public class FactoriesResponse {

    private List<FactoryDto> factories = Lists.newArrayList();

    public FactoriesResponse(List<FactoryDto> factories) {
        this.factories = factories;;
    }

    public List<FactoryDto> getFactories() {
        return factories;
    }

    public void setFactories(List<FactoryDto> factories) {
        this.factories = factories;
    }
}
