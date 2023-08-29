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
package com.qcadoo.mes.materialFlowResources.print.helper;

import com.google.common.base.Strings;
import com.qcadoo.mes.advancedGenealogy.constants.BatchFields;
import com.qcadoo.mes.basic.constants.PalletNumberFields;
import com.qcadoo.mes.basic.constants.ProductFields;
import com.qcadoo.mes.materialFlowResources.constants.DocumentFields;
import com.qcadoo.mes.materialFlowResources.constants.PositionFields;
import com.qcadoo.mes.materialFlowResources.constants.StorageLocationFields;
import com.qcadoo.model.api.BigDecimalUtils;
import com.qcadoo.model.api.Entity;
import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import static com.qcadoo.model.api.BigDecimalUtils.convertNullToZero;

public class PositionDataProvider {

    public static String index(final Entity position) {
        return position.getIntegerField(PositionFields.NUMBER).toString();
    }

    public static String product(final Entity position) {
        Entity product = position.getBelongsToField(PositionFields.PRODUCT);
        return product != null ? product.getStringField(ProductFields.NUMBER) + "\n" + product.getStringField(ProductFields.NAME)
                : StringUtils.EMPTY;
    }

    public static String quantity(final BigDecimal quantity) {
        return quantity != null ? quantity.stripTrailingZeros().toPlainString() : BigDecimal.ZERO.toPlainString();
    }

    public static String unit(final Entity position) {
        Entity product = position.getBelongsToField(PositionFields.PRODUCT);
        return product != null ? product.getStringField(ProductFields.UNIT) : StringUtils.EMPTY;
    }

    public static String price(final Entity position) {
        BigDecimal price = position.getDecimalField(PositionFields.PRICE);
        return price != null ? price.stripTrailingZeros().toPlainString() : BigDecimal.ZERO.toPlainString();
    }

    public static String batch(final Entity position) {
        StringBuilder builder = new StringBuilder();

        String batch = null;
        if (Objects.nonNull(position.getBelongsToField(PositionFields.BATCH))) {
            batch = position.getBelongsToField(PositionFields.BATCH).getStringField(BatchFields.NUMBER);
        }

        if (Strings.isNullOrEmpty(batch)) {
            builder.append(StringUtils.EMPTY).append("\n");
        } else {
            builder.append(batch).append("\n");
        }

        Entity storageLocation = position.getBelongsToField(PositionFields.STORAGE_LOCATION);
        if (Objects.isNull(storageLocation)) {
            builder.append(StringUtils.EMPTY).append("\n");
        } else {
            builder.append(storageLocation.getStringField(StorageLocationFields.NUMBER)).append("\n");
        }

        Entity palletNumber = position.getBelongsToField(PositionFields.PALLET_NUMBER);
        if (Objects.isNull(palletNumber)) {
            builder.append(StringUtils.EMPTY).append("\n");
        } else {
            builder.append(palletNumber.getStringField(PalletNumberFields.NUMBER)).append("\n");
        }
        return builder.toString();
    }

    public static String onlyBatch(final Entity position) {
        if (Objects.nonNull(position.getBelongsToField(PositionFields.BATCH))) {
            return position.getBelongsToField(PositionFields.BATCH).getStringField(BatchFields.NUMBER);
        }
        return StringUtils.EMPTY;
    }

    public static String palletNumber(final Entity position) {
        Entity palletNumber = position.getBelongsToField(PositionFields.PALLET_NUMBER);

        return palletNumber == null ? "" : palletNumber.getStringField(PalletNumberFields.NUMBER);
    }

    public static String typeOfPallet(final Entity position) {
        String typeOfallet = position.getStringField(PositionFields.TYPE_OF_PALLET);

        return typeOfallet == null ? "" : typeOfallet;
    }

    public static String productionDate(final Entity position) {
        Date productionDate = position.getDateField(PositionFields.PRODUCTION_DATE);
        Date expirationDate = position.getDateField(PositionFields.EXPIRATION_DATE);
        String productionDateString = productionDate != null ? (new SimpleDateFormat("yyyy-MM-dd").format(productionDate))
                : StringUtils.EMPTY;

        String expirationDateString = expirationDate != null ? (new SimpleDateFormat("yyyy-MM-dd").format(expirationDate))
                : StringUtils.EMPTY;
        return productionDateString + "\n" + expirationDateString;
    }

    public static String value(final Entity position) {
        return getValue(position).toPlainString();
    }

    public static List<Entity> getPositions(final Entity documentEntity) {
        return documentEntity.getHasManyField(DocumentFields.POSITIONS);
    }

