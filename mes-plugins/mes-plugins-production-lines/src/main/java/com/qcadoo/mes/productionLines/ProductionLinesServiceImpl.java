package com.qcadoo.mes.productionLines;

import org.springframework.stereotype.Service;

import com.qcadoo.model.api.Entity;

@Service
public class ProductionLinesServiceImpl implements ProductionLinesService {

    public Integer getWorkstationTypesCount(final Entity operationComponent) {
        return 1;
    }

}
