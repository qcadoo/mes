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
package com.qcadoo.mes.deliveriesMinState;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.qcadoo.mes.basic.CompanyService;
import com.qcadoo.mes.basic.ParameterService;
import com.qcadoo.mes.basic.constants.ParameterFields;
import com.qcadoo.mes.basic.constants.ProductFields;
import com.qcadoo.mes.basic.constants.UnitConversionItemFieldsB;
import com.qcadoo.mes.costNormsForProduct.constants.ProductFieldsCNFP;
import com.qcadoo.mes.deliveries.DeliveriesService;
import com.qcadoo.mes.deliveries.constants.CompanyProductFields;
import com.qcadoo.mes.deliveries.constants.DeliveriesConstants;
import com.qcadoo.mes.deliveries.constants.DeliveryFields;
import com.qcadoo.mes.deliveries.constants.OrderedProductFields;
import com.qcadoo.mes.deliveries.states.constants.DeliveryState;
import com.qcadoo.mes.deliveriesMinState.notifications.constants.StaffNotificationFieldsMS;
import com.qcadoo.mes.deliveriesMinState.notifications.service.MailingService;
import com.qcadoo.mes.emailNotifications.constants.EmailNotificationsConstants;
import com.qcadoo.mes.emailNotifications.constants.StaffNotificationFields;
import com.qcadoo.mes.materialFlow.constants.MaterialFlowConstants;
import com.qcadoo.mes.materialFlowResources.constants.MaterialFlowResourcesConstants;
import com.qcadoo.mes.warehouseMinimalState.WarehouseMinimalStateHelper;
import com.qcadoo.model.api.*;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.model.api.units.PossibleUnitConversions;
import com.qcadoo.model.api.units.UnitConversionService;
import com.qcadoo.view.api.utils.NumberGeneratorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class DeliveriesMinStateHelper {

    private static final Logger LOG = LoggerFactory.getLogger(DeliveriesMinStateHelper.class);

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private NumberService numberService;

    @Autowired
    private DeliveriesService deliveriesService;

    @Autowired
    private ParameterService parameterService;

    @Autowired
    private NumberGeneratorService numberGeneratorService;

    @Autowired
    private CompanyService companyService;

    @Autowired
    private MailingService mailingService;

    @Autowired
    private WarehouseMinimalStateHelper warehouseMinimalStateHelper;

    @Autowired
    private UnitConversionService unitConversionService;

    private void sendEmailNotifications(final List<String> createdDeliveries) {
        if (!createdDeliveries.isEmpty()) {
            List<String> emails = dataDefinitionService
                    .get(EmailNotificationsConstants.PLUGIN_IDENTIFIER, EmailNotificationsConstants.MODEL_STAFF_NOTIFICATION)
                    .find().add(SearchRestrictions.eq(StaffNotificationFieldsMS.CREATE_DELIVERY_MIN_STATE, true)).list()
                    .getEntities().stream().map(entity -> entity.getStringField(StaffNotificationFields.EMAIL))
                    .collect(Collectors.toList());

            mailingService.sendTemplateDeliveryInfoEmailsBySendinblue(emails, createdDeliveries);
        }

    }

    private String createDeliveries(final Entity location, final Entity supplier, final List<Entity> warehouseMinimumStates) {
        DataDefinition deliveryDataDefinition = deliveriesService.getDeliveryDD();
        Entity delivery = deliveryDataDefinition.create();

        String generatedNumber = getNewDeliveryNumber();
        delivery.setField(DeliveryFields.NUMBER, generatedNumber);
        delivery.setField(DeliveryFields.SUPPLIER, supplier);
        delivery.setField(DeliveryFields.LOCATION, location);
        delivery.setField(DeliveryFields.STATE, DeliveryState.DRAFT);
        delivery.setField(DeliveryFields.CURRENCY, getNewDeliveryCurrency());
        delivery.setField(DeliveryFields.DELIVERY_ADDRESS, deliveriesService.getDeliveryAddressDefaultValue());
        delivery.setField(DeliveryFields.EXTERNAL_SYNCHRONIZED, true);
        delivery = deliveryDataDefinition.save(delivery);

        setDeliveryProducts(delivery, warehouseMinimumStates);

        if (LOG.isDebugEnabled()) {
            LOG.debug(String.format("Delivery created with number: %s", delivery.getField(DeliveryFields.NUMBER)));
        }
        return generatedNumber;
    }

    private List<Entity> setDeliveryProducts(final Entity delivery, final List<Entity> warehouseMinimumStates) {
        List<Entity> products = Lists.newArrayList();

        for (Entity minimumState : warehouseMinimumStates) {
            Entity deliveredProduct = deliveriesService.getOrderedProductDD().create();
            Entity product = minimumState.getBelongsToField("product");
            BigDecimal orderedQuantity = BigDecimalUtils.convertNullToZero(minimumState.getField("optimalOrderQuantity"));
            BigDecimal conversion = getConversion(product);

            deliveredProduct.setField(OrderedProductFields.DELIVERY, delivery);
            deliveredProduct.setField(OrderedProductFields.PRODUCT, product);
            deliveredProduct.setField(OrderedProductFields.ORDERED_QUANTITY, orderedQuantity);
            deliveredProduct.setField(OrderedProductFields.PRICE_PER_UNIT,
                    product.getDecimalField(ProductFieldsCNFP.LAST_PURCHASE_COST));
            deliveredProduct.setField(OrderedProductFields.CONVERSION, conversion);
            deliveredProduct.setField(OrderedProductFields.ADDITIONAL_QUANTITY,
                    orderedQuantity.multiply(conversion, numberService.getMathContext()));
            products.add(deliveriesService.getOrderedProductDD().save(deliveredProduct));
        }
        return products;
    }

    private BigDecimal getConversion(Entity product) {
        String unit = product.getStringField(ProductFields.UNIT);
        String additionalUnit = product.getStringField(ProductFields.ADDITIONAL_UNIT);
        if (additionalUnit == null) {
            return BigDecimal.ONE;
        }
        PossibleUnitConversions unitConversions = unitConversionService.getPossibleConversions(unit,
                searchCriteriaBuilder -> searchCriteriaBuilder.add(SearchRestrictions.belongsTo(
                        UnitConversionItemFieldsB.PRODUCT, product)));
        if (unitConversions.isDefinedFor(additionalUnit)) {
            return unitConversions.asUnitToConversionMap().get(additionalUnit);
        } else {
            return BigDecimal.ZERO;
        }
    }

    private Entity getNewDeliveryCurrency() {
        Entity parameter = parameterService.getParameter();
        Entity currency = parameter.getBelongsToField(ParameterFields.CURRENCY);

        return currency;
    }

    private String getNewDeliveryNumber() {
        String number = numberGeneratorService.generateNumber(DeliveriesConstants.PLUGIN_IDENTIFIER,
                DeliveriesConstants.MODEL_DELIVERY);
        return number;
    }

    public Map<Long, Multimap<Long, Entity>> createDeliveriesFromMinimalState() {
        Map<Long, Multimap<Long, Entity>> minimalStatePerWarehousesAndSupplier = Maps.newHashMap();

        List<Entity> warehouses = getWarehousesFromMinimalState();
        if (LOG.isDebugEnabled()) {
            LOG.debug(String.format("CreateDeliveriesFromMinimalState invoked with: %d warehouses.", warehouses.size()));
        }
        List<String> createdDeliveries = Lists.newArrayList();
        warehouses.forEach(w -> fillPositions(w, minimalStatePerWarehousesAndSupplier));
        minimalStatePerWarehousesAndSupplier.keySet().forEach(
                p -> createdDeliveries.addAll(createDeliveries(p, minimalStatePerWarehousesAndSupplier.get(p))));

        sendEmailNotifications(createdDeliveries);
        return minimalStatePerWarehousesAndSupplier;
    }

    private List<String> createDeliveries(Long warehouse, Multimap<Long, Entity> multiMap) {
        List<String> deliveriesNumbers = Lists.newArrayList();
        multiMap.keySet().forEach(p -> deliveriesNumbers.add(createDelivery(warehouse, p, multiMap.get(p))));
        return deliveriesNumbers;
    }

    private String createDelivery(Long warehouseId, Long supplierId, Collection<Entity> minimalStates) {
        Entity warehouse = null;
        if (warehouseId != null) {
            warehouse = dataDefinitionService.get(MaterialFlowConstants.PLUGIN_IDENTIFIER, MaterialFlowConstants.MODEL_LOCATION)
                    .get(warehouseId);
        }
        Entity supplier = companyService.getCompany(supplierId);
        return createDeliveries(warehouse, supplier, Lists.newArrayList(minimalStates));
    }

    private void fillPositions(final Entity warehouse,
            final Map<Long, Multimap<Long, Entity>> minimalStatePerWarehousesAndSupplier) {

        List<Entity> minmialStates = getMinimalStateGreaterThanZeroForWarehouse(warehouse);
        Map<Long, Entity> minmialStatesByProduct = minmialStates.stream().collect(
                Collectors.toMap(res -> res.getBelongsToField("product").getId(), res -> res));
        List<Entity> stocks = getWarehouseStockWithTooSmallMinState(warehouse,
                minmialStates.stream().map(res -> res.getBelongsToField("product")).collect(Collectors.toList()));
        Map<Long, Entity> stocksByProduct = stocks.stream()
                .collect(Collectors.toMap(res -> res.getIntegerField("product_id").longValue(), res -> res));
        Multimap<Long, Entity> positions = ArrayListMultimap.create();
        minmialStatesByProduct.keySet().forEach(
                productId -> createPositions(positions, warehouse.getId(), productId, minmialStatesByProduct, stocksByProduct));
        minimalStatePerWarehousesAndSupplier.put(warehouse.getId(), positions);
    }

    private void createPositions(Multimap<Long, Entity> positions, Long warehouseId, Long productId,
            Map<Long, Entity> minmialStatesByProduct, Map<Long, Entity> stocksByProduct) {

        Entity stock = stocksByProduct.get(productId);
        Entity minimalState = minmialStatesByProduct.get(productId);
        if (stock == null) {
            deliveriesService.getDefaultSupplier(productId).ifPresent(supplier -> {
                BigDecimal ordered = warehouseMinimalStateHelper.getOrderedQuantityForProductAndLocation(warehouseId, productId);
                if (warehouseMinimalStateHelper.checkIfLowerThanMinimum(productId, ordered,
                        minimalState.getDecimalField("minimumState"))) {
                    positions.put(supplier.getBelongsToField(CompanyProductFields.COMPANY).getId(), minimalState);
                }
            });
        } else {
            BigDecimal statePlusOrder = BigDecimalUtils.convertNullToZero(stock.getDecimalField("orderedQuantity"))
                    .add(BigDecimalUtils.convertNullToZero(stock.getDecimalField("quantity")), numberService.getMathContext());
            if (warehouseMinimalStateHelper.checkIfLowerThanMinimum(productId, statePlusOrder,
                    stock.getDecimalField("minimumState"))) {
                deliveriesService.getDefaultSupplier(productId).ifPresent(supplier -> positions
                        .put(supplier.getBelongsToField(CompanyProductFields.COMPANY).getId(), minimalState));
            }
        }
    }

    private List<Entity> getWarehousesFromMinimalState() {
        String query = "select distinct state.location from #warehouseMinimalState_warehouseMinimumState state";
        return getWarehouseMinimumStateDD().find(query).list().getEntities();
    }

    public List<Entity> getMinimalStateGreaterThanZeroForWarehouse(final Entity warehouse) {
        String query = "select state from #warehouseMinimalState_warehouseMinimumState as state where state.minimumState > 0"
                + " and state.location.id = :warehouseId";
        return getWarehouseMinimumStateDD().find(query).setParameter("warehouseId", warehouse.getId()).list().getEntities();
    }

    // WARNING unused argument is used in aspect in plugin integration
    public List<Entity> getWarehouseStockWithTooSmallMinState(final Entity warehouse, final List<Entity> products) {
        String query = "select stock from #materialFlowResources_resourceStockDto as stock where stock.minimumState > 0"
                + " and stock.location_id = :warehouseId";
        return getResourceStockDtoDD().find(query).setParameter("warehouseId", warehouse.getId().intValue()).list().getEntities();
    }

    public DataDefinition getWarehouseMinimumStateDD() {
        return dataDefinitionService.get("warehouseMinimalState", "warehouseMinimumState");
    }

    private DataDefinition getResourceStockDtoDD() {
        return dataDefinitionService.get(MaterialFlowResourcesConstants.PLUGIN_IDENTIFIER,
                MaterialFlowResourcesConstants.MODEL_RESOURCE_STOCK_DTO);
    }
}
