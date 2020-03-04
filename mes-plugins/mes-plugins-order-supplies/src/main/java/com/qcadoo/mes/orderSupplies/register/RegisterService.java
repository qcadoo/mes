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
import com.qcadoo.mes.basic.constants.ProductFamilyElementType;
import com.qcadoo.mes.basic.constants.ProductFields;
import com.qcadoo.mes.basicProductionCounting.constants.BasicProductionCountingFields;
import com.qcadoo.mes.basicProductionCounting.constants.ProductionCountingQuantityFields;
import com.qcadoo.mes.basicProductionCounting.constants.ProductionCountingQuantityRole;
import com.qcadoo.mes.basicProductionCounting.constants.ProductionCountingQuantityTypeOfMaterial;
import com.qcadoo.mes.orderSupplies.constants.CoverageProductLoggingEventType;
import com.qcadoo.mes.orderSupplies.constants.CoverageRegisterFields;
import com.qcadoo.mes.orderSupplies.constants.OrderFieldsOS;
import com.qcadoo.mes.orderSupplies.constants.OrderSuppliesConstants;
import com.qcadoo.mes.orders.constants.OrderFields;
import com.qcadoo.mes.orders.constants.OrdersConstants;
import com.qcadoo.mes.productionCounting.constants.*;
import com.qcadoo.mes.productionCounting.states.constants.ProductionTrackingStateStringValues;
import com.qcadoo.mes.technologies.TechnologyService;
import com.qcadoo.mes.technologies.constants.ProductToProductGroupFields;
import com.qcadoo.mes.technologies.constants.TechnologiesConstants;
import com.qcadoo.mes.technologies.constants.TechnologyFields;
import com.qcadoo.mes.technologies.constants.TechnologyOperationComponentFields;
import com.qcadoo.mes.technologies.states.constants.TechnologyState;
import com.qcadoo.model.api.BigDecimalUtils;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.NumberService;
import com.qcadoo.model.api.search.JoinType;
import com.qcadoo.model.api.search.SearchCriteriaBuilder;
import com.qcadoo.model.api.search.SearchProjections;
import com.qcadoo.model.api.search.SearchRestrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class RegisterService {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private NumberService numberService;

    @Autowired
    private TechnologyService technologyService;

    public List<Entity> getOPICForTechnology(final Entity technology) {
        String sql = "select opic as opic, product.id as productId, product.number as productNumber, product.entityType as productEntityType, "
                + "operation.id as operationId, toc.id as tocId from #technologies_operationProductInComponent opic "
                + "left join opic.product as product left join opic.operationComponent toc "
                + "left join toc.operation operation left join toc.technology tech where tech.id = :technologyId";

        return dataDefinitionService
                .get(TechnologiesConstants.PLUGIN_IDENTIFIER, TechnologiesConstants.MODEL_OPERATION_PRODUCT_IN_COMPONENT)
                .find(sql).setParameter("technologyId", technology.getId()).list().getEntities();
    }

    public void removeEntriesForOrder(final Entity order, final boolean all) {
        if (all) {
            order.setField(OrderFieldsOS.COVERAGE_REGISTERS, Lists.newArrayList());
        } else {
            List<Entity> entries = order.getHasManyField(OrderFieldsOS.COVERAGE_REGISTERS);
            List<Entity> filtered = entries.stream()
                    .filter(o -> o.getBooleanField(CoverageRegisterFields.FROM_PRODUCTION_COUNTING_QUANTITY))
                    .collect(Collectors.toList());
            order.setField(OrderFieldsOS.COVERAGE_REGISTERS, filtered);
        }

    }

    public void fillExistProductionCountingEntries(final Entity order, final Entity orderDb, List<Entity> oldEntries) {
        List<Entity> entries = orderDb.getHasManyField(OrderFieldsOS.COVERAGE_REGISTERS);
        List<Entity> filtered = entries.stream()
                .filter(o -> o.getBooleanField(CoverageRegisterFields.FROM_PRODUCTION_COUNTING_QUANTITY))
                .collect(Collectors.toList());
        filtered.forEach(o -> o.setField(CoverageRegisterFields.DATE, order.getDateField(OrderFields.START_DATE)));
        List<Entity> entriesOrder = Lists.newArrayList(order.getHasManyField(OrderFieldsOS.COVERAGE_REGISTERS));
        List<Entity> oldTechnologyEntries = oldEntries.stream()
                .filter(o -> !o.getBooleanField(CoverageRegisterFields.FROM_PRODUCTION_COUNTING_QUANTITY))
                .collect(Collectors.toList());
        List<RegisterEntry> mappedEntries = oldTechnologyEntries.stream().map(this::mapToEntry)
                .collect(Collectors.toList());
        for (Entity entity : Lists.newArrayList(entriesOrder)) {
            if (!mappedEntries.contains(new RegisterEntry(entity))) {
                entriesOrder.remove(entity);
            }
        }
        entriesOrder.addAll(filtered);
        order.setField(OrderFieldsOS.COVERAGE_REGISTERS, entriesOrder);

    }

    private RegisterEntry mapToEntry(Entity entity) {
        return new RegisterEntry(entity);
    }

    public Entity findOrderForTechnology(Entity technology) {
        String sql = "select o from #orders_order o left join o.technology tech where tech.id = :technologyID";
        return dataDefinitionService.get(OrdersConstants.PLUGIN_IDENTIFIER, OrdersConstants.MODEL_ORDER).find(sql)
                .setParameter("technologyID", technology.getId()).setMaxResults(1).uniqueResult();
    }

    public void addRegisterEntryForOrder(List<Entity> entries, Entity order, Entity opic, BigDecimal quantity, String eventType,
            List<Long> intermediateProducts) {
        Entity registerEntry = dataDefinitionService.get(OrderSuppliesConstants.PLUGIN_IDENTIFIER,
                OrderSuppliesConstants.MODEL_COVERAGE_REGISTER).create();
        registerEntry.setField(CoverageRegisterFields.DATE, order.getDateField(OrderFields.START_DATE));
        registerEntry.setField(CoverageRegisterFields.ORDER, order);
        registerEntry.setField(CoverageRegisterFields.ORDER_NUMBER, order.getStringField(OrderFields.NUMBER));
        registerEntry.setField(
                CoverageRegisterFields.OPERATION,
                opic.getLongField("operationId"));
        Long productId = opic.getLongField("productId");
        String productNumber = opic.getStringField("productNumber");
        if (ProductFamilyElementType.PRODUCTS_FAMILY.getStringValue().equals(opic.getStringField("productEntityType"))) {
            Entity productToProductGroupTechnology = technologyService
                    .getProductToProductGroupTechnology(order.getBelongsToField(OrderFields.PRODUCT), productId);
            if (productToProductGroupTechnology != null) {
                Entity orderProduct = productToProductGroupTechnology
                        .getBelongsToField(ProductToProductGroupFields.ORDER_PRODUCT);
                productId = orderProduct.getId();
                productNumber = orderProduct.getStringField(ProductFields.NUMBER);
            }
        }
        registerEntry.setField(CoverageRegisterFields.PRODUCT, productId);
        registerEntry.setField(CoverageRegisterFields.PRODUCT_NUMBER, productNumber);
        registerEntry.setField(CoverageRegisterFields.QUANTITY, quantity);
        registerEntry.setField(CoverageRegisterFields.PRODUCTION_COUNTING_QUANTITIES, quantity);
        registerEntry.setField(CoverageRegisterFields.EVENT_TYPE, eventType);
        registerEntry.setField(CoverageRegisterFields.TECHNOLOGY_OPERATION_COMPONENT, opic.getLongField("tocId"));
        if (intermediateProducts.contains(productId)) {
            registerEntry.setField(CoverageRegisterFields.PRODUCT_TYPE, "02intermediate");
        } else {
            registerEntry.setField(CoverageRegisterFields.PRODUCT_TYPE, "01component");
        }
        entries.add(registerEntry);
    }

    public boolean isIntermediate(final Entity product) {
        SearchCriteriaBuilder scb = dataDefinitionService
                .get(TechnologiesConstants.PLUGIN_IDENTIFIER, TechnologiesConstants.MODEL_TECHNOLOGY).find()
                .setProjection(SearchProjections.id()).add(SearchRestrictions.belongsTo(TechnologyFields.PRODUCT, product))
                .add(SearchRestrictions.isNull(TechnologyFields.TECHNOLOGY_TYPE))
                .add(SearchRestrictions.eq(TechnologyFields.STATE, TechnologyState.ACCEPTED.getStringValue()))
                .add(SearchRestrictions.eq(TechnologyFields.MASTER, true));

        return scb.setMaxResults(1).uniqueResult() != null;
    }

    public void saveRegistryEntries(List<Entity> entries) {
        for (Entity entry : entries) {
            entry.getDataDefinition().save(entry);
        }

    }

    public void removeEntriesFromOrder(Entity order) {
        List<Entity> entries = order.getHasManyField(OrderFieldsOS.COVERAGE_REGISTERS);
        for (Entity entry : entries) {
            entry.getDataDefinition().delete(entry.getId());
        }
    }

    public Entity findRegistryEntry(Entity order, Entity product) {
        return dataDefinitionService
                .get(OrderSuppliesConstants.PLUGIN_IDENTIFIER, OrderSuppliesConstants.MODEL_COVERAGE_REGISTER).find()
                .add(SearchRestrictions.belongsTo(CoverageRegisterFields.ORDER, order))
                .add(SearchRestrictions.belongsTo(CoverageRegisterFields.PRODUCT, product)).setMaxResults(1).uniqueResult();
    }

    public List<Entity> findComponentRegistryEntries(final Entity order) {
        String query = "select registry from #orderSupplies_coverageRegister as registry, "
                + "#technologies_operationProductInComponent as operationProductInComponent "
                + "where registry.order.id = :orderId and eventType in ('04orderInput','03operationInput') "
                + "and productType = '02intermediate' "
                + "and operationProductInComponent.operationComponent = registry.technologyOperationComponent "
                + "and (operationProductInComponent.product = registry.product or operationProductInComponent.product = "
                + "(select productFamily from #technologies_productToProductGroupTechnology as productToProductGroupTechnology "
                + "where productToProductGroupTechnology.finalProduct = registry.order.product and "
                + "productToProductGroupTechnology.orderProduct = registry.product)) order by operationProductInComponent.priority ";
        return dataDefinitionService.get(OrderSuppliesConstants.PLUGIN_IDENTIFIER, "coverageRegister").find(query)
                .setParameter("orderId", order.getId()).list().getEntities();
    }

    public List<Entity> findRegistryEntries(Entity order) {
        return dataDefinitionService
                .get(OrderSuppliesConstants.PLUGIN_IDENTIFIER, OrderSuppliesConstants.MODEL_COVERAGE_REGISTER).find()
                .add(SearchRestrictions.belongsTo(CoverageRegisterFields.ORDER, order)).list().getEntities();
    }

    public List<Entity> findRegistryEntries(Entity order, Entity product) {
        return dataDefinitionService
                .get(OrderSuppliesConstants.PLUGIN_IDENTIFIER, OrderSuppliesConstants.MODEL_COVERAGE_REGISTER).find()
                .add(SearchRestrictions.belongsTo(CoverageRegisterFields.ORDER, order))
                .add(SearchRestrictions.belongsTo(CoverageRegisterFields.PRODUCT, product)).list().getEntities();
    }

    public Entity findRegistryEntry(Entity order, Entity product, Entity toc) {
        SearchCriteriaBuilder scb = dataDefinitionService
                .get(OrderSuppliesConstants.PLUGIN_IDENTIFIER, OrderSuppliesConstants.MODEL_COVERAGE_REGISTER).find()
                .add(SearchRestrictions.belongsTo(CoverageRegisterFields.ORDER, order))
                .add(SearchRestrictions.belongsTo(CoverageRegisterFields.PRODUCT, product));

        if (toc != null) {
            scb = scb.add(SearchRestrictions.belongsTo(CoverageRegisterFields.TECHNOLOGY_OPERATION_COMPONENT, toc));
        }

        return scb.setMaxResults(1).uniqueResult();
    }

    public void updateRegistryEntriesForOrder(Entity order) {
        List<Entity> registryEntries = order.getHasManyField(OrderFieldsOS.COVERAGE_REGISTERS);
        for (Entity entry : registryEntries) {
            Entity entryDB = findRegistryEntry(order, entry.getBelongsToField(CoverageRegisterFields.PRODUCT));
            BigDecimal registredQuantity = entryDB.getDecimalField(CoverageRegisterFields.PRODUCTION_COUNTING_QUANTITIES)
                    .subtract(entryDB.getDecimalField(CoverageRegisterFields.QUANTITY), numberService.getMathContext());
            BigDecimal quantity = entry.getDecimalField(CoverageRegisterFields.PRODUCTION_COUNTING_QUANTITIES).subtract(
                    registredQuantity, numberService.getMathContext());

            if (quantity.compareTo(BigDecimal.ZERO) < 0) {
                entry.setField(CoverageRegisterFields.QUANTITY, BigDecimal.ZERO);
            } else {
                entry.setField(CoverageRegisterFields.QUANTITY, quantity);
            }
        }
    }

    public List<Entity> findTrackingOperationProductInComponents(Entity order, Entity toc, Entity product,
            boolean forEachOperation) {

        SearchCriteriaBuilder scb = dataDefinitionService
                .get(ProductionCountingConstants.PLUGIN_IDENTIFIER,
                        ProductionCountingConstants.MODEL_TRACKING_OPERATION_PRODUCT_IN_COMPONENT)
                .find()
                .createAlias(TrackingOperationProductInComponentFields.PRODUCTION_TRACKING, "pTracking", JoinType.INNER)
                .add(SearchRestrictions.belongsTo("pTracking." + ProductionTrackingFields.ORDER, order))
                .add(SearchRestrictions.eq("pTracking." + ProductionTrackingFields.STATE,
                        ProductionTrackingStateStringValues.ACCEPTED))
                .add(SearchRestrictions.belongsTo(TrackingOperationProductInComponentFields.PRODUCT, product));

        if (forEachOperation) {
            scb.add(SearchRestrictions.belongsTo("pTracking." + ProductionTrackingFields.TECHNOLOGY_OPERATION_COMPONENT, toc));
        }

        return scb.list().getEntities();
    }

    public List<Entity> findTrackingOperationProductOutComponents(Entity order, Entity toc, Entity product,
            boolean forEachOperation) {

        SearchCriteriaBuilder scb = dataDefinitionService
                .get(ProductionCountingConstants.PLUGIN_IDENTIFIER,
                        ProductionCountingConstants.MODEL_TRACKING_OPERATION_PRODUCT_OUT_COMPONENT)
                .find()
                .createAlias(TrackingOperationProductOutComponentFields.PRODUCTION_TRACKING, "pTracking", JoinType.INNER)
                .add(SearchRestrictions.belongsTo("pTracking." + ProductionTrackingFields.ORDER, order))
                .add(SearchRestrictions.eq("pTracking." + ProductionTrackingFields.STATE,
                        ProductionTrackingStateStringValues.ACCEPTED))
                .add(SearchRestrictions.belongsTo(TrackingOperationProductOutComponentFields.PRODUCT, product));

        if (forEachOperation) {
            scb.add(SearchRestrictions.belongsTo("pTracking." + ProductionTrackingFields.TECHNOLOGY_OPERATION_COMPONENT, toc));
        }

        return scb.list().getEntities();
    }

    public BigDecimal getTrackedQuantity(Entity trackingOperationProductInComponent,
            List<Entity> trackingOperationProductInComponents, boolean corrected) {

        BigDecimal trackedQuantity = BigDecimal.ZERO;

        for (Entity trackingProduct : trackingOperationProductInComponents) {

            if (trackingOperationProductInComponent.getId() != null
                    && trackingProduct.getId().equals(trackingOperationProductInComponent.getId())) {
                if (!corrected) {
                    trackedQuantity = trackedQuantity.add(trackingOperationProductInComponent
                            .getDecimalField(TrackingOperationProductInComponentFields.USED_QUANTITY), numberService
                            .getMathContext());
                }
            } else {
                trackedQuantity = trackedQuantity.add(BigDecimalUtils.convertNullToZero(trackingProduct
                        .getDecimalField(TrackingOperationProductInComponentFields.USED_QUANTITY)), numberService
                        .getMathContext());
            }

        }

        return trackedQuantity;
    }

    public void updateRegistryEntriesOnProductionTracking(Entity registerEntry, BigDecimal trackedQuantity) {
        if (registerEntry == null) {
            return;
        }
        BigDecimal demandQuantity = registerEntry.getDecimalField(CoverageRegisterFields.PRODUCTION_COUNTING_QUANTITIES)
                .subtract(trackedQuantity, numberService.getMathContext());

        if (demandQuantity.compareTo(BigDecimal.ZERO) < 0) {
            registerEntry.setField(CoverageRegisterFields.QUANTITY, BigDecimal.ZERO);
        } else {
            registerEntry.setField(CoverageRegisterFields.QUANTITY, demandQuantity);
        }

        registerEntry.getDataDefinition().save(registerEntry);
    }

    public void updateRegistryEntriesOnProductionTracking(List<Entity> registerEntries, BigDecimal trackedQuantity) {
        BigDecimal usedQuantity = BigDecimalUtils.convertNullToZero(trackedQuantity);

        for (Entity registerEntry : registerEntries) {
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

    public Entity createRegisterEntryFromPCQ(Entity productionCountingQuantity) {

        if (!(productionCountingQuantity.getStringField(ProductionCountingQuantityFields.ROLE).equals(
                ProductionCountingQuantityRole.USED.getStringValue()) && productionCountingQuantity.getStringField(
                ProductionCountingQuantityFields.TYPE_OF_MATERIAL).equals(
                ProductionCountingQuantityTypeOfMaterial.COMPONENT.getStringValue()))) {
            return null;
        }
        Entity registryEntry = findRegistryEntry(
                productionCountingQuantity.getBelongsToField(ProductionCountingQuantityFields.ORDER),
                productionCountingQuantity.getBelongsToField(ProductionCountingQuantityFields.PRODUCT),
                productionCountingQuantity.getBelongsToField(ProductionCountingQuantityFields.TECHNOLOGY_OPERATION_COMPONENT));

        if (registryEntry != null) {
            return null;
        }
        Entity order = productionCountingQuantity.getBelongsToField(ProductionCountingQuantityFields.ORDER);
        Entity product = productionCountingQuantity.getBelongsToField(ProductionCountingQuantityFields.PRODUCT);
        Entity toc = productionCountingQuantity
                .getBelongsToField(ProductionCountingQuantityFields.TECHNOLOGY_OPERATION_COMPONENT);

        String typeOfProductionRecording = order.getStringField(OrderFieldsPC.TYPE_OF_PRODUCTION_RECORDING);
        String eventType = null;

        Entity registerEntry = dataDefinitionService.get(OrderSuppliesConstants.PLUGIN_IDENTIFIER,
                OrderSuppliesConstants.MODEL_COVERAGE_REGISTER).create();
        if (TypeOfProductionRecording.FOR_EACH.getStringValue().equals(typeOfProductionRecording)) {
            eventType = CoverageProductLoggingEventType.OPERATION_INPUT.getStringValue();
            if (isIntermediate(product)) {
                registerEntry.setField(CoverageRegisterFields.PRODUCT_TYPE, "02intermediate");
            } else {
                registerEntry.setField(CoverageRegisterFields.PRODUCT_TYPE, "01component");
            }
        } else if (TypeOfProductionRecording.CUMULATED.getStringValue().equals(typeOfProductionRecording)
                || TypeOfProductionRecording.BASIC.getStringValue().equals(typeOfProductionRecording)) {
            eventType = CoverageProductLoggingEventType.ORDER_INPUT.getStringValue();
            if (isIntermediate(product)) {
                registerEntry.setField(CoverageRegisterFields.PRODUCT_TYPE, "02intermediate");
            } else {
                registerEntry.setField(CoverageRegisterFields.PRODUCT_TYPE, "01component");
            }
        }

        registerEntry.setField(CoverageRegisterFields.DATE, order.getDateField(OrderFields.START_DATE));
        registerEntry.setField(CoverageRegisterFields.ORDER, order);
        registerEntry.setField(CoverageRegisterFields.ORDER_NUMBER, order.getStringField(OrderFields.NUMBER));
        if (toc != null) {
            registerEntry.setField(CoverageRegisterFields.OPERATION,
                    toc.getBelongsToField(TechnologyOperationComponentFields.OPERATION));
        }
        registerEntry.setField(CoverageRegisterFields.PRODUCT, product);
        registerEntry.setField(CoverageRegisterFields.PRODUCT_NUMBER, product.getStringField(ProductFields.NUMBER));
        registerEntry.setField(CoverageRegisterFields.QUANTITY,
                productionCountingQuantity.getDecimalField(ProductionCountingQuantityFields.PLANNED_QUANTITY));
        registerEntry.setField(CoverageRegisterFields.PRODUCTION_COUNTING_QUANTITIES,
                productionCountingQuantity.getDecimalField(ProductionCountingQuantityFields.PLANNED_QUANTITY));
        registerEntry.setField(CoverageRegisterFields.EVENT_TYPE, eventType);
        registerEntry.setField(CoverageRegisterFields.TECHNOLOGY_OPERATION_COMPONENT, toc);
        registerEntry.setField(CoverageRegisterFields.FROM_PRODUCTION_COUNTING_QUANTITY, true);

        return registerEntry.getDataDefinition().save(registerEntry);
    }

    public void updateRegisterEntryFromPCQ(Entity productionCountingQuantity) {
        Entity registryEntry = findRegistryEntry(
                productionCountingQuantity.getBelongsToField(ProductionCountingQuantityFields.ORDER),
                productionCountingQuantity.getBelongsToField(ProductionCountingQuantityFields.PRODUCT),
                productionCountingQuantity.getBelongsToField(ProductionCountingQuantityFields.TECHNOLOGY_OPERATION_COMPONENT));

        if (registryEntry == null) {
            return;
        }

        Entity order = productionCountingQuantity.getBelongsToField(ProductionCountingQuantityFields.ORDER);

        String typeOfProductionRecording = order.getStringField(OrderFieldsPC.TYPE_OF_PRODUCTION_RECORDING);
        BigDecimal demandQuantity;
        if (TypeOfProductionRecording.BASIC.getStringValue().equals(typeOfProductionRecording)) {
            if (Objects.isNull(productionCountingQuantity
                    .getBelongsToField(ProductionCountingQuantityFields.BASIC_PRODUCTION_COUNTING))) {
                return;
            }
            demandQuantity = productionCountingQuantity.getDecimalField(ProductionCountingQuantityFields.PLANNED_QUANTITY)
                    .subtract(
                            BigDecimalUtils.convertNullToZero(productionCountingQuantity.getBelongsToField(
                                    ProductionCountingQuantityFields.BASIC_PRODUCTION_COUNTING).getDecimalField(
                                    BasicProductionCountingFields.USED_QUANTITY)), numberService.getMathContext());
        } else {
            BigDecimal trackedQuantity = BigDecimalUtils.convertNullToZero(
                    registryEntry.getDecimalField(CoverageRegisterFields.PRODUCTION_COUNTING_QUANTITIES)).subtract(
                    BigDecimalUtils.convertNullToZero(registryEntry.getDecimalField(CoverageRegisterFields.QUANTITY)),
                    numberService.getMathContext());

            demandQuantity = productionCountingQuantity.getDecimalField(ProductionCountingQuantityFields.PLANNED_QUANTITY)
                    .subtract(trackedQuantity, numberService.getMathContext());
        }

        if (demandQuantity.compareTo(BigDecimal.ZERO) < 0) {
            registryEntry.setField(CoverageRegisterFields.QUANTITY, BigDecimal.ZERO);
        } else {
            registryEntry.setField(CoverageRegisterFields.QUANTITY, demandQuantity);
        }
        registryEntry.setField(CoverageRegisterFields.PRODUCTION_COUNTING_QUANTITIES,
                productionCountingQuantity.getDecimalField(ProductionCountingQuantityFields.PLANNED_QUANTITY));
        registryEntry.getDataDefinition().save(registryEntry);

    }

    public List<Entity> getRegisterEntriesForOrder(Entity order) {
        return dataDefinitionService
                .get(OrderSuppliesConstants.PLUGIN_IDENTIFIER, OrderSuppliesConstants.MODEL_COVERAGE_REGISTER).find()
                .add(SearchRestrictions.belongsTo(CoverageRegisterFields.ORDER, order)).list().getEntities();
    }

    public List<Long> getRegisterOrderProductsIds(final Entity order) {
        return getRegisterEntriesForOrder(order).stream().map(r -> r.getBelongsToField(CoverageRegisterFields.PRODUCT).getId())
                .collect(Collectors.toList());
    }

    public List<Long> getRegisterOrderProductsIds(final List<Entity> order) {
        List<Long> orderIds = order.stream().map(Entity::getId).collect(Collectors.toList());
        List<Entity> entries = dataDefinitionService
                .get(OrderSuppliesConstants.PLUGIN_IDENTIFIER, OrderSuppliesConstants.MODEL_COVERAGE_REGISTER).find()
                .createAlias(CoverageRegisterFields.ORDER, "ord", JoinType.LEFT)
                .add(SearchRestrictions.in("ord.id", Lists.newArrayList(orderIds))).list().getEntities();
        return entries.stream().map(r -> r.getBelongsToField(CoverageRegisterFields.PRODUCT).getId())
                .collect(Collectors.toList());
    }

    public List<Long> getIntermediateProducts(final List<Long> ids) {
        if(ids.isEmpty()) {
            return Collections.EMPTY_LIST;
        }
        String sql = "SELECT technology.id as technologyId, technology.product.id as productId " + "FROM #technologies_technology technology "
                + "WHERE technology.product.id in (:ids)"
                + " and technology.state = '02accepted' and technology.master = true) ";

        List<Entity> entities =  dataDefinitionService
                .get(TechnologiesConstants.PLUGIN_IDENTIFIER, TechnologiesConstants.MODEL_TECHNOLOGY)
                .find(sql).setParameterList("ids", ids).list().getEntities();

        return entities.stream().map(en -> en.getLongField("productId")).collect(Collectors.toList());
    }
}
