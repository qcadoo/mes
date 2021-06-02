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
package com.qcadoo.mes.costCalculation.listeners;

import com.qcadoo.mes.basic.ParameterService;
import com.qcadoo.mes.basic.constants.ProductFields;
import com.qcadoo.mes.costCalculation.constants.CalculationResultFields;
import com.qcadoo.mes.costCalculation.constants.CostCalculationFields;
import com.qcadoo.mes.costCalculation.print.CostCalculationReportService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.NumberService;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ComponentState.MessageType;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.constants.QcadooViewConstants;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import static com.google.common.base.Preconditions.checkArgument;

@Service
public class CostCalculationDetailsListeners {

    public static final String L_COMPLETE_NOMINAL_COST_IN_ARTICLE_AND_PRODUCTS = "completeNominalCostInArticleAndProducts";

    public static final String L_NOMINAL_COST = "nominalCost";

    @Autowired
    private NumberService numberService;

    @Autowired
    private CostCalculationReportService costCalculationReportService;

    @Autowired
    private ParameterService parameterService;

    public void generateCostCalculation(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        costCalculationReportService.generateCostCalculationReport(view, state, args);
    }

    private Entity getEntityFromForm(final ViewDefinitionState view) {
        FormComponent costCalculationForm = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);
        checkArgument(costCalculationForm != null, "form is null");
        checkArgument(costCalculationForm.isValid(), "invalid form");

        Long costCalculationId = costCalculationForm.getEntityId();

        return costCalculationForm.getEntity().getDataDefinition().get(costCalculationId);
    }

    public void printCostCalculationReport(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        costCalculationReportService.printCostCalculationReport(view, state, args);
    }

    public void saveNominalCosts(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        Entity costCalculation = getEntityFromForm(view);
        boolean hasErrors = false;
        for (Entity calculationResult : costCalculation.getManyToManyField(CostCalculationFields.CALCULATION_RESULTS)) {
            Entity product = calculationResult.getBelongsToField(CalculationResultFields.PRODUCT);
            product.setField(L_NOMINAL_COST, numberService.setScaleWithDefaultMathContext(calculationResult
                    .getDecimalField(CalculationResultFields.TECHNICAL_PRODUCTION_COST)));
            Entity savedEntity = product.getDataDefinition().save(product);
            if (!savedEntity.isValid()) {
                hasErrors = true;

            } else {
                if (parameterService.getParameter().getBooleanField(L_COMPLETE_NOMINAL_COST_IN_ARTICLE_AND_PRODUCTS)) {
                    for (Entity child : savedEntity.getHasManyField(ProductFields.CHILDREN)) {
                        child.setField(L_NOMINAL_COST, numberService.setScaleWithDefaultMathContext(calculationResult
                                .getDecimalField(CalculationResultFields.TECHNICAL_PRODUCTION_COST)));
                        child.getDataDefinition().save(child);
                    }
                }
            }
        }
        if (hasErrors) {
            view.getComponentByReference(QcadooViewConstants.L_FORM).addMessage(
                    "costCalculation.messages.success.saveCostsFailure", MessageType.FAILURE);
        } else {

            view.getComponentByReference(QcadooViewConstants.L_FORM).addMessage(
                    "costCalculation.messages.success.saveCostsSuccess", MessageType.SUCCESS);
        }
    }
}
