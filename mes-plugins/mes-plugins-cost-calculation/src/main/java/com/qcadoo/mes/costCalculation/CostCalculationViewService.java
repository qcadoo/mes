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
package com.qcadoo.mes.costCalculation;

import static com.google.common.base.Preconditions.checkArgument;
import static com.qcadoo.mes.basic.constants.BasicConstants.MODEL_PRODUCT;
import static com.qcadoo.mes.costCalculation.constants.CostCalculationConstants.DEFAULT_TECHNOLOGY;
import static com.qcadoo.mes.costCalculation.constants.CostCalculationConstants.FORM;
import static com.qcadoo.mes.costCalculation.constants.CostCalculationConstants.ORDER;
import static com.qcadoo.mes.costCalculation.constants.CostCalculationConstants.PRODUCT;
import static com.qcadoo.mes.costCalculation.constants.CostCalculationConstants.QUANTITY;
import static com.qcadoo.mes.costCalculation.constants.CostCalculationConstants.TECHNOLOGY;
import static com.qcadoo.mes.orders.constants.OrdersConstants.MODEL_ORDER;
import static com.qcadoo.mes.technologies.constants.TechnologiesConstants.MODEL_TECHNOLOGY;
import static com.qcadoo.view.api.ComponentState.MessageType.SUCCESS;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.qcadoo.mes.basic.constants.BasicConstants;
import com.qcadoo.mes.basic.util.CurrencyService;
import com.qcadoo.mes.costCalculation.constants.CalculateMaterialCostsMode;
import com.qcadoo.mes.costCalculation.constants.CostCalculationConstants;
import com.qcadoo.mes.costCalculation.constants.SourceOfMaterialCosts;
import com.qcadoo.mes.costCalculation.print.CostCalculationReportService;
import com.qcadoo.mes.costNormsForOperation.constants.CalculateOperationCostMode;
import com.qcadoo.mes.orders.constants.OrdersConstants;
import com.qcadoo.mes.technologies.constants.TechnologiesConstants;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchCriteriaBuilder;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ComponentState.MessageType;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.utils.NumberGeneratorService;

@Service
public class CostCalculationViewService {

    private static final String L_PRODUCTION_LINE = "productionLine";

    private static final String GENERATED = "generated";

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private CostCalculationService costCalculationService;

    @Autowired
    private CostCalculationReportService costCalculationReportService;

    @Autowired
    private NumberGeneratorService numberGeneratorService;

    @Autowired
    private CurrencyService currencyService;

    private static final String EMPTY = "";

    public void showCostCalculateFromOrder(final ViewDefinitionState viewDefinitionState, final ComponentState state,
            final String[] args) {
        Long orderId = (Long) state.getFieldValue();

        if (orderId != null) {
            String url = "../page/costCalculation/costCalculationDetails.html?context={\"orderId\":\"" + orderId + "\"}";
            viewDefinitionState.redirectTo(url, false, true);
        }
    }

    public void showCostCalculateFromTechnology(final ViewDefinitionState viewDefinitionState, final ComponentState state,
            final String[] args) {
        Long technologyId = (Long) state.getFieldValue();

        if (technologyId != null) {
            String url = "../page/costCalculation/costCalculationDetails.html?context={\"technologyId\":\"" + technologyId
                    + "\"}";
            viewDefinitionState.redirectTo(url, false, true);
        }
    }

    public void copyFieldValues(final ViewDefinitionState viewDefinitionState, final ComponentState componentState,
            final String[] args) {
        if (args.length < 2) {
            return;
        }
        String sourceType = args[0];
        Long sourceId = Long.valueOf(args[1]);
        Boolean cameFromOrder = ORDER.equals(sourceType);
        Boolean cameFromTechnology = TECHNOLOGY.equals(sourceType);
        Entity technology;
        Entity order;

        if (!cameFromOrder && !cameFromTechnology) {
            return;
        }
        if (cameFromOrder) {
            order = dataDefinitionService.get(OrdersConstants.PLUGIN_IDENTIFIER, OrdersConstants.MODEL_ORDER).get(sourceId);
            technology = order.getBelongsToField(TECHNOLOGY);
            if (technology == null) {
                return;
            }
        } else {
            order = null;
            technology = dataDefinitionService.get(TechnologiesConstants.PLUGIN_IDENTIFIER,
                    TechnologiesConstants.MODEL_TECHNOLOGY).get(sourceId);
        }

        applyValuesToFields(viewDefinitionState, technology, order);
    }

