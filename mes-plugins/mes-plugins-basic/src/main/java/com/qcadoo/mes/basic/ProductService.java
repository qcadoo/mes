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

import static com.qcadoo.mes.basic.constants.ProductFields.UNIT;

import java.math.BigDecimal;
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
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.utils.NumberGeneratorService;

@Service
public final class ProductService {

    private static final String L_FORM = "form";

    private static final String SUBSTITUTE_FIELD = "substitute";

    private static final String PRODUCT_FIELD = "product";

    private static final String UNIT_FROM = "unitFrom";

    private static final String UNIT_TO = "unitTo";

    private static final String QUANTITY_FROM = "quantityFrom";

    private static final String QUANTITY_TO = "quantityTo";

    private static final String PARENT = "parent";

    @Autowired
    private UnitService unitService;

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private NumberGeneratorService numberGeneratorService;

    public void conversionTree(final DataDefinition conversionDD, String unit, ConversionTree parent,
            List<ConversionTree> conversionTreeList, int x) {

        if (x > conversionTreeList.size() - 1) {
            return;
        } else {

            if (conversionTreeList.get(x) != null) {

                final List<Entity> left = conversionDD.find().add(SearchRestrictions.eq(UNIT_FROM, unit))
                        .add(SearchRestrictions.neField(UNIT_FROM, UNIT_TO)).add(SearchRestrictions.isNull(PRODUCT_FIELD)).list()
                        .getEntities();

                final List<Entity> right = conversionDD.find().add(SearchRestrictions.eq(UNIT_TO, unit))
                        .add(SearchRestrictions.neField(UNIT_FROM, UNIT_TO)).add(SearchRestrictions.isNull(PRODUCT_FIELD)).list()
                        .getEntities();

                if (!left.isEmpty()) {

                    for (int i = 0; i < left.size(); i++) {

                        if (checkList(conversionTreeList, left.get(i).getStringField(UNIT_TO))) {

                            ConversionTree ct = new ConversionTree();
                            ct.setQuantityFrom((BigDecimal) left.get(i).getField(QUANTITY_FROM));
                            ct.setQuantityTo((BigDecimal) left.get(i).getField(QUANTITY_TO));
                            ct.setUnitFrom(left.get(i).getStringField(UNIT_FROM));
                            ct.setUnitTo(left.get(i).getStringField(UNIT_TO));
                            ct.setParent(parent);
                            conversionTreeList.add(ct);
                        }
                    }

                }
                if (!right.isEmpty()) {

                    for (int i = 0; i < right.size(); i++) {

                        if (checkList(conversionTreeList, right.get(i).getStringField(UNIT_FROM))) {

                            ConversionTree ct = new ConversionTree();
                            ct.setQuantityFrom((BigDecimal) right.get(i).getField(QUANTITY_TO));
                            ct.setQuantityTo((BigDecimal) right.get(i).getField(QUANTITY_FROM));
                            ct.setUnitFrom(right.get(i).getStringField(UNIT_TO));
                            ct.setUnitTo(right.get(i).getStringField(UNIT_FROM));
                            ct.setParent(parent);
                            conversionTreeList.add(ct);
                        }
                    }
                }
                x++;

                conversionTree(conversionDD, conversionTreeList.get(x).getUnitTo(), conversionTreeList.get(x),
                        conversionTreeList, x);

            } else {
                return;
            }
        }
    }

    public boolean checkList(List<ConversionTree> list, String unit) {
        if (!list.isEmpty()) {
            for (int j = 0; j < list.size(); j++) {
                if (unit.equals(list.get(j).getUnitTo())) {
                    return false;
                }
            }

            return true;
        }
        return true;
    }

