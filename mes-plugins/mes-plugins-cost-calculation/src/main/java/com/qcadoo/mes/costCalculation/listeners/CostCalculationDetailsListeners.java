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

import com.qcadoo.mes.costCalculation.CostCalculationService;
import com.qcadoo.mes.costCalculation.constants.CalculationResultFields;
import com.qcadoo.mes.costCalculation.constants.CostCalculationConstants;
import com.qcadoo.mes.costCalculation.constants.CostCalculationFields;
import com.qcadoo.mes.costCalculation.print.CostCalculationReportService;
import com.qcadoo.mes.technologies.constants.TechnologyFields;
import com.qcadoo.mes.technologies.tree.ProductStructureTreeService;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.NumberService;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ComponentState.MessageType;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.constants.QcadooViewConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.google.common.base.Preconditions.checkArgument;

@Service
public class CostCalculationDetailsListeners {

    @Autowired
    private NumberService numberService;

    @Autowired
    private CostCalculationService costCalculationService;

    @Autowired
    private CostCalculationReportService costCalculationReportService;

    @Autowired
    private ProductStructureTreeService productStructureTreeService;

    @Autowired
    private DataDefinitionService dataDefinitionService;

    public void generateCostCalculation(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        state.performEvent(view, "save", new String[0]);

        if (state.isHasError()) {
            return;
        }

        Entity costCalculation = getEntityFromForm(view);
        List<Entity> technologies = costCalculation.getManyToManyField(CostCalculationFields.TECHNOLOGIES);
        if (technologies.size() == 1) {
            Entity technology = technologies.get(0);
            productStructureTreeService.generateProductStructureTree(null, technology);
            costCalculation = costCalculationService.calculateTotalCost(costCalculation, technology);

            costCalculationService.calculateSellPriceOverhead(costCalculation);
            costCalculationService.calculateSellPrice(costCalculation);

            DataDefinition calculationResultDD = dataDefinitionService.get(CostCalculationConstants.PLUGIN_IDENTIFIER,
                    CostCalculationConstants.MODEL_CALCULATION_RESULT);
            Entity calculationResult = calculationResultDD.create();
            calculationResult.setField(CalculationResultFields.COST_CALCULATION, costCalculation);
            calculationResult.setField(CalculationResultFields.TECHNOLOGY, technology);
            calculationResult.setField(CalculationResultFields.PRODUCT, technology.getBelongsToField(TechnologyFields.PRODUCT));
            calculationResult.setField(CalculationResultFields.MATERIAL_COSTS, numberService.setScaleWithDefaultMathContext(
                    costCalculation.getDecimalField(CostCalculationFields.TOTAL_MATERIAL_COSTS), 2));
            calculationResult
                    .setField(CalculationResultFields.LABOUR_COST,
                            numberService
                                    .setScaleWithDefaultMathContext(
                                            costCalculation.getDecimalField(CostCalculationFields.TOTAL_MACHINE_HOURLY_COSTS)
                                                    .add(costCalculation
                                                            .getDecimalField(CostCalculationFields.TOTAL_LABOR_HOURLY_COSTS)),
                                            2));
            calculationResult.setField(CalculationResultFields.PRODUCTION_COSTS, numberService.setScaleWithDefaultMathContext(
                    costCalculation.getDecimalField(CostCalculationFields.TOTAL_TECHNICAL_PRODUCTION_COSTS), 2));
            calculationResult.setField(CalculationResultFields.TOTAL_COST, numberService
                    .setScaleWithDefaultMathContext(costCalculation.getDecimalField(CostCalculationFields.TOTAL_COSTS), 2));
            calculationResult.setField(CalculationResultFields.REGISTRATION_PRICE, numberService.setScaleWithDefaultMathContext(
                    costCalculation.getDecimalField(CostCalculationFields.TOTAL_COST_PER_UNIT), 2));
            calculationResult.setField(CalculationResultFields.TECHNICAL_PRODUCTION_COST,
                    numberService.setScaleWithDefaultMathContext(
                            costCalculation.getDecimalField(CostCalculationFields.TECHNICAL_PRODUCTION_COSTS), 2));
            calculationResult.setField(CalculationResultFields.SELLING_PRICE, numberService
                    .setScaleWithDefaultMathContext(costCalculation.getDecimalField(CostCalculationFields.SELL_PRICE_VALUE), 2));
            calculationResult.setField(CalculationResultFields.NO_MATERIAL_PRICE, false);
            calculationResultDD.save(calculationResult);
            costCalculationReportService.generateCostCalculationReport(view, state, args);
        }

        view.getComponentByReference(QcadooViewConstants.L_FORM)
                .addMessage("costCalculation.messages.success.calculationComplete", MessageType.SUCCESS);
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
            product.setField("nominalCost", numberService.setScaleWithDefaultMathContext(
                    calculationResult.getDecimalField(CalculationResultFields.TECHNICAL_PRODUCTION_COST)));
            Entity savedEntity = product.getDataDefinition().save(product);
            if (!savedEntity.isValid()) {
                hasErrors = true;

            }
        }
        if (hasErrors) {
            view.getComponentByReference(QcadooViewConstants.L_FORM)
                    .addMessage("costCalculation.messages.success.saveCostsFailure", MessageType.FAILURE);
        } else {

            view.getComponentByReference(QcadooViewConstants.L_FORM)
                    .addMessage("costCalculation.messages.success.saveCostsSuccess", MessageType.SUCCESS);
        }
    }
}
