package com.qcadoo.mes.basic.controllers.dataProvider.responses;

import com.google.common.collect.Lists;
import com.qcadoo.mes.basic.controllers.dataProvider.dto.WorkstationTypeDto;

import java.util.List;

public class WorkstationTypesGridResponse {

    private Integer total;

    private List<WorkstationTypeDto> rows = Lists.newArrayList();
    public WorkstationTypesGridResponse(Integer total, List<WorkstationTypeDto> rows) {
        this.total = total;
        this.rows = rows;
    }

    public Integer getTotal() {
        return total;
    }

    public void setTotal(Integer total) {
        this.total = total;
    }

    public List<WorkstationTypeDto> getRows() {
        return rows;
    }

    public void setRows(List<WorkstationTypeDto> rows) {
        this.rows = rows;
    }
}
