/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.1.2
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
package com.qcadoo.mes.costNormsForProduct;

import static com.google.common.base.Preconditions.checkArgument;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.qcadoo.mes.basic.constants.BasicConstants;
import com.qcadoo.mes.basic.util.CurrencyService;
import com.qcadoo.mes.costNormsForProduct.constants.CostNormsForProductConstants;
import com.qcadoo.mes.orders.constants.OrdersConstants;
import com.qcadoo.mes.technologies.ProductQuantitiesService;
import com.qcadoo.mes.technologies.TechnologyService;
import com.qcadoo.mes.technologies.constants.TechnologiesConstants;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ComponentState.MessageType;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.GridComponent;

@Service
public class CostNormsForProductService {

    private static final String FORM_L = "form";

    private static final String AVERAGE_COST_L = "averageCost";

    private static final String LAST_PURCHASE_COST_L = "lastPurchaseCost";

    private static final String NOMINAL_COST_L = "nominalCost";

    private static final String COST_FOR_NUMBER_L = "costForNumber";

    private static final String COST_FOR_NUMBER_UNIT_L = "costForNumberUnit";

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private CurrencyService currencyService;

    @Autowired
    private TechnologyService technologyService;

    @Autowired
    private ProductQuantitiesService productQuantitiesService;

    public void fillUnitFieldInProduct(final ViewDefinitionState viewDefinitionState) {
        fillUnitField(viewDefinitionState, COST_FOR_NUMBER_UNIT_L);
    }

    public void fillUnitFieldInOrder(final ViewDefinitionState viewDefinitionState) {
        fillUnitField(viewDefinitionState, COST_FOR_NUMBER_UNIT_L);
    }

    private void fillUnitField(final ViewDefinitionState viewDefinitionState, final String fieldName) {
        checkArgument(viewDefinitionState != null, "viewDefinitionState is null");

        FormComponent form = (FormComponent) viewDefinitionState.getComponentByReference(FORM_L);

        if (form == null || form.getEntityId() == null) {
            return;
        }

        Entity product = dataDefinitionService.get(BasicConstants.PLUGIN_IDENTIFIER, BasicConstants.MODEL_PRODUCT).get(
                form.getEntityId());

        if (product == null) {
            return;
        }

        String unit = product.getStringField("unit");

        FieldComponent unitField = (FieldComponent) viewDefinitionState.getComponentByReference(fieldName);

        fillField(unitField, unit);
    }

    public void fillCurrencyFieldsInProduct(final ViewDefinitionState viewDefinitionState) {
        fillCurrencyFields(viewDefinitionState, CostNormsForProductConstants.CURRENCY_FIELDS_PRODUCT);
    }

    public void fillCurrencyFieldsInOrder(final ViewDefinitionState viewDefinitionState) {
        fillCurrencyFields(viewDefinitionState, CostNormsForProductConstants.CURRENCY_FIELDS_ORDER);
    }

    public void fillCurrencyFields(final ViewDefinitionState viewDefinitionState, final Set<String> fieldNames) {
        checkArgument(viewDefinitionState != null, "viewDefinitionState is null");

        FormComponent form = (FormComponent) viewDefinitionState.getComponentByReference(FORM_L);

        if (form == null || form.getEntityId() == null) {
            return;
        }

        String currency = currencyService.getCurrencyAlphabeticCode();

        if (currency == null) {
            return;
        }

        for (String fieldName : fieldNames) {
            FieldComponent currencyField = (FieldComponent) viewDefinitionState.getComponentByReference(fieldName);

            fillField(currencyField, currency);
        }
    }

    public void fillField(final FieldComponent fieldComponent, final String fieldValue) {
        checkArgument(fieldComponent != null, "fieldComponent is null");

        fieldComponent.setFieldValue(fieldValue);
        fieldComponent.requestComponentUpdateState();
        fieldComponent.setEnabled(false);
    }

    public void fillInProductsGridInTechnology(final ViewDefinitionState viewDefinitionState) {
        checkArgument(viewDefinitionState != null, "viewDefinitionState is null");

        GridComponent grid = (GridComponent) viewDefinitionState.getComponentByReference("grid");

        FormComponent technology = (FormComponent) viewDefinitionState.getComponentByReference("technology");

        if ((technology == null) || (technology.getEntityId() == null)) {
            return;
        }

        Long technologyId = technology.getEntityId();

        List<Entity> inputProducts = Lists.newArrayList();

        Map<Entity, BigDecimal> productQuantities = getProductQuantitiesFromTechnology(technologyId);

        if (!productQuantities.isEmpty()) {
            for (Map.Entry<Entity, BigDecimal> productQuantity : productQuantities.entrySet()) {
                Entity product = productQuantity.getKey();
                BigDecimal quantity = productQuantity.getValue();

                Entity operationProductInComponent = dataDefinitionService.get(TechnologiesConstants.PLUGIN_IDENTIFIER,
                        TechnologiesConstants.MODEL_OPERATION_PRODUCT_IN_COMPONENT).create();

                operationProductInComponent.setField(BasicConstants.MODEL_PRODUCT, product);
                operationProductInComponent.setField("quantity", quantity);

                inputProducts.add(operationProductInComponent);
            }
        }

        grid.setEntities(inputProducts);
    }

