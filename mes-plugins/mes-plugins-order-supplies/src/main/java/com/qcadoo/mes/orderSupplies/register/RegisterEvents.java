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

import com.google.common.collect.Lists;
import com.qcadoo.mes.basic.ParameterService;
import com.qcadoo.mes.basic.constants.ProductFamilyElementType;
import com.qcadoo.mes.basic.constants.ProductFields;
import com.qcadoo.mes.basicProductionCounting.constants.BasicProductionCountingFields;
import com.qcadoo.mes.basicProductionCounting.constants.ProductionCountingQuantityFields;
import com.qcadoo.mes.basicProductionCounting.constants.ProductionCountingQuantityRole;
import com.qcadoo.mes.orderSupplies.constants.CoverageProductLoggingEventType;
import com.qcadoo.mes.orderSupplies.constants.CoverageRegisterFields;
import com.qcadoo.mes.orderSupplies.constants.OrderFieldsOS;
import com.qcadoo.mes.orderSupplies.constants.OrderSuppliesConstants;
import com.qcadoo.mes.orders.constants.OrderFields;
import com.qcadoo.mes.orders.hooks.OrderHooks;
import com.qcadoo.mes.orders.states.constants.OrderState;
import com.qcadoo.mes.productionCounting.constants.*;
import com.qcadoo.mes.technologies.ProductQuantitiesService;
import com.qcadoo.mes.technologies.TechnologyService;
import com.qcadoo.mes.technologies.constants.ProductToProductGroupFields;
import com.qcadoo.mes.technologies.constants.TechnologiesConstants;
import com.qcadoo.mes.technologies.constants.TechnologyFields;
import com.qcadoo.mes.technologies.constants.TechnologyOperationComponentFields;
import com.qcadoo.mes.technologies.dto.OperationProductComponentHolder;
import com.qcadoo.mes.technologies.dto.OperationProductComponentWithQuantityContainer;
import com.qcadoo.mes.timeNormsForOperations.constants.TechnologyOperationComponentFieldsTNFO;
import com.qcadoo.model.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
public class RegisterEvents {

    @Autowired
    private RegisterService registerService;

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private NumberService numberService;

    @Autowired
    private ProductQuantitiesService productQuantitiesService;

    @Autowired
    private ParameterService parameterService;

    @Autowired
    private OrderHooks orderHooks;

    @Autowired
    private TechnologyService technologyService;

