/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.1.3
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
package com.qcadoo.mes.costCalculation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.technologies.TechnologyService;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.EntityTree;

@Service
public class CostCalculationModelValidators {

    @Autowired
    private TechnologyService technologyService;

    private static final Logger LOG = LoggerFactory.getLogger(CostCalculationModelValidators.class);

    public boolean checkIfTheTechnologyTreeIsntEmpty(final DataDefinition dataDefinition, final Entity costCalculation) {
        Entity technology = costCalculation.getBelongsToField("technology");
        EntityTree tree = technology.getTreeField("operationComponents");

        if (tree != null && !tree.isEmpty()) {
            try {
                for (Entity operationComponent : tree) {
                    technologyService.getProductCountForOperationComponent(operationComponent);
                }
                return true;
            } catch (IllegalStateException e) {
                LOG.debug("invalid technology tree passed to cost calculation");
            }
        }

        costCalculation.addError(dataDefinition.getField("technology"), "costNormsForOperation.messages.fail.emptyTree");
        return false;
    }

    public boolean checkIfCurrentGlobalIsSelected(final DataDefinition costCalculationDD, final Entity costCalculation) {
        if ((costCalculation.getField("sourceOfMaterialCosts").equals("01currentGlobalDefinitionsInProduct"))
                && (costCalculation.getField("calculateMaterialCostsMode").equals("04costForOrder"))) {
            costCalculation.addError(costCalculationDD.getField("calculateMaterialCostsMode"),
                    "costCalculation.messages.optionUnavailable");
            return false;
        } else {
            return true;
        }
    }

}
