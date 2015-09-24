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
package com.qcadoo.mes.basic;

import com.qcadoo.mes.basic.constants.*;
import com.qcadoo.mes.basic.util.UnitService;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.*;
import com.qcadoo.model.api.units.PossibleUnitConversions;
import com.qcadoo.model.api.units.UnitConversionService;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ComponentState.MessageType;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.GridComponent;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

import static com.qcadoo.mes.basic.constants.ProductFields.*;

@Service
public class ProductService {

    private static final String L_FORM = "form";

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private UnitService unitService;

    @Autowired
    private UnitConversionService unitConversionService;

    public Entity find(final SearchProjection projection, final SearchCriterion criteria, final SearchOrder order) {
        return prepareCriteriaBuilder(projection, criteria, order).setMaxResults(1).uniqueResult();
    }

    public List<Entity> findAll(final SearchProjection projection, final SearchCriterion criteria, final SearchOrder order) {
        return prepareCriteriaBuilder(projection, criteria, order).list().getEntities();
    }

    private SearchCriteriaBuilder prepareCriteriaBuilder(final SearchProjection projection, final SearchCriterion criteria,
            final SearchOrder order) {
        SearchCriteriaBuilder scb = getProductDataDefinition().find();
        if (projection != null) {
            scb.setProjection(projection);
        }
        if (criteria != null) {
            scb.add(criteria);
        }
        if (order != null) {
            scb.addOrder(order);
        }
        return scb;
    }

    private DataDefinition getProductDataDefinition() {
        return dataDefinitionService.get(BasicConstants.PLUGIN_IDENTIFIER, BasicConstants.MODEL_PRODUCT);
    }

    public boolean checkIfProductEntityTypeIsCorrect(final Entity product, final ProductFamilyElementType entityType) {
        return entityType.getStringValue().equals(product.getStringField(ENTITY_TYPE));
    }

    public void conversionForProductUnit(final Entity product) {
        final String productUnit = product.getStringField(ProductFields.UNIT);
        if (StringUtils.isNotEmpty(productUnit)) {
            final PossibleUnitConversions conversions = unitConversionService.getPossibleConversions(productUnit);
            product.setField(CONVERSION_ITEMS, conversions.asEntities(UnitConversionItemFieldsB.PRODUCT, product));
        }
    }

    public BigDecimal convertQuantityFromProductUnit(final Entity product, final BigDecimal quantity, final String targetUnit) {
        String productUnit = product.getStringField(ProductFields.UNIT);
        PossibleUnitConversions possibleUnitConversions = findPossibleUnitConversions(targetUnit, product);

        BigDecimal convertedQuantity = possibleUnitConversions.convertTo(quantity, productUnit);

        return convertedQuantity;
    }

    private PossibleUnitConversions findPossibleUnitConversions(final String unitName, final Entity productEntity) {
        CustomRestriction belongsToProductRestriction = new CustomRestriction() {

            @Override
            public void addRestriction(final SearchCriteriaBuilder scb) {
                scb.add(SearchRestrictions.belongsTo(UnitConversionItemFieldsB.PRODUCT, productEntity));
            }
        };

        PossibleUnitConversions possibleUnitConversions = unitConversionService.getPossibleConversions(unitName,
                belongsToProductRestriction);

        if (possibleUnitConversions == null) {
            possibleUnitConversions = unitConversionService.getPossibleConversions(unitName);
        }

        return possibleUnitConversions;
    }

    public boolean hasUnitChangedOnUpdate(final Entity product) {
        final Entity existingProduct = product.getDataDefinition().get(product.getId());
        final String existingProductUnit = existingProduct.getStringField(ProductFields.UNIT);
        final String currentUnit = product.getStringField(ProductFields.UNIT);
        if (existingProductUnit == null) {
            return currentUnit != null;
        } else {
            return !existingProductUnit.equals(currentUnit);
        }
    }

    public void getDefaultConversions(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        final FormComponent productForm = (FormComponent) view.getComponentByReference(L_FORM);
        if (productForm.getEntityId() == null) {
            return;
        }

        Entity product = productForm.getEntity();
        conversionForProductUnit(product);
        product = product.getDataDefinition().save(product);
        if(product.isValid()) {
            productForm.addMessage("basic.productDetails.message.getDefaultConversionsForProductSuccess", MessageType.SUCCESS);
            state.performEvent(view, "reset", new String[0]);
        } else {
            productForm.setEntity(product);
        }
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

    public boolean checkIfSubstituteIsNotRemoved(final DataDefinition substituteComponentDD, final Entity substituteComponent) {
        Entity substitute = substituteComponent.getBelongsToField(SubstituteComponentFields.SUBSTITUTE);

        if (substitute == null || substitute.getId() == null) {
            return true;
        }

        Entity substituteEntity = dataDefinitionService.get(BasicConstants.PLUGIN_IDENTIFIER, BasicConstants.MODEL_SUBSTITUTE)
                .get(substitute.getId());

        if (substituteEntity == null) {
            substituteComponent.addGlobalError("qcadooView.message.belongsToNotFound");
            substituteComponent.setField(SubstituteComponentFields.SUBSTITUTE, null);
            return false;
        } else {
            return true;
        }
    }

    public boolean checkSubstituteComponentUniqueness(final DataDefinition substituteComponentDD, final Entity substituteComponent) {
        Entity product = substituteComponent.getBelongsToField(SubstituteComponentFields.PRODUCT);
        Entity baseProduct = substituteComponent.getBelongsToField(SubstituteComponentFields.BASE_PRODUCT);

        if (baseProduct == null || product == null) {
            return false;
        }

        final SearchResult searchResult = substituteComponentDD.find()
                .add(SearchRestrictions.belongsTo(SubstituteComponentFields.PRODUCT, product))
                .add(SearchRestrictions.belongsTo(SubstituteComponentFields.BASE_PRODUCT, baseProduct)).list();

        if (searchResult.getTotalNumberOfEntities() > 0
                && !searchResult.getEntities().get(0).getId().equals(substituteComponent.getId())) {
            substituteComponent.addError(substituteComponentDD.getField(SubstituteComponentFields.PRODUCT),
                    "basic.validate.global.error.substituteComponentDuplicated");
            return false;
        } else {
            return true;
        }
    }

    public boolean checkIfProductIsNotRemoved(final DataDefinition substituteDD, final Entity substitute) {
        Entity product = substitute.getBelongsToField(SubstituteFields.PRODUCT);

        if (product == null || product.getId() == null) {
            return true;
        }

        Entity productEntity = dataDefinitionService.get(BasicConstants.PLUGIN_IDENTIFIER, BasicConstants.MODEL_PRODUCT).get(
                product.getId());

        if (productEntity == null) {
            substitute.addGlobalError("qcadooView.message.belongsToNotFound");
            substitute.setField(SubstituteFields.PRODUCT, null);
            return false;
        }

        return true;
    }

    public void fillUnit(final DataDefinition productDD, final Entity product) {
        if (product.getField(UNIT) == null) {
            product.setField(UNIT, unitService.getDefaultUnitFromSystemParameters());
        }
    }

}