    public void onSaveOrder(final DataDefinition orderDD, final Entity order) {

        Entity orderDB = null;
        if (OrderState.of(order) == OrderState.ABANDONED || OrderState.of(order) == OrderState.COMPLETED
                || OrderState.of(order) == OrderState.DECLINED) {
            registerService.removeEntriesForOrder(order, true);
            return;
        }

        boolean registerFilled = order.getBooleanField(OrderFieldsOS.REGISTER_FILLED);

        if (order.getId() != null) {
            StringBuilder query = new StringBuilder();
            query.append("SELECT ord.id as id, technology.id as technology, ord.number as number, ord.startDate as startDate, ");
            query.append("ord.finishDate as finishDate, ord.plannedQuantity as plannedQuantity, ");
            query.append("ord.typeOfProductionRecording as typeOfProductionRecording ");
            query.append("FROM #orders_order ord WHERE id = :id");
            orderDB = orderDD.find(query.toString()).setLong("id", order.getId()).setMaxResults(1).uniqueResult();
        }

        if (OrderState.of(order) == OrderState.IN_PROGRESS && checkIfOrderChange(order, orderDB)) {
            orderDB = order.getDataDefinition().get(order.getId());
            List<Entity> entries = orderDB.getHasManyField(OrderFieldsOS.COVERAGE_REGISTERS);
            createRegistryEntriesForOrder(order, checkIfPlannedQuantityChange(order, orderDB));
            registerService.fillExistProductionCountingEntries(order, orderDB, entries);
            registerService.updateRegistryEntriesForOrder(order);
            return;
        }

        if (!registerFilled || order.getId() == null) {
            Entity technology = order.getBelongsToField(OrderFields.TECHNOLOGY);
            if (technology != null) {
                Entity technologyDB = technology.getDataDefinition().get(order.getBelongsToField(OrderFields.TECHNOLOGY).getId());
                order.setField(OrderFields.TECHNOLOGY, technologyDB);
                createRegistryEntriesForOrder(order, true);
                order.setField(OrderFieldsOS.REGISTER_FILLED, true);
            }
        } else if (order.getBelongsToField(OrderFields.TECHNOLOGY) == null) {
            registerService.removeEntriesForOrder(order, true);
        } else if (checkIfTechnologyInOrderChange(order, orderDB)) {
            Entity technologyDB = order.getBelongsToField(OrderFields.TECHNOLOGY).getDataDefinition()
                    .get(order.getBelongsToField(OrderFields.TECHNOLOGY).getId());
            order.setField(OrderFields.TECHNOLOGY, technologyDB);
            registerService.removeEntriesForOrder(order, true);
            createRegistryEntriesForOrder(order, true);
        } else if (checkIfOrderChange(order, orderDB)) {
            orderDB = order.getDataDefinition().get(order.getId());
            List<Entity> entries = orderDB.getHasManyField(OrderFieldsOS.COVERAGE_REGISTERS);
            registerService.removeEntriesForOrder(orderDB, false);
            if (order.getBelongsToField(OrderFields.TECHNOLOGY) == null) {
                return;
            }
            Entity technologyDB = order.getBelongsToField(OrderFields.TECHNOLOGY).getDataDefinition()
                    .get(order.getBelongsToField(OrderFields.TECHNOLOGY).getId());
            order.setField(OrderFields.TECHNOLOGY, technologyDB);
            createRegistryEntriesForOrder(order, checkIfPlannedQuantityChange(order, orderDB));
            if (!entries.isEmpty()) {
                registerService.fillExistProductionCountingEntries(order, orderDB, entries);
            }
        }
        orderDB = null;
    }

    public void onDeleteOrder(final DataDefinition orderDD, final Entity order) {
        registerService.removeEntriesForOrder(order, true);
    }

    public void onChangeTechnologyInOrder(final DataDefinition technologyDD, final Entity technology) {

        if (technology.getId() == null || technology.getStringField(TechnologyFields.TECHNOLOGY_TYPE) == null) {
            return;
        }
        Entity technologyDB = technologyDD.get(technology.getId());
        if (!technology.getStringField(TechnologyFields.STATE).equals(technologyDB.getStringField(TechnologyFields.STATE))) {
            Entity order = registerService.findOrderForTechnology(technology);
            if (order == null) {
                return;
            }
            order.setField(OrderFields.TECHNOLOGY, technology);
            registerService.removeEntriesFromOrder(order);
            createRegistryEntriesForOrder(order, true);
            registerService.saveRegistryEntries(order.getHasManyField(OrderFieldsOS.COVERAGE_REGISTERS));
        }

    }

    public void onBasicProductionCounting(final DataDefinition basicProductionCountingDD, final Entity basicProductionCounting) {
        if (basicProductionCounting.getId() == null) {
            return;
        }

        if (!TypeOfProductionRecording.BASIC.getStringValue().equals(
                basicProductionCounting.getBelongsToField(BasicProductionCountingFields.ORDER).getStringField(
                        OrderFieldsPC.TYPE_OF_PRODUCTION_RECORDING))) {
            return;
        }

        List<Entity> registerEntries = registerService.findRegistryEntries(
                basicProductionCounting.getBelongsToField(BasicProductionCountingFields.ORDER),
                basicProductionCounting.getBelongsToField(BasicProductionCountingFields.PRODUCT));

        BigDecimal usedQuantity = BigDecimalUtils.convertNullToZero(
                basicProductionCounting.getDecimalField(BasicProductionCountingFields.USED_QUANTITY));

        for (Entity registerEntry : registerEntries) {

            if (registerEntry.getStringField(CoverageRegisterFields.EVENT_TYPE).equals("05orderOutput")) {
                BigDecimal demandQuantity = registerEntry.getDecimalField(CoverageRegisterFields.PRODUCTION_COUNTING_QUANTITIES)
                        .subtract(
                                BigDecimalUtils.convertNullToZero(basicProductionCounting
                                        .getDecimalField(BasicProductionCountingFields.PRODUCED_QUANTITY)),
                                numberService.getMathContext());

                registerEntry.setField(CoverageRegisterFields.QUANTITY, demandQuantity);
                registerEntry.getDataDefinition().save(registerEntry);

            } else {
                BigDecimal demandQuantity = registerEntry.getDecimalField(CoverageRegisterFields.PRODUCTION_COUNTING_QUANTITIES)
                        .subtract(usedQuantity, numberService.getMathContext());

                if (demandQuantity.compareTo(BigDecimal.ZERO) < 0) {
                    usedQuantity = demandQuantity.abs();
                    registerEntry.setField(CoverageRegisterFields.QUANTITY, BigDecimal.ZERO);
                } else {
                    usedQuantity = BigDecimal.ZERO;
                    registerEntry.setField(CoverageRegisterFields.QUANTITY, demandQuantity);
                }
                registerEntry.getDataDefinition().save(registerEntry);
            }
        }
    }

