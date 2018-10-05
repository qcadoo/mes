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
package com.qcadoo.mes.orderSupplies.register;

import com.qcadoo.mes.basicProductionCounting.constants.BasicProductionCountingConstants;
import com.qcadoo.mes.basicProductionCounting.constants.BasicProductionCountingFields;
import com.qcadoo.mes.orderSupplies.constants.CoverageProductLoggingEventType;
import com.qcadoo.mes.orderSupplies.constants.CoverageRegisterFields;
import com.qcadoo.mes.orderSupplies.constants.OrderFieldsOS;
import com.qcadoo.mes.orders.constants.OrderFields;
import com.qcadoo.mes.orders.constants.OrdersConstants;
import com.qcadoo.mes.orders.states.constants.OrderStateStringValues;
import com.qcadoo.mes.productionCounting.constants.OrderFieldsPC;
import com.qcadoo.mes.productionCounting.constants.ProductionCountingConstants;
import com.qcadoo.mes.productionCounting.constants.ProductionTrackingFields;
import com.qcadoo.mes.productionCounting.constants.TrackingOperationProductInComponentFields;
import com.qcadoo.mes.productionCounting.constants.TypeOfProductionRecording;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.NumberService;
import com.qcadoo.model.api.search.SearchCriteriaBuilder;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class RegisterListeners {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private NumberService numberService;

    @Autowired
    private RegisterEvents registerEvents;

    @Autowired
    private RegisterService registerService;

    public final void regenerateRegisterForDrafts(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        List<Entity> orders = getDraftsOrdersFromDB();
        for (Entity order : orders) {
            regenerateForOrder(order);
        }
    }

    @Transactional
    public void regenerateForOrder(Entity order) {
        registerService.removeEntriesForOrder(order, true);
        registerEvents.createRegistryEntriesForOrder(order, false);
        order.getDataDefinition().save(order);
    }

    @Transactional
    public final void fillRegister(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        List<Entity> includedOrders = getOrdersFromDB();
        for (Entity order : includedOrders) {
            registerEvents.createRegistryEntriesForOrder(order, false);
            for (Entity registryEntry : order.getHasManyField(OrderFieldsOS.COVERAGE_REGISTERS)) {
                BigDecimal quantity = subtractUsedQuantityFromProductionTrackings(
                        registryEntry.getDecimalField(CoverageRegisterFields.QUANTITY),
                        registryEntry.getBelongsToField(CoverageRegisterFields.ORDER),
                        registryEntry.getBelongsToField(CoverageRegisterFields.TECHNOLOGY_OPERATION_COMPONENT),
                        registryEntry.getBelongsToField(CoverageRegisterFields.PRODUCT),
                        registryEntry.getBelongsToField(CoverageRegisterFields.ORDER).getStringField(
                                OrderFieldsPC.TYPE_OF_PRODUCTION_RECORDING));
                registryEntry.setField(CoverageRegisterFields.QUANTITY, quantity);
            }
            order.getDataDefinition().save(order);
        }

    }

    @Transactional
    public final void fillProductType(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        List<Entity> includedOrders = getOrdersFromDB();
        for (Entity order : includedOrders) {
            List<Entity> entries = registerService.findRegistryEntries(order);
            entries.forEach(entry -> {
                String eventType = entry.getStringField("eventType");
                if (eventType.equals(CoverageProductLoggingEventType.OPERATION_INPUT.getStringValue()) || eventType
                        .equals(CoverageProductLoggingEventType.ORDER_INPUT.getStringValue())) {
                    if (registerService.isIntermediate(entry.getBelongsToField("product"))) {
                        entry.setField(CoverageRegisterFields.PRODUCT_TYPE, "02intermediate");
                    } else {
                        entry.setField(CoverageRegisterFields.PRODUCT_TYPE, "01component");
                    }
                    entry.getDataDefinition().save(entry);
                }
            });
        }
    }

    private BigDecimal subtractUsedQuantityFromProductionTrackings(final BigDecimal quantity, final Entity order,
            final Entity technologyOperationComponent, final Entity product, final String typeOfProductionRecording) {
        BigDecimal usedQuantity;

        if (TypeOfProductionRecording.BASIC.getStringValue().equals(typeOfProductionRecording)) {
            usedQuantity = getUsedQuantityForBasic(order, product);
        } else {
            usedQuantity = getUsedQuantityForOtherFromProductionTrackings(order, technologyOperationComponent, product,
                    typeOfProductionRecording);
        }

        BigDecimal demandQuantity = quantity.subtract(usedQuantity, numberService.getMathContext());

        if (demandQuantity.compareTo(BigDecimal.ZERO) > 0) {
            return demandQuantity;
        } else {
            return BigDecimal.ZERO;
        }
    }

    private BigDecimal getUsedQuantityForBasic(final Entity order, final Entity product) {
        BigDecimal usedQuantity = BigDecimal.ZERO;

        Entity basicProductionCounting = dataDefinitionService
                .get(BasicProductionCountingConstants.PLUGIN_IDENTIFIER,
                        BasicProductionCountingConstants.MODEL_BASIC_PRODUCTION_COUNTING).find()
                .add(SearchRestrictions.belongsTo(BasicProductionCountingFields.ORDER, order))
                .add(SearchRestrictions.belongsTo(BasicProductionCountingFields.PRODUCT, product)).setMaxResults(1)
                .uniqueResult();

        if (basicProductionCounting != null) {
            BigDecimal quantity = basicProductionCounting.getDecimalField(BasicProductionCountingFields.USED_QUANTITY);

            if (quantity != null) {
                usedQuantity = usedQuantity.add(quantity, numberService.getMathContext());
            }
        }

        return usedQuantity;
    }

    private BigDecimal getUsedQuantityForOtherFromProductionTrackings(final Entity order,
            final Entity technologyOperationComponent, final Entity product, final String typeOfProductionRecording) {
        BigDecimal usedQuantity = BigDecimal.ZERO;

        SearchCriteriaBuilder searchCriteriaBuilder = dataDefinitionService
                .get(ProductionCountingConstants.PLUGIN_IDENTIFIER, ProductionCountingConstants.MODEL_PRODUCTION_TRACKING).find()
                .add(SearchRestrictions.belongsTo(ProductionTrackingFields.ORDER, order));

        if (TypeOfProductionRecording.FOR_EACH.getStringValue().equals(typeOfProductionRecording)) {
            searchCriteriaBuilder.add(SearchRestrictions.belongsTo(ProductionTrackingFields.TECHNOLOGY_OPERATION_COMPONENT,
                    technologyOperationComponent));
        }

        List<Entity> productionTrackings = searchCriteriaBuilder.list().getEntities();

        for (Entity productionTracking : productionTrackings) {
            Entity trackingOperationProductInComponent = productionTracking
                    .getHasManyField(ProductionTrackingFields.TRACKING_OPERATION_PRODUCT_IN_COMPONENTS).find()
                    .add(SearchRestrictions.belongsTo(TrackingOperationProductInComponentFields.PRODUCT, product))
                    .setMaxResults(1).uniqueResult();

            if (trackingOperationProductInComponent != null) {
                BigDecimal quantity = trackingOperationProductInComponent
                        .getDecimalField(TrackingOperationProductInComponentFields.USED_QUANTITY);

                if (quantity != null) {
                    usedQuantity = usedQuantity.add(quantity, numberService.getMathContext());
                }
            }
        }

        return usedQuantity;
    }

    private List<Entity> getOrdersFromDB() {
        return dataDefinitionService
                .get(OrdersConstants.PLUGIN_IDENTIFIER, OrdersConstants.MODEL_ORDER)
                .find()
                .add(SearchRestrictions.isNotNull(OrderFieldsPC.TYPE_OF_PRODUCTION_RECORDING))
                .add(SearchRestrictions.isNotNull(OrderFields.TECHNOLOGY))
                .add(SearchRestrictions.or(SearchRestrictions.eq(OrderFields.STATE, OrderStateStringValues.PENDING),
                        SearchRestrictions.eq(OrderFields.STATE, OrderStateStringValues.ACCEPTED),
                        SearchRestrictions.eq(OrderFields.STATE, OrderStateStringValues.IN_PROGRESS),
                        SearchRestrictions.eq(OrderFields.STATE, OrderStateStringValues.INTERRUPTED)))
                .add(SearchRestrictions.eq(OrderFields.ACTIVE, true)).list().getEntities();
    }

    private List<Entity> getDraftsOrdersFromDB() {
        return dataDefinitionService
                .get(OrdersConstants.PLUGIN_IDENTIFIER, OrdersConstants.MODEL_ORDER)
                .find()
                .add(SearchRestrictions.isNotNull(OrderFieldsPC.TYPE_OF_PRODUCTION_RECORDING))
                .add(SearchRestrictions.isNotNull(OrderFields.TECHNOLOGY))
                .add(SearchRestrictions.eq(OrderFields.STATE, OrderStateStringValues.PENDING))
                .add(SearchRestrictions.eq(OrderFields.ACTIVE, true)).list().getEntities();
    }
}
