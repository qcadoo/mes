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
package com.qcadoo.mes.supplyNegotiations.columnExtension;

import static com.qcadoo.mes.basic.constants.ProductFields.NAME;
import static com.qcadoo.mes.basic.constants.ProductFields.NUMBER;
import static com.qcadoo.mes.basic.constants.ProductFields.UNIT;
import static com.qcadoo.mes.supplyNegotiations.constants.OfferProductFields.PRICE_PER_UNIT;
import static com.qcadoo.mes.supplyNegotiations.constants.OfferProductFields.QUANTITY;
import static com.qcadoo.mes.supplyNegotiations.constants.OfferProductFields.TOTAL_PRICE;
import static com.qcadoo.mes.supplyNegotiations.constants.RequestForQuotationProductFields.ANNUAL_VOLUME;
import static com.qcadoo.mes.supplyNegotiations.constants.RequestForQuotationProductFields.ORDERED_QUANTITY;
import static com.qcadoo.mes.supplyNegotiations.constants.RequestForQuotationProductFields.PRODUCT;
import static com.qcadoo.mes.supplyNegotiations.constants.RequestForQuotationProductFields.SUCCESSION;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.qcadoo.mes.basic.util.CurrencyService;
import com.qcadoo.mes.supplyNegotiations.print.OfferColumnFiller;
import com.qcadoo.mes.supplyNegotiations.print.RequestForQuotationColumnFiller;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.NumberService;

@Component
public class SupplyNegotiationsColumnFiller implements RequestForQuotationColumnFiller, OfferColumnFiller {

    @Autowired
    private NumberService numberService;

    @Autowired
    private CurrencyService currencyService;

    @Override
    public Map<Entity, Map<String, String>> getRequestForQuotationProductsColumnValues(
            final List<Entity> requestForQuotationProducts) {
        Map<Entity, Map<String, String>> values = new HashMap<Entity, Map<String, String>>();

        for (Entity requestForQuotationProduct : requestForQuotationProducts) {
            if (!values.containsKey(requestForQuotationProduct)) {
                values.put(requestForQuotationProduct, new HashMap<String, String>());
            }

            fillProductNumber(values, requestForQuotationProduct);
            fillProductName(values, requestForQuotationProduct);
            fillProductUnit(values, requestForQuotationProduct);

            fillSuccession(values, requestForQuotationProduct);
            fillOrderedQuantity(values, requestForQuotationProduct);
            fillAnnualVolume(values, requestForQuotationProduct);
        }

        return values;
    }

    @Override
    public Map<Entity, Map<String, String>> getOfferProductsColumnValues(final List<Entity> offerProducts) {
        Map<Entity, Map<String, String>> values = new HashMap<Entity, Map<String, String>>();

        for (Entity offerProduct : offerProducts) {
            if (!values.containsKey(offerProduct)) {
                values.put(offerProduct, new HashMap<String, String>());
            }

            fillProductNumber(values, offerProduct);
            fillProductName(values, offerProduct);
            fillProductUnit(values, offerProduct);

            fillSuccession(values, offerProduct);
            fillQuantity(values, offerProduct);
            fillPricePerUnit(values, offerProduct);
            fillTotalPrice(values, offerProduct);

            fillCurrency(values, offerProduct);
        }

        return values;
    }

    private void fillProductNumber(final Map<Entity, Map<String, String>> values, final Entity requestForQuotationOrOfferProduct) {
        String productNumber = null;

        if (requestForQuotationOrOfferProduct == null) {
            productNumber = "";
        } else {
            Entity product = requestForQuotationOrOfferProduct.getBelongsToField(PRODUCT);

            productNumber = product.getStringField(NUMBER);
        }

        values.get(requestForQuotationOrOfferProduct).put("productNumber", productNumber);
    }