    public void onDeleteProductionCountingQuantity(final DataDefinition productionCountingQuantityDD,
            final Entity productionCountingQuantity) {
        if (productionCountingQuantity.getStringField(ProductionCountingQuantityFields.ROLE).equals(
                ProductionCountingQuantityRole.PRODUCED.getStringValue())) {
            return;
        }
        Entity registryEntry = registerService.findRegistryEntry(
                productionCountingQuantity.getBelongsToField(ProductionCountingQuantityFields.ORDER),
                productionCountingQuantity.getBelongsToField(ProductionCountingQuantityFields.PRODUCT),
                productionCountingQuantity.getBelongsToField(ProductionCountingQuantityFields.TECHNOLOGY_OPERATION_COMPONENT));
        if (registryEntry != null) {
            registryEntry.getDataDefinition().delete(registryEntry.getId());
        }
    }

    public void onSaveProductionCountingQuantity(final DataDefinition productionCountingQuantityDD,
            final Entity productionCountingQuantity) {
        if(!productionCountingQuantity.getBooleanField(ProductionCountingQuantityFields.FLOW_FILLED)) {
            if (productionCountingQuantity.getStringField(ProductionCountingQuantityFields.ROLE)
                    .equals(ProductionCountingQuantityRole.PRODUCED.getStringValue())) {
                return;
            }
            if (productionCountingQuantity.getId() == null) {
                registerService.createRegisterEntryFromPCQ(productionCountingQuantity);
            } else {
                registerService.updateRegisterEntryFromPCQ(productionCountingQuantity);
            }
        }
    }

    public void onProductionTracking(final Entity productionTracking) {
        onProductionTracking(productionTracking, false);
    }

    public void onCorrectedProductionTracking(final Entity productionTracking) {
        onProductionTracking(productionTracking, true);
    }

    public void onProductionTracking(final Entity productionTracking, boolean corrected) {

        Entity order = productionTracking.getBelongsToField(ProductionTrackingFields.ORDER);
        Entity toc = productionTracking.getBelongsToField(ProductionTrackingFields.TECHNOLOGY_OPERATION_COMPONENT);

        List<Entity> trackingOperationProductInComponents = productionTracking
                .getHasManyField(ProductionTrackingFields.TRACKING_OPERATION_PRODUCT_IN_COMPONENTS);

        trackingOperationProductInComponents.stream().forEach(trackingProduct -> {
            if (trackingProduct.getDecimalField(TrackingOperationProductInComponentFields.USED_QUANTITY) != null) {
                updateRegistryOnProductionTrackingIn(trackingProduct, order, toc, corrected);
            }
        });

        String priceBasedOn = parameterService.getParameter().getStringField(ParameterFieldsPC.PRICE_BASED_ON);

        if (priceBasedOn.equals(PriceBasedOn.REAL_PRODUCTION_COST.getStringValue())) {
            return;
        }

        List<Entity> trackingOperationProductOutComponents = productionTracking
                .getHasManyField(ProductionTrackingFields.TRACKING_OPERATION_PRODUCT_OUT_COMPONENTS);

        trackingOperationProductOutComponents.stream().forEach(trackingProduct -> {
            if (trackingProduct.getDecimalField(TrackingOperationProductOutComponentFields.USED_QUANTITY) != null) {
                updateRegistryOnProductionTrackingOut(trackingProduct, order, toc, corrected);
            }
        });

    }

