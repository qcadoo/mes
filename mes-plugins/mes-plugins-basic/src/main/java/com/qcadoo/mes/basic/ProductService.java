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

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Maps;
import com.qcadoo.mes.basic.constants.BasicConstants;
import com.qcadoo.mes.basic.constants.ProductFields;
import com.qcadoo.mes.basic.constants.SubstituteComponentFields;
import com.qcadoo.mes.basic.constants.UnitConversionItemFieldsB;
import com.qcadoo.mes.basic.util.UnitService;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.CustomRestriction;
import com.qcadoo.model.api.search.SearchCriteriaBuilder;
import com.qcadoo.model.api.search.SearchCriterion;
import com.qcadoo.model.api.search.SearchOrder;
import com.qcadoo.model.api.search.SearchProjection;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.model.api.search.SearchResult;
import com.qcadoo.model.api.units.PossibleUnitConversions;
import com.qcadoo.model.api.units.UnitConversionService;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ComponentState.MessageType;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.GridComponent;
import com.qcadoo.view.constants.QcadooViewConstants;

@Service
public class ProductService {

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
        SearchCriteriaBuilder scb = getProductDD().find();

        if (Objects.nonNull(projection)) {
            scb.setProjection(projection);
        }
        if (Objects.nonNull(criteria)) {
            scb.add(criteria);
        }
        if (Objects.nonNull(order)) {
            scb.addOrder(order);
        }

        return scb;
    }

    public void conversionForProductUnit(final Entity product) {
        String unit = product.getStringField(ProductFields.UNIT);
        List<Entity> conversionItems = product.getHasManyField(ProductFields.CONVERSION_ITEMS);

        if (StringUtils.isNotEmpty(unit) && conversionItems.isEmpty()) {
            PossibleUnitConversions conversions = unitConversionService.getPossibleConversions(unit);

            product.setField(ProductFields.CONVERSION_ITEMS, conversions.asEntities(UnitConversionItemFieldsB.PRODUCT, product));
        }
    }

    public BigDecimal convertQuantityFromProductUnit(final Entity product, final BigDecimal quantity, final String targetUnit) {
        String unit = product.getStringField(ProductFields.UNIT);
        PossibleUnitConversions possibleUnitConversions = findPossibleUnitConversions(targetUnit, product);

        return possibleUnitConversions.convertTo(quantity, unit);
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

        if (Objects.isNull(possibleUnitConversions)) {
            possibleUnitConversions = unitConversionService.getPossibleConversions(unitName);
        }

        return possibleUnitConversions;
    }

    public boolean hasUnitChangedOnUpdate(final Entity product) {
        final Entity existingProduct = product.getDataDefinition().get(product.getId());
        final String existingProductUnit = existingProduct.getStringField(ProductFields.UNIT);
        final String currentUnit = product.getStringField(ProductFields.UNIT);

        if (Objects.isNull(existingProductUnit)) {
            return Objects.nonNull(currentUnit);
        } else {
            return !existingProductUnit.equals(currentUnit);
        }
    }

    public void cleanExternalNumber(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        FormComponent productForm = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);

        Entity product = productForm.getEntity();

        if (Objects.isNull(product.getId())) {
            return;
        }

        Entity productDb = getProductDD().get(product.getId());
        productDb.setField(ProductFields.EXTERNAL_NUMBER, null);
        productDb = getProductDD().save(productDb);
        productForm.setEntity(productDb);

    }

    public void openAdditionalDetails(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        FormComponent productForm = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);

        Entity product = productForm.getEntity();

        if (Objects.isNull(product.getId())) {
            return;
        }

        Map<String, Object> parameters = Maps.newHashMap();

        parameters.put("form.id", product.getId());

        String url = "../page/basic/productAdditionalDetails.html";
        view.redirectTo(url, false, true, parameters);
    }

    public void productFamilySizes(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        FormComponent productForm = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);

        Long productId = productForm.getEntityId();

        if (Objects.isNull(productId)) {
            return;
        }

        Map<String, Object> parameters = Maps.newHashMap();

        parameters.put("form.id", productId);

        String url = "../page/basic/productFamilySizes.html";
        view.redirectTo(url, false, true, parameters);
    }

    public void getDefaultConversions(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        FormComponent productForm = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);

        if (Objects.isNull(productForm.getEntityId())) {
            return;
        }

        Entity product = productForm.getEntity();
        conversionForProductUnit(product);

        product = product.getDataDefinition().save(product);

        if (product.isValid()) {
            productForm.addMessage("basic.productDetails.message.getDefaultConversionsForProductSuccess", MessageType.SUCCESS);

            state.performEvent(view, "reset");
        } else {
            productForm.setEntity(product);
        }
    }

    public void getDefaultConversionsForGrid(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        GridComponent productsGrid = (GridComponent) view.getComponentByReference(QcadooViewConstants.L_GRID);

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

    public boolean checkSubstituteComponentUniqueness(final DataDefinition substituteComponentDD,
            final Entity substituteComponent) {
        Entity product = substituteComponent.getBelongsToField(SubstituteComponentFields.PRODUCT);
        Entity baseProduct = substituteComponent.getBelongsToField(SubstituteComponentFields.BASE_PRODUCT);

        if (Objects.isNull(baseProduct) || Objects.isNull(product)) {
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

    public boolean checkIfProductIsNotRemoved(final DataDefinition dataDefinition, final Entity entity) {
        Entity product = entity.getBelongsToField(BasicConstants.MODEL_PRODUCT);

        if (Objects.isNull(product) || Objects.isNull(product.getId())) {
            return true;
        }

        Entity productEntity = getProductDD().get(product.getId());

        if (Objects.isNull(productEntity)) {
            entity.addGlobalError("qcadooView.message.belongsToNotFound");

            entity.setField(BasicConstants.MODEL_PRODUCT, null);

            return false;
        }

        return true;
    }

    public void fillUnit(final DataDefinition productDD, final Entity product) {
        if (Objects.isNull(product.getField(ProductFields.UNIT))) {
            product.setField(ProductFields.UNIT, unitService.getDefaultUnitFromSystemParameters());
        }
    }

    private DataDefinition getProductDD() {
        return dataDefinitionService.get(BasicConstants.PLUGIN_IDENTIFIER, BasicConstants.MODEL_PRODUCT);
    }

}
