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

import com.qcadoo.mes.basic.constants.AdditionalCodeFields;
import com.qcadoo.mes.basic.constants.PalletNumberFields;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.qcadoo.mes.basic.constants.ProductFields;
import com.qcadoo.mes.materialFlowResources.constants.DocumentFields;
import com.qcadoo.mes.materialFlowResources.constants.PositionFields;
import com.qcadoo.mes.materialFlowResources.constants.StorageLocationFields;
import com.qcadoo.model.api.Entity;

public class PositionDataProvider {
    
    public static String index(final Entity position) {
        return position.getIntegerField(PositionFields.NUMBER).toString();
    }
    
    public static String product(final Entity position) {
        Entity product = position.getBelongsToField(PositionFields.PRODUCT);
        return product != null ? product.getStringField(ProductFields.NUMBER) + "\n" + product.getStringField(ProductFields.NAME)
                : StringUtils.EMPTY;
    }
    
    public static String quantity(final Entity position) {
        BigDecimal quantity = position.getDecimalField(PositionFields.QUANTITY);
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
        String batch = position.getStringField(PositionFields.BATCH);
        Entity storageLocation = position.getBelongsToField(PositionFields.STORAGE_LOCATION);
        return batch != null ? (storageLocation != null ? batch + "\n"
                + storageLocation.getStringField(StorageLocationFields.NUMBER) : batch)
                : (storageLocation != null ? storageLocation.getStringField(StorageLocationFields.NUMBER) : StringUtils.EMPTY);
    }
    
    public static String palletNumber(final Entity position) {
        Entity palletNumber = position.getBelongsToField(PositionFields.PALLET_NUMBER);
        
        return palletNumber == null ? "" : palletNumber.getStringField(PalletNumberFields.NUMBER);
    }
    
    public static String typeOfPallet(final Entity position) {
        String typeOfallet = position.getStringField(PositionFields.TYPE_OF_PALLET);
        
        return typeOfallet == null ? "" : typeOfallet;
    }
    
    public static String additionalCode(final Entity position) {
        Entity additionalCode = position.getBelongsToField(PositionFields.ADDITIONAL_CODE);
        String productNumber = position.getBelongsToField(PositionFields.PRODUCT).getStringField(ProductFields.NUMBER);
        
        return additionalCode == null ? productNumber : additionalCode.getStringField(AdditionalCodeFields.CODE);
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
}
