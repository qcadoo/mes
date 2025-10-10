package com.qcadoo.mes.materialFlowResources.validators;

import com.qcadoo.mes.materialFlowResources.constants.StocktakingFields;
import com.qcadoo.mes.materialFlowResources.constants.StocktakingPositionFields;
import com.qcadoo.mes.materialFlowResources.constants.StorageLocationFields;
import com.qcadoo.mes.materialFlowResources.constants.StorageLocationMode;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Service
public class StocktakingPositionValidators {

    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    public boolean validatesWith(final DataDefinition stocktakingPositionDD, final Entity stocktakingPosition) {
        if (positionExists(stocktakingPosition)) {
            stocktakingPosition.addGlobalError("materialFlowResources.error.stocktakingPosition.notUnique");
            return false;
        }
        Entity stocktaking = stocktakingPosition.getBelongsToField(StocktakingPositionFields.STOCKTAKING);
        if (StorageLocationMode.SELECTED.getStringValue().equals(
                stocktaking.getStringField(StocktakingFields.STORAGE_LOCATION_MODE)) &&
                stocktakingPosition.getBelongsToField(StocktakingPositionFields.STORAGE_LOCATION) == null) {
            stocktakingPosition.addError(stocktakingPositionDD.getField(StocktakingPositionFields.STORAGE_LOCATION), "qcadooView.validate.field.error.missing");
            return false;
        }

        Entity storageLocation = stocktakingPosition.getBelongsToField(StocktakingPositionFields.STORAGE_LOCATION);
        Entity palletNumber = stocktakingPosition.getBelongsToField(StocktakingPositionFields.PALLET_NUMBER);

        if (Objects.isNull(storageLocation) && Objects.nonNull(palletNumber)) {
            stocktakingPosition.addError(stocktakingPositionDD.getField(StocktakingPositionFields.STORAGE_LOCATION), "qcadooView.validate.field.error.missing");
            return false;
        } else if (Objects.nonNull(storageLocation)) {
            boolean placeStorageLocation = storageLocation.getBooleanField(StorageLocationFields.PLACE_STORAGE_LOCATION);
            if (placeStorageLocation && Objects.isNull(palletNumber)) {
                stocktakingPosition.addError(stocktakingPositionDD.getField(StocktakingPositionFields.PALLET_NUMBER), "qcadooView.validate.field.error.missing");
                return false;
            }
        }
        return true;
    }

    public boolean positionExists(Entity stocktakingPosition) {
        Entity storageLocation = stocktakingPosition.getBelongsToField(StocktakingPositionFields.STORAGE_LOCATION);
        Entity palletNumber = stocktakingPosition.getBelongsToField(StocktakingPositionFields.PALLET_NUMBER);
        Entity product = stocktakingPosition.getBelongsToField(StocktakingPositionFields.PRODUCT);
        Entity batch = stocktakingPosition.getBelongsToField(StocktakingPositionFields.BATCH);
        Entity stocktaking = stocktakingPosition.getBelongsToField(StocktakingPositionFields.STOCKTAKING);
        Date expirationDate = stocktakingPosition.getDateField(StocktakingPositionFields.EXPIRATION_DATE);
        BigDecimal conversion = stocktakingPosition.getDecimalField(StocktakingPositionFields.CONVERSION);
        Map<String, Object> queryParameters = new HashMap<>();
        queryParameters.put("product", product.getId());
        queryParameters.put("stocktaking", stocktaking.getId());
        queryParameters.put("conversion", conversion);

        StringBuilder query = new StringBuilder();
        query.append("SELECT COUNT(*) ");
        query.append("FROM materialflowresources_stocktakingposition sp ");
        query.append("WHERE sp.product_id = :product ");
        query.append("AND sp.stocktaking_id = :stocktaking ");
        query.append("AND sp.conversion = :conversion ");

        if (storageLocation != null) {
            queryParameters.put("storageLocation", storageLocation.getId());
            query.append("AND sp.storagelocation_id = :storageLocation ");
        } else {
            query.append("AND sp.storagelocation_id IS NULL ");
        }

        if (palletNumber != null) {
            queryParameters.put("palletNumber", palletNumber.getId());
            query.append("AND sp.palletnumber_id = :palletNumber ");
        } else {
            query.append("AND sp.palletnumber_id IS NULL ");
        }

        if (batch != null) {
            queryParameters.put("batch", batch.getId());
            query.append("AND sp.batch_id = :batch ");
        } else {
            query.append("AND sp.batch_id IS NULL ");
        }

        if (expirationDate != null) {
            queryParameters.put("expirationDate", expirationDate);
            query.append("AND sp.expirationdate = :expirationDate ");
        } else {
            query.append("AND sp.expirationdate IS NULL ");
        }

        if (stocktakingPosition.getId() != null) {
            queryParameters.put("id", stocktakingPosition.getId());
            query.append("AND sp.id <> :id ");
        }

        Long count = jdbcTemplate.queryForObject(query.toString(), queryParameters, Long.class);

        return count > 0;
    }
}
