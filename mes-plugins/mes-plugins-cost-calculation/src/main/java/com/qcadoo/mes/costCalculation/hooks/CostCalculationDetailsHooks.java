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
package com.qcadoo.mes.costCalculation.hooks;

import com.google.common.base.Optional;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.qcadoo.commons.functional.Either;
import com.qcadoo.mes.basic.ParameterService;
import com.qcadoo.mes.basic.constants.ProductFields;
import com.qcadoo.mes.basic.util.CurrencyService;
import com.qcadoo.mes.costCalculation.constants.CostCalculationConstants;
import com.qcadoo.mes.costCalculation.constants.CostCalculationFields;
import com.qcadoo.mes.orders.constants.OrderFields;
import com.qcadoo.mes.orders.constants.OrderType;
import com.qcadoo.mes.orders.constants.OrdersConstants;
import com.qcadoo.mes.technologies.constants.TechnologiesConstants;
import com.qcadoo.mes.technologies.constants.TechnologyFields;
import com.qcadoo.model.api.BigDecimalUtils;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.NumberService;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.CheckBoxComponent;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.LookupComponent;
import com.qcadoo.view.api.components.WindowComponent;
import com.qcadoo.view.api.components.lookup.FilterValueHolder;
import com.qcadoo.view.api.ribbon.RibbonActionItem;
import com.qcadoo.view.api.utils.NumberGeneratorService;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;