    private void applyValuesToFields(final ViewDefinitionState viewDefinitionState, final Entity technology, final Entity order) {
        if (technology == null) {
            clearValueFromField(viewDefinitionState);
            return;
        }
        Boolean cameFromOrder = false;
        Boolean cameFromTechnology = false;
        Set<String> referenceNames = new HashSet<String>(Arrays.asList(L_PRODUCTION_LINE, DEFAULT_TECHNOLOGY, PRODUCT, ORDER,
                QUANTITY, TECHNOLOGY));
        Map<String, FieldComponent> componentsMap = new HashMap<String, FieldComponent>();
        for (String referenceName : referenceNames) {
            FieldComponent fieldComponent = (FieldComponent) viewDefinitionState.getComponentByReference(referenceName);
            componentsMap.put(referenceName, fieldComponent);
        }

        if (order == null) {
            cameFromTechnology = true;
        } else {
            cameFromOrder = true;
        }

        if (cameFromOrder) {
            componentsMap.get(ORDER).setFieldValue(order.getId());
            componentsMap.get(DEFAULT_TECHNOLOGY).setEnabled(false);
            componentsMap.get(L_PRODUCTION_LINE).setFieldValue(order.getBelongsToField(L_PRODUCTION_LINE).getId());
            componentsMap.get(QUANTITY).setFieldValue(order.getField("plannedQuantity"));
        } else {
            componentsMap.get(ORDER).setFieldValue(EMPTY);
            componentsMap.get(DEFAULT_TECHNOLOGY).setEnabled(false);
            componentsMap.get(QUANTITY).setFieldValue(technology.getField("minimalQuantity"));
        }
        componentsMap.get(ORDER).setEnabled(cameFromOrder);
        componentsMap.get(TECHNOLOGY).setFieldValue(technology.getId());
        componentsMap.get(TECHNOLOGY).setEnabled(cameFromTechnology);
        componentsMap.get(DEFAULT_TECHNOLOGY).setFieldValue(technology.getId());

        componentsMap.get(QUANTITY).setEnabled(!cameFromOrder);
        componentsMap.get(PRODUCT).setFieldValue(technology.getBelongsToField(PRODUCT).getId());
        componentsMap.get(PRODUCT).setEnabled(false);
    }

    private void clearValueFromField(final ViewDefinitionState view) {
        view.getComponentByReference(ORDER).addMessage("costCalculation.messages.lackOfTechnology", MessageType.FAILURE);
        view.getComponentByReference(DEFAULT_TECHNOLOGY).setFieldValue(EMPTY);
        view.getComponentByReference(TECHNOLOGY).setFieldValue(EMPTY);
        view.getComponentByReference(QUANTITY).setFieldValue(EMPTY);
        view.getComponentByReference(PRODUCT).setFieldValue(EMPTY);
        view.getComponentByReference(L_PRODUCTION_LINE).setFieldValue(EMPTY);
    }

    private void generateNumber(final ViewDefinitionState viewDefinitionState) {
        numberGeneratorService.generateAndInsertNumber(viewDefinitionState, CostCalculationConstants.PLUGIN_IDENTIFIER,
                CostCalculationConstants.MODEL_COST_CALCULATION, FORM, "number");
    }

    public void fillCurrencyFields(final ViewDefinitionState viewDefinitionState) {
        final String currencyAlphabeticCode = currencyService.getCurrencyAlphabeticCode();
        generateNumber(viewDefinitionState);
        Set<String> fields = Sets.newHashSet("totalCostsCurrency", "totalOverheadCurrency", "additionalOverheadValueCurrency",
                "materialCostMarginValueCurrency", "productionCostMarginValueCurrency", "totalTechnicalProductionCostsCurrency",
                "totalPieceworkCostsCurrency", "totalLaborHourlyCostsCurrency", "totalMachineHourlyCostsCurrency",
                "totalMaterialCostsCurrency", "additionalOverheadCurrency");

        for (String componentReference : fields) {
            FieldComponent field = (FieldComponent) viewDefinitionState.getComponentByReference(componentReference);
            field.setFieldValue(currencyAlphabeticCode);
            field.requestComponentUpdateState();
        }

        FieldComponent productionCostMarginProc = (FieldComponent) viewDefinitionState
                .getComponentByReference("productionCostMarginProc");
        productionCostMarginProc.setFieldValue("%");
        productionCostMarginProc.requestComponentUpdateState();
        FieldComponent materialCostMarginProc = (FieldComponent) viewDefinitionState
                .getComponentByReference("materialCostMarginProc");
        materialCostMarginProc.setFieldValue("%");
        materialCostMarginProc.requestComponentUpdateState();
        fillCostPerUnitUnitField(viewDefinitionState, null, null);
    }