    public static String totalValue(final Entity documentEntity) {
        return calculateTotalValue(documentEntity).toPlainString();
    }

    private static BigDecimal getValue(final Entity position) {
        BigDecimal price = position.getDecimalField(PositionFields.PRICE);
        BigDecimal quantity = position.getDecimalField(PositionFields.QUANTITY);
        return (price != null && quantity != null) ? price.multiply(quantity).setScale(2, RoundingMode.HALF_UP)
                .stripTrailingZeros() : BigDecimal.ZERO;
    }

    private static BigDecimal calculateTotalValue(final Entity documentEntity) {
        List<Entity> positions = getPositions(documentEntity);

        BigDecimal result = positions.stream().map(position -> getValue(position)).reduce(BigDecimal.ZERO, BigDecimal::add);

        return result;

    }

    public static String quantity(Entity position) {
        BigDecimal quantity = position.getDecimalField(PositionFields.QUANTITY);
        return quantity != null ? quantity.stripTrailingZeros().toPlainString() : BigDecimal.ZERO.toPlainString();

    }

    public static BigDecimal quantityDecimalVal(Entity position) {
        BigDecimal quantity = position.getDecimalField(PositionFields.QUANTITY);
        return quantity != null ? quantity.stripTrailingZeros() : BigDecimal.ZERO;

    }

    public static String quantityAdd(Entity position) {
        BigDecimal quantity = position.getDecimalField(PositionFields.GIVEN_QUANTITY);
        return quantity != null ? quantity.stripTrailingZeros().toPlainString() : BigDecimal.ZERO.toPlainString();
    }

    public static BigDecimal quantityAddDecimalValDifferencePrint(Entity position) {
        if (Objects.nonNull(position.getDecimalField(PositionFields.REST_AFTER_SHIFT_DISPOSITION))
                || Objects.nonNull(position.getDecimalField(PositionFields.REST_AFTER_SHIFT_DISPOSITION_ADD_UNIT))) {
            BigDecimal gq = BigDecimalUtils.convertNullToZero(position.getDecimalField(PositionFields.REST_AFTER_SHIFT_DISPOSITION));
            BigDecimal gqAU = BigDecimalUtils.convertNullToZero(position.getDecimalField(PositionFields.REST_AFTER_SHIFT_DISPOSITION_ADD_UNIT));
            BigDecimal addDecimalVal = gq.multiply(convertNullToZero(position.getDecimalField(PositionFields.CONVERSION)));
            addDecimalVal = addDecimalVal.add(gqAU);
            addDecimalVal = addDecimalVal.setScale(5, RoundingMode.HALF_UP);
            return addDecimalVal.stripTrailingZeros();
        }
        BigDecimal quantity = position.getDecimalField(PositionFields.GIVEN_QUANTITY);
        return quantity != null ? quantity.stripTrailingZeros() : BigDecimal.ZERO;
    }

    public static BigDecimal quantityAddDecimalVal(Entity position) {
        BigDecimal quantity = position.getDecimalField(PositionFields.GIVEN_QUANTITY);
        return quantity != null ? quantity.stripTrailingZeros() : BigDecimal.ZERO;
    }

    public static String unitAdd(Entity position) {
        return Strings.nullToEmpty(position.getStringField(PositionFields.GIVEN_UNIT));
    }

    public static String amountAndRest(Entity position) {
        BigDecimal amount = position.getDecimalField(PositionFields.QUANTITY);
        amount = amount.setScale(0, RoundingMode.DOWN);
        BigDecimal wholeAmount = amount.multiply(convertNullToZero(position.getDecimalField(PositionFields.CONVERSION)));
        BigDecimal rest = convertNullToZero(position.getDecimalField(PositionFields.GIVEN_QUANTITY)).subtract(wholeAmount);
        rest = rest.setScale(5, RoundingMode.HALF_UP);
        return amount.stripTrailingZeros().toPlainString() + "\n" + rest.stripTrailingZeros().toPlainString();
    }

    public static String amount(Entity position) {

        return amountDecimal(position).stripTrailingZeros().toPlainString();
    }

    public static String amountNonZero(Entity position) {
        BigDecimal amount = amountDecimal(position);
        if (amount.compareTo(BigDecimal.ZERO) == 0) {
            return StringUtils.EMPTY;
        }
        return amount.stripTrailingZeros().toPlainString();
    }

