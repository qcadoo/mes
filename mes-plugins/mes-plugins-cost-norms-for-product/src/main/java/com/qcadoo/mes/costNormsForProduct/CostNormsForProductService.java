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
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.qcadoo.localization.api.TranslationService;
import com.qcadoo.mes.basic.constants.BasicConstants;
import com.qcadoo.mes.basic.util.CurrencyService;
import com.qcadoo.mes.technologies.ProductQuantitiesService;
import com.qcadoo.mes.technologies.TechnologyService;
import com.qcadoo.mes.technologies.constants.TechnologiesConstants;
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

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private CurrencyService currencyService;

    @Autowired
    private TranslationService translationService;

    @Autowired
    private TechnologyService technologyService;

    @Autowired
    private ProductQuantitiesService productQuantitiesService;

    /* ****** VIEW HOOKS ******* */

    public void fillCostTabUnit(final ViewDefinitionState viewDefinitionState) {
        checkArgument(viewDefinitionState != null, "viewDefinitionState is null");
        FormComponent form = (FormComponent) viewDefinitionState.getComponentByReference("form");
        FieldComponent costUnit = (FieldComponent) viewDefinitionState.getComponentByReference("costTabUnit");
        if (form == null || costUnit == null) {
            return;
        }
        if (form.getEntityId() == null) {
            return;
        }
        Entity product = dataDefinitionService.get(BasicConstants.PLUGIN_IDENTIFIER, BasicConstants.MODEL_PRODUCT).get(
                form.getEntityId());

        if (product == null) {
            return;
        }

        costUnit.setFieldValue(product.getStringField("unit"));
        costUnit.requestComponentUpdateState();
        costUnit.setEnabled(false);
    }

    public void fillCostTabCurrency(final ViewDefinitionState viewDefinitionState) {
        checkArgument(viewDefinitionState != null, "viewDefinitionState is null");
        String currencyAlphabeticCode = currencyService.getCurrencyAlphabeticCode();
        if (currencyAlphabeticCode == null) {
            return;
        }
        for (String componentReference : Arrays.asList("nominalCostCurrency", "lastPurchaseCostCurrency", "averageCostCurrency")) {
            FieldComponent field = (FieldComponent) viewDefinitionState.getComponentByReference(componentReference);
            field.setEnabled(true);
            field.setFieldValue(currencyAlphabeticCode);
            field.setEnabled(false);
            field.requestComponentUpdateState();
        }
    }

    public void fillInProductsGridInTechnology(final ViewDefinitionState viewDefinitionState) {
        checkArgument(viewDefinitionState != null, "viewDefinitionState is null");
        GridComponent grid = (GridComponent) viewDefinitionState.getComponentByReference("grid");
        Long technologyId = ((FormComponent) viewDefinitionState.getComponentByReference("technology")).getEntityId();
        if (technologyId == null || grid == null) {
            return;
        }

        Entity technology = dataDefinitionService.get(TechnologiesConstants.PLUGIN_IDENTIFIER,
                TechnologiesConstants.MODEL_TECHNOLOGY).get(technologyId);

        Entity operationComponentRoot = technology.getTreeField("operationComponents").getRoot();

        List<Entity> products = Lists.newArrayList();

        if (operationComponentRoot == null) {
            return;
        } else {
            BigDecimal giventQty = technologyService.getProductCountForOperationComponent(operationComponentRoot);

            Map<Entity, BigDecimal> productQuantities = productQuantitiesService.getNeededProductQuantities(technology,
                    giventQty, true);

            for (Map.Entry<Entity, BigDecimal> productQuantity : productQuantities.entrySet()) {
                Entity proxyProduct = productQuantity.getKey();
                BigDecimal quantity = productQuantity.getValue();

                Entity operationProductInComponent = dataDefinitionService.get("technologies", "operationProductInComponent")
                        .create();

                operationProductInComponent.setField("product", proxyProduct);
                operationProductInComponent.setField("quantity", quantity);

                products.add(operationProductInComponent);
            }
        }

        grid.setEntities(products);
    }

    /* ******* MODEL HOOKS ******* */

    public void checkTechnologyProductsInNorms(final ViewDefinitionState viewDefinitionState, final ComponentState triggerState,
            final String[] args) {
        ComponentState form = viewDefinitionState.getComponentByReference("form");

        if (form.getFieldValue() == null) {
            return;
        }

        Entity technology = dataDefinitionService.get(TechnologiesConstants.PLUGIN_IDENTIFIER,
                TechnologiesConstants.MODEL_TECHNOLOGY).get((Long) form.getFieldValue());
        List<Entity> operationComponents = dataDefinitionService
                .get(TechnologiesConstants.PLUGIN_IDENTIFIER, TechnologiesConstants.MODEL_TECHNOLOGY_OPERATION_COMPONENT).find()
                .add(SearchRestrictions.belongsTo("technology", technology)).list().getEntities();
        List<Entity> productInComponents = new ArrayList<Entity>();
        for (Entity operationComponent : operationComponents) {
            productInComponents.addAll(operationComponent.getHasManyField("operationProductInComponents"));
        }
        List<Entity> products = new ArrayList<Entity>();
        for (Entity productInComponent : productInComponents) {
            products.add(productInComponent.getBelongsToField("product"));
        }
        for (Entity product : products) {
            if (technologyService.getProductType(product, technology).equals(TechnologyService.COMPONENT)
                    && (product.getField("costForNumber") == null || product.getField("nominalCost") == null
                            || product.getField("lastPurchaseCost") == null || product.getField("averageCost") == null)) {
                form.addMessage(translationService.translate(
                        "technologies.technologyDetails.error.inputProductsWithoutCostNorms", viewDefinitionState.getLocale()),
                        MessageType.INFO, false);
                break;
            }
        }
    }

    public void enabledFieldForExternalID(final ViewDefinitionState view) {
        FieldComponent nominalCost = (FieldComponent) view.getComponentByReference("nominalCost");
        FormComponent form = (FormComponent) view.getComponentByReference("form");
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