    private void updateRegistryOnProductionTrackingIn(Entity trackingProduct, Entity order, Entity toc, boolean corrected) {
        Entity registerEntry = null;
        List<Entity> trackingOperationProductInComponents = null;
        Entity product = trackingProduct.getBelongsToField(TrackingOperationProductInComponentFields.PRODUCT);

        if (TypeOfProductionRecording.CUMULATED.getStringValue().equals(
                order.getStringField(OrderFieldsPC.TYPE_OF_PRODUCTION_RECORDING))) {
            trackingOperationProductInComponents = registerService.findTrackingOperationProductInComponents(order, toc, product,
                    false);
            List<Entity> registerEntries = registerService.findRegistryEntries(order, product);
            BigDecimal trackedQuantity = registerService.getTrackedQuantity(trackingProduct,
                    trackingOperationProductInComponents, corrected);
            registerService.updateRegistryEntriesOnProductionTracking(registerEntries, trackedQuantity);
        } else {
            trackingOperationProductInComponents = registerService.findTrackingOperationProductInComponents(order, toc, product,
                    true);
            registerEntry = registerService.findRegistryEntry(order, product, toc);
            BigDecimal trackedQuantity = registerService.getTrackedQuantity(trackingProduct,
                    trackingOperationProductInComponents, corrected);
            registerService.updateRegistryEntriesOnProductionTracking(registerEntry, trackedQuantity);
        }

    }

    private void updateRegistryOnProductionTrackingOut(Entity trackingProduct, Entity order, Entity toc, boolean corrected) {
        Entity registerEntry = null;
        List<Entity> trackingOperationProductOutComponents = null;
        Entity product = trackingProduct.getBelongsToField(TrackingOperationProductOutComponentFields.PRODUCT);

        if (TypeOfProductionRecording.CUMULATED.getStringValue().equals(
                order.getStringField(OrderFieldsPC.TYPE_OF_PRODUCTION_RECORDING))) {
            trackingOperationProductOutComponents = registerService.findTrackingOperationProductOutComponents(order, toc,
                    product, false);
            List<Entity> registerEntries = registerService.findRegistryEntries(order, product);
            BigDecimal trackedQuantity = registerService.getTrackedQuantity(trackingProduct,
                    trackingOperationProductOutComponents, corrected);
            registerService.updateRegistryEntriesOnProductionTracking(registerEntries, trackedQuantity);
        } else {
            trackingOperationProductOutComponents = registerService.findTrackingOperationProductOutComponents(order, toc,
                    product, true);
            registerEntry = registerService.findRegistryEntry(order, product, toc);
            BigDecimal trackedQuantity = registerService.getTrackedQuantity(trackingProduct,
                    trackingOperationProductOutComponents, corrected);
            registerService.updateRegistryEntriesOnProductionTracking(registerEntry, trackedQuantity);
        }

    }

    private boolean checkIfTechnologyInOrderChange(final Entity order, final Entity orderDB) {
        Entity technology = order.getBelongsToField(OrderFields.TECHNOLOGY);

        Long technologyDBId = orderDB.getLongField(OrderFields.TECHNOLOGY);
        if (technologyDBId == null) {
            return true;
        }
        if (!technology.getId().equals(technologyDBId)) {
            return true;
        }

        return false;
    }

