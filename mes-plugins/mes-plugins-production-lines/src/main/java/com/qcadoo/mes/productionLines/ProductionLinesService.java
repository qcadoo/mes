package com.qcadoo.mes.productionLines;

import com.qcadoo.model.api.Entity;

public interface ProductionLinesService {

    Integer getWorkstationTypesCount(final Entity operationComponent, final Entity productionLine);
}
