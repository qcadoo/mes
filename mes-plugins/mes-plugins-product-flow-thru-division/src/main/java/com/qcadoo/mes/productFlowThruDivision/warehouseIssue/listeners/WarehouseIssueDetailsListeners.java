/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo Framework
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
package com.qcadoo.mes.productFlowThruDivision.warehouseIssue.listeners;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.qcadoo.localization.api.TranslationService;
import com.qcadoo.mes.basic.constants.ProductFields;
import com.qcadoo.mes.productFlowThruDivision.constants.ProductFlowThruDivisionConstants;
import com.qcadoo.mes.productFlowThruDivision.service.WarehouseIssueService;
import com.qcadoo.mes.productFlowThruDivision.warehouseIssue.constans.CollectionProducts;
import com.qcadoo.mes.productFlowThruDivision.warehouseIssue.constans.ProductsToIssueFields;
import com.qcadoo.mes.productFlowThruDivision.warehouseIssue.constans.WarehouseIssueFields;
import com.qcadoo.mes.productFlowThruDivision.warehouseIssue.hooks.IssueDetailsHooks;
import com.qcadoo.mes.productFlowThruDivision.warehouseIssue.hooks.WarehouseIssueHooks;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.validators.ErrorMessage;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.GridComponent;
import com.qcadoo.view.api.components.LookupComponent;
import com.qcadoo.view.constants.QcadooViewConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class WarehouseIssueDetailsListeners {

    @Autowired
    private WarehouseIssueHooks warehouseIssueHooks;

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private WarehouseIssueService warehouseIssueService;

    @Autowired
    private IssueDetailsHooks issueDetailsHooks;

    @Autowired
    private TranslationService translationService;

    public void showProductAttributes(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        GridComponent positionGird = (GridComponent) view.getComponentByReference("productsToIssues");
        Set<Long> ids = positionGird.getSelectedEntitiesIds();
        if (ids.size() == 1) {
            Entity productToIssue = dataDefinitionService.get(ProductFlowThruDivisionConstants.PLUGIN_IDENTIFIER,
                    ProductFlowThruDivisionConstants.MODEL_PRODUCTS_TO_ISSUE).get(ids.stream().findFirst().get());
            Map<String, Object> parameters = Maps.newHashMap();
            parameters.put("form.id", productToIssue.getBelongsToField(ProductsToIssueFields.PRODUCT).getId());
            view.redirectTo("/page/materialFlowResources/productAttributesForPositionList.html", false, true, parameters);
        } else {
            view.addMessage("productFlowThruDivision.warehouseIssueDetails.showProductAttributes.toManyPositionsSelected",
                    ComponentState.MessageType.INFO);
        }
    }

    public void fillProductsToIssue(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        FormComponent issueForm = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);
        FieldComponent collectionProductsField = (FieldComponent) view
                .getComponentByReference(WarehouseIssueFields.COLLECTION_PRODUCTS);
        LookupComponent operation = (LookupComponent) view
                .getComponentByReference(WarehouseIssueFields.TECHNOLOGY_OPERATION_COMPONENT);
        LookupComponent division = (LookupComponent) view.getComponentByReference(WarehouseIssueFields.DIVISION);

        Long warehouseIssueId = issueForm.getEntityId();

        Entity toc = operation.getEntity();
        Entity divisionEntity = division.getEntity();

        List<Entity> createdProducts = warehouseIssueService.fillProductsToIssue(warehouseIssueId,
                CollectionProducts.fromStringValue(collectionProductsField.getFieldValue().toString()), toc, divisionEntity);

        if (createdProducts != null) {
            List<Entity> invalidProducts = createdProducts.stream().filter(productToIssue -> !productToIssue.isValid())
                    .collect(Collectors.toList());

            if (invalidProducts.isEmpty()) {
                view.addMessage("productFlowThruDivision.issue.downloadedProducts", ComponentState.MessageType.SUCCESS);
            } else {
                Multimap<String, String> errors = ArrayListMultimap.create();

                for (Entity productToIssue : invalidProducts) {
                    String number = productToIssue.getBelongsToField(ProductsToIssueFields.PRODUCT).getStringField(
                            ProductFields.NUMBER);
                    Map<String, ErrorMessage> errorMessages = productToIssue.getErrors();
                    errorMessages.entrySet().stream().forEach(entry -> errors.put(entry.getValue().getMessage(), number));
                    productToIssue.getGlobalErrors().stream().forEach(error -> errors.put(error.getMessage(), number));
                }

                view.addMessage("productFlowThruDivision.issue.downloadedProductsError", ComponentState.MessageType.INFO, false);

                for (String message : errors.keySet()) {
                    String translatedMessage = translationService.translate(message, LocaleContextHolder.getLocale());
                    String products = errors.get(message).stream().collect(Collectors.joining(", "));
                    if ((translatedMessage + products).length() < 255) {
                        view.addMessage("productFlowThruDivision.issue.downloadedProductsErrorMessages",
                                ComponentState.MessageType.FAILURE, false, translatedMessage, products);
                    }
                }
            }
        } else {
            view.addMessage("productFlowThruDivision.issue.noProductsToDownload", ComponentState.MessageType.INFO);
        }
    }

    public void copyProductsToIssue(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        FormComponent issueForm = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);
        Long warehouseIssueId = issueForm.getEntityId();

        if (warehouseIssueId == null) {
            return;
        }

        Entity warehouseIssue = warehouseIssueHooks.getWarehouseIssueDD().get(warehouseIssueId);

        warehouseIssue.getDataDefinition().save(warehouseIssue);
        GridComponent grid = (GridComponent) view.getComponentByReference(WarehouseIssueFields.PRODUCTS_TO_ISSUES);

        if (grid.getSelectedEntities().isEmpty()) {
            return;
        }

        for (Entity productToIssue : grid.getSelectedEntities()) {
            createIssue(view, warehouseIssue, productToIssue);
        }

        view.performEvent(view, "reset");
        view.addMessage("productFlowThruDivision.issue.copiedProducts", ComponentState.MessageType.SUCCESS);
    }

    private void createIssue(final ViewDefinitionState view, final Entity warehouseIssue, final Entity productToIssue) {
       warehouseIssueService.createIssue(warehouseIssue, productToIssue);
    }

    public void onCollectionProductsChange(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        FieldComponent collectionProductsField = (FieldComponent) view
                .getComponentByReference(WarehouseIssueFields.COLLECTION_PRODUCTS);
        LookupComponent operation = (LookupComponent) view
                .getComponentByReference(WarehouseIssueFields.TECHNOLOGY_OPERATION_COMPONENT);
        LookupComponent division = (LookupComponent) view.getComponentByReference(WarehouseIssueFields.DIVISION);

        if (collectionProductsField.getFieldValue().equals(CollectionProducts.ON_ORDER.getStringValue())) {
            division.setFieldValue(null);
            division.requestComponentUpdateState();
            operation.setFieldValue(null);
            operation.requestComponentUpdateState();
        } else if (collectionProductsField.getFieldValue().equals(CollectionProducts.ON_DIVISION.getStringValue())) {
            operation.setFieldValue(null);
            operation.requestComponentUpdateState();
        } else if (collectionProductsField.getFieldValue().equals(CollectionProducts.ON_OPERATION.getStringValue())) {
            division.setFieldValue(null);
            division.requestComponentUpdateState();
        }
    }

    public void onOrderChange(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        LookupComponent technologyOperationComponentLookup = (LookupComponent) view
                .getComponentByReference(WarehouseIssueFields.TECHNOLOGY_OPERATION_COMPONENT);

        technologyOperationComponentLookup.setFieldValue(null);
        technologyOperationComponentLookup.requestComponentUpdateState();

        GridComponent grid = (GridComponent) view.getComponentByReference(WarehouseIssueFields.PRODUCTS_TO_ISSUES);
        grid.setFieldValue(null);
        grid.setEntities(new ArrayList<>());
    }

    public void fillUnits(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        issueDetailsHooks.onBeforeRender(view);
    }

}