    public void createRegistryEntriesForOrder(Entity order, boolean plannedQuantityChanged) {
        OperationProductComponentWithQuantityContainer operationProductComponentWithQuantityContainer = productQuantitiesService
                .getProductComponentQuantitiesWithoutNonComponents(Lists.newArrayList(order), true);

        String typeOfProductionRecording = order.getStringField(OrderFieldsPC.TYPE_OF_PRODUCTION_RECORDING);
        String eventType = null;

        if (TypeOfProductionRecording.FOR_EACH.getStringValue().equals(typeOfProductionRecording)) {

            eventType = CoverageProductLoggingEventType.OPERATION_INPUT.getStringValue();

        } else if (TypeOfProductionRecording.CUMULATED.getStringValue().equals(typeOfProductionRecording)
                || TypeOfProductionRecording.BASIC.getStringValue().equals(typeOfProductionRecording)) {

            eventType = CoverageProductLoggingEventType.ORDER_INPUT.getStringValue();

        }

        OperationProductComponentWithQuantityContainer operationProductComponentWithQuantityContainerIn = operationProductComponentWithQuantityContainer
                .getAllWithSameEntityType(TechnologiesConstants.MODEL_OPERATION_PRODUCT_IN_COMPONENT);

        List<Entity> opics = registerService.getOPICForTechnology(order.getBelongsToField(OrderFields.TECHNOLOGY));

        List<Entity> entries = Lists.newArrayList();

        List<Long> productIds = new ArrayList<>();
        for (Entity opic : opics) {
            Long productId = opic.getLongField("productId");
            if (ProductFamilyElementType.PRODUCTS_FAMILY.getStringValue().equals(opic.getStringField("productEntityType"))) {
                Entity productToProductGroupTechnology = technologyService
                        .getProductToProductGroupTechnology(order.getBelongsToField(OrderFields.PRODUCT), productId);
                if (productToProductGroupTechnology != null) {
                    productId = productToProductGroupTechnology.getBelongsToField(ProductToProductGroupFields.ORDER_PRODUCT)
                            .getId();
                }
            }
            productIds.add(productId);
        }
        List<Long> intermediateProducts = registerService.getIntermediateProducts(productIds);
        for (Entity opic : opics) {
            BigDecimal quantity = operationProductComponentWithQuantityContainerIn.get(opic.getBelongsToField("opic"));

            if (quantity != null) {
                registerService.addRegisterEntryForOrder(entries, order, opic, quantity, eventType, intermediateProducts);
            }
        }
        addOutputEntry(operationProductComponentWithQuantityContainer, order, entries, plannedQuantityChanged);
        order.setField(OrderFieldsOS.COVERAGE_REGISTERS, entries);
    }

    private void addOutputEntry(
            final OperationProductComponentWithQuantityContainer operationProductComponentWithQuantityContainer,
            final Entity order, final List<Entity> entries, boolean plannedQuantityChanged) {
        for (Map.Entry<OperationProductComponentHolder, BigDecimal> productComponentQuantity : operationProductComponentWithQuantityContainer
                .asMap().entrySet()) {
            OperationProductComponentHolder operationProductComponentHolder = productComponentQuantity.getKey();
            Entity technologyOperationComponent = operationProductComponentHolder.getTechnologyOperationComponent();
            Entity product = operationProductComponentHolder.getProduct();
            if (checkIfProductIsFinalProduct(order, technologyOperationComponent, product)) {
                BigDecimal plannedQuantity = productComponentQuantity.getValue();
                Entity registerEntry = dataDefinitionService.get(OrderSuppliesConstants.PLUGIN_IDENTIFIER,
                        OrderSuppliesConstants.MODEL_COVERAGE_REGISTER).create();
                registerEntry.setField(CoverageRegisterFields.DATE, order.getDateField(OrderFields.FINISH_DATE));
                registerEntry.setField(CoverageRegisterFields.ORDER, order);
                registerEntry.setField(CoverageRegisterFields.ORDER_NUMBER, order.getStringField(OrderFields.NUMBER));
                registerEntry.setField(CoverageRegisterFields.OPERATION,
                        technologyOperationComponent.getBelongsToField(TechnologyOperationComponentFields.OPERATION));
                registerEntry.setField(CoverageRegisterFields.PRODUCT, product);
                registerEntry.setField(CoverageRegisterFields.PRODUCT_NUMBER, product.getStringField(ProductFields.NUMBER));
                registerEntry.setField(CoverageRegisterFields.QUANTITY, plannedQuantity);
                registerEntry.setField(CoverageRegisterFields.PRODUCTION_COUNTING_QUANTITIES, plannedQuantity);
                registerEntry.setField(CoverageRegisterFields.EVENT_TYPE, "05orderOutput");
                registerEntry.setField(CoverageRegisterFields.TECHNOLOGY_OPERATION_COMPONENT, technologyOperationComponent);
                if (plannedQuantityChanged
                        && order.getDecimalField(OrderFields.PLANNED_QUANTITY).compareTo(plannedQuantity) == -1) {
                    order.setField(OrderFields.PLANNED_QUANTITY, plannedQuantity);
                    if (Objects.isNull(order.getId())) {
                        order.setField(OrderFields.COMMISSIONED_PLANNED_QUANTITY, plannedQuantity);
                    } else {
                        orderHooks.setProductQuantity(order);
                    }
                    order.addGlobalMessage(
                            "orders.order.message.plannedQuantityChanged",
                            false,
                            false,
                            numberService.formatWithMinimumFractionDigits(plannedQuantity, 0),
                            numberService.formatWithMinimumFractionDigits(technologyOperationComponent.getDecimalField(
                                    TechnologyOperationComponentFieldsTNFO.PRODUCTION_IN_ONE_CYCLE),0));
                }
                entries.add(registerEntry);
            }
        }

    }