    public void conversionForProductUnit(final DataDefinition dataDefinition, final Entity product) {

        String productUnit = product.getStringField("unit");

        final DataDefinition conversionDD = dataDefinitionService.get("basic", "conversionItem");

        ConversionTree root = new ConversionTree();
        root.setParent(null);

        root.setUnitTo(product.getStringField("unit"));

        final List<ConversionTree> conversionTreeList = Lists.newArrayList();

        conversionTreeList.add(root);

        conversionTree(conversionDD, productUnit, root, conversionTreeList, 0);

        // final List<Entity> left = conversionDD.find().add(SearchRestrictions.eq(UNIT_FROM, productUnit))
        // .add(SearchRestrictions.neField(UNIT_FROM, UNIT_TO)).add(SearchRestrictions.isNull(PRODUCT_FIELD)).list()
        // .getEntities();
        //
        // final List<Entity> conversionListTo = conversionDD.find().add(SearchRestrictions.eq(UNIT_TO, productUnit))
        // .add(SearchRestrictions.neField(UNIT_FROM, UNIT_TO)).add(SearchRestrictions.isNull(PRODUCT_FIELD)).list()
        // .getEntities();

        // final List<Entity> conversionListForProduct = Lists.newArrayList();

        // if (!left.isEmpty()) {
        //
        // if (!conversionMap.containsKey(left.get(0).getStringField(UNIT_TO))) {
        //
        // conversionMap.put(left.get(0).getStringField(UNIT_TO), (BigDecimal) left.get(0).getField(QUANTITY_TO));
        //
        // }
        // for (Entity conversion : left) {
        //
        // Entity conversionItem = conversionDD.create();
        // conversionItem.setField(QUANTITY_FROM, conversion.getField(QUANTITY_FROM));
        // conversionItem.setField(QUANTITY_TO, conversion.getField(QUANTITY_TO));
        // conversionItem.setField(UNIT_FROM, conversion.getField(UNIT_FROM));
        // conversionItem.setField(UNIT_TO, conversion.getField(UNIT_TO));
        // conversionListForProduct.add(conversionItem);
        //
        // }
        // }
        // if (!conversionListTo.isEmpty()) {
        //
        // for (Entity conversion : conversionListTo) {
        //
        // Entity conversionItem = conversionDD.create();
        // conversionItem.setField(QUANTITY_FROM, conversion.getField(QUANTITY_TO));
        // conversionItem.setField(QUANTITY_TO, conversion.getField(QUANTITY_FROM));
        // conversionItem.setField(UNIT_FROM, conversion.getField(UNIT_TO));
        // conversionItem.setField(UNIT_TO, conversion.getField(UNIT_FROM));
        // conversionListForProduct.add(conversionItem);
        //
        // }
        // }

        // entity.setField("productConversion", conversionListForProduct);

    }

    public void generateProductNumber(final ViewDefinitionState state) {
        numberGeneratorService.generateAndInsertNumber(state, BasicConstants.PLUGIN_IDENTIFIER, BasicConstants.MODEL_PRODUCT,
                L_FORM, "number");
    }

    public boolean checkIfSubstituteIsNotRemoved(final DataDefinition dataDefinition, final Entity entity) {
        Entity substitute = entity.getBelongsToField(SUBSTITUTE_FIELD);

        if (substitute == null || substitute.getId() == null) {
            return true;
        }

        Entity substituteEntity = dataDefinitionService.get(BasicConstants.PLUGIN_IDENTIFIER, BasicConstants.MODEL_SUBSTITUTE)
                .get(substitute.getId());

        if (substituteEntity == null) {
            entity.addGlobalError("qcadooView.message.belongsToNotFound");
            entity.setField(SUBSTITUTE_FIELD, null);
            return false;
        } else {
            return true;
        }
    }

    public boolean checkSubstituteComponentUniqueness(final DataDefinition dataDefinition, final Entity entity) {
        Entity product = entity.getBelongsToField(PRODUCT_FIELD);
        Entity substitute = entity.getBelongsToField(SUBSTITUTE_FIELD);

        if (substitute == null || product == null) {
            return false;
        }

        SearchResult searchResult = dataDefinition.find().add(SearchRestrictions.belongsTo(PRODUCT_FIELD, product))
                .add(SearchRestrictions.belongsTo(SUBSTITUTE_FIELD, substitute)).list();

        if (searchResult.getTotalNumberOfEntities() > 0 && !searchResult.getEntities().get(0).getId().equals(entity.getId())) {
            entity.addError(dataDefinition.getField(PRODUCT_FIELD), "basic.validate.global.error.substituteComponentDuplicated");
            return false;
        } else {
            return true;
        }
    }

    public boolean checkIfProductIsNotRemoved(final DataDefinition dataDefinition, final Entity entity) {
        Entity product = entity.getBelongsToField(PRODUCT_FIELD);

        if (product == null || product.getId() == null) {
            return true;
        }

        Entity productEntity = dataDefinitionService.get(BasicConstants.PLUGIN_IDENTIFIER, BasicConstants.MODEL_PRODUCT).get(
                product.getId());

        if (productEntity == null) {
            entity.addGlobalError("qcadooView.message.belongsToNotFound");
            entity.setField(PRODUCT_FIELD, null);
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
