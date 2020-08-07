package com.qcadoo.mes.productionLines.controller.dataProvider;

import com.google.common.collect.Lists;

import java.util.List;

public class ProductionLinesResponse {

    private List<ProductionLineDto> productionLines = Lists.newArrayList();

    public List<ProductionLineDto> getProductionLines() {
        return productionLines;
    }

    public void setProductionLines(List<ProductionLineDto> productionLines) {
        this.productionLines = productionLines;
    }
}
