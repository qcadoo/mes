/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.3
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
package com.qcadoo.mes.productionCounting.hooks.helpers;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import com.qcadoo.mes.basicProductionCounting.BasicProductionCountingService;
import com.qcadoo.mes.basicProductionCounting.constants.ProductionCountingQuantityFields;
import com.qcadoo.mes.basicProductionCounting.constants.ProductionCountingQuantityRole;
import com.qcadoo.mes.productionCounting.constants.OrderFieldsPC;
import com.qcadoo.mes.productionCounting.constants.ProductionTrackingFields;
import com.qcadoo.mes.productionCounting.constants.TrackingOperationProductInComponentFields;
import com.qcadoo.mes.productionCounting.constants.TrackingOperationProductOutComponentFields;
import com.qcadoo.mes.productionCounting.constants.TypeOfProductionRecording;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.NumberService;
import com.qcadoo.model.api.search.SearchCriteriaBuilder;
import com.qcadoo.model.api.search.SearchProjection;
import com.qcadoo.model.api.search.SearchProjections;
import com.qcadoo.model.api.search.SearchRestrictions;

public abstract class AbstractPlannedQuantitiesCounter {

    @Autowired
    private NumberService numberService;

    @Autowired
    private BasicProductionCountingService basicProductionCountingService;

    private final ProductionCountingQuantityRole role;

    private final String topcProductionTrackingFieldName;

    private final String topcProductFieldName;

    protected AbstractPlannedQuantitiesCounter(final ProductionCountingQuantityRole role) {
        this.role = role;
        if (role == ProductionCountingQuantityRole.USED) {
            this.topcProductionTrackingFieldName = TrackingOperationProductInComponentFields.PRODUCTION_TRACKING;
            this.topcProductFieldName = TrackingOperationProductInComponentFields.PRODUCT;
        } else if (role == ProductionCountingQuantityRole.PRODUCED) {
            this.topcProductionTrackingFieldName = TrackingOperationProductOutComponentFields.PRODUCTION_TRACKING;
            this.topcProductFieldName = TrackingOperationProductOutComponentFields.PRODUCT;
        } else {
            throw new IllegalArgumentException(String.format("Unsupported type of production counting quantity: %s", role));
        }
    }

    protected BigDecimal getPlannedQuantity(final Entity trackingOperationProductInComponent) {
        Entity productionTracking = trackingOperationProductInComponent.getBelongsToField(topcProductionTrackingFieldName);
        Entity product = trackingOperationProductInComponent.getBelongsToField(topcProductFieldName);

        return getPlannedQuantity(productionTracking, product);
    }

    private BigDecimal getPlannedQuantity(final Entity productionTracking, final Entity product) {
        Entity order = productionTracking.getBelongsToField(ProductionTrackingFields.ORDER);
        Entity technologyOperationComponent = productionTracking
                .getBelongsToField(ProductionTrackingFields.TECHNOLOGY_OPERATION_COMPONENT);

        SearchCriteriaBuilder searchCriteriaBuilder = prepareCriteria(product, order, technologyOperationComponent);
        List<Entity> partialResults = searchCriteriaBuilder.list().getEntities();

        return sumOfPlannedQuantities(partialResults);
    }

    private SearchCriteriaBuilder prepareCriteria(final Entity product, final Entity order,
            final Entity technologyOperationComponent) {
        SearchCriteriaBuilder searchCriteriaBuilder = criteriaBuilderFor(order);

        TypeOfProductionRecording recordingType = TypeOfProductionRecording.parseString(order
                .getStringField(OrderFieldsPC.TYPE_OF_PRODUCTION_RECORDING));

        if (recordingType == TypeOfProductionRecording.FOR_EACH) {
            // since belongsTo restriction produces .isNull(fieldName) for null entity argument we do not have to deal with any
            // null checks
            searchCriteriaBuilder.add(SearchRestrictions.belongsTo(
                    ProductionCountingQuantityFields.TECHNOLOGY_OPERATION_COMPONENT, technologyOperationComponent));
        }

        searchCriteriaBuilder.add(SearchRestrictions.belongsTo(ProductionCountingQuantityFields.PRODUCT, product));
        searchCriteriaBuilder.add(SearchRestrictions.eq(ProductionCountingQuantityFields.ROLE, role.getStringValue()));
        searchCriteriaBuilder.add(SearchRestrictions.isNotNull(ProductionCountingQuantityFields.PLANNED_QUANTITY));

        SearchProjection sumOfPlannedQntty = SearchProjections.alias(
                SearchProjections.field(ProductionCountingQuantityFields.PLANNED_QUANTITY),
                ProductionCountingQuantityFields.PLANNED_QUANTITY);
        searchCriteriaBuilder.setProjection(sumOfPlannedQntty);

        return searchCriteriaBuilder;
    }

    private SearchCriteriaBuilder criteriaBuilderFor(final Entity order) {
        SearchCriteriaBuilder searchCriteriaBuilder = basicProductionCountingService.getProductionCountingQuantityDD().find();
        searchCriteriaBuilder.add(SearchRestrictions.belongsTo(ProductionCountingQuantityFields.ORDER, order));

        return searchCriteriaBuilder;
    }

    private BigDecimal sumOfPlannedQuantities(final List<Entity> productionCountingQuantities) {
        BigDecimal plannedQuantity = BigDecimal.ZERO;

        for (Entity productionCountingQuantity : productionCountingQuantities) {
            BigDecimal productionCountingQuantityPlannedQuantity = productionCountingQuantity
                    .getDecimalField(ProductionCountingQuantityFields.PLANNED_QUANTITY);

            plannedQuantity = plannedQuantity.add(productionCountingQuantityPlannedQuantity, numberService.getMathContext());
        }

        return numberService.setScale(plannedQuantity);
    }

}
