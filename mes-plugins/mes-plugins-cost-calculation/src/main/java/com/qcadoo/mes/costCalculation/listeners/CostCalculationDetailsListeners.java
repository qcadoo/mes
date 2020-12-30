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

import com.google.common.collect.Sets;
import com.qcadoo.mes.basic.constants.BasicConstants;
import com.qcadoo.mes.costCalculation.CostCalculationService;
import com.qcadoo.mes.costCalculation.constants.CostCalculationFields;
import com.qcadoo.mes.costCalculation.print.CostCalculationReportService;
import com.qcadoo.mes.technologies.tree.ProductStructureTreeService;
import com.qcadoo.model.api.BigDecimalUtils;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.NumberService;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ComponentState.MessageType;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.constants.QcadooViewConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

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

            fillFields(view, costCalculation);

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

    private void fillFields(final ViewDefinitionState view, final Entity costCalculation) {
        final Set<String> costFields = Sets.newHashSet(CostCalculationFields.PRODUCTION_COST_MARGIN_VALUE,
                CostCalculationFields.MATERIAL_COST_MARGIN_VALUE, CostCalculationFields.TOTAL_OVERHEAD,
                CostCalculationFields.TOTAL_MATERIAL_COSTS, CostCalculationFields.TOTAL_MACHINE_HOURLY_COSTS,
                CostCalculationFields.TOTAL_LABOR_HOURLY_COSTS, CostCalculationFields.TOTAL_PIECEWORK_COSTS,
                CostCalculationFields.TOTAL_TECHNICAL_PRODUCTION_COSTS, CostCalculationFields.TOTAL_COSTS,
                CostCalculationFields.TOTAL_COST_PER_UNIT, CostCalculationFields.ADDITIONAL_OVERHEAD_VALUE,
                CostCalculationFields.REGISTRATION_PRICE_OVERHEAD_VALUE, CostCalculationFields.PROFIT_VALUE,
                CostCalculationFields.SELL_PRICE_VALUE, CostCalculationFields.TECHNICAL_PRODUCTION_COSTS);

        for (String costField : costFields) {
            FieldComponent fieldComponent = (FieldComponent) view.getComponentByReference(costField);
            fieldComponent.setFieldValue(numberService.setScaleWithDefaultMathContext(
                    BigDecimalUtils.convertNullToZero(costCalculation.getDecimalField(costField)), 2));
        }
    }

    public void printCostCalculationReport(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        costCalculationReportService.printCostCalculationReport(view, state, args);
    }

    public void saveNominalCosts(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        FormComponent formComponent = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);
        Entity costsEntity = formComponent.getEntity();
        Entity product = costsEntity.getBelongsToField(BasicConstants.MODEL_PRODUCT);
        BigDecimal tkw = costsEntity.getDecimalField(CostCalculationFields.TECHNICAL_PRODUCTION_COSTS);
        product.setField("nominalCost", numberService.setScaleWithDefaultMathContext(tkw));
        Entity savedEntity = product.getDataDefinition().save(product);
        if (!savedEntity.isValid()) {
            view.getComponentByReference(QcadooViewConstants.L_FORM)
                    .addMessage("costCalculation.messages.success.saveCostsFailure", MessageType.FAILURE);
        }

        view.getComponentByReference(QcadooViewConstants.L_FORM).addMessage("costCalculation.messages.success.saveCostsSuccess",
                MessageType.SUCCESS);
    }
}