    public void copyCostsFromProducts(final ViewDefinitionState viewDefinitionState, final ComponentState component,
            final String[] args) {
        checkArgument(viewDefinitionState != null, "viewDefinitionState is null");

        GridComponent grid = (GridComponent) viewDefinitionState.getComponentByReference("grid");

        FormComponent order = (FormComponent) viewDefinitionState.getComponentByReference("order");

        if ((order == null) || (order.getEntityId() == null)) {
            return;
        }

        Long orderId = order.getEntityId();

        List<Entity> inputProducts = Lists.newArrayList();

        Entity existingOrder = dataDefinitionService.get(OrdersConstants.PLUGIN_IDENTIFIER, OrdersConstants.MODEL_ORDER).get(
                orderId);

        List<Entity> orderOperationProductIncomponents = existingOrder
                .getHasManyField(CostNormsForProductConstants.ORDER_OPERATION_PRODUCT_IN_COMPONENTS);

        if (orderOperationProductIncomponents != null) {
            for (Entity orderOperationProductIncomponent : orderOperationProductIncomponents) {
                Entity product = orderOperationProductIncomponent.getBelongsToField(BasicConstants.MODEL_PRODUCT);

                orderOperationProductIncomponent.setField(COST_FOR_NUMBER_L, product.getField(COST_FOR_NUMBER_L));
                orderOperationProductIncomponent.setField(NOMINAL_COST_L, product.getField(NOMINAL_COST_L));
                orderOperationProductIncomponent.setField(LAST_PURCHASE_COST_L, product.getField(LAST_PURCHASE_COST_L));
                orderOperationProductIncomponent.setField(AVERAGE_COST_L, product.getField(AVERAGE_COST_L));

                orderOperationProductIncomponent = orderOperationProductIncomponent.getDataDefinition().save(
                        orderOperationProductIncomponent);

                inputProducts.add(orderOperationProductIncomponent);
            }
        }

        grid.setEntities(inputProducts);
    }

    public void fillOrderOperationProductsInComponents(final DataDefinition orderDD, final Entity order) {
        Entity technology = order.getBelongsToField(TechnologiesConstants.MODEL_TECHNOLOGY);

        if (!shouldFill(order, technology)) {
            return;
        }

        Long technologyId = technology.getId();

        List<Entity> orderOperationProductInComponents = Lists.newArrayList();

        Map<Entity, BigDecimal> productQuantities = getProductQuantitiesFromTechnology(technologyId);

        if (!productQuantities.isEmpty()) {
            for (Map.Entry<Entity, BigDecimal> productQuantity : productQuantities.entrySet()) {
                Entity product = productQuantity.getKey();

                Entity orderOperationProductInComponent = dataDefinitionService.get(
                        CostNormsForProductConstants.PLUGIN_IDENTIFIER,
                        CostNormsForProductConstants.MODEL_ORDER_OPERATION_PRODUCT_IN_COMPONENT).create();

                orderOperationProductInComponent.setField(OrdersConstants.MODEL_ORDER, order);
                orderOperationProductInComponent.setField(BasicConstants.MODEL_PRODUCT, product);
                orderOperationProductInComponent.setField(COST_FOR_NUMBER_L, product.getField(COST_FOR_NUMBER_L));
                orderOperationProductInComponent.setField(NOMINAL_COST_L, product.getField(NOMINAL_COST_L));
                orderOperationProductInComponent.setField(LAST_PURCHASE_COST_L, product.getField(LAST_PURCHASE_COST_L));
                orderOperationProductInComponent.setField(AVERAGE_COST_L, product.getField(AVERAGE_COST_L));

                orderOperationProductInComponent = orderOperationProductInComponent.getDataDefinition().save(
                        orderOperationProductInComponent);

                orderOperationProductInComponents.add(orderOperationProductInComponent);
            }
        }

        order.setField(CostNormsForProductConstants.ORDER_OPERATION_PRODUCT_IN_COMPONENTS, orderOperationProductInComponents);
    }

    private boolean shouldFill(final Entity order, final Entity technology) {
        return (technology != null) && (technology.getId() != null)
                && (hasTechnologyChanged(order, technology) || !hasOrderOperationProductInComponents(order));
    }