    public void fillCostPerUnitUnitField(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        final String currencyAlphabeticCode = currencyService.getCurrencyAlphabeticCode();
        if (view.getComponentByReference(PRODUCT).getFieldValue() == null) {
            return;
        }

        Long productId = (Long) view.getComponentByReference(PRODUCT).getFieldValue();
        Entity product = dataDefinitionService.get(BasicConstants.PLUGIN_IDENTIFIER, MODEL_PRODUCT).get(productId);

        FieldComponent totalCostPerUnitUnit = (FieldComponent) view.getComponentByReference("totalCostPerUnitUnit");
        totalCostPerUnitUnit.setFieldValue(currencyAlphabeticCode + " / " + product.getStringField("unit"));
        totalCostPerUnitUnit.requestComponentUpdateState();
    }

    public void fillFieldWhenTechnologyChanged(final ViewDefinitionState viewDefinitionState, final ComponentState state,
            final String[] args) {
        if (!(state instanceof FieldComponent)) {
            return;
        }
        FieldComponent technologyLookup = (FieldComponent) viewDefinitionState.getComponentByReference(TECHNOLOGY);
        if (technologyLookup.getFieldValue() == null) {
            return;
        }

        Entity technology = dataDefinitionService.get(TechnologiesConstants.PLUGIN_IDENTIFIER,
                TechnologiesConstants.MODEL_TECHNOLOGY).get((Long) technologyLookup.getFieldValue());
        if (technology != null) {
            applyValuesToFields(viewDefinitionState, technology, null);
        }

    }

    public void fillFieldWhenOrderChanged(final ViewDefinitionState viewDefinitionState, final ComponentState state,
            final String[] args) {
        if (!(state instanceof FieldComponent)) {
            return;
        }
        FieldComponent orderLookup = (FieldComponent) viewDefinitionState.getComponentByReference(ORDER);
        if (orderLookup.getFieldValue() == null) {
            return;
        }
        Entity order = dataDefinitionService.get(OrdersConstants.PLUGIN_IDENTIFIER, OrdersConstants.MODEL_ORDER).get(
                (Long) orderLookup.getFieldValue());

        if (order == null) {
            return;
        }
        Entity technology = order.getBelongsToField(TECHNOLOGY);

        applyValuesToFields(viewDefinitionState, technology, order);
    }

    public void setFieldEnable(final ViewDefinitionState viewDefinitionState) {
        Set<String> referenceNames = new HashSet<String>(Arrays.asList(PRODUCT, ORDER, QUANTITY, TECHNOLOGY, "number",
                "productionLine", "description", "calculateMaterialCostsMode", "calculateOperationCostsMode",
                "productionCostMargin", "productionCostMarginProc", "materialCostMargin", "materialCostMarginProc",
                "additionalOverhead", "additionalOverheadCurrency", "printCostNormsOfMaterials", "printOperationNorms",
                "includeTPZ", "includeAdditionalTime", "sourceOfMaterialCosts"));
        Map<String, FieldComponent> componentsMap = new HashMap<String, FieldComponent>();
        for (String referenceName : referenceNames) {
            FieldComponent fieldComponent = (FieldComponent) viewDefinitionState.getComponentByReference(referenceName);
            componentsMap.put(referenceName, fieldComponent);
        }

        Boolean isGenerated = "1".equals(viewDefinitionState.getComponentByReference(GENERATED).getFieldValue());

        if (isGenerated) {

            for (Entry<String, FieldComponent> entry : componentsMap.entrySet()) {
                entry.getValue().setEnabled(false);
            }
        } else {
            for (Entry<String, FieldComponent> entry : componentsMap.entrySet()) {
                entry.getValue().setEnabled(true);
            }
        }

        if (componentsMap.get(TECHNOLOGY).getFieldValue() == null) {
            componentsMap.get(PRODUCT).setEnabled(true);
            componentsMap.get(QUANTITY).setEnabled(true);
            componentsMap.get(ORDER).setEnabled(true);
            componentsMap.get(TECHNOLOGY).setEnabled(true);
        }
    }

    /* FUNCTIONS FOR FIRE CALCULATION AND HANDLING RESULTS BELOW */