import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CostCalculationDetailsHooks {

    private static final String L_FORM = "form";

    private static final String L_WINDOW = "window";

    private static final String L_PRODUCTION_COST_MARGIN_PROC = "productionCostMarginProc";

    private static final String L_MATERIAL_COST_MARGIN_PROC = "materialCostMarginProc";

    private static final String L_REGISTRATION_PRICE_OVERHEAD_PROC = "registrationPriceOverheadProc";

    private static final String L_PROFIT_PROC = "profitProc";

    private static final String L_ADDITIONAL_OVERHEAD_CURRENCY = "additionalOverheadCurrency";

    private static final String L_TOTAL_COST_PER_UNIT_UNIT = "totalCostPerUnitUnit";

    private static final String L_TOTAL_MACHINE_HOURLY_COSTS_CURRENCY = "totalMachineHourlyCostsCurrency";

    private static final String L_TOTAL_LABOR_HOURLY_COSTS_CURRENCY = "totalLaborHourlyCostsCurrency";

    private static final String L_TOTAL_PIECEWORK_COSTS_CURRENCY = "totalPieceworkCostsCurrency";

    private static final String L_MINIMAL_QUANTITY = "minimalQuantity";

    @Autowired
    private NumberGeneratorService numberGeneratorService;

    @Autowired
    private CurrencyService currencyService;

    @Autowired
    private ParameterService parameterService;

    @Autowired
    private NumberService numbersService;

    @Autowired
    private DataDefinitionService dataDefinitionService;

    public void setCriteriaModifierParameters(final ViewDefinitionState view) {
        LookupComponent orderLookup = (LookupComponent) view.getComponentByReference(CostCalculationFields.ORDER);
        LookupComponent technologyLookup = (LookupComponent) view.getComponentByReference(CostCalculationFields.TECHNOLOGY);
        LookupComponent productLookup = (LookupComponent) view.getComponentByReference(CostCalculationFields.PRODUCT);

        Entity order = orderLookup.getEntity();
        Entity product = productLookup.getEntity();

        FilterValueHolder filterValueHolder = technologyLookup.getFilterValue();

        if (order == null) {
            filterValueHolder.remove(CostCalculationFields.TECHNOLOGY);
        } else {
            String orderType = order.getStringField(OrderFields.ORDER_TYPE);

            Entity technology = null;

            if (OrderType.WITH_PATTERN_TECHNOLOGY.getStringValue().equals(orderType)) {
                technology = order.getBelongsToField(OrderFields.TECHNOLOGY_PROTOTYPE);
            } else {
                technology = order.getBelongsToField(OrderFields.TECHNOLOGY);
            }

            if (technology == null) {
                filterValueHolder.remove(CostCalculationFields.TECHNOLOGY);
            } else {
                filterValueHolder.put(CostCalculationFields.TECHNOLOGY, technology.getId());
            }
        }

        if (product == null) {
            filterValueHolder.remove(CostCalculationFields.PRODUCT);
        } else {
            filterValueHolder.put(CostCalculationFields.PRODUCT, product.getId());
        }

        technologyLookup.setFilterValue(filterValueHolder);
    }

    public void onBeforeRender(final ViewDefinitionState view) throws JSONException {
        if (view.isViewAfterRedirect()) {
            JSONObject json = view.getJsonContext();
            Entity technology = null;
            Entity order = null;
            if (json.has("window.mainTab.technologyId")) {
                Long techId = json.getLong("window.mainTab.technologyId");
                technology = dataDefinitionService.get(TechnologiesConstants.PLUGIN_IDENTIFIER,
                        TechnologiesConstants.MODEL_TECHNOLOGY).get(techId);
            } else if (json.has("window.mainTab.orderId")) {
                Long orderId = json.getLong("window.mainTab.orderId");
                order = dataDefinitionService.get(OrdersConstants.PLUGIN_IDENTIFIER, OrdersConstants.MODEL_ORDER).get(orderId);

                technology = getTechnologyFromOrder(order);
            }
            if(Objects.nonNull(order) || Objects.nonNull(technology)) {
                applyValuesToFields(view, technology, order);
            }
        }
        setCriteriaModifierParameters(view);
        setFieldsEnabled(view);
        generateNumber(view);
        fillCurrencyFields(view);
        fillOverheadsFromParameters(view);
        roundResults(view);
        setButtonEnabled(view);
    }

    private void roundResults(final ViewDefinitionState view) {
        Set<String> referenceNames = Sets.newHashSet(CostCalculationFields.TOTAL_MATERIAL_COSTS,
                CostCalculationFields.TOTAL_MACHINE_HOURLY_COSTS, CostCalculationFields.TOTAL_LABOR_HOURLY_COSTS,
                CostCalculationFields.TOTAL_PIECEWORK_COSTS, CostCalculationFields.TOTAL_TECHNICAL_PRODUCTION_COSTS,
                CostCalculationFields.PRODUCTION_COST_MARGIN_VALUE, CostCalculationFields.MATERIAL_COST_MARGIN_VALUE,
                CostCalculationFields.ADDITIONAL_OVERHEAD_VALUE, CostCalculationFields.TOTAL_OVERHEAD,
                CostCalculationFields.TOTAL_COSTS, CostCalculationFields.TOTAL_COST_PER_UNIT,
                CostCalculationFields.REGISTRATION_PRICE_OVERHEAD_VALUE, CostCalculationFields.TECHNICAL_PRODUCTION_COSTS,
                CostCalculationFields.PROFIT_VALUE, CostCalculationFields.SELL_PRICE_VALUE);

        for (String name : referenceNames) {
            FieldComponent component = (FieldComponent) view.getComponentByReference(name);
            String value = (String) component.getFieldValue();
            Either<Exception, Optional<BigDecimal>> eitherValue = BigDecimalUtils.tryParse(value, view.getLocale());
            if (eitherValue.isRight()) {
                Optional<BigDecimal> maybeValue = eitherValue.getRight();
                if (maybeValue.isPresent()) {
                    component.setFieldValue(numbersService.format(numbersService.setScaleWithDefaultMathContext(maybeValue.get(),
                            2)));
                    component.requestComponentUpdateState();
                }
            }
        }
    }

    public void setFieldsEnabled(final ViewDefinitionState view) {
        Set<String> referenceNames = Sets.newHashSet(CostCalculationFields.PRODUCT, CostCalculationFields.ORDER,
                CostCalculationFields.QUANTITY, CostCalculationFields.TECHNOLOGY, CostCalculationFields.NUMBER,
                CostCalculationFields.PRODUCTION_LINE, CostCalculationFields.DESCRIPTION,
                CostCalculationFields.CALCULATE_MATERIAL_COSTS_MODE, CostCalculationFields.PRODUCTION_COST_MARGIN,
                CostCalculationFields.MATERIAL_COST_MARGIN, CostCalculationFields.ADDITIONAL_OVERHEAD,
                CostCalculationFields.PRINT_COST_NORMS_OF_MATERIALS, CostCalculationFields.PRINT_OPERATION_NORMS,
                CostCalculationFields.INCLUDE_TPZ, CostCalculationFields.INCLUDE_ADDITIONAL_TIME,
                CostCalculationFields.SOURCE_OF_MATERIAL_COSTS, CostCalculationFields.SOURCE_OF_OPERATION_COSTS,
                CostCalculationFields.REGISTRATION_PRICE_OVERHEAD, CostCalculationFields.PROFIT, L_PRODUCTION_COST_MARGIN_PROC,
                L_MATERIAL_COST_MARGIN_PROC, L_ADDITIONAL_OVERHEAD_CURRENCY);

        Map<String, FieldComponent> componentsMap = Maps.newHashMap();

        for (String referenceName : referenceNames) {
            FieldComponent fieldComponent = (FieldComponent) view.getComponentByReference(referenceName);
            componentsMap.put(referenceName, fieldComponent);
        }

        CheckBoxComponent generatedField = (CheckBoxComponent) view.getComponentByReference(CostCalculationFields.GENERATED);

        boolean isGenerated = generatedField.isChecked();

        for (Entry<String, FieldComponent> entry : componentsMap.entrySet()) {
            entry.getValue().setEnabled(!isGenerated);
        }

        if (componentsMap.get(CostCalculationFields.TECHNOLOGY).getFieldValue() == null) {
            componentsMap.get(CostCalculationFields.PRODUCT).setEnabled(true);
            componentsMap.get(CostCalculationFields.QUANTITY).setEnabled(true);
            componentsMap.get(CostCalculationFields.ORDER).setEnabled(true);
            componentsMap.get(CostCalculationFields.TECHNOLOGY).setEnabled(true);
        }
    }

    public void generateNumber(final ViewDefinitionState view) {
        numberGeneratorService.generateAndInsertNumber(view, CostCalculationConstants.PLUGIN_IDENTIFIER,
                CostCalculationConstants.MODEL_COST_CALCULATION, L_FORM, CostCalculationFields.NUMBER);
    }

    public void fillCurrencyFields(final ViewDefinitionState viewDefinitionState) {
        final String currencyAlphabeticCode = currencyService.getCurrencyAlphabeticCode();

        Set<String> referenceNames = Sets.newHashSet("totalCostsCurrency", "totalOverheadCurrency",
                "additionalOverheadValueCurrency", "materialCostMarginValueCurrency", "productionCostMarginValueCurrency",
                "totalTechnicalProductionCostsCurrency", L_TOTAL_PIECEWORK_COSTS_CURRENCY, L_TOTAL_LABOR_HOURLY_COSTS_CURRENCY,
                L_TOTAL_MACHINE_HOURLY_COSTS_CURRENCY, "totalMaterialCostsCurrency", L_ADDITIONAL_OVERHEAD_CURRENCY,
                "profitValueCurrency", "registrationPriceOverheadValueCurrency");

        for (String referenceName : referenceNames) {
            FieldComponent fieldComponent = (FieldComponent) viewDefinitionState.getComponentByReference(referenceName);

            fieldComponent.setFieldValue(currencyAlphabeticCode);
            fieldComponent.requestComponentUpdateState();
        }

        fillComponentWithPercent(L_PRODUCTION_COST_MARGIN_PROC, viewDefinitionState);
        fillComponentWithPercent(L_MATERIAL_COST_MARGIN_PROC, viewDefinitionState);
        fillComponentWithPercent(L_REGISTRATION_PRICE_OVERHEAD_PROC, viewDefinitionState);
        fillComponentWithPercent(L_PROFIT_PROC, viewDefinitionState);

        fillCostPerUnitUnitField(viewDefinitionState);
    }

    private void fillComponentWithPercent(String componentName, ViewDefinitionState viewDefinitionState) {
        FieldComponent materialCostMarginProc = (FieldComponent) viewDefinitionState.getComponentByReference(componentName);
        materialCostMarginProc.setFieldValue("%");
        materialCostMarginProc.requestComponentUpdateState();
    }

    public void fillCostPerUnitUnitField(final ViewDefinitionState view) {
        final String currencyAlphabeticCode = currencyService.getCurrencyAlphabeticCode();

        FieldComponent totalCostPerUnitUnit = (FieldComponent) view.getComponentByReference(L_TOTAL_COST_PER_UNIT_UNIT);
        FieldComponent sellPriceValueCurrency = (FieldComponent) view.getComponentByReference("sellPriceValueCurrency");
        FieldComponent technicalProductionCostsCurrency = (FieldComponent) view
                .getComponentByReference("technicalProductionCostsCurrency");
        LookupComponent productField = (LookupComponent) view.getComponentByReference(CostCalculationFields.PRODUCT);

        Entity product = productField.getEntity();

        if (product == null) {
            return;
        }
        String unit = currencyAlphabeticCode + " / " + product.getStringField(ProductFields.UNIT);
        totalCostPerUnitUnit.setFieldValue(unit);
        totalCostPerUnitUnit.requestComponentUpdateState();
        sellPriceValueCurrency.setFieldValue(unit);
        sellPriceValueCurrency.requestComponentUpdateState();
        technicalProductionCostsCurrency.setFieldValue(unit);
        technicalProductionCostsCurrency.requestComponentUpdateState();

    }

    private void fillOverheadsFromParameters(ViewDefinitionState view) {
        FormComponent form = (FormComponent) view.getComponentByReference("form");
        Boolean justCreated = form.getEntity().getBooleanField("justCreated");
        if (form.getEntityId() == null && justCreated) {
            fillWithProperty("sourceOfMaterialCosts", "sourceOfMaterialCostsPB", view);
            fillWithProperty("calculateMaterialCostsMode", "calculateMaterialCostsModePB", view);
            fillWithProperty("sourceOfOperationCosts", "sourceOfOperationCostsPB", view);

            fillWithPropertyOrZero("productionCostMargin", "productionCostMarginPB", view);
            fillWithPropertyOrZero("materialCostMargin", "materialCostMarginPB", view);
            fillWithPropertyOrZero("additionalOverhead", "additionalOverheadPB", view);
            fillWithPropertyOrZero("registrationPriceOverhead", "registrationPriceOverheadPB", view);
            fillWithPropertyOrZero("profit", "profitPB", view);
            view.getComponentByReference("justCreated").setFieldValue(false);
        }
    }

    private void fillWithProperty(String componentName, String propertyName, ViewDefinitionState view) {
        FieldComponent component = (FieldComponent) view.getComponentByReference(componentName);
        String propertyValue = parameterService.getParameter().getStringField(propertyName);
        if (propertyValue != null) {
            component.setFieldValue(propertyValue);
        }
    }

    private void fillWithPropertyOrZero(String componentName, String propertyName, ViewDefinitionState view) {
        FieldComponent component = (FieldComponent) view.getComponentByReference(componentName);
        if (component.getFieldValue() == null) {
            BigDecimal propertyValue = parameterService.getParameter().getDecimalField(propertyName);

            if (propertyValue != null) {
                String formattedProductionCostMargin = numbersService.formatWithMinimumFractionDigits(propertyValue.setScale(2),
                        0);
                component.setFieldValue(formattedProductionCostMargin);
            } else {
                component.setFieldValue(0);
            }
        }
    }

    private void setButtonEnabled(ViewDefinitionState view) {
        WindowComponent window = (WindowComponent) view.getComponentByReference(L_WINDOW);
        RibbonActionItem saveNominalCosts = window.getRibbon().getGroupByName(CostCalculationFields.SAVE_COSTS)
                .getItemByName(CostCalculationFields.NOMINAL_COSTS);
        CheckBoxComponent generatedField = (CheckBoxComponent) view.getComponentByReference(CostCalculationFields.GENERATED);
        if (generatedField.isChecked()) {
            saveNominalCosts.setEnabled(true);
            saveNominalCosts.requestUpdate(true);
        }
    }

    private Entity getTechnologyFromOrder(final Entity order) {
        Entity technology = null;

        String orderType = order.getStringField(OrderFields.ORDER_TYPE);

        if (OrderType.WITH_PATTERN_TECHNOLOGY.getStringValue().equals(orderType)) {
            technology = order.getBelongsToField(OrderFields.TECHNOLOGY_PROTOTYPE);
        } else {
            technology = order.getBelongsToField(OrderFields.TECHNOLOGY);
        }

        return technology;
    }

    public void applyValuesToFields(final ViewDefinitionState view, final Entity technology, final Entity order) {
        if (technology == null) {
            clearFieldValues(view, order);
            return;
        }

        Boolean cameFromOrder = false;
        Boolean cameFromTechnology = false;

        Set<String> referenceNames = Sets.newHashSet(CostCalculationFields.PRODUCTION_LINE,
                CostCalculationFields.DEFAULT_TECHNOLOGY, CostCalculationFields.PRODUCT, CostCalculationFields.ORDER,
                CostCalculationFields.QUANTITY, CostCalculationFields.TECHNOLOGY);

        Map<String, FieldComponent> componentsMap = Maps.newHashMap();

        for (String referenceName : referenceNames) {
            FieldComponent fieldComponent = (FieldComponent) view.getComponentByReference(referenceName);
            componentsMap.put(referenceName, fieldComponent);
        }

        if (order == null) {
            cameFromTechnology = true;
        } else {
            cameFromOrder = true;
        }

        if (cameFromOrder) {
            componentsMap.get(CostCalculationFields.ORDER).setFieldValue(order.getId());
            componentsMap.get(CostCalculationFields.DEFAULT_TECHNOLOGY).setEnabled(false);
            if (order.getBelongsToField(CostCalculationFields.PRODUCTION_LINE) != null) {
                componentsMap.get(CostCalculationFields.PRODUCTION_LINE).setFieldValue(
                        order.getBelongsToField(CostCalculationFields.PRODUCTION_LINE).getId());
            }
            componentsMap.get(CostCalculationFields.QUANTITY).setFieldValue(
                    numbersService.format(order.getField(OrderFields.PLANNED_QUANTITY)));
        } else {
            componentsMap.get(CostCalculationFields.ORDER).setFieldValue(null);
            componentsMap.get(CostCalculationFields.DEFAULT_TECHNOLOGY).setEnabled(false);
            componentsMap.get(CostCalculationFields.QUANTITY).setFieldValue(
                    numbersService.format(technology.getField(L_MINIMAL_QUANTITY)));
        }

        componentsMap.get(CostCalculationFields.ORDER).setEnabled(cameFromOrder);
        componentsMap.get(CostCalculationFields.TECHNOLOGY).setFieldValue(technology.getId());
        componentsMap.get(CostCalculationFields.TECHNOLOGY).setEnabled(cameFromTechnology);
        componentsMap.get(CostCalculationFields.DEFAULT_TECHNOLOGY).setFieldValue(technology.getId());
        componentsMap.get(CostCalculationFields.QUANTITY).setEnabled(!cameFromOrder);
        componentsMap.get(CostCalculationFields.PRODUCT).setFieldValue(
                technology.getBelongsToField(TechnologyFields.PRODUCT).getId());
        componentsMap.get(CostCalculationFields.PRODUCT).setEnabled(false);
    }

    private void clearFieldValues(final ViewDefinitionState view, final Entity order) {
        if(Objects.nonNull(order)) {
            view.getComponentByReference(CostCalculationFields.ORDER)
                    .addMessage("costCalculation.messages.lackOfTechnology", ComponentState.MessageType.FAILURE);
        }
        view.getComponentByReference(CostCalculationFields.DEFAULT_TECHNOLOGY).setFieldValue(null);
        view.getComponentByReference(CostCalculationFields.TECHNOLOGY).setFieldValue(null);
        view.getComponentByReference(CostCalculationFields.QUANTITY).setFieldValue(null);
        view.getComponentByReference(CostCalculationFields.PRODUCT).setFieldValue(null);
        view.getComponentByReference(CostCalculationFields.PRODUCTION_LINE).setFieldValue(null);
    }
}
