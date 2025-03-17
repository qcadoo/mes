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
package com.qcadoo.mes.masterOrders.hooks;

import com.beust.jcommander.internal.Lists;
import com.qcadoo.mes.basic.ParameterService;
import com.qcadoo.mes.basic.constants.ProductFields;
import com.qcadoo.mes.masterOrders.constants.*;
import com.qcadoo.mes.masterOrders.util.MasterOrderOrdersDataProvider;
import com.qcadoo.mes.orders.constants.OrderFields;
import com.qcadoo.mes.orders.constants.OrdersConstants;
import com.qcadoo.model.api.*;
import com.qcadoo.model.api.search.SearchCriterion;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.model.constants.DictionaryItemFields;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.qcadoo.model.api.search.SearchRestrictions.and;

@Service
public class MasterOrderProductHooks {

    @Autowired
    private MasterOrderOrdersDataProvider masterOrderOrdersDataProvider;

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private DictionaryService dictionaryService;

    @Autowired
    private ParameterService parameterService;

    public void onSave(final DataDefinition masterOrderProductDD, final Entity masterOrderProduct) {
        setMasterOrderPositionStatus(masterOrderProduct);
        checkMasterOrderProductTechnology(masterOrderProduct);
        setQuantityTakenFromWarehouse(masterOrderProduct);
        setMasterOrderState(masterOrderProduct);
        setMasterOrderProductAttrValues(masterOrderProduct);
    }


    private void setMasterOrderPositionStatus(final Entity masterOrderProduct) {
        if (Objects.isNull(masterOrderProduct.getId())) {
            Entity item = dictionaryService.getItemEntityByTechnicalCode(MasterOrderProductFields.MASTER_ORDER_POSITION_STATUS,
                    MasterOrderPositionStatus.NEW.getStringValue());

            if (Objects.nonNull(item)) {
                masterOrderProduct.setField(MasterOrderProductFields.MASTER_ORDER_POSITION_STATUS,
                        item.getStringField(DictionaryItemFields.NAME));
            }
        }
    }

    private void checkMasterOrderProductTechnology(final Entity masterOrderProduct) {
        if (Objects.nonNull(masterOrderProduct.getBelongsToField(MasterOrderProductFields.TECHNOLOGY))) {
            List<Entity> orders = getOrderDD().find()
                    .add(SearchRestrictions.belongsTo(OrderFieldsMO.MASTER_ORDER,
                            masterOrderProduct.getBelongsToField(MasterOrderProductFields.MASTER_ORDER)))
                    .add(SearchRestrictions.belongsTo(OrderFields.PRODUCT,
                            masterOrderProduct.getBelongsToField(MasterOrderProductFields.PRODUCT))).list().getEntities();

            orders = orders.stream().filter(order -> Objects.nonNull(order.getBelongsToField(OrderFields.TECHNOLOGY))
                            && !order.getBelongsToField(OrderFields.TECHNOLOGY).getId().equals(
                            masterOrderProduct.getBelongsToField(MasterOrderProductFields.TECHNOLOGY).getId()))
                    .collect(Collectors.toList());

            if (!orders.isEmpty()) {
                masterOrderProduct.addGlobalMessage(
                        "masterOrders.masterOrderProduct.info.needToChangeTechnologyForExistingProductionOrders", false, false);
            }
        }
    }

    private void setQuantityTakenFromWarehouse(Entity masterOrderProduct) {
        masterOrderProduct.setField(MasterOrderProductFields.QUANTITY_TAKEN_FROM_WAREHOUSE, BigDecimalUtils
                .convertNullToZero(masterOrderProduct.getDecimalField(MasterOrderProductFields.QUANTITY_TAKEN_FROM_WAREHOUSE)));
    }

    private void setMasterOrderState(final Entity masterOrderProduct) {
        Entity masterOrder = masterOrderProduct.getBelongsToField(MasterOrderProductFields.MASTER_ORDER);

        if (MasterOrderState.NEW.getStringValue().equals(masterOrder.getStringField(MasterOrderFields.STATE))
                && masterOrderProduct.getDecimalField(MasterOrderProductFields.QUANTITY_TAKEN_FROM_WAREHOUSE).compareTo(
                BigDecimal.ZERO) > 0) {
            masterOrder.setField(MasterOrderFields.STATE, MasterOrderState.IN_EXECUTION.getStringValue());

            masterOrder = masterOrder.getDataDefinition().save(masterOrder);
        }

        if (isCompleteMasterOrderAfterOrderingPositions(masterOrderProduct, masterOrder)) {
            masterOrder.setField(MasterOrderFields.STATE, MasterOrderState.COMPLETED.getStringValue());

            masterOrder.getDataDefinition().save(masterOrder);
        }
    }

    private boolean isCompleteMasterOrderAfterOrderingPositions(final Entity masterOrderProduct,
                                                                final Entity masterOrder) {
        return parameterService.getParameter().getBooleanField(ParameterFieldsMO.COMPLETE_MASTER_ORDER_AFTER_ORDERING_POSITIONS)
                && !masterOrder.getHasManyField(MasterOrderFields.MASTER_ORDER_PRODUCTS).isEmpty()
                && !MasterOrderState.COMPLETED.getStringValue().equals(masterOrder.getStringField(MasterOrderFields.STATE))
                && MasterOrderPositionStatus.ORDERED.getText().equals(
                masterOrderProduct.getStringField(MasterOrderProductFields.MASTER_ORDER_POSITION_STATUS))
                && isAllOrdered(masterOrderProduct, masterOrder);
    }

