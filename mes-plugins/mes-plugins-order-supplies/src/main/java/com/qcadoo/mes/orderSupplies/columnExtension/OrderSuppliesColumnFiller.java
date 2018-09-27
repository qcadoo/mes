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
package com.qcadoo.mes.orderSupplies.columnExtension;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

import com.qcadoo.localization.api.TranslationService;
import com.qcadoo.localization.api.utils.DateUtils;
import com.qcadoo.mes.basic.constants.ProductFields;
import com.qcadoo.mes.orderSupplies.constants.CoverageProductFields;
import com.qcadoo.mes.orderSupplies.print.MaterialRequirementCoverageColumnFiller;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.NumberService;

@Component
public class OrderSuppliesColumnFiller implements MaterialRequirementCoverageColumnFiller {

    @Autowired
    private NumberService numberService;

    @Autowired
    private TranslationService translationService;

    @Override
    public Map<Entity, Map<String, String>> getCoverageProductsColumnValues(final List<Entity> coverageProducts) {
        Map<Entity, Map<String, String>> values = new HashMap<Entity, Map<String, String>>();

        for (Entity coverageProduct : coverageProducts) {
            if (!values.containsKey(coverageProduct)) {
                values.put(coverageProduct, new HashMap<String, String>());
            }

            fillProductNumber(values, coverageProduct);
            fillProductName(values, coverageProduct);
            fillProductUnit(values, coverageProduct);

            fillState(values, coverageProduct);
            fillLackFromDate(values, coverageProduct);
            fillDemandQuantity(values, coverageProduct);
            fillCoveredQuantity(values, coverageProduct);
            fillReserveMissingQuantity(values, coverageProduct);
            fillLocationsQuantity(values, coverageProduct);
            fillDeliveredQuantity(values, coverageProduct);
            fillProduceQuantity(values, coverageProduct);
        }

        return values;
    }

    private void fillProductNumber(final Map<Entity, Map<String, String>> values, final Entity coverageProduct) {
        String productNumber = null;

        if (coverageProduct == null) {
            productNumber = "";
        } else {
            Entity product = coverageProduct.getBelongsToField(CoverageProductFields.PRODUCT);

            productNumber = product.getStringField(ProductFields.NUMBER);
        }

        values.get(coverageProduct).put("productNumber", productNumber);
    }

    private void fillProductName(final Map<Entity, Map<String, String>> values, final Entity coverageProduct) {
        String productName = null;

        if (coverageProduct == null) {
            productName = "";
        } else {
            Entity product = coverageProduct.getBelongsToField(CoverageProductFields.PRODUCT);

            productName = product.getStringField(ProductFields.NAME);
        }

        values.get(coverageProduct).put("productName", productName);
    }

    private void fillProductUnit(final Map<Entity, Map<String, String>> values, final Entity coverageProduct) {
        String productUnit = null;

        if (coverageProduct == null) {
            productUnit = "";
        } else {
            Entity product = coverageProduct.getBelongsToField(CoverageProductFields.PRODUCT);

            productUnit = product.getStringField(ProductFields.UNIT);
        }

        values.get(coverageProduct).put("productUnit", productUnit);
    }

    private void fillState(final Map<Entity, Map<String, String>> values, final Entity coverageProduct) {
        String state = null;

        if (coverageProduct == null) {
            state = "";
        } else {
            state = translationService.translate(
                    "orderSupplies.coverageProduct.state.value." + coverageProduct.getStringField(CoverageProductFields.STATE),
                    LocaleContextHolder.getLocale());
        }

        values.get(coverageProduct).put("state", state);
    }

    private void fillLackFromDate(final Map<Entity, Map<String, String>> values, final Entity coverageProduct) {
        String lackFromDate = null;

        if (coverageProduct == null) {
            lackFromDate = "";
        } else {
            Date fromDate = coverageProduct.getDateField(CoverageProductFields.LACK_FROM_DATE);

            if (fromDate == null) {
                lackFromDate = "";
            } else {
                lackFromDate = new SimpleDateFormat(DateUtils.L_DATE_FORMAT, LocaleContextHolder.getLocale()).format(fromDate);
            }
        }

        values.get(coverageProduct).put("lackFromDate", lackFromDate);
    }

    private void fillDemandQuantity(final Map<Entity, Map<String, String>> values, final Entity coverageProduct) {
        BigDecimal demandQuantity = null;

        if (coverageProduct == null) {
            demandQuantity = BigDecimal.ZERO;
        } else {
            demandQuantity = coverageProduct.getDecimalField(CoverageProductFields.DEMAND_QUANTITY);
        }

        values.get(coverageProduct).put("demandQuantity", numberService.format(demandQuantity));
    }

    private void fillCoveredQuantity(final Map<Entity, Map<String, String>> values, final Entity coverageProduct) {
        BigDecimal coveredQuantity = null;

        if (coverageProduct == null) {
            coveredQuantity = BigDecimal.ZERO;
        } else {
            coveredQuantity = coverageProduct.getDecimalField(CoverageProductFields.COVERED_QUANTITY);
        }

        values.get(coverageProduct).put("coveredQuantity", numberService.format(coveredQuantity));
    }

    private void fillReserveMissingQuantity(final Map<Entity, Map<String, String>> values, final Entity coverageProduct) {
        BigDecimal reserveMissingQuantity = null;

        if (coverageProduct == null) {
            reserveMissingQuantity = BigDecimal.ZERO;
        } else {
            reserveMissingQuantity = coverageProduct.getDecimalField(CoverageProductFields.RESERVE_MISSING_QUANTITY);
        }

        values.get(coverageProduct).put("reserveMissingQuantity", numberService.format(reserveMissingQuantity));
    }

    private void fillLocationsQuantity(final Map<Entity, Map<String, String>> values, final Entity coverageProduct) {
        BigDecimal locationsQuantity = null;

        if (coverageProduct == null) {
            locationsQuantity = BigDecimal.ZERO;
        } else {
            locationsQuantity = coverageProduct.getDecimalField(CoverageProductFields.LOCATIONS_QUANTITY);
        }

        values.get(coverageProduct).put("locationsQuantity", numberService.format(locationsQuantity));
    }

    private void fillDeliveredQuantity(final Map<Entity, Map<String, String>> values, final Entity coverageProduct) {
        BigDecimal deliveredQuantity = null;

        if (coverageProduct == null) {
            deliveredQuantity = BigDecimal.ZERO;
        } else {
            deliveredQuantity = coverageProduct.getDecimalField(CoverageProductFields.DELIVERED_QUANTITY);
        }

        values.get(coverageProduct).put("deliveredQuantity", numberService.format(deliveredQuantity));
    }

    private void fillProduceQuantity(final Map<Entity, Map<String, String>> values, final Entity coverageProduct) {
        BigDecimal produceQuantity = null;

        if (coverageProduct == null) {
            produceQuantity = BigDecimal.ZERO;
        } else {
            produceQuantity = coverageProduct.getDecimalField(CoverageProductFields.PRODUCE_QUANTITY);
        }

        values.get(coverageProduct).put("produceQuantity", numberService.format(produceQuantity));
    }

}
