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
package com.qcadoo.mes.deliveries.columnExtension;

import static com.qcadoo.mes.basic.constants.ProductFields.NAME;
import static com.qcadoo.mes.basic.constants.ProductFields.NUMBER;
import static com.qcadoo.mes.basic.constants.ProductFields.UNIT;
import static com.qcadoo.mes.deliveries.constants.DeliveredProductFields.DAMAGED_QUANTITY;
import static com.qcadoo.mes.deliveries.constants.DeliveredProductFields.DELIVERED_QUANTITY;
import static com.qcadoo.mes.deliveries.constants.DeliveredProductFields.SUCCESSION;
import static com.qcadoo.mes.deliveries.constants.OrderedProductFields.DESCRIPTION;
import static com.qcadoo.mes.deliveries.constants.OrderedProductFields.ORDERED_QUANTITY;
import static com.qcadoo.mes.deliveries.constants.OrderedProductFields.PRODUCT;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.qcadoo.mes.deliveries.DeliveriesService;
import com.qcadoo.mes.deliveries.constants.DeliveredProductFields;
import com.qcadoo.mes.deliveries.constants.DeliveriesConstants;
import com.qcadoo.mes.deliveries.constants.OrderedProductFields;
import com.qcadoo.mes.deliveries.print.DeliveryColumnFiller;
import com.qcadoo.mes.deliveries.print.DeliveryProduct;
import com.qcadoo.mes.deliveries.print.OrderColumnFiller;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.NumberService;

@Component
public class DeliveriesColumnFiller implements DeliveryColumnFiller, OrderColumnFiller {

    @Autowired
    private DeliveriesService deliveriesService;

    @Autowired
    private NumberService numberService;

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Override
    public Map<DeliveryProduct, Map<String, String>> getDeliveryProductsColumnValues(final List<DeliveryProduct> deliveryProducts) {
        Map<DeliveryProduct, Map<String, String>> values = new HashMap<DeliveryProduct, Map<String, String>>();

        Integer succession = 0;
        for (DeliveryProduct deliveryProduct : deliveryProducts) {
            succession++;
            if (!values.containsKey(deliveryProduct)) {
                values.put(deliveryProduct, new HashMap<String, String>());
            }

            fillProductNumber(values, deliveryProduct);
            fillProductName(values, deliveryProduct);
            fillProductUnit(values, deliveryProduct);

            fillSuccession(values, deliveryProduct, succession);

            fillOrderedQuantity(values, deliveryProduct);
            fillDeliveredQuantity(values, deliveryProduct);
            fillDamagedQuantity(values, deliveryProduct);

            fillPricePerUnit(values, deliveryProduct);
            fillTotalPrice(values, deliveryProduct);
            fillCurrency(values, deliveryProduct);
        }

        return values;
    }

    @Override
    public Map<Entity, Map<String, String>> getOrderedProductsColumnValues(final List<Entity> orderedProducts) {
        Map<Entity, Map<String, String>> values = new HashMap<Entity, Map<String, String>>();

        for (Entity orderedProduct : orderedProducts) {
            if (!values.containsKey(orderedProduct)) {
                values.put(orderedProduct, new HashMap<String, String>());
            }

            fillProductNumber(values, orderedProduct);
            fillProductName(values, orderedProduct);
            fillProductUnit(values, orderedProduct);

            fillDescription(values, orderedProduct);
            fillSuccession(values, orderedProduct);
            fillOrderedQuantity(values, orderedProduct);

            fillPricePerUnit(values, orderedProduct);
            fillTotalPrice(values, orderedProduct);
            fillCurrency(values, orderedProduct);
        }

        return values;
    }

    private void fillProductNumber(final Map<Entity, Map<String, String>> values, final Entity orderedProduct) {
        String productNumber = null;

        if (orderedProduct == null) {
            productNumber = "";
        } else {
            Entity product = orderedProduct.getBelongsToField(PRODUCT);

            productNumber = product.getStringField(NUMBER);
        }

        values.get(orderedProduct).put("productNumber", productNumber);
    }

    private void fillProductNumber(final Map<DeliveryProduct, Map<String, String>> values, final DeliveryProduct deliveryProduct) {
        String productNumber = null;

        if (deliveryProduct == null) {
            productNumber = "";
        } else {
            Entity product = deliveriesService.getProduct(deliveryProduct);

            productNumber = product.getStringField(NUMBER);
        }

        values.get(deliveryProduct).put("productNumber", productNumber);
    }

