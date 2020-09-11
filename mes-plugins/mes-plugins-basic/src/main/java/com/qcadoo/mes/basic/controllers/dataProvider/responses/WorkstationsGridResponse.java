package com.qcadoo.mes.basic.controllers.dataProvider.responses;

import com.google.common.collect.Lists;
import com.qcadoo.mes.basic.controllers.dataProvider.dto.WorkstationDto;

import java.util.List;

public class WorkstationsGridResponse {

    private Integer total;

    private List<WorkstationDto> rows = Lists.newArrayList();

    public WorkstationsGridResponse(Integer total, List<WorkstationDto> rows) {
        this.total = total;
        this.rows = rows;
    }

    public Integer getTotal() {
        return total;
    }

    public void setTotal(Integer total) {
        this.total = total;
    }

    public List<WorkstationDto> getRows() {
        return rows;
    }

    public void setRows(List<WorkstationDto> rows) {
        this.rows = rows;
    }
}