    private boolean checkIfProductIsFinalProduct(final Entity order, final Entity technologyOperationComponent,
            final Entity product) {
        return (checkIfProductsAreSame(order, product) && checkIfTechnologyOperationComponentsAreSame(order,
                technologyOperationComponent));
    }

    private boolean checkIfProductsAreSame(final Entity order, final Entity product) {
        Entity orderProduct = order.getBelongsToField(OrderFields.PRODUCT);

        if (orderProduct == null) {
            return false;
        } else {
            return product.getId().equals(orderProduct.getId());
        }
    }

    private boolean checkIfTechnologyOperationComponentsAreSame(final Entity order, final Entity technologyOperationComponent) {
        Entity orderTechnologyOperationComponent = getOrderTechnologyOperationComponent(order);

        if (orderTechnologyOperationComponent == null) {
            return false;
        } else {
            return technologyOperationComponent.getId().equals(orderTechnologyOperationComponent.getId());
        }
    }

    private Entity getOrderTechnologyOperationComponent(final Entity order) {
        Entity technology = order.getBelongsToField(OrderFields.TECHNOLOGY);

        if (technology == null) {
            return null;
        } else {
            Entity technologyOperationComponent = technology.getTreeField(TechnologyFields.OPERATION_COMPONENTS).getRoot();

            return technologyOperationComponent;
        }
    }

    private boolean checkIfPlannedQuantityChange(final Entity order, final Entity orderDB) {
        return order.getDecimalField(OrderFields.PLANNED_QUANTITY).compareTo(orderDB.getDecimalField(OrderFields.PLANNED_QUANTITY)) != 0;
    }

    private boolean checkIfOrderChange(final Entity order, final Entity orderDB) {
        if (!order.getStringField(OrderFields.NUMBER).equals(orderDB.getStringField(OrderFields.NUMBER))) {
            return true;
        }
        if (order.getDateField(OrderFields.START_DATE) == null) {
            return true;
        }
        if (!order.getDateField(OrderFields.START_DATE).equals(orderDB.getDateField(OrderFields.START_DATE))) {
            return true;
        }
        if (order.getDateField(OrderFields.FINISH_DATE) == null) {
            return true;
        }
        if (!order.getDateField(OrderFields.FINISH_DATE).equals(orderDB.getDateField(OrderFields.FINISH_DATE))) {
            return true;
        }
        if (order.getDecimalField(OrderFields.PLANNED_QUANTITY).compareTo(orderDB.getDecimalField(OrderFields.PLANNED_QUANTITY)) != 0) {
            return true;
        }
        if (order.getStringField(OrderFieldsPC.TYPE_OF_PRODUCTION_RECORDING) == null) {
            return false;
        }
        if (orderDB.getStringField(OrderFieldsPC.TYPE_OF_PRODUCTION_RECORDING) == null) {
            return false;
        }
        if (!order.getStringField(OrderFieldsPC.TYPE_OF_PRODUCTION_RECORDING).equals(
                orderDB.getStringField(OrderFieldsPC.TYPE_OF_PRODUCTION_RECORDING))) {
            return true;
        }
        return false;
    }
}
