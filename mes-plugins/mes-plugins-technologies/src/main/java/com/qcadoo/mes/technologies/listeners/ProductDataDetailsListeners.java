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
package com.qcadoo.mes.technologies.listeners;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.qcadoo.mes.basic.constants.ProductFields;
import com.qcadoo.mes.technologies.constants.*;
import com.qcadoo.mes.technologies.services.ProductDataPdfService;
import com.qcadoo.mes.technologies.services.ProductDataService;
import com.qcadoo.mes.technologies.tree.ProductStructureTreeService;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.EntityTree;
import com.qcadoo.model.api.file.FileService;
import com.qcadoo.model.api.search.SearchCriteriaBuilder;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.report.api.ReportService;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.LookupComponent;
import com.qcadoo.view.constants.QcadooViewConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;

@Service
public class ProductDataDetailsListeners {

    @Autowired
    private FileService fileService;

    @Autowired
    private ReportService reportService;

    @Autowired
    private ProductDataService productDataService;

    @Autowired
    private ProductStructureTreeService productStructureTreeService;

    @Autowired
    private ProductDataPdfService productDataPdfService;

    @Autowired
    private DataDefinitionService dataDefinitionService;

    public void printReport(final ViewDefinitionState view, final ComponentState state, final String args[]) {
        FormComponent productDataForm = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);
        Entity productData = productDataForm.getPersistedEntityWithIncludedFormValues();

        state.setFieldValue(productData.getId());

