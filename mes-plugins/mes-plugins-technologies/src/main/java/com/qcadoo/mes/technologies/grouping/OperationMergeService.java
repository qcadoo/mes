/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.4
 *
 * This file is part of Qcadoo.
 *
 * Qcadoo is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation; either version 3 of the License,
 * or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 * ***************************************************************************
 */
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

    boolean isMergedByTocId(Long tocId);

    Entity findMergedFromOperationInByOperationComponentId(Long operationComponentId);

    Entity findMergedFromOperationOutByOperationComponentId(Long operationComponentId);

    List<Entity> findMergedToByOperationComponentId(Long operationComponentId);

    List<Entity> findMergedProductInComponentsByOperationComponent(Entity operationComponent);

    List<Entity> findMergedToProductOutComponentsByOperationComponent(Entity operationComponent);

    List<Entity> findMergedEntitiesByOperationComponent(Entity operationComponent);

    List<Entity> findMergedEntitiesByOperationComponentId(Long operationComponentId);

    void adjustOperationProductComponentsDueMerge(Entity operationComponent);
}