    /* Event handler, fire total calculation */
    public void generateCostCalculation(final ViewDefinitionState view, final ComponentState componentState, final String[] args) {
        componentState.performEvent(view, "save", new String[0]);
        if (componentState.isHasError()) {
            return;
        }
        Entity costCalculation = getEntityFromForm(view);
        attachBelongsToFields(costCalculation);
        // Fire cost calculation algorithm
        costCalculation = costCalculationService.calculateTotalCost(costCalculation);
        fillFields(view, costCalculation);
        costCalculationReportService.generateCostCalculationReport(view, componentState, args);
        view.getComponentByReference(FORM).addMessage("costCalculation.messages.success.calculationComplete", SUCCESS);
    }

    private void attachBelongsToFields(final Entity costCalculation) {
        final Map<String, DataDefinition> belongsToFieldDDs = Maps.newHashMap();
        belongsToFieldDDs.put(ORDER, dataDefinitionService.get(OrdersConstants.PLUGIN_IDENTIFIER, MODEL_ORDER));
        belongsToFieldDDs.put("technology", dataDefinitionService.get(TechnologiesConstants.PLUGIN_IDENTIFIER, MODEL_TECHNOLOGY));
        belongsToFieldDDs.put(DEFAULT_TECHNOLOGY,
                dataDefinitionService.get(TechnologiesConstants.PLUGIN_IDENTIFIER, MODEL_TECHNOLOGY));
        belongsToFieldDDs.put(PRODUCT, dataDefinitionService.get(BasicConstants.PLUGIN_IDENTIFIER, MODEL_PRODUCT));

        for (Map.Entry<String, DataDefinition> belongsToFieldDD : belongsToFieldDDs.entrySet()) {
            Object fieldValue = costCalculation.getField(belongsToFieldDD.getKey());
            if (!(fieldValue instanceof Long)) {
                continue;
            }
            Entity fieldEntity = belongsToFieldDD.getValue().get((Long) fieldValue);
            costCalculation.setField(belongsToFieldDD.getKey(), fieldEntity);
        }
    }

    // get values from form fields
    private Entity getEntityFromForm(final ViewDefinitionState view) {
        FormComponent form = (FormComponent) view.getComponentByReference(FORM);
        checkArgument(form != null, "form is null");
        checkArgument(form.isValid(), "invalid form");

        Long id = form.getEntityId();
        return form.getEntity().getDataDefinition().get(id);
    }

    private BigDecimal getBigDecimal(final Object value) {
        if (value == null) {
            return BigDecimal.ZERO;
        }
        if (value instanceof BigDecimal) {
            return (BigDecimal) value;
        }

        // MAKU - using BigDecimal.valueOf(Double) instead of new BigDecimal(String) to prevent issue described at
        // https://forums.oracle.com/forums/thread.jspa?threadID=2251030
        return BigDecimal.valueOf(Double.valueOf(value.toString()));
    }

    // put result values into proper form fields
    private void fillFields(final ViewDefinitionState view, final Entity costCalculation) {
        final Set<String> outputDecimalFields = Sets.newHashSet("productionCostMarginValue", "materialCostMarginValue",
                "totalOverhead", "totalMaterialCosts", "totalMachineHourlyCosts", "totalLaborHourlyCosts", "totalPieceworkCosts",
                "totalTechnicalProductionCosts", "totalCosts", "totalCostPerUnit", "additionalOverheadValue");

        for (String referenceName : outputDecimalFields) {
            FieldComponent fieldComponent = (FieldComponent) view.getComponentByReference(referenceName);
            fieldComponent.setFieldValue(getBigDecimal(costCalculation.getField(referenceName)));
        }
    }

    public void ifCurrentGlobalIsSelected(final ViewDefinitionState viewDefinitionState, final ComponentState state,
            final String[] args) {

        FieldComponent sourceOfMaterialCosts = (FieldComponent) viewDefinitionState
                .getComponentByReference("sourceOfMaterialCosts");
        FieldComponent calculateMaterialCostsMode = (FieldComponent) viewDefinitionState
                .getComponentByReference("calculateMaterialCostsMode");

        if (SourceOfMaterialCosts.CURRENT_GLOBAL_DEFINITIONS_IN_PRODUCT.getStringValue().equals(
                sourceOfMaterialCosts.getFieldValue())
                && (CalculateMaterialCostsMode.COST_FOR_ORDER.getStringValue().equals(calculateMaterialCostsMode.getFieldValue()))) {
            sourceOfMaterialCosts.addMessage("costCalculation.messages.optionUnavailable", MessageType.FAILURE);
        }
    }

