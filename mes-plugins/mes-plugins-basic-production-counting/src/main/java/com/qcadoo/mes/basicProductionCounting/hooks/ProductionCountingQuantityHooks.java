/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.4
 * <p>
 * This file is part of Qcadoo.
 * <p>
 * Qcadoo is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation; either version 3 of the License,
 * or (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details.
 * <p>
 * You should have received a copy of the GNU Affero General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 * ***************************************************************************
 */
package com.qcadoo.mes.basicProductionCounting.hooks;

import static com.qcadoo.model.api.search.SearchOrders.asc;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import com.google.common.base.Strings;
import com.qcadoo.mes.basic.constants.ProductFields;
import com.qcadoo.mes.basicProductionCounting.ProductionCountingQuantityChangeService;
import com.qcadoo.mes.orders.constants.OrderFields;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;
import com.qcadoo.mes.basicProductionCounting.BasicProductionCountingService;
import com.qcadoo.mes.basicProductionCounting.constants.BasicProductionCountingConstants;
import com.qcadoo.mes.basicProductionCounting.constants.BasicProductionCountingFields;
import com.qcadoo.mes.basicProductionCounting.constants.OrderFieldsBPC;
import com.qcadoo.mes.basicProductionCounting.constants.ProductionCountingAttributeValueFields;
import com.qcadoo.mes.basicProductionCounting.constants.ProductionCountingQuantityFields;
import com.qcadoo.mes.basicProductionCounting.constants.ProductionCountingQuantityRole;
import com.qcadoo.mes.basicProductionCounting.constants.ProductionCountingQuantityTypeOfMaterial;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchCriteriaBuilder;
import com.qcadoo.model.api.search.SearchProjections;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.plugin.api.PluginUtils;

@Service
public class ProductionCountingQuantityHooks {

    public static final String MASTER_ORDERS = "masterOrders";

    public static final String MASTER_ORDER = "masterOrder";

    public static final String MASTER_ORDER_PRODUCT = "masterOrderProduct";

    public static final String MASTER_ORDER_PRODUCT_ATTR_VALUES = "masterOrderProductAttrValues";

    @Autowired
    private BasicProductionCountingService basicProductionCountingService;

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private ProductionCountingQuantityChangeService productionCountingQuantityChangeService;

    public void onSave(final DataDefinition productionCountingQuantityDD, final Entity productionCountingQuantity) {
        productionCountingQuantityChangeService.addEntry(productionCountingQuantity);
    }

    public void onCreate(final DataDefinition productionCountingQuantityDD, final Entity productionCountingQuantity) {
        fillOrder(productionCountingQuantity);
        fillBasicProductionCounting(productionCountingQuantity);
        fillIsNonComponent(productionCountingQuantity);
        moveAttributesFromMasterOrderProduct(productionCountingQuantity);
    }

    private void moveAttributesFromMasterOrderProduct(Entity productionCountingQuantity) {
        Entity order = productionCountingQuantity.getBelongsToField(ProductionCountingQuantityFields.ORDER);

        if (Objects.nonNull(order) && PluginUtils.isEnabled(MASTER_ORDERS)
                && Objects.nonNull(order.getBelongsToField(MASTER_ORDER))) {
            Entity product = productionCountingQuantity.getBelongsToField(ProductionCountingQuantityFields.PRODUCT);

            if (Objects.nonNull(product)) {
                String typeOfMaterial = productionCountingQuantity
                        .getStringField(ProductionCountingQuantityFields.TYPE_OF_MATERIAL);

                if (checkIfIsFinalProduct(typeOfMaterial)) {
                    Entity masterOrderProduct = dataDefinitionService.get(MASTER_ORDERS, MASTER_ORDER_PRODUCT).find()
                            .add(SearchRestrictions.belongsTo(MASTER_ORDER, order.getBelongsToField(MASTER_ORDER)))
                            .add(SearchRestrictions.belongsTo(ProductionCountingQuantityFields.PRODUCT, product)).setMaxResults(1)
                            .uniqueResult();

                    if (Objects.nonNull(masterOrderProduct)) {
                        List<Entity> masterOrderProductAttrValues = masterOrderProduct
                                .getHasManyField(MASTER_ORDER_PRODUCT_ATTR_VALUES);

                        List<Entity> productionCountingAttributeValues = Lists.newArrayList();

                        for (Entity masterOrderProductAttrValue : masterOrderProductAttrValues) {
                            Entity productionCountingAttributeValue = dataDefinitionService
                                    .get(BasicProductionCountingConstants.PLUGIN_IDENTIFIER,
                                            BasicProductionCountingConstants.MODEL_PRODUCTION_COUNTING_ATTRIBUTE_VALUE)
                                    .create();

                            productionCountingAttributeValue.setField(ProductionCountingAttributeValueFields.ATTRIBUTE,
                                    masterOrderProductAttrValue.getField(ProductionCountingAttributeValueFields.ATTRIBUTE));
                            productionCountingAttributeValue.setField(ProductionCountingAttributeValueFields.ATTRIBUTE_VALUE,
                                    masterOrderProductAttrValue.getField(ProductionCountingAttributeValueFields.ATTRIBUTE_VALUE));
                            productionCountingAttributeValue.setField(ProductionCountingAttributeValueFields.VALUE,
                                    masterOrderProductAttrValue.getField(ProductionCountingAttributeValueFields.VALUE));
                            productionCountingAttributeValues.add(productionCountingAttributeValue);
                        }

                        productionCountingQuantity.setField(ProductionCountingQuantityFields.PRODUCTION_COUNTING_ATTRIBUTE_VALUES,
                                productionCountingAttributeValues);
                    }
                }
            }
        }
    }

    public boolean onDelete(final DataDefinition productionCountingQuantityDD, final Entity productionCountingQuantity) {
        productionCountingQuantityChangeService.addRemoveEntry(productionCountingQuantity);
        if (productionCountingQuantity.getStringField(ProductionCountingQuantityFields.TYPE_OF_MATERIAL)
                .equals(ProductionCountingQuantityTypeOfMaterial.ADDITIONAL_FINAL_PRODUCT.getStringValue())) {
            Entity order = productionCountingQuantity.getBelongsToField(ProductionCountingQuantityFields.ORDER);

            Entity p = productionCountingQuantity.getBelongsToField(ProductionCountingQuantityFields.PRODUCT);

            List<Entity> additionalFinalProducts = order.getHasManyField(OrderFieldsBPC.PRODUCTION_COUNTING_QUANTITIES).stream().filter(pcq -> pcq.getStringField(ProductionCountingQuantityFields.TYPE_OF_MATERIAL)
                            .equals(ProductionCountingQuantityTypeOfMaterial.ADDITIONAL_FINAL_PRODUCT.getStringValue()))
                    .filter(pcq -> !pcq.getBelongsToField(ProductionCountingQuantityFields.PRODUCT).getId().equals(p.getId()))
                    .map(pcq -> pcq.getBelongsToField(ProductionCountingQuantityFields.PRODUCT))
                    .collect(Collectors.toList());

            String additionalFinalProductsNumbers = additionalFinalProducts.stream()
                    .map(prod -> prod.getStringField(ProductFields.NUMBER) + " - " + prod.getStringField(ProductFields.NAME))
                    .collect(Collectors.joining("\n"));

            order.setField(OrderFields.ADDITIONAL_FINAL_PRODUCTS, additionalFinalProductsNumbers);
            order.getDataDefinition().fastSave(order);
        }
        return deleteBasicProductionCounting(productionCountingQuantity);
    }

    private void fillOrder(final Entity productionCountingQuantity) {
        if (Objects.isNull(productionCountingQuantity.getBelongsToField(ProductionCountingQuantityFields.ORDER))) {
            Entity basicProductionCounting = productionCountingQuantity
                    .getBelongsToField(ProductionCountingQuantityFields.BASIC_PRODUCTION_COUNTING);

            if (Objects.nonNull(basicProductionCounting)) {
                Entity order = basicProductionCounting.getBelongsToField(BasicProductionCountingFields.ORDER);

                productionCountingQuantity.setField(ProductionCountingQuantityFields.ORDER, order);
            }
        }
    }

    private void fillBasicProductionCounting(final Entity productionCountingQuantity) {
        if (Objects.isNull(
                productionCountingQuantity.getBelongsToField(ProductionCountingQuantityFields.BASIC_PRODUCTION_COUNTING))) {
            Entity order = productionCountingQuantity.getBelongsToField(ProductionCountingQuantityFields.ORDER);
            Entity product = productionCountingQuantity.getBelongsToField(ProductionCountingQuantityFields.PRODUCT);
            String typeOfMaterial = productionCountingQuantity.getStringField(ProductionCountingQuantityFields.TYPE_OF_MATERIAL);
            String role = productionCountingQuantity.getStringField(ProductionCountingQuantityFields.ROLE);

            if (checkIfShouldFillBasicProductionCounting(order, product, typeOfMaterial, role)) {
                productionCountingQuantity.setField(ProductionCountingQuantityFields.BASIC_PRODUCTION_COUNTING,
                        fillBasicProductionCounting(order, product));
            }
        }
    }

    private boolean checkIfShouldFillBasicProductionCounting(final Entity order, final Entity product,
                                                             final String typeOfMaterial, final String role) {
        return (Objects.nonNull(order) && Objects.nonNull(product) && !checkIfBasicProductionCountingIsEmpty(order)
                && (checkIfIsUsed(role) || (checkIfIsProduced(role) && checkIfIsWaste(typeOfMaterial))));
    }

    private boolean checkIfBasicProductionCountingIsEmpty(final Entity order) {
        SearchCriteriaBuilder searchBuilder = order.getHasManyField(OrderFieldsBPC.BASIC_PRODUCTION_COUNTINGS).find()
                .setProjection(SearchProjections.alias(SearchProjections.rowCount(), "count")).addOrder(asc("count"));

        return (Long) searchBuilder.setMaxResults(1).uniqueResult().getField("count") == 0;
    }

    private Entity fillBasicProductionCounting(final Entity order, final Entity product) {
        Entity basicProductionCounting = getBasicProductionCounting(order, product);

        if (Objects.isNull(basicProductionCounting)) {
            basicProductionCounting = basicProductionCountingService.createBasicProductionCounting(order, product);
        }

        return basicProductionCounting;
    }

    private Entity getBasicProductionCounting(final Entity order, final Entity product) {
        return order.getHasManyField(OrderFieldsBPC.BASIC_PRODUCTION_COUNTINGS).find()
                .add(SearchRestrictions.belongsTo(BasicProductionCountingFields.PRODUCT, product)).setMaxResults(1)
                .uniqueResult();
    }

    private void fillIsNonComponent(final Entity productionCountingQuantity) {
        if (Objects.isNull(productionCountingQuantity.getField(ProductionCountingQuantityFields.IS_NON_COMPONENT))) {
            String typeOfMaterial = productionCountingQuantity.getStringField(ProductionCountingQuantityFields.TYPE_OF_MATERIAL);

            boolean isNonComponent = !checkIfIsFinalProduct(typeOfMaterial) && !checkIfIsComponent(typeOfMaterial);

            productionCountingQuantity.setField(ProductionCountingQuantityFields.IS_NON_COMPONENT, isNonComponent);
        }
    }

    private boolean checkIfIsFinalProduct(final String typeOfMaterial) {
        return (ProductionCountingQuantityTypeOfMaterial.FINAL_PRODUCT.getStringValue().equals(typeOfMaterial));
    }

    private boolean checkIfIsComponent(final String typeOfMaterial) {
        return (ProductionCountingQuantityTypeOfMaterial.COMPONENT.getStringValue().equals(typeOfMaterial));
    }

    private boolean checkIfIsWaste(final String typeOfMaterial) {
        return (ProductionCountingQuantityTypeOfMaterial.WASTE.getStringValue().equals(typeOfMaterial));
    }

    private boolean checkIfIsUsed(final String role) {
        return (ProductionCountingQuantityRole.USED.getStringValue().equals(role));
    }

    private boolean checkIfIsProduced(final String role) {
        return (ProductionCountingQuantityRole.PRODUCED.getStringValue().equals(role));
    }

    private boolean deleteBasicProductionCounting(final Entity productionCountingQuantity) {
        boolean isDeleted = true;

        Entity basicProductionCounting = productionCountingQuantity
                .getBelongsToField(ProductionCountingQuantityFields.BASIC_PRODUCTION_COUNTING);

        if (Objects.nonNull(basicProductionCounting) && checkIfItIsLastProductionCountingQuantity(basicProductionCounting)) {
            productionCountingQuantity.setField(ProductionCountingQuantityFields.BASIC_PRODUCTION_COUNTING, null);
            productionCountingQuantity.getDataDefinition().save(productionCountingQuantity);

            isDeleted = basicProductionCounting.getDataDefinition().delete(basicProductionCounting.getId()).isSuccessfull();
        }

        return isDeleted;
    }

    private boolean checkIfItIsLastProductionCountingQuantity(final Entity basicProductionCounting) {
        return (basicProductionCounting.getHasManyField(BasicProductionCountingFields.PRODUCTION_COUNTING_QUANTITIES)
                .size() == 1);
    }

}