    private void fillProductName(final Map<DeliveryProduct, Map<String, String>> values, final DeliveryProduct deliveryProduct) {
        String productName = null;

        if (deliveryProduct == null) {
            productName = "";
        } else {
            Entity product = deliveriesService.getProduct(deliveryProduct);

            productName = product.getStringField(NAME);
        }

        values.get(deliveryProduct).put("productName", productName);
    }

    private void fillProductName(final Map<Entity, Map<String, String>> values, final Entity orderedProduct) {
        String productName = null;

        if (orderedProduct == null) {
            productName = "";
        } else {
            Entity product = orderedProduct.getBelongsToField(PRODUCT);

            productName = product.getStringField(NAME);
        }

        values.get(orderedProduct).put("productName", productName);
    }

    private void fillProductUnit(final Map<Entity, Map<String, String>> values, final Entity orderedProduct) {
        values.get(orderedProduct).put("productUnit", orderedProduct == null ? "" : orderedProduct.getBelongsToField(PRODUCT).getStringField(UNIT));
    }

    private void fillProductUnit(final Map<DeliveryProduct, Map<String, String>> values, final DeliveryProduct deliveryProduct) {
        values.get(deliveryProduct).put("productUnit", deliveryProduct == null ? "" : deliveriesService.getProduct(deliveryProduct).getStringField(UNIT));
    }

    private void fillSuccession(final Map<DeliveryProduct, Map<String, String>> values, final DeliveryProduct deliveryProduct,
            final Integer succession) {

        values.get(deliveryProduct).put("succession", succession.toString());
    }

    private void fillOrderedQuantity(final Map<DeliveryProduct, Map<String, String>> values, final DeliveryProduct deliveryProduct) {
        BigDecimal orderedQuantity = null;

        if (deliveryProduct.getOrderedProductId() == null) {
            orderedQuantity = null;
        } else {
            Entity orderedProduct = deliveriesService.getOrderedProduct(deliveryProduct.getOrderedProductId());

            if (orderedProduct != null) {
                orderedQuantity = orderedProduct.getDecimalField(ORDERED_QUANTITY);
            }
        }

        values.get(deliveryProduct).put("orderedQuantity", numberService.format(orderedQuantity));
    }

    private void fillDeliveredQuantity(final Map<DeliveryProduct, Map<String, String>> values,
            final DeliveryProduct deliveryProduct) {
        BigDecimal deliveredQuantity = null;

        if (deliveryProduct.getDeliveredProductId() != null) {
            Entity deliveredProduct = deliveriesService.getDeliveredProduct(deliveryProduct.getDeliveredProductId());

            if (deliveredProduct != null) {
                deliveredQuantity = deliveredProduct.getDecimalField(DELIVERED_QUANTITY);
            }
        }

        values.get(deliveryProduct).put("deliveredQuantity", numberService.format(deliveredQuantity));
    }

    private void fillDamagedQuantity(final Map<DeliveryProduct, Map<String, String>> values, final DeliveryProduct deliveryProduct) {
        BigDecimal damagedQuantity = null;

        if (deliveryProduct.getDeliveredProductId() != null) {
            Entity deliveredProduct = deliveriesService.getDeliveredProduct(deliveryProduct.getDeliveredProductId());

            if (deliveredProduct != null) {
                damagedQuantity = deliveredProduct.getDecimalField(DAMAGED_QUANTITY);
            }
        }

        values.get(deliveryProduct).put("damagedQuantity", numberService.format(damagedQuantity));
    }

    private void fillOrderedQuantity(final Map<Entity, Map<String, String>> values, final Entity orderedProduct) {
        BigDecimal orderedQuantity = null;

        if (orderedProduct != null) {
            orderedQuantity = orderedProduct.getDecimalField(ORDERED_QUANTITY);
        }

        values.get(orderedProduct).put("orderedQuantity", numberService.format(orderedQuantity));
    }

    private void fillSuccession(final Map<Entity, Map<String, String>> values, final Entity orderedProduct) {
        Integer succession = null;

        if (orderedProduct == null) {
            succession = 0;
        } else {
            succession = orderedProduct.getIntegerField(SUCCESSION);
        }

        values.get(orderedProduct).put("succession", succession.toString());
    }