    public void disableCheckboxIfPieceworkIsSelected(final ViewDefinitionState viewDefinitionState, final ComponentState state,
            final String[] args) {
        disableCheckboxIfPieceworkIsSelected(viewDefinitionState);
    }

    public void disableCheckboxIfPieceworkIsSelected(final ViewDefinitionState viewDefinitionState) {

        FieldComponent calculateOperationCostsMode = (FieldComponent) viewDefinitionState
                .getComponentByReference("calculateOperationCostsMode");

        FieldComponent includeTPZ = (FieldComponent) viewDefinitionState.getComponentByReference("includeTPZ");
        FieldComponent includeAdditionalTime = (FieldComponent) viewDefinitionState
                .getComponentByReference("includeAdditionalTime");

        FieldComponent machineHourlyCosts = (FieldComponent) viewDefinitionState
                .getComponentByReference("totalMachineHourlyCosts");
        FieldComponent machineHourlyCostsCurrency = (FieldComponent) viewDefinitionState
                .getComponentByReference("totalMachineHourlyCostsCurrency");

        FieldComponent totalLabourHourlyCosts = (FieldComponent) viewDefinitionState
                .getComponentByReference("totalLaborHourlyCosts");
        FieldComponent totalLaborHourlyCostsCurrency = (FieldComponent) viewDefinitionState
                .getComponentByReference("totalLaborHourlyCostsCurrency");

        FieldComponent totalPieceworkCosts = (FieldComponent) viewDefinitionState.getComponentByReference("totalPieceworkCosts");
        FieldComponent totalPieceworkCostsCurrency = (FieldComponent) viewDefinitionState
                .getComponentByReference("totalPieceworkCostsCurrency");

        if (CalculateOperationCostMode.PIECEWORK.getStringValue().equals(calculateOperationCostsMode.getFieldValue())) {
            includeTPZ.setFieldValue(false);
            includeTPZ.setEnabled(false);
            includeTPZ.requestComponentUpdateState();
            includeAdditionalTime.setFieldValue(false);
            includeAdditionalTime.setEnabled(false);
            includeAdditionalTime.requestComponentUpdateState();

            machineHourlyCosts.setVisible(false);
            machineHourlyCostsCurrency.setVisible(false);
            totalLabourHourlyCosts.setVisible(false);
            totalLaborHourlyCostsCurrency.setVisible(false);
            totalPieceworkCosts.setVisible(true);
            totalPieceworkCostsCurrency.setVisible(true);
        } else {
            machineHourlyCosts.setVisible(true);
            machineHourlyCostsCurrency.setVisible(true);
            totalLabourHourlyCosts.setVisible(true);
            totalLaborHourlyCostsCurrency.setVisible(true);
            totalPieceworkCosts.setVisible(false);
            totalPieceworkCostsCurrency.setVisible(false);

        }
    }

    public void changeOrderProduct(final ViewDefinitionState viewDefinitionState, final ComponentState state, final String[] args) {
        if (!(state instanceof FieldComponent)) {
            return;
        }
        FieldComponent product = (FieldComponent) state;
        FieldComponent technology = (FieldComponent) viewDefinitionState.getComponentByReference(TECHNOLOGY);
        FieldComponent defaultTechnology = (FieldComponent) viewDefinitionState.getComponentByReference(DEFAULT_TECHNOLOGY);

        if (viewDefinitionState.getComponentByReference(PRODUCT).getFieldValue() != null) {
            fillCostPerUnitUnitField(viewDefinitionState, state, args);
        }
        defaultTechnology.setFieldValue("");
        technology.setFieldValue(null);

        if (product.getFieldValue() != null) {
            Entity defaultTechnologyEntity = getDefaultTechnology((Long) product.getFieldValue());

            if (defaultTechnologyEntity != null) {
                technology.setFieldValue(defaultTechnologyEntity.getId());
                defaultTechnology.setFieldValue(defaultTechnologyEntity.getId());
            }
        }
    }

    private Entity getDefaultTechnology(final Long selectedProductId) {
        DataDefinition technologyDD = dataDefinitionService.get(TechnologiesConstants.PLUGIN_IDENTIFIER, MODEL_TECHNOLOGY);

        SearchCriteriaBuilder searchCriteria = technologyDD.find().add(SearchRestrictions.eq("master", true))
                .add(SearchRestrictions.belongsTo(PRODUCT, BasicConstants.PLUGIN_IDENTIFIER, MODEL_PRODUCT, selectedProductId));

        return searchCriteria.uniqueResult();
    }
}
