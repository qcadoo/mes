/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.1.7
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
package com.qcadoo.mes.basic;

import static com.qcadoo.mes.basic.constants.ProductFields.CONVERSION_ITEMS;
import static com.qcadoo.mes.basic.constants.ProductFields.UNIT;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.basic.constants.BasicConstants;
import com.qcadoo.mes.basic.constants.ProductFamilyElementType;
import com.qcadoo.mes.basic.constants.ProductFields;
import com.qcadoo.mes.basic.constants.SubstituteComponentFields;
import com.qcadoo.mes.basic.constants.SubstituteFields;
import com.qcadoo.mes.basic.constants.UnitConversionItemFieldsB;
import com.qcadoo.mes.basic.util.UnitService;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.model.api.search.SearchResult;
import com.qcadoo.model.api.units.PossibleUnitConversions;
import com.qcadoo.model.api.units.UnitConversionService;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ComponentState.MessageType;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.AwesomeDynamicListComponent;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.GridComponent;
import com.qcadoo.view.api.utils.NumberGeneratorService;

@Service
public class ProductService {

    private static final String L_FORM = "form";

    private static final String UNIT_FROM = "unitFrom";

    @Autowired
    private UnitConversionService unitConversionService;

    @Autowired
    private UnitService unitService;

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private NumberGeneratorService numberGeneratorService;

    public void calculateConversionIfUnitChanged(final DataDefinition productDD, final Entity product) {
        if (hasUnitChangedOnUpdate(product, product.getStringField(ProductFields.UNIT))) {
            conversionForProductUnit(product);
        }
    }

    public void calculateConversionOnCreate(final DataDefinition productDD, final Entity product) {
        conversionForProductUnit(product);
    }

    private void conversionForProductUnit(final Entity product) {
        final String productUnit = product.getStringField(ProductFields.UNIT);
        final PossibleUnitConversions conversions = unitConversionService.getPossibleConversions(productUnit);
        product.setField(CONVERSION_ITEMS, conversions.asEntities(UnitConversionItemFieldsB.PRODUCT, product));
    }

    private boolean hasUnitChangedOnUpdate(final Entity product, final String unit) {
        Entity existingProduct = product.getDataDefinition().get(product.getId());
        String existingProductUnit = existingProduct.getStringField(ProductFields.UNIT);
        return !existingProductUnit.equals(unit);
    }

    public void getDefaultConversions(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        final FormComponent productForm = (FormComponent) view.getComponentByReference(L_FORM);
        if (productForm.getEntityId() == null) {
            return;
        }

        final Entity product = productForm.getEntity();
        conversionForProductUnit(product);
        product.getDataDefinition().save(product);
        productForm.addMessage("basic.productDetails.message.getDefaultConversionsForProductSuccess", MessageType.SUCCESS);
        state.performEvent(view, "reset", new String[0]);
    }

    public void getDefaultConversionsForGrid(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        final GridComponent productsGrid = (GridComponent) view.getComponentByReference("grid");
        if (productsGrid.getSelectedEntities().isEmpty()) {
            return;
        }

        final List<Entity> products = productsGrid.getSelectedEntities();
        for (Entity product : products) {
            conversionForProductUnit(product);
            product.getDataDefinition().save(product);
        }
        productsGrid.addMessage("basic.productsList.message.getDefaultConversionsForProductsSuccess", MessageType.SUCCESS);
    }

    public void disableUnitFromWhenFormIsSaved(final ViewDefinitionState view) {
        final FormComponent productForm = (FormComponent) view.getComponentByReference(L_FORM);
        final AwesomeDynamicListComponent conversionItemsAdl = (AwesomeDynamicListComponent) view
                .getComponentByReference(CONVERSION_ITEMS);

        conversionItemsAdl.setEnabled(productForm.getEntityId() != null);
        for (FormComponent formComponent : conversionItemsAdl.getFormComponents()) {
            formComponent.findFieldComponentByName(UNIT_FROM).setEnabled(formComponent.getEntityId() == null);
        }
    }

