/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.2.0
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
package com.qcadoo.mes.materialRequirements.listeners;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.lowagie.text.DocumentException;
import com.qcadoo.mes.materialRequirements.MaterialRequirementService;
import com.qcadoo.mes.materialRequirements.constants.MaterialRequirementsConstants;
import com.qcadoo.mes.orders.util.OrderReportService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.validators.ErrorMessage;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.utils.NumberGeneratorService;

@Service
public class OrdersPlanningListListenersMR {

    @Autowired
    private NumberGeneratorService numberGeneratorService;

    @Autowired
    private MaterialRequirementService materialRequirementService;

    @Autowired
    private OrderReportService orderReportService;

    public void printMaterialRequirementForOrder(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        Entity materialRequirement = printMaterialReqForOrder(state);

        if (materialRequirement == null) {
            return;
        }

        try {
            materialRequirementService.generateMaterialRequirementDocuments(state, materialRequirement);
            view.redirectTo(
                    "/generateSavedReport/" + MaterialRequirementsConstants.PLUGIN_IDENTIFIER + "/"
                            + MaterialRequirementsConstants.MODEL_MATERIAL_REQUIREMENT + "." + args[0] + "?id="
                            + materialRequirement.getId(), true, false);
        } catch (IOException e) {
            throw new IllegalStateException(e.getMessage(), e);
        } catch (DocumentException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    private Entity printMaterialReqForOrder(final ComponentState state) {
        Map<String, Object> entityFieldsMap = new HashMap<String, Object>();

        entityFieldsMap.put("mrpAlgorithm", materialRequirementService.getDefaultMrpAlgorithm().getStringValue());
        entityFieldsMap.put("number", numberGeneratorService.generateNumber(MaterialRequirementsConstants.PLUGIN_IDENTIFIER,
                MaterialRequirementsConstants.MODEL_MATERIAL_REQUIREMENT));

        OrderReportService.OrderValidator orderValidator = new OrderReportService.OrderValidator() {

            @Override
            public ErrorMessage validateOrder(final Entity order) {
                if (order.getField("technology") == null) {
                    return new ErrorMessage("orders.validate.global.error.orderMustHaveTechnology",
                            order.getStringField("number"));
                }
                return null;
            }
        };

        return orderReportService.printForOrder(state, MaterialRequirementsConstants.PLUGIN_IDENTIFIER,
                MaterialRequirementsConstants.MODEL_MATERIAL_REQUIREMENT, entityFieldsMap, orderValidator);
    }

}
