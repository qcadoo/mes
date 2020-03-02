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
package com.qcadoo.mes.costNormsForMaterials.orderRawMaterialCosts.dataProvider;

import com.google.common.base.Function;
import com.qcadoo.mes.basic.constants.ProductFields;
import com.qcadoo.mes.costNormsForMaterials.orderRawMaterialCosts.domain.ProductWithCosts;
import com.qcadoo.mes.costNormsForProduct.constants.ProductFieldsCNFP;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchProjection;

import java.math.BigDecimal;

import static com.qcadoo.model.api.search.SearchProjections.*;

public final class ProductWithCostsBuilder {

    public static final Function<Entity, ProductWithCosts> BUILD_FROM_PROJECTION = projection -> ProductWithCostsBuilder
            .from(projection);

    private static final String PRODUCT_ID = "id";

    private static final String COST_FOR_NUMBER = "costForNumber_alias";

    private static final String NOMINAL_COST = "nominalCost_alias";

    private static final String LAST_PURCHASE_COST = "lastPurchaseCost_alias";

    private static final String AVERAGE_COST = "averageCost_alias";

    private static final String ENTITY_TYPE = "entityType_alias";

    private ProductWithCostsBuilder() {
    }

    public static ProductWithCosts from(final Entity projection) {
        Long productId = (Long) projection.getField(PRODUCT_ID);
        BigDecimal costForNumber = projection.getDecimalField(COST_FOR_NUMBER);
        BigDecimal nominalCost = projection.getDecimalField(NOMINAL_COST);
        BigDecimal lastPurchaseCost = projection.getDecimalField(LAST_PURCHASE_COST);
        BigDecimal averageCost = projection.getDecimalField(AVERAGE_COST);
        String entityType = projection.getStringField(ENTITY_TYPE);
        return new ProductWithCosts(productId, costForNumber, nominalCost, lastPurchaseCost, averageCost, entityType);
    }

    public static ProductWithCosts fromProduct(final Entity product) {
        Long productId = product.getId();
        BigDecimal costForNumber = product.getDecimalField(ProductFieldsCNFP.COST_FOR_NUMBER);
        BigDecimal nominalCost = product.getDecimalField(ProductFieldsCNFP.NOMINAL_COST);
        BigDecimal lastPurchaseCost = product.getDecimalField(ProductFieldsCNFP.LAST_PURCHASE_COST);
        BigDecimal averageCost = product.getDecimalField(ProductFieldsCNFP.AVERAGE_COST);
        String entityType = product.getStringField(ProductFields.ENTITY_TYPE);
        return new ProductWithCosts(productId, costForNumber, nominalCost, lastPurchaseCost, averageCost, entityType);
    }

    public static SearchProjection buildProjectionForProduct(final String prefix) {
        return list().add(fieldProj(prefix, "id", PRODUCT_ID))
                .add(fieldProj(prefix, ProductFieldsCNFP.COST_FOR_NUMBER, COST_FOR_NUMBER))
                .add(fieldProj(prefix, ProductFieldsCNFP.NOMINAL_COST, NOMINAL_COST))
                .add(fieldProj(prefix, ProductFieldsCNFP.LAST_PURCHASE_COST, LAST_PURCHASE_COST))
                .add(fieldProj(prefix, ProductFieldsCNFP.AVERAGE_COST, AVERAGE_COST))
                .add(fieldProj(prefix, ProductFields.ENTITY_TYPE, ENTITY_TYPE));
    }

    private static SearchProjection fieldProj(final String prefix, final String fieldName, final String fieldAlias) {
        return alias(field(fieldPath(prefix, fieldName)), fieldAlias);
    }

    private static String fieldPath(final String prefix, final String fieldName) {
        if (prefix == null) {
            return fieldName;
        }
        return prefix + "." + fieldName;
    }

}
