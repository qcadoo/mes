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
package com.qcadoo.mes.materialFlowResources;

import com.google.common.collect.Maps;
import com.qcadoo.mes.basic.constants.BasicConstants;
import com.qcadoo.mes.basic.util.CurrencyService;
import com.qcadoo.mes.materialFlow.constants.MaterialFlowConstants;
import com.qcadoo.mes.materialFlowResources.constants.MaterialFlowResourcesConstants;
import com.qcadoo.mes.materialFlowResources.constants.ResourceFields;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.NumberService;
import com.qcadoo.model.api.search.SearchOrders;
import com.qcadoo.model.api.search.SearchQueryBuilder;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import static com.qcadoo.mes.basic.constants.BasicConstants.MODEL_PRODUCT;
import static com.qcadoo.mes.basic.constants.ProductFields.UNIT;

@Service
public class MaterialFlowResourcesServiceImpl implements MaterialFlowResourcesService {

    private static final String L_PRICE_CURRENCY = "priceCurrency";

    public static final String QUANTITY_UNIT = "quantityUNIT";

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private NumberService numberService;

    @Autowired
    private CurrencyService currencyService;

    @Override
    public BigDecimal getResourcesQuantityForLocationAndProduct(final Entity location, final Entity product) {
        List<Entity> resources = getResourcesForLocationAndProduct(location, product);

        if (Objects.isNull(resources)) {
            return null;
        } else {
            BigDecimal resourcesQuantity = BigDecimal.ZERO;

            for (Entity resource : resources) {
                resourcesQuantity = resourcesQuantity.add(resource.getDecimalField(ResourceFields.QUANTITY),
                        numberService.getMathContext());
            }

            return resourcesQuantity;
        }
    }

    @Override
    public List<Entity> getWarehouseLocationsFromDB() {
        return getLocationDD().find().list().getEntities();
    }

    @Override
    public List<Entity> getResourcesForLocationAndProduct(final Entity location, final Entity product) {
        return getResourceDD().find().add(SearchRestrictions.belongsTo(ResourceFields.LOCATION, location))
                .add(SearchRestrictions.belongsTo(ResourceFields.PRODUCT, product))
                .addOrder(SearchOrders.asc(ResourceFields.TIME)).list().getEntities();
    }

    @Override
    public Map<Long, BigDecimal> getQuantitiesForProductsAndLocation(final List<Entity> products, final Entity location) {
        return getQuantitiesForProductsAndLocation(products, location, false);
    }

    @Override
    public Map<Long, BigDecimal> getQuantitiesForProductsAndLocation(final List<Entity> products, final Entity location,
            final boolean withoutBlockedForQualityControl) {
        Map<Long, BigDecimal> quantities = Maps.newHashMap();

        if (products.size() > 0) {
            StringBuilder sb = new StringBuilder();

            sb.append("SELECT p.id AS product, SUM(r.quantity) AS quantity ");
            sb.append("FROM #materialFlowResources_resource AS r ");
            sb.append("JOIN r.product AS p ");
            sb.append("JOIN r.location AS l ");
            if (withoutBlockedForQualityControl) {
                sb.append("WHERE r.blockedForQualityControl = false ");
            }
            sb.append("GROUP BY p.id, l.id ");
            sb.append("HAVING p.id IN (:productIds) ");
            sb.append("AND l.id = :locationId ");

            SearchQueryBuilder sqb = getResourceDD().find(sb.toString());

            sqb.setParameter("locationId", location.getId());
            sqb.setParameterList("productIds", products.stream().map(Entity::getId).collect(Collectors.toList()));

            List<Entity> productsAndQuantities = sqb.list().getEntities();

            productsAndQuantities.forEach(productAndQuantity -> quantities.put((Long) productAndQuantity.getField("product"),
                    productAndQuantity.getDecimalField("quantity")));
        }

        return quantities;
    }

    @Override
    public Map<Long, BigDecimal> getQuantitiesForProductIdsAndLocation(final List<Long> ids, final Long locationId) {
        Map<Long, BigDecimal> quantities = Maps.newHashMap();

        if (ids.size() > 0) {
            StringBuilder sb = new StringBuilder();

            sb.append("SELECT p.id AS product, SUM(r.quantity) AS quantity ");
            sb.append("FROM #materialFlowResources_resource AS r ");
            sb.append("JOIN r.product AS p ");
            sb.append("JOIN r.location AS l ");

            sb.append("GROUP BY p.id, l.id ");
            sb.append("HAVING p.id IN (:productIds) ");
            sb.append("AND l.id = :locationId ");

            SearchQueryBuilder sqb = getResourceDD().find(sb.toString());

            sqb.setParameter("locationId", locationId);
            sqb.setParameterList("productIds", ids);

            List<Entity> productsAndQuantities = sqb.list().getEntities();

            productsAndQuantities.forEach(productAndQuantity -> quantities.put((Long) productAndQuantity.getField("product"),
                    productAndQuantity.getDecimalField("quantity")));
        }

        return quantities;
    }

    private DataDefinition getLocationDD() {
        return dataDefinitionService.get(MaterialFlowConstants.PLUGIN_IDENTIFIER, MaterialFlowConstants.MODEL_LOCATION);
    }

    private DataDefinition getResourceDD() {
        return dataDefinitionService.get(MaterialFlowResourcesConstants.PLUGIN_IDENTIFIER,
                MaterialFlowResourcesConstants.MODEL_RESOURCE);
    }

    public void fillUnitFieldValues(final ViewDefinitionState view) {
        Long productId = (Long) view.getComponentByReference(ResourceFields.PRODUCT).getFieldValue();
        if (productId == null) {
            return;
        }
        Entity product = dataDefinitionService.get(BasicConstants.PLUGIN_IDENTIFIER, MODEL_PRODUCT).get(productId);
        String unit = product.getStringField(UNIT);

        FieldComponent unitField = (FieldComponent) view.getComponentByReference(QUANTITY_UNIT);
        unitField.setFieldValue(unit);
        unitField.requestComponentUpdateState();
    }

    public void fillCurrencyFieldValues(final ViewDefinitionState view) {
        String currency = currencyService.getCurrencyAlphabeticCode();
        FieldComponent currencyField = (FieldComponent) view.getComponentByReference(L_PRICE_CURRENCY);
        currencyField.setFieldValue(currency);
        currencyField.requestComponentUpdateState();
    }

}