    public void generateProductNumber(final ViewDefinitionState state) {
        numberGeneratorService.generateAndInsertNumber(state, BasicConstants.PLUGIN_IDENTIFIER, BasicConstants.MODEL_PRODUCT,
                L_FORM, "number");
    }

    public boolean checkIfSubstituteIsNotRemoved(final DataDefinition dataDefinition, final Entity entity) {
        Entity substitute = entity.getBelongsToField(SubstituteComponentFields.SUBSTITUTE);

        if (substitute == null || substitute.getId() == null) {
            return true;
        }

        Entity substituteEntity = dataDefinitionService.get(BasicConstants.PLUGIN_IDENTIFIER, BasicConstants.MODEL_SUBSTITUTE)
                .get(substitute.getId());

        if (substituteEntity == null) {
            entity.addGlobalError("qcadooView.message.belongsToNotFound");
            entity.setField(SubstituteComponentFields.SUBSTITUTE, null);
            return false;
        } else {
            return true;
        }
    }

    public boolean checkSubstituteComponentUniqueness(final DataDefinition dataDefinition, final Entity entity) {
        Entity product = entity.getBelongsToField(SubstituteComponentFields.PRODUCT);
        Entity substitute = entity.getBelongsToField(SubstituteComponentFields.SUBSTITUTE);

        if (substitute == null || product == null) {
            return false;
        }

        final SearchResult searchResult = dataDefinition.find()
                .add(SearchRestrictions.belongsTo(SubstituteComponentFields.PRODUCT, product))
                .add(SearchRestrictions.belongsTo(SubstituteComponentFields.SUBSTITUTE, substitute)).list();

        if (searchResult.getTotalNumberOfEntities() > 0 && !searchResult.getEntities().get(0).getId().equals(entity.getId())) {
            entity.addError(dataDefinition.getField(SubstituteComponentFields.PRODUCT),
                    "basic.validate.global.error.substituteComponentDuplicated");
            return false;
        } else {
            return true;
        }
    }

    public boolean checkIfProductIsNotRemoved(final DataDefinition dataDefinition, final Entity entity) {
        Entity product = entity.getBelongsToField(SubstituteFields.PRODUCT);

        if (product == null || product.getId() == null) {
            return true;
        }

        Entity productEntity = dataDefinitionService.get(BasicConstants.PLUGIN_IDENTIFIER, BasicConstants.MODEL_PRODUCT).get(
                product.getId());

        if (productEntity == null) {
            entity.addGlobalError("qcadooView.message.belongsToNotFound");
            entity.setField(SubstituteFields.PRODUCT, null);
            return false;
        }

        return true;
    }

    public void disableProductFormForExternalItems(final ViewDefinitionState state) {
        FormComponent form = (FormComponent) state.getComponentByReference(L_FORM);

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
            form.setFormEnabled(false);
        }
    }

    public boolean clearExternalIdOnCopy(final DataDefinition dataDefinition, final Entity entity) {
        if (entity == null) {
            return true;
        }
        entity.setField("externalNumber", null);
        return true;
    }

    public void fillUnit(final ViewDefinitionState view) {
        FormComponent productForm = (FormComponent) view.getComponentByReference(L_FORM);

        FieldComponent unitField = (FieldComponent) view.getComponentByReference(UNIT);

        if ((productForm.getEntityId() == null) && (unitField.getFieldValue() == null)) {
            unitField.setFieldValue(unitService.getDefaultUnitFromSystemParameters());
            unitField.requestComponentUpdateState();
        }
    }

    public void fillUnit(final DataDefinition productDD, final Entity product) {
        if (product.getField(UNIT) == null) {
            product.setField(UNIT, unitService.getDefaultUnitFromSystemParameters());
        }
    }

    public boolean checkIfParentIsFamily(final DataDefinition productDD, final Entity product) {
        Entity parent = product.getBelongsToField(ProductFields.PARENT);
        if (parent == null) {
            return true;
        }
        if (ProductFamilyElementType.PRODUCTS_FAMILY.getStringValue().equals(parent.getStringField(ProductFields.ENTITY_TYPE))) {
            return true;
        } else {
            product.addError(productDD.getField(ProductFields.PARENT), "basic.product.parent.parentIsNotFamily");
            return false;
        }
    }

}
