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
package com.qcadoo.mes.deliveries.hooks;

import com.qcadoo.mes.basic.ParameterService;
import com.qcadoo.mes.basic.constants.ParameterFields;
import com.qcadoo.mes.basic.util.CurrencyService;
import com.qcadoo.mes.deliveries.DeliveriesService;
import com.qcadoo.mes.deliveries.constants.DeliveredProductFields;
import com.qcadoo.mes.deliveries.constants.DeliveryFields;
import com.qcadoo.mes.deliveries.constants.ParameterFieldsD;
import com.qcadoo.mes.deliveries.states.constants.DeliveryStateChangeDescriber;
import com.qcadoo.mes.deliveries.states.constants.DeliveryStateStringValues;
import com.qcadoo.mes.deliveries.util.DeliveryPricesAndQuantities;
import com.qcadoo.mes.states.service.StateChangeEntityBuilder;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.EntityList;
import com.qcadoo.model.api.NumberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

import static com.qcadoo.mes.deliveries.states.constants.DeliveryState.DRAFT;

@Service
public class DeliveryHooks {

    @Autowired
    private DeliveriesService deliveriesService;

    @Autowired
    private StateChangeEntityBuilder stateChangeEntityBuilder;

    @Autowired
    private DeliveryStateChangeDescriber describer;

    @Autowired
    private NumberService numberService;

    @Autowired
    private ParameterService parameterService;

    @Autowired
    private CurrencyService currencyService;

    public void onCreate(final DataDefinition deliveryDD, final Entity delivery) {
        setInitialState(delivery);
        setDeliveryAddressDefaultValue(delivery);
        setDescriptionDefaultValue(delivery);
        setLocationDefaultValue(deliveryDD, delivery);
        delivery.setField(DeliveryFields.RELEASED_FOR_PAYMENT, false);
        delivery.setField(DeliveryFields.PAID, false);
    }

    public void onCopy(final DataDefinition deliveryDD, final Entity delivery) {
        setInitialState(delivery);
        clearFieldsOnCopy(delivery);
        delivery.setField(DeliveryFields.RELEASED_FOR_PAYMENT, false);
        delivery.setField(DeliveryFields.PAID, false);
    }

    public void onView(final DataDefinition deliveryDD, final Entity delivery) {
        fillOrderedAndDeliveredCumulatedQuantityAndCumulatedTotalPrice(delivery);
    }

    public void onSave(final DataDefinition deliveryDD, final Entity delivery) {
        setStorageLocations(delivery);
        checkCurrency(delivery);
    }

    private void setInitialState(final Entity delivery) {
        stateChangeEntityBuilder.buildInitial(describer, delivery, DRAFT);
    }

    private void checkCurrency(final Entity delivery) {
        Long deliveryId = delivery.getId();
        Entity currency = delivery.getBelongsToField(DeliveryFields.CURRENCY);
        List<Entity> orderedProducts = delivery.getHasManyField(DeliveryFields.ORDERED_PRODUCTS);

        if (Objects.nonNull(deliveryId)) {
            Entity deliveryFromDB = deliveriesService.getDelivery(deliveryId);
            Entity currencyFromDB = deliveryFromDB.getBelongsToField(DeliveryFields.CURRENCY);

            if (Objects.nonNull(currencyFromDB) && (currency == null || !currencyFromDB.getId().equals(currency.getId())) && !orderedProducts.isEmpty()) {
                delivery.addGlobalMessage("deliveries.delivery.currencyChange.orderedProductsPriceVerificationRequired", false, false);
            }
        }

        Entity systemCurrency = parameterService.getParameter().getBelongsToField(ParameterFields.CURRENCY);
        Entity plnCurrency = currencyService.getCurrencyByAlphabeticCode(CurrencyService.PLN);
        if (Objects.nonNull(currency) && !systemCurrency.getId().equals(plnCurrency.getId())
                && !currency.getId().equals(plnCurrency.getId()) && !currency.getId().equals(systemCurrency.getId())) {
            delivery.addGlobalMessage("deliveries.delivery.currency.currencyDifferentFromSystemCurrency", false, false);
        }
    }