    private void fillDescription(final Map<Entity, Map<String, String>> values, final Entity orderedProduct) {
        String description = "";

        if (orderedProduct == null || StringUtils.isEmpty(orderedProduct.getStringField(DESCRIPTION))) {
            description = "";
        } else {
            description = orderedProduct.getStringField(DESCRIPTION);
        }

        values.get(orderedProduct).put(DESCRIPTION, description);
    }

    private void fillPricePerUnit(Map<Entity, Map<String, String>> values, Entity orderedProduct) {
        BigDecimal pricePerUnit = null;

        if (orderedProduct != null) {
            pricePerUnit = orderedProduct.getDecimalField(OrderedProductFields.PRICE_PER_UNIT);
        }

        if (pricePerUnit == null) {
            values.get(orderedProduct).put(OrderedProductFields.PRICE_PER_UNIT, "");
        } else {
            values.get(orderedProduct).put(OrderedProductFields.PRICE_PER_UNIT, numberService.format(pricePerUnit));
        }
    }

    private void fillPricePerUnit(final Map<DeliveryProduct, Map<String, String>> values, final DeliveryProduct deliveryProduct) {
        BigDecimal pricePerUnit = null;

        if (deliveryProduct.getDeliveredProductId() != null) {
            Entity deliveredProduct = deliveriesService.getDeliveredProduct(deliveryProduct.getDeliveredProductId());

            if (deliveredProduct != null) {
                pricePerUnit = deliveredProduct.getDecimalField(DeliveredProductFields.PRICE_PER_UNIT);
            }
        }

        if (pricePerUnit == null) {
            values.get(deliveryProduct).put(DeliveredProductFields.PRICE_PER_UNIT, "");
        } else {
            values.get(deliveryProduct).put(DeliveredProductFields.PRICE_PER_UNIT, numberService.format(pricePerUnit));
        }
    }

    private void fillTotalPrice(final Map<Entity, Map<String, String>> values, final Entity orderedProduct) {
        BigDecimal totalPrice = null;

        if (orderedProduct != null) {
            totalPrice = orderedProduct.getDecimalField(OrderedProductFields.TOTAL_PRICE);
        }

        if (totalPrice == null) {
            values.get(orderedProduct).put(OrderedProductFields.TOTAL_PRICE, "");
        } else {
            values.get(orderedProduct).put(OrderedProductFields.TOTAL_PRICE, numberService.format(totalPrice));
        }
    }

    private void fillTotalPrice(final Map<DeliveryProduct, Map<String, String>> values, final DeliveryProduct deliveryProduct) {
        BigDecimal totalPrice = null;

        if (deliveryProduct.getDeliveredProductId() != null) {
            Entity deliveredProduct = deliveriesService.getDeliveredProduct(deliveryProduct.getDeliveredProductId());

            if (deliveredProduct != null) {
                totalPrice = deliveredProduct.getDecimalField(DeliveredProductFields.TOTAL_PRICE);
            }
        }

        if (totalPrice == null) {
            values.get(deliveryProduct).put(DeliveredProductFields.TOTAL_PRICE, "");
        } else {
            values.get(deliveryProduct).put(DeliveredProductFields.TOTAL_PRICE, numberService.format(totalPrice));
        }
    }

    private void fillCurrency(final Map<Entity, Map<String, String>> values, final Entity orderedProduct) {
        Entity delivery = orderedProduct.getBelongsToField(OrderedProductFields.DELIVERY);
        String currency = deliveriesService.getCurrency(delivery);
        values.get(orderedProduct).put("currency", currency);
    }

    private void fillCurrency(final Map<DeliveryProduct, Map<String, String>> values, final DeliveryProduct deliveryProduct) {
        if(deliveryProduct.getDeliveredProductId() != null) {
            Entity deliveredProduct = deliveriesService.getDeliveredProduct(deliveryProduct.getDeliveredProductId());
            values.get(deliveryProduct).put("currency", pricePerUnit(deliveredProduct) == null ? "" : deliveriesService.getCurrency(delivery(deliveredProduct)));
        }
    }

    private Entity delivery(Entity deliveredProduct) {
        return deliveredProduct.getBelongsToField(DeliveredProductFields.DELIVERY);
    }

    private BigDecimal pricePerUnit(Entity delivery) {
        return delivery.getDecimalField(DeliveredProductFields.PRICE_PER_UNIT);
    }
}
