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

import static com.qcadoo.mes.basic.constants.BasicConstants.MODEL_CONVERSION_ITEM;
import static com.qcadoo.mes.basic.constants.BasicConstants.PLUGIN_IDENTIFIER;
import static com.qcadoo.mes.basic.constants.ConversionItemFields.PRODUCT;
import static com.qcadoo.mes.basic.constants.ProductFields.CONVERSION_ITEMS;
import static com.qcadoo.mes.basic.constants.ProductFields.UNIT;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;
import com.qcadoo.mes.basic.constants.BasicConstants;
import com.qcadoo.mes.basic.constants.ProductFamilyElementType;
import com.qcadoo.mes.basic.constants.ProductFields;
import com.qcadoo.mes.basic.util.UnitService;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.model.api.search.SearchResult;
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

    private static final String L_SUBSTITUTE_FIELD = "substitute";

    private static final String PRODUCT_FIELD = "product";

    private static final String UNIT_FROM = "unitFrom";

    private static final String UNIT_TO = "unitTo";

    private static final String QUANTITY_FROM = "quantityFrom";

    private static final String QUANTITY_TO = "quantityTo";

    @Autowired
    private ConversionService conversionService;

    @Autowired
    private UnitService unitService;

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private NumberGeneratorService numberGeneratorService;

    public void conversionForProductUnit(final DataDefinition dataDefinition, final Entity product) {

        String productUnit = product.getStringField(UNIT);

        final DataDefinition conversionDD = dataDefinitionService.get(PLUGIN_IDENTIFIER, MODEL_CONVERSION_ITEM);

        final List<ConversionTree> conversionTreeList = Lists.newArrayList();

        final List<Entity> conversionListForProduct = Lists.newArrayList();

        ConversionTree root = new ConversionTree();

        root.setParent(null);

        root.setUnitTo(product.getStringField(UNIT));

        conversionTreeList.add(root);

        conversionService.getUnitConversionTree(conversionDD, productUnit, root, conversionTreeList);

        for (int i = 1; i < conversionTreeList.size(); i++) {

            conversionService.calculateTree(conversionTreeList.get(i), productUnit);

            Entity conversionItem = conversionDD.create();
            conversionItem.setField(QUANTITY_FROM, conversionTreeList.get(i).getQuantityFrom());
            conversionItem.setField(QUANTITY_TO, conversionTreeList.get(i).getQuantityTo());
            conversionItem.setField(UNIT_FROM, conversionTreeList.get(i).getUnitFrom());
            conversionItem.setField(UNIT_TO, conversionTreeList.get(i).getUnitTo());
            conversionListForProduct.add(conversionItem);

        }

        product.setField(CONVERSION_ITEMS, conversionListForProduct);

    }

    public void calculateConversionIfUnitChanged(final DataDefinition productDD, final Entity product) {
        String productUnit = product.getStringField(UNIT);

        if (hasUnitChanged(product, productUnit)) {
            conversionForProductUnit(productDD, product);
        }

    }

    private boolean hasUnitChanged(final Entity product, final String unit) {
        Entity existingProduct = getExistingProduct(product);
        if (existingProduct == null) {
            return true;
        }
        String existingProductUnit = existingProduct.getStringField(UNIT);
        if (existingProductUnit == null) {
            return true;
        }
        return !existingProductUnit.equals(unit);
    }

    private Entity getExistingProduct(final Entity product) {
        if (product.getId() == null) {
            return null;
        }
        return product.getDataDefinition().get(product.getId());
    }

    public void getDefaultConversions(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        FormComponent productForm = (FormComponent) view.getComponentByReference(L_FORM);

        if (productForm.getEntityId() == null) {
            return;
        }

        Entity product = productForm.getEntity();

        conversionForProductUnit(product.getDataDefinition(), product);

        product.getDataDefinition().save(product);

        productForm.addMessage("basic.productDetails.message.getDefaultConversionsForProductSuccess", MessageType.SUCCESS);

        state.performEvent(view, "reset", new String[0]);
    }

    public void getDefaultConversionsForGrid(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        GridComponent productsGrid = (GridComponent) view.getComponentByReference("grid");

        if (productsGrid.getSelectedEntities() == null) {
            return;
        }

        List<Entity> products = productsGrid.getSelectedEntities();

        for (Entity product : products) {
            conversionForProductUnit(product.getDataDefinition(), product);

            product.getDataDefinition().save(product);
        }

        productsGrid.addMessage("basic.productsList.message.getDefaultConversionsForProductsSuccess", MessageType.SUCCESS);
    }

    public void disableUnitFromWhenFormIsSaved(final ViewDefinitionState view) {
        FormComponent productForm = (FormComponent) view.getComponentByReference(L_FORM);

        AwesomeDynamicListComponent adl = (AwesomeDynamicListComponent) view.getComponentByReference(CONVERSION_ITEMS);

        if (productForm.getEntityId() == null) {
            adl.setEnabled(false);
        } else {
            adl.setEnabled(true);

            List<FormComponent> formComponents = adl.getFormComponents();

            for (FormComponent formComponent : formComponents) {
                if (formComponent.getEntityId() != null) {
                    formComponent.findFieldComponentByName(UNIT_FROM).setEnabled(false);
                } else {
                    formComponent.findFieldComponentByName(UNIT_FROM).setEnabled(true);
                }
            }
        }
    }

    public void generateProductNumber(final ViewDefinitionState state) {
        numberGeneratorService.generateAndInsertNumber(state, BasicConstants.PLUGIN_IDENTIFIER, BasicConstants.MODEL_PRODUCT,
                L_FORM, "number");
    }

    public boolean checkIfSubstituteIsNotRemoved(final DataDefinition dataDefinition, final Entity entity) {
        Entity substitute = entity.getBelongsToField(L_SUBSTITUTE_FIELD);

        if (substitute == null || substitute.getId() == null) {
            return true;
        }

        Entity substituteEntity = dataDefinitionService.get(BasicConstants.PLUGIN_IDENTIFIER, BasicConstants.MODEL_SUBSTITUTE)
                .get(substitute.getId());

        if (substituteEntity == null) {
            entity.addGlobalError("qcadooView.message.belongsToNotFound");
            entity.setField(L_SUBSTITUTE_FIELD, null);
            return false;
        } else {
            return true;
        }
    }

    public boolean checkSubstituteComponentUniqueness(final DataDefinition dataDefinition, final Entity entity) {
        Entity product = entity.getBelongsToField(PRODUCT);
        Entity substitute = entity.getBelongsToField(L_SUBSTITUTE_FIELD);

        if (substitute == null || product == null) {
            return false;
        }

        SearchResult searchResult = dataDefinition.find().add(SearchRestrictions.belongsTo(PRODUCT, product))
                .add(SearchRestrictions.belongsTo(L_SUBSTITUTE_FIELD, substitute)).list();

        if (searchResult.getTotalNumberOfEntities() > 0 && !searchResult.getEntities().get(0).getId().equals(entity.getId())) {
            entity.addError(dataDefinition.getField(PRODUCT), "basic.validate.global.error.substituteComponentDuplicated");
            return false;
        } else {
            return true;
        }
    }

    public boolean checkIfProductIsNotRemoved(final DataDefinition dataDefinition, final Entity entity) {
        Entity product = entity.getBelongsToField(PRODUCT);

        if (product == null || product.getId() == null) {
            return true;
        }

        Entity productEntity = dataDefinitionService.get(BasicConstants.PLUGIN_IDENTIFIER, BasicConstants.MODEL_PRODUCT).get(
                product.getId());

        if (productEntity == null) {
            entity.addGlobalError("qcadooView.message.belongsToNotFound");
            entity.setField(PRODUCT, null);
            return false;
        } else {
            return true;
        }
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
