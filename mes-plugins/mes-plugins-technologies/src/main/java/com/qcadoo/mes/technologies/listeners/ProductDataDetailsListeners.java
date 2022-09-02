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

import com.qcadoo.mes.technologies.constants.*;
import com.qcadoo.mes.technologies.hooks.ProductDataDetailsHooks;
import com.qcadoo.mes.technologies.print.ProductDataPdfService;
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

import java.util.Date;
import java.util.List;
import java.util.Objects;

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

    @Autowired
    private ProductDataDetailsHooks productDataDetailsHooks;

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

        productDataService.createProductDataInputs(productData, operationProductInComponents);
    }

    private void generateProductDataOperations(final Entity productData, final EntityTree operations) {
        List<Entity> technologyOperationComponents = productDataService.getOperations(operations);

        productDataService.createProductDataOperations(productData, technologyOperationComponents);
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

    public void printReport(final ViewDefinitionState view, final ComponentState state, final String args[]) {
        FormComponent productDataForm = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);
        Entity productData = productDataForm.getPersistedEntityWithIncludedFormValues();

        state.setFieldValue(productData.getId());

        reportService.printGeneratedReport(view, state, new String[]{args[0],
                TechnologiesConstants.PLUGIN_IDENTIFIER, TechnologiesConstants.MODEL_PRODUCT_DATA});
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

        productDataDetailsHooks.setTechnologyFields(view, technology);
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