    private void clearFieldsOnCopy(final Entity delivery) {
        delivery.setField(DeliveryFields.STATE, DeliveryStateStringValues.DRAFT);
        delivery.setField(DeliveryFields.EXTERNAL_NUMBER, null);
        delivery.setField(DeliveryFields.EXTERNAL_SYNCHRONIZED, true);
    }

    private void setDeliveryAddressDefaultValue(final Entity delivery) {
        String deliveryAddress = delivery.getStringField(DeliveryFields.DELIVERY_ADDRESS);

        if (Objects.isNull(deliveryAddress)) {
            delivery.setField(DeliveryFields.DELIVERY_ADDRESS, deliveriesService.getDeliveryAddressDefaultValue());
        }
    }

    private void setDescriptionDefaultValue(final Entity delivery) {
        String description = delivery.getStringField(DeliveryFields.DESCRIPTION);

        if (Objects.isNull(description)) {
            delivery.setField(DeliveryFields.DESCRIPTION, deliveriesService.getDescriptionDefaultValue());
        }
    }

    private void fillOrderedAndDeliveredCumulatedQuantityAndCumulatedTotalPrice(final Entity delivery) {
        DeliveryPricesAndQuantities deliveryPricesAndQuantities = new DeliveryPricesAndQuantities(delivery, numberService);

        delivery.setField(DeliveryFields.ORDERED_PRODUCTS_CUMULATED_QUANTITY, deliveryPricesAndQuantities.getOrderedCumulatedQuantity());
        delivery.setField(DeliveryFields.DELIVERED_PRODUCTS_CUMULATED_QUANTITY, deliveryPricesAndQuantities.getDeliveredCumulatedQuantity());
        delivery.setField(DeliveryFields.ORDERED_PRODUCTS_CUMULATED_TOTAL_PRICE, deliveryPricesAndQuantities.getOrderedTotalPrice());
        delivery.setField(DeliveryFields.DELIVERED_PRODUCTS_CUMULATED_TOTAL_PRICE, deliveryPricesAndQuantities.getDeliveredTotalPrice());
    }

    public void setLocationDefaultValue(final DataDefinition deliveryDD, final Entity delivery) {
        Entity location = delivery.getBelongsToField(DeliveryFields.LOCATION);

        if (Objects.isNull(location)) {
            delivery.setField(DeliveryFields.LOCATION, parameterService.getParameter().getBelongsToField(DeliveryFields.LOCATION));
        }
    }

    public boolean validate(final DataDefinition deliveryDD, final Entity delivery) {
        if (Objects.isNull(delivery.getBelongsToField(DeliveryFields.SUPPLIER))
                && parameterService.getParameter().getBooleanField(ParameterFieldsD.REQUIRE_SUPPLIER_IDENTIFICATION)) {
            delivery.addError(deliveryDD.getField(DeliveryFields.SUPPLIER), "qcadooView.validate.field.error.missing");
            return false;
        }

        return true;
    }

    private void setStorageLocations(final Entity delivery) {
        Entity location = delivery.getBelongsToField(DeliveryFields.LOCATION);

        if (Objects.isNull(location)) {
            clearStorageLocations(delivery);
        } else if (Objects.nonNull(delivery.getId())) {
            Entity locationFromDb = delivery.getDataDefinition().get(delivery.getId()).getBelongsToField(DeliveryFields.LOCATION);

            if (Objects.isNull(locationFromDb) || !Objects.equals(location.getId(), locationFromDb.getId())) {
                clearStorageLocations(delivery);
            }
        }
    }

    private void clearStorageLocations(final Entity delivery) {
        EntityList deliveredProducts = delivery.getHasManyField(DeliveryFields.DELIVERED_PRODUCTS);

        if (Objects.nonNull(deliveredProducts)) {
            for (Entity deliveryProduct : deliveredProducts) {
                deliveryProduct.setField(DeliveredProductFields.STORAGE_LOCATION, null);

                deliveryProduct.getDataDefinition().save(deliveryProduct);
            }
        }
    }

}
