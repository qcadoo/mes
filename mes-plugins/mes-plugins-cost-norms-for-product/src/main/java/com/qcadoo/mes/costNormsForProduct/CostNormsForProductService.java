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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.localization.api.TranslationService;
import com.qcadoo.mes.basic.constants.BasicConstants;
import com.qcadoo.mes.basic.util.CurrencyService;
import com.qcadoo.mes.technologies.TechnologyService;
import com.qcadoo.mes.technologies.constants.TechnologiesConstants;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchDisjunction;
import com.qcadoo.model.api.search.SearchOrders;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.model.api.search.SearchResult;
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

    public void fillInProductsGrid(final ViewDefinitionState viewDefinitionState) {
        checkArgument(viewDefinitionState != null, "viewDefinitionState is null");
        GridComponent grid = (GridComponent) viewDefinitionState.getComponentByReference("inProductsGrid");
        Long technologyId = ((FormComponent) viewDefinitionState.getComponentByReference("form")).getEntityId();
        if (technologyId == null || grid == null) {
            return;
        }
        Entity technology = dataDefinitionService.get(TechnologiesConstants.PLUGIN_IDENTIFIER,
                TechnologiesConstants.MODEL_TECHNOLOGY).get(technologyId);

        DataDefinition dd = dataDefinitionService.get(TechnologiesConstants.PLUGIN_IDENTIFIER,
                TechnologiesConstants.MODEL_OPERATION_PRODUCT_IN_COMPONENT);

        SearchDisjunction disjunction = SearchRestrictions.disjunction();
        for (Entity operationComponent : technology.getTreeField("operationComponents")) {
            disjunction.add(SearchRestrictions.belongsTo("operationComponent", operationComponent));
        }

        SearchResult searchResult = dd.find().add(disjunction).createAlias("product", "product")
                .addOrder(SearchOrders.asc("product.name")).list();

        grid.setEntities(searchResult.getEntities());
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
}
