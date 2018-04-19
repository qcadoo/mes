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
package com.qcadoo.mes.productionCounting;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.productionCounting.constants.ProductionBalanceFields;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;

@Service
public class ProductionBalanceServiceImpl implements ProductionBalanceService {

    @Autowired
    private ProductionCountingService productionCountingService;

    @Override
    public void disableCheckboxes(final ViewDefinitionState view) {
        FieldComponent calculateOperationCostsModeField = (FieldComponent) view
                .getComponentByReference(ProductionBalanceFields.CALCULATE_OPERATION_COST_MODE);

        FieldComponent includeTPZ = (FieldComponent) view.getComponentByReference(ProductionBalanceFields.INCLUDE_TPZ);
        FieldComponent includeAdditionalTime = (FieldComponent) view
                .getComponentByReference(ProductionBalanceFields.INCLUDE_ADDITIONAL_TIME);

        String calculateOperationCostsMode = (String) calculateOperationCostsModeField.getFieldValue();

        if (productionCountingService.isCalculateOperationCostModePiecework(calculateOperationCostsMode)) {
            includeTPZ.setFieldValue(false);
            includeTPZ.setEnabled(false);
            includeTPZ.requestComponentUpdateState();

            includeAdditionalTime.setFieldValue(false);
            includeAdditionalTime.setEnabled(false);
            includeAdditionalTime.requestComponentUpdateState();
        } else {
            includeTPZ.setEnabled(true);
            includeTPZ.requestComponentUpdateState();

            includeAdditionalTime.setEnabled(true);
            includeAdditionalTime.requestComponentUpdateState();
        }
    }
}
