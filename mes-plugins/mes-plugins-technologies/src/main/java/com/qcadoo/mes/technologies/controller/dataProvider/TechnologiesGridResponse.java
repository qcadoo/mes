package com.qcadoo.mes.technologies.controller.dataProvider;

import com.google.common.collect.Lists;

import java.util.List;

public class TechnologiesGridResponse {

    private Integer total;

    private List<TechnologyDto> rows = Lists.newArrayList();

    public TechnologiesGridResponse(Integer total, List<TechnologyDto> rows) {
        this.total = total;
        this.rows = rows;
    }

    public Integer getTotal() {
        return total;
    }

    public void setTotal(Integer total) {
        this.total = total;
    }

    public List<TechnologyDto> getRows() {
        return rows;
    }

    public void setRows(List<TechnologyDto> rows) {
        this.rows = rows;
    }
}
