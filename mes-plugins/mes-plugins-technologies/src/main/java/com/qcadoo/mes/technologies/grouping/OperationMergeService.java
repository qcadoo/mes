package com.qcadoo.mes.technologies.grouping;

import java.math.BigDecimal;

import com.qcadoo.model.api.Entity;

public interface OperationMergeService {

    void mergeProductIn(Entity existingOperationComponent, Entity operationProductIn, BigDecimal quantity);

    void storeProductIn(Entity operationComponent, Entity mergeOperationComponent, Entity operationProductIn, BigDecimal quantity);

    public void mergeProductOut(Entity existingOperationComponent, Entity operationProductOut, BigDecimal quantity);

    void storeProductOut(Entity operationComponent, Entity mergeOperationComponent, Entity operationProductIn, BigDecimal quantity);
}
