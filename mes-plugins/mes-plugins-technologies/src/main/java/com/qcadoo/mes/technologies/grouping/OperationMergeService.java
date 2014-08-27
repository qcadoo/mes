package com.qcadoo.mes.technologies.grouping;

import java.math.BigDecimal;
import java.util.List;

import com.qcadoo.model.api.Entity;

public interface OperationMergeService {

    void mergeProductIn(Entity existingOperationComponent, Entity operationProductIn, BigDecimal quantity);

    void storeProductIn(Entity operationComponent, Entity mergeOperationComponent, Entity operationProductIn, BigDecimal quantity);

    public void mergeProductOut(Entity existingOperationComponent, Entity operationProductOut, BigDecimal quantity);

    void storeProductOut(Entity operationComponent, Entity mergeOperationComponent, Entity operationProductIn, BigDecimal quantity);

    List<Long> findMergedToOperationComponentIds();

    Entity findMergedByOperationComponent(Entity operationComponent);

    List<Entity> findMergedProductInComponentsByOperationComponent(Entity operationComponent);

    List<Entity> findMergedProductOutComponentsByOperationComponent(Entity operationComponent);

    void adjustOperationProductComponentsDueMerge(Entity operationComponent);
}
