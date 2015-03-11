package com.qcadoo.mes.materialFlowResources.print.helper;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.qcadoo.mes.basic.constants.ProductFields;
import com.qcadoo.mes.materialFlowResources.constants.DocumentFields;
import com.qcadoo.mes.materialFlowResources.constants.PositionFields;
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
        String storageLocation = position.getStringField(PositionFields.STORAGE_LOCATION);
        return batch != null ? (storageLocation != null ? batch + "\n" + storageLocation : batch)
                : (storageLocation != null ? storageLocation : StringUtils.EMPTY);
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