        reportService.printGeneratedReport(view, state, new String[]{args[0],
                TechnologiesConstants.PLUGIN_IDENTIFIER, TechnologiesConstants.MODEL_PRODUCT_DATA});
    }

    public void generateReport(final ViewDefinitionState view, final ComponentState state, final String args[]) {
        FormComponent productDataForm = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);

        state.performEvent(view, "save", new String[0]);

        if (state.isHasError()) {
            return;
        }

        Entity productData = productDataForm.getPersistedEntityWithIncludedFormValues();

        productData.setField(ProductDataFields.GENERATED, true);
        productData.setField(ProductDataFields.DATE_GENERATED, new Date());

        productDataForm.setEntity(productData);

        productData.getDataDefinition().save(productData);

        try {
            Entity productDataWithFileName = fileService.updateReportFileName(
                    productData, ProductDataFields.DATE_GENERATED,
                    "productData.productData.report.fileName");

            productDataPdfService.generateDocument(productDataWithFileName, state.getLocale());
        } catch (Exception e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    public void loadProductData(final ViewDefinitionState view, final ComponentState state, final String args[]) {
        FormComponent productDataForm = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);

        state.performEvent(view, "save", new String[0]);

        if (state.isHasError()) {
            return;
        }

        Entity productData = productDataForm.getPersistedEntityWithIncludedFormValues();

        deleteProductDataInputs(productData);
        deleteProductDataOperations(productData);

        Entity technology = productData.getBelongsToField(ProductDataFields.TECHNOLOGY);
        EntityTree operations = productStructureTreeService.getOperationComponentsFromTechnology(technology);

        generateProductDataInputs(productData, operations);
        generateProductDataOperations(productData, operations);

        productData.setField(ProductDataFields.LOADED, true);

        productDataForm.setEntity(productData);

        productData.getDataDefinition().save(productData);
    }

    private void deleteProductDataInputs(final Entity productData) {
        DataDefinition productDataInputDD = getProductDataInputDD();

        List<Entity> productDataInputs = productDataInputDD.find()
                .add(SearchRestrictions.eq("productData.id", productData.getId()))
                .list().getEntities();

        productDataInputs.forEach(productDataInput -> productDataInputDD.delete(productDataInput.getId()));
    }

    private void deleteProductDataOperations(final Entity productData) {
        DataDefinition productDataOperationDD = getProductDataOperationDD();

        List<Entity> productDataOperations = getProductDataOperationDD().find()
                .add(SearchRestrictions.eq("productData.id", productData.getId()))
                .list().getEntities();

        productDataOperations.forEach(productDataOperation -> productDataOperationDD.delete(productDataOperation.getId()));
    }

    private void generateProductDataInputs(final Entity productData, final EntityTree operations) {
        List<Entity> operationProductInComponents = productDataService.getOperationProductInComponents(operations);

        operationProductInComponents.forEach(operationProductInComponent -> {
            Entity product = operationProductInComponent.getBelongsToField(OperationProductInComponentFields.PRODUCT);

            String name = product.getStringField(ProductFields.NAME);
            String number = product.getStringField(ProductFields.NUMBER);
            BigDecimal quantity = operationProductInComponent.getDecimalField(OperationProductInComponentFields.GIVEN_QUANTITY);
            String unit = operationProductInComponent.getStringField(OperationProductInComponentFields.GIVEN_UNIT);

            if (Objects.isNull(quantity)) {
                quantity = operationProductInComponent.getDecimalField(OperationProductInComponentFields.QUANTITY);

                unit = product.getStringField(ProductFields.UNIT);
            }

            Entity productDataInput = getProductDataInputDD().create();

            productDataInput.setField(ProductDataInputFields.PRODUCT_DATA, productData);
            productDataInput.setField(ProductDataInputFields.PRODUCT, product);
            productDataInput.setField(ProductDataInputFields.NAME, name);
            productDataInput.setField(ProductDataInputFields.NUMBER, number);
            productDataInput.setField(ProductDataInputFields.QUANTITY, quantity);
            productDataInput.setField(ProductDataInputFields.UNIT, unit);

            productDataInput.getDataDefinition().save(productDataInput);
        });
    }

    private void generateProductDataOperations(final Entity productData, final EntityTree operations) {
        List<Entity> technologyOperationComponents = productDataService.getOperations(operations);

        technologyOperationComponents.forEach(technologyOperationComponent -> {
            Entity operation = technologyOperationComponent.getBelongsToField(TechnologyOperationComponentFields.OPERATION);

            String name = operation.getStringField(OperationFields.NAME);
            String number = operation.getStringField(OperationFields.NUMBER);

            String description = technologyOperationComponent.getStringField(TechnologyOperationComponentFields.COMMENT);

            Entity productDataOperation = getProductDataOperationDD().create();

            productDataOperation.setField(ProductDataOperationFields.PRODUCT_DATA, productData);
            productDataOperation.setField(ProductDataOperationFields.TECHNOLOGY_OPERATION_COMPONENT, technologyOperationComponent);
            productDataOperation.setField(ProductDataOperationFields.NAME, name);
            productDataOperation.setField(ProductDataOperationFields.NUMBER, number);
            productDataOperation.setField(ProductDataOperationFields.DESCRIPTION, description);

            productDataOperation.getDataDefinition().save(productDataOperation);
        });
    }

    public void onTechnologyChange(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        LookupComponent technologyLookup = (LookupComponent) view.getComponentByReference(ProductDataFields.TECHNOLOGY);

        if (!(state instanceof FieldComponent)) {
            return;
        }

        Entity technology = technologyLookup.getEntity();

        if (Objects.isNull(technology)) {
            return;
        }

        applyValuesToFields(view, technology);
    }

    private void applyValuesToFields(final ViewDefinitionState view, final Entity technology) {
        if (Objects.isNull(technology)) {
            clearFieldValues(view);

            return;
        }

        Set<String> referenceNames = Sets.newHashSet(ProductDataFields.PRODUCT, ProductDataFields.TECHNOLOGY);

        Map<String, FieldComponent> componentsMap = Maps.newHashMap();

        for (String referenceName : referenceNames) {
            FieldComponent fieldComponent = (FieldComponent) view.getComponentByReference(referenceName);

            componentsMap.put(referenceName, fieldComponent);
        }

        componentsMap.get(ProductDataFields.TECHNOLOGY).setFieldValue(technology.getId());
        componentsMap.get(ProductDataFields.PRODUCT).setFieldValue(
                technology.getBelongsToField(TechnologyFields.PRODUCT).getId());
    }

    private void clearFieldValues(final ViewDefinitionState view) {
        view.getComponentByReference(ProductDataFields.TECHNOLOGY).setFieldValue(null);
        view.getComponentByReference(ProductDataFields.PRODUCT).setFieldValue(null);
    }

    public void onProductChange(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        LookupComponent productLookup = (LookupComponent) state;
        LookupComponent technologyLookup = (LookupComponent) view.getComponentByReference(ProductDataFields.TECHNOLOGY);

        if (!(state instanceof LookupComponent)) {
            return;
        }

        Entity product = productLookup.getEntity();
        Entity technology = technologyLookup.getEntity();

        if (Objects.isNull(product)) {
            return;
        }

        Entity defaultTechnology = getDefaultTechnology(product);

        if (Objects.isNull(technology)) {
            if (Objects.nonNull(defaultTechnology)) {
                technologyLookup.setFieldValue(defaultTechnology.getId());
            }
        } else {
            Entity technologyProduct = technology.getBelongsToField(ProductDataFields.PRODUCT);

            if (!product.getId().equals(technologyProduct.getId())) {
                if (Objects.isNull(defaultTechnology)) {
                    technologyLookup.setFieldValue(null);
                } else {
                    technologyLookup.setFieldValue(defaultTechnology.getId());
                }
            }
        }

        technologyLookup.requestComponentUpdateState();
    }

    private Entity getDefaultTechnology(final Entity product) {
        SearchCriteriaBuilder searchCriteriaBuilder = getTechnologyDD().find()
                .add(SearchRestrictions.eq(TechnologyFields.MASTER, true))
                .add(SearchRestrictions.belongsTo(TechnologyFields.PRODUCT, product));

        return searchCriteriaBuilder.setMaxResults(1).uniqueResult();
    }

    private DataDefinition getTechnologyDD() {
        return dataDefinitionService.get(TechnologiesConstants.PLUGIN_IDENTIFIER, TechnologiesConstants.MODEL_TECHNOLOGY);
    }

    private DataDefinition getProductDataInputDD() {
        return dataDefinitionService.get(TechnologiesConstants.PLUGIN_IDENTIFIER, TechnologiesConstants.MODEL_PRODUCT_DATA_INPUT);
    }

    private DataDefinition getProductDataOperationDD() {
        return dataDefinitionService.get(TechnologiesConstants.PLUGIN_IDENTIFIER, TechnologiesConstants.MODEL_PRODUCT_DATA_OPERATION);
    }

}