    public static BigDecimal amountNonZeroDecimalVal(Entity position) {
        BigDecimal amount = amountDecimal(position);
        return amount != null ? amount.stripTrailingZeros() : BigDecimal.ZERO;
    }

    public static BigDecimal amountDecimal(Entity position) {
        BigDecimal amount = position.getDecimalField(PositionFields.QUANTITY);
        return amount.setScale(0, RoundingMode.DOWN);
    }

    public static String restNonZero(Entity position) {
        BigDecimal rest = restDecimal(position);
        if (rest.compareTo(BigDecimal.ZERO) == 0) {
            return StringUtils.EMPTY;
        }
        return rest.stripTrailingZeros().toPlainString();
    }

    public static BigDecimal amountNonZeroDecimalValDifference(Entity position) {
        BigDecimal givenQuantity = BigDecimal.ZERO;
        if (Objects.nonNull(position.getDecimalField(PositionFields.REST_AFTER_SHIFT_DISPOSITION))) {
            givenQuantity = position.getDecimalField(PositionFields.REST_AFTER_SHIFT_DISPOSITION).stripTrailingZeros();
            return givenQuantity != null ? givenQuantity : BigDecimal.ZERO;
        } else {
            givenQuantity = position.getDecimalField(PositionFields.GIVEN_QUANTITY);

            BigDecimal conversion = position.getDecimalField(PositionFields.CONVERSION);
            BigDecimal amount = givenQuantity.divide(conversion, MathContext.DECIMAL64).setScale(5, RoundingMode.HALF_UP);
            amount = amount.setScale(0, RoundingMode.DOWN);
            return amount != null ? amount.stripTrailingZeros() : BigDecimal.ZERO;
        }
    }

    public static BigDecimal restNonZeroDecimalValDifference(Entity position) {
        BigDecimal gq = BigDecimal.ZERO;
        if (Objects.nonNull(position.getDecimalField(PositionFields.REST_AFTER_SHIFT_DISPOSITION_ADD_UNIT))) {
            gq = position.getDecimalField(PositionFields.REST_AFTER_SHIFT_DISPOSITION_ADD_UNIT).stripTrailingZeros();
            return gq != null ? gq : BigDecimal.ZERO;
        } else {
            gq = position.getDecimalField(PositionFields.GIVEN_QUANTITY);

            BigDecimal conversion = position.getDecimalField(PositionFields.CONVERSION);
            BigDecimal amount = gq.divide(conversion, MathContext.DECIMAL64).setScale(5, RoundingMode.HALF_UP);
            amount = amount.setScale(0, RoundingMode.DOWN);

            BigDecimal wholeAmount = amount.multiply(convertNullToZero(position.getDecimalField(PositionFields.CONVERSION)));
            BigDecimal rest = convertNullToZero(gq).subtract(wholeAmount, MathContext.DECIMAL64);
            rest = rest.setScale(5, RoundingMode.HALF_UP);

            return rest != null ? rest.stripTrailingZeros() : BigDecimal.ZERO;
        }
    }

    public static BigDecimal restNonZeroDecimalVal(Entity position) {
        BigDecimal rest = restDecimal(position);
        return rest != null ? rest.stripTrailingZeros() : BigDecimal.ZERO;
    }

    public static String rest(Entity position) {

        return restDecimal(position).stripTrailingZeros().toPlainString();
    }

    public static BigDecimal restDecimal(Entity position) {
        BigDecimal amount = position.getDecimalField(PositionFields.QUANTITY);
        amount = amount.setScale(0, RoundingMode.DOWN);
        BigDecimal wholeAmount = amount.multiply(convertNullToZero(position.getDecimalField(PositionFields.CONVERSION)));
        BigDecimal rest = convertNullToZero(position.getDecimalField(PositionFields.GIVEN_QUANTITY)).subtract(wholeAmount);
        return rest.setScale(5, RoundingMode.HALF_UP);
    }

    public static String storageLocation(Entity position) {
        Entity storageLocation = position.getBelongsToField(PositionFields.STORAGE_LOCATION);
        return storageLocation != null ? storageLocation.getStringField(StorageLocationFields.NUMBER) : StringUtils.EMPTY;
    }

    public static String productName(Entity position) {
        return position.getBelongsToField(PositionFields.PRODUCT).getStringField(ProductFields.NAME);
    }

    public static BigDecimal conversion(Entity position) {
        return convertNullToZero(position.getDecimalField(PositionFields.CONVERSION));
    }

    public static String productNumber(Entity position) {
        return position.getBelongsToField(PositionFields.PRODUCT).getStringField(ProductFields.NUMBER);
    }
}