    private Map<Entity, BigDecimal> getProductQuantitiesFromTechnology(final Long technologyId) {
        Entity technology = dataDefinitionService.get(TechnologiesConstants.PLUGIN_IDENTIFIER,
                TechnologiesConstants.MODEL_TECHNOLOGY).get(technologyId);

        Entity operationComponentRoot = technology.getTreeField(TechnologiesConstants.OPERATION_COMPONENTS).getRoot();

        if (operationComponentRoot != null) {
            BigDecimal giventQty = technologyService.getProductCountForOperationComponent(operationComponentRoot);

            Map<Entity, BigDecimal> productQuantities = productQuantitiesService.getNeededProductQuantities(technology,
                    giventQty, true);

            return productQuantities;
        }

        return Maps.newHashMap();
    }

    private boolean hasOrderOperationProductInComponents(final Entity order) {
        return (order.getField(CostNormsForProductConstants.ORDER_OPERATION_PRODUCT_IN_COMPONENTS) != null);
    }

    private boolean hasTechnologyChanged(final Entity order, final Entity technology) {
        Entity existingOrder = getExistingOrder(order);
        if (existingOrder == null) {
            return false;
        }
        Entity existingOrderTechnology = existingOrder.getBelongsToField(TechnologiesConstants.MODEL_TECHNOLOGY);
        if (existingOrderTechnology == null) {
            return false;
        }
        return !existingOrderTechnology.equals(technology);
    }

    private Entity getExistingOrder(final Entity order) {
        if (order.getId() == null) {
            return null;
        }
        return order.getDataDefinition().get(order.getId());
    }

    public void checkTechnologyProductsInNorms(final ViewDefinitionState viewDefinitionState, final ComponentState triggerState,
            final String[] args) {
        ComponentState form = viewDefinitionState.getComponentByReference(FORM_L);

        if (form.getFieldValue() == null) {
            return;
        }

        Entity technology = dataDefinitionService.get(TechnologiesConstants.PLUGIN_IDENTIFIER,
                TechnologiesConstants.MODEL_TECHNOLOGY).get((Long) form.getFieldValue());
        List<Entity> operationComponents = dataDefinitionService
                .get(TechnologiesConstants.PLUGIN_IDENTIFIER, TechnologiesConstants.MODEL_TECHNOLOGY_OPERATION_COMPONENT).find()
                .add(SearchRestrictions.belongsTo(TechnologiesConstants.MODEL_TECHNOLOGY, technology)).list().getEntities();
        List<Entity> productInComponents = new ArrayList<Entity>();
        for (Entity operationComponent : operationComponents) {
            productInComponents.addAll(operationComponent.getHasManyField(TechnologiesConstants.OPERATION_PRODUCT_IN_COMPONENTS));
        }
        List<Entity> products = new ArrayList<Entity>();
        for (Entity productInComponent : productInComponents) {
            products.add(productInComponent.getBelongsToField(BasicConstants.MODEL_PRODUCT));
        }
        for (Entity product : products) {
            if (technologyService.getProductType(product, technology).equals(TechnologyService.COMPONENT)
                    && (product.getField(COST_FOR_NUMBER_L) == null || product.getField(NOMINAL_COST_L) == null
                            || product.getField(LAST_PURCHASE_COST_L) == null || product.getField(AVERAGE_COST_L) == null)) {
                form.addMessage("technologies.technologyDetails.error.inputProductsWithoutCostNorms", MessageType.INFO, false);
                break;
            }
        }
    }

    public void enabledFieldForExternalID(final ViewDefinitionState view) {
        FieldComponent nominalCost = (FieldComponent) view.getComponentByReference(NOMINAL_COST_L);
        FormComponent form = (FormComponent) view.getComponentByReference(FORM_L);
        if (form.getEntityId() == null) {
            return;
        }
        Entity entity = dataDefinitionService.get(BasicConstants.PLUGIN_IDENTIFIER, BasicConstants.MODEL_PRODUCT).get(
                form.getEntityId());

        if (entity == null) {
            return;
        }
        String externalNumber = entity.getStringField("externalNumber");

        if (externalNumber != null) {
            nominalCost.setEnabled(true);
        }
    }

    public final void showInputProductsCostInTechnology(final ViewDefinitionState viewState, final ComponentState componentState,
            final String[] args) {
        Long technologyId = (Long) componentState.getFieldValue();

        if (technologyId == null) {
            return;
        }

        Map<String, Object> parameters = Maps.newHashMap();
        parameters.put("technology.id", technologyId);

        String url = "../page/costNormsForProduct/costNormsForProductsInTechnologyList.html";
        viewState.redirectTo(url, false, true, parameters);
    }

    public final void showInputProductsCostInOrder(final ViewDefinitionState viewState, final ComponentState componentState,
            final String[] args) {
        Long orderId = (Long) componentState.getFieldValue();

        if (orderId == null) {
            return;
        }

        Map<String, Object> parameters = Maps.newHashMap();
        parameters.put("order.id", orderId);

        String url = "../page/costNormsForProduct/costNormsForProductsInOrderList.html";
        viewState.redirectTo(url, false, true, parameters);
    }
}
