package com.qcadoo.mes.productionLines.controller.dataProvider;

import com.google.common.collect.Lists;

import java.util.List;

public class ProductionLinesGridResponse {

    private Integer total;

    private List<ProductionLineDto> rows = Lists.newArrayList();

    public ProductionLinesGridResponse(Integer total, List<ProductionLineDto> rows) {
        this.total = total;
        this.rows = rows;
    }

    public Integer getTotal() {
        return total;
    }

    public void setTotal(Integer total) {
        this.total = total;
    }

    public List<ProductionLineDto> getRows() {
        return rows;
    }

    public void setRows(List<ProductionLineDto> rows) {
        this.rows = rows;
    }
}
