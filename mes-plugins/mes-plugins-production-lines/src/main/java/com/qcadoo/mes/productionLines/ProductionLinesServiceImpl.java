/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.3
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
package com.qcadoo.mes.productionLines;

import static com.qcadoo.mes.productionLines.constants.ProductionLineFields.QUANTITY_FOR_OTHER_WORKSTATION_TYPES;
import static com.qcadoo.mes.productionLines.constants.ProductionLineFields.WORKSTATION_TYPE_COMPONENTS;
import static com.qcadoo.mes.productionLines.constants.WorkstationTypeComponentFields.QUANTITY;
import static com.qcadoo.mes.technologies.constants.OperationFields.WORKSTATION_TYPE;
import static com.qcadoo.model.api.search.SearchOrders.asc;
import static com.qcadoo.model.api.search.SearchProjections.alias;
import static com.qcadoo.model.api.search.SearchProjections.list;
import static com.qcadoo.model.api.search.SearchProjections.rowCount;
import static com.qcadoo.model.api.search.SearchProjections.sum;
import static com.qcadoo.model.api.search.SearchRestrictions.eq;
import static com.qcadoo.model.api.search.SearchRestrictions.idEq;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.basic.constants.WorkstationTypeFields;
import com.qcadoo.mes.productionLines.constants.ProductionLinesConstants;
import com.qcadoo.mes.productionLines.constants.WorkstationTypeComponentFields;
import com.qcadoo.mes.technologies.constants.TechnologyOperationComponentFields;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.IntegerUtils;
import com.qcadoo.model.api.search.SearchCriteriaBuilder;

@Service
public class ProductionLinesServiceImpl implements ProductionLinesService {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Override
    public Integer getWorkstationTypesCount(final Entity operationComponent, final Entity productionLine) {
        List<Entity> workstationTypeComponents = productionLine.getHasManyField(WORKSTATION_TYPE_COMPONENTS);

        Entity desiredWorkstation = operationComponent.getBelongsToField(TechnologyOperationComponentFields.OPERATION)
                .getBelongsToField(WORKSTATION_TYPE);

        if (desiredWorkstation != null) {
            for (Entity workstationTypeComponent : workstationTypeComponents) {
                Entity workstation = workstationTypeComponent.getBelongsToField(WORKSTATION_TYPE);

                // FIXME dev_team, proxy entity equals thing
                if (desiredWorkstation.getId().equals(workstation.getId())) {
                    return (Integer) workstationTypeComponent.getField(QUANTITY);
                }
            }
        }

        return productionLine.getIntegerField(QUANTITY_FOR_OTHER_WORKSTATION_TYPES);
    }

    @Override
    public Integer getWorkstationTypesCount(final Long productionLineId, final String workstationTypeNumber) {
        Entity projection = getWorkstationTypesSumProjection(productionLineId, workstationTypeNumber);
        return IntegerUtils.convertNullToZero(projection.getField("sum"));
    }

    private Entity getWorkstationTypesSumProjection(final Long productionLineId, final String workstationTypeNumber) {
        SearchCriteriaBuilder scb = getWorkstationTypeComponentDD().find();
        scb.createCriteria(WorkstationTypeComponentFields.PRODUCTIONLINE, "pl").add(idEq(productionLineId));
        scb.createCriteria(WorkstationTypeComponentFields.WORKSTATIONTYPE, "wt").add(
                eq(WorkstationTypeFields.NUMBER, workstationTypeNumber));
        scb.setProjection(list().add(alias(sum(WorkstationTypeComponentFields.QUANTITY), "sum")).add(rowCount()));
        scb.addOrder(asc("sum"));
        return scb.setMaxResults(1).uniqueResult();
    }

    private DataDefinition getWorkstationTypeComponentDD() {
        return dataDefinitionService.get(ProductionLinesConstants.PLUGIN_IDENTIFIER,
                ProductionLinesConstants.MODEL_WORKSTATION_TYPE_COMPONENT);
    }

}