    private void fillProductName(final Map<Entity, Map<String, String>> values, final Entity requestForQuotationOrOfferProduct) {
        String productName = null;

        if (requestForQuotationOrOfferProduct == null) {
            productName = "";
        } else {
            Entity product = requestForQuotationOrOfferProduct.getBelongsToField(PRODUCT);

            productName = product.getStringField(NAME);
        }

        values.get(requestForQuotationOrOfferProduct).put("productName", productName);
    }

    private void fillProductUnit(final Map<Entity, Map<String, String>> values, final Entity requestForQuotationOrOfferProduct) {
        String productUnit = null;

        if (requestForQuotationOrOfferProduct == null) {
            productUnit = "";
        } else {
            Entity product = requestForQuotationOrOfferProduct.getBelongsToField(PRODUCT);

            productUnit = product.getStringField(UNIT);
        }

        values.get(requestForQuotationOrOfferProduct).put("productUnit", productUnit);
    }

    private void fillSuccession(final Map<Entity, Map<String, String>> values, final Entity requestForQuotationOrOfferProduct) {
        Integer succession = null;

        if (requestForQuotationOrOfferProduct == null) {
            succession = 0;
        } else {
            succession = requestForQuotationOrOfferProduct.getIntegerField(SUCCESSION);
        }

        values.get(requestForQuotationOrOfferProduct).put("succession", succession.toString());
    }

    private void fillOrderedQuantity(final Map<Entity, Map<String, String>> values, final Entity requestForQuotationProduct) {
        BigDecimal orderedQuantity = null;

        if (requestForQuotationProduct == null) {
            orderedQuantity = BigDecimal.ZERO;
        } else {
            orderedQuantity = requestForQuotationProduct.getDecimalField(ORDERED_QUANTITY);
        }

        values.get(requestForQuotationProduct).put("orderedQuantity", numberService.format(orderedQuantity));
    }

    private void fillAnnualVolume(final Map<Entity, Map<String, String>> values, final Entity requestForQuotationProduct) {
        BigDecimal annualVolume = null;

        if (requestForQuotationProduct == null) {
            annualVolume = BigDecimal.ZERO;
        } else {
            annualVolume = requestForQuotationProduct.getDecimalField(ANNUAL_VOLUME);
        }

        values.get(requestForQuotationProduct).put("annualVolume", numberService.format(annualVolume));
    }

    private void fillQuantity(final Map<Entity, Map<String, String>> values, final Entity offerProduct) {
        BigDecimal quantity = null;

        if (offerProduct == null) {
            quantity = BigDecimal.ZERO;
        } else {
            quantity = offerProduct.getDecimalField(QUANTITY);
        }

        values.get(offerProduct).put("quantity", numberService.format(quantity));
    }

    private void fillPricePerUnit(final Map<Entity, Map<String, String>> values, final Entity offerProduct) {
        BigDecimal pricePerUnit = null;

        if (offerProduct == null) {
            pricePerUnit = BigDecimal.ZERO;
        } else {
            pricePerUnit = offerProduct.getDecimalField(PRICE_PER_UNIT);
        }

        values.get(offerProduct).put("pricePerUnit", numberService.format(pricePerUnit));
    }

    private void fillTotalPrice(final Map<Entity, Map<String, String>> values, final Entity offerProduct) {
        BigDecimal totalPrice = null;

        if (offerProduct == null) {
            totalPrice = BigDecimal.ZERO;
        } else {
            totalPrice = offerProduct.getDecimalField(TOTAL_PRICE);
        }

        values.get(offerProduct).put("totalPrice", numberService.format(totalPrice));
    }

    private void fillCurrency(final Map<Entity, Map<String, String>> values, final Entity offerProduct) {
        String currency = null;

        Entity currentCurrency = currencyService.getCurrentCurrency();

        if (currentCurrency == null) {
            currency = "";
        } else {
            currency = currencyService.getCurrencyAlphabeticCode();
        }

        values.get(offerProduct).put("currency", currency);
    }

}
