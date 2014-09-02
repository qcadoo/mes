package com.qcadoo.mes.technologies.grouping;

import java.math.BigDecimal;
import java.util.List;

import com.qcadoo.model.api.Entity;

public interface OperationMergeService {

    void mergeProductIn(Entity order, Entity existingOperationComponent, Entity operationProductIn, BigDecimal quantity);

    void storeProductIn(Entity order, Entity operationComponent, Entity mergeOperationComponent, Entity operationProductIn, BigDecimal quantity);

    public void mergeProductOut(Entity order, Entity existingOperationComponent, Entity operationProductOut, BigDecimal quantity);

    void storeProductOut(Entity order, Entity operationComponent, Entity mergeOperationComponent, Entity operationProductIn, BigDecimal quantity);

    List<Entity> findMergedProductInByOrder(Entity order);

    List<Entity> findMergedProductOutByOrder(Entity order);

    List<Long> findMergedToOperationComponentIds();

    Entity findMergedByOperationComponent(Entity operationComponent);

    Entity findMergedByMergedToc(Entity toc);

    Entity findMergedByMergedTocId(Long tocId);

    Entity findMergedFromOperationInByOperationComponentId(Long operationComponentId);

    Entity findMergedFromOperationOutByOperationComponentId(Long operationComponentId);

    List<Entity> findMergedToByOperationComponentId(Long operationComponentId);

    List<Entity> findMergedProductInComponentsByOperationComponent(Entity operationComponent);

    List<Entity> findMergedToProductOutComponentsByOperationComponent(Entity operationComponent);

    List<Entity> findMergedEntitiesByOperationComponent(Entity operationComponent);

    List<Entity> findMergedEntitiesByOperationComponentId(Long operationComponentId);

    void adjustOperationProductComponentsDueMerge(Entity operationComponent);
}