    private boolean isAllOrdered(final Entity masterOrderProduct, final Entity masterOrder) {
        List<Entity> newPositions = masterOrder.getHasManyField(MasterOrderFields.MASTER_ORDER_PRODUCTS)
                .stream().filter(mop -> MasterOrderPositionStatus.NEW.getText().equals(
                        mop.getStringField(MasterOrderProductFields.MASTER_ORDER_POSITION_STATUS)))
                .filter(mop -> !mop.getId().equals(masterOrderProduct.getId())).collect(Collectors.toList());

        return newPositions.isEmpty();
    }


    public boolean onDelete(final DataDefinition masterOrderProductDD, final Entity masterOrderProduct) {
        return checkAssignedOrder(masterOrderProduct);
    }

    private boolean checkAssignedOrder(final Entity masterOrderProduct) {
        if (Objects.isNull(masterOrderProduct.getId())) {
            return true;
        }

        Entity masterOrder = masterOrderProduct.getBelongsToField(MasterOrderProductFields.MASTER_ORDER);
        Entity product = masterOrderProduct.getBelongsToField(MasterOrderProductFields.PRODUCT);
        String vendorInfo = masterOrderProduct.getStringField(MasterOrderProductFields.VENDOR_INFO);

        SearchCriterion searchCriterion;
        if (vendorInfo != null) {
            searchCriterion = and(SearchRestrictions.belongsTo(OrderFields.PRODUCT, product), SearchRestrictions.eq(OrderFields.VENDOR_INFO, vendorInfo));
        } else {
            searchCriterion = and(SearchRestrictions.belongsTo(OrderFields.PRODUCT, product), SearchRestrictions.isNull(OrderFields.VENDOR_INFO));
        }

        long numOfBelongingOrdersMatchingProduct = masterOrderOrdersDataProvider.countBelongingOrders(masterOrder, searchCriterion);

        if (numOfBelongingOrdersMatchingProduct > 0) {
            masterOrderProduct.addGlobalError("masterOrders.masterOrderProduct.delete.existsAssignedOrder");

            return false;
        }

        return true;
    }

    private void setMasterOrderProductAttrValues(final Entity masterOrderProduct) {
        if (Objects.isNull(masterOrderProduct.getId())) {
            Entity product = masterOrderProduct.getBelongsToField(MasterOrderProductFields.PRODUCT);

            if (Objects.nonNull(product)) {
                List<Entity> orderedProductConfiguratorAttributes = getOrderedProductConfiguratorAttributes(product);

                if (orderedProductConfiguratorAttributes.isEmpty()) {
                    Entity parent = product.getBelongsToField(ProductFields.PARENT);

                    orderedProductConfiguratorAttributes = getOrderedProductConfiguratorAttributes(parent);
                }

                List<Entity> masterOrderProductAttrValues = Lists.newArrayList();

                orderedProductConfiguratorAttributes.stream().sorted(Comparator.comparing(orderedProductConfiguratorAttribute ->
                        orderedProductConfiguratorAttribute.getIntegerField(OrderedProductConfiguratorAttributeFields.SUCCESSION))).forEach(orderedProductConfiguratorAttribute -> {
                    Entity attribute = orderedProductConfiguratorAttribute.getBelongsToField(OrderedProductConfiguratorAttributeFields.ATTRIBUTE);
                    Integer succession = orderedProductConfiguratorAttribute.getIntegerField(OrderedProductConfiguratorAttributeFields.SUCCESSION);

                    Entity masterOrderProductAttrValue = getMasterOrderProductAttrValueDD().create();

                    masterOrderProductAttrValue.setField(MasterOrderProductAttrValueFields.ATTRIBUTE, attribute);
                    masterOrderProductAttrValue.setField(MasterOrderProductAttrValueFields.SUCCESSION, succession);

                    masterOrderProductAttrValues.add(masterOrderProductAttrValue);
                });

                if (!masterOrderProductAttrValues.isEmpty()) {
                    masterOrderProduct.addGlobalMessage("masterOrders.masterOrderProduct.masterOrderProductAttrValues.filled");
                }

                masterOrderProduct.setField(MasterOrderProductFields.MASTER_ORDER_PRODUCT_ATTR_VALUES, masterOrderProductAttrValues);
            }
        }
    }

    private List<Entity> getOrderedProductConfiguratorAttributes(final Entity product) {
        List<Entity> orderedProductConfiguratorAttributes = Lists.newArrayList();

        if (Objects.nonNull(product)) {
            Entity orderedProductConfigurator = product.getBelongsToField(ProductFieldsMO.ORDERED_PRODUCT_CONFIGURATOR);

            if (Objects.nonNull(orderedProductConfigurator)) {
                orderedProductConfiguratorAttributes = orderedProductConfigurator.getHasManyField(OrderedProductConfiguratorFields.ORDERED_PRODUCT_CONFIGURATOR_ATTRIBUTES);
            }
        }

        return orderedProductConfiguratorAttributes;
    }

    private DataDefinition getOrderDD() {
        return dataDefinitionService
                .get(OrdersConstants.PLUGIN_IDENTIFIER, OrdersConstants.MODEL_ORDER);
    }

    private DataDefinition getMasterOrderProductAttrValueDD() {
        return dataDefinitionService
                .get(MasterOrdersConstants.PLUGIN_IDENTIFIER, MasterOrdersConstants.MASTER_ORDER_PRODUCT_ATTR_VALUE);
    }

}
