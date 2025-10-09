package com.qcadoo.mes.materialFlowResources.hooks;

import com.qcadoo.mes.basic.constants.ProductFields;
import com.qcadoo.mes.materialFlowResources.constants.StocktakingFields;
import com.qcadoo.mes.materialFlowResources.constants.StocktakingPositionFields;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class StocktakingPositionHooks {

    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    public void onSave(final DataDefinition stocktakingPositionDD, final Entity stocktakingPosition) {
        BigDecimal stock = findStock(stocktakingPosition);
        stocktakingPosition.setField(StocktakingPositionFields.STOCK, stock);
        if (stock.precision() > 14) {
            stocktakingPosition.addGlobalError("materialFlowResources.error.stocktakingPosition.stock.invalidPrecision", stocktakingPosition.getBelongsToField(StocktakingPositionFields.PRODUCT).getStringField(ProductFields.NUMBER));
        }
    }

    private BigDecimal findStock(Entity stocktakingPosition) {
        Entity location = stocktakingPosition.getBelongsToField(StocktakingPositionFields.STOCKTAKING).getBelongsToField(StocktakingFields.LOCATION);
        Entity storageLocation = stocktakingPosition.getBelongsToField(StocktakingPositionFields.STORAGE_LOCATION);
        Entity palletNumber = stocktakingPosition.getBelongsToField(StocktakingPositionFields.PALLET_NUMBER);
        Entity product = stocktakingPosition.getBelongsToField(StocktakingPositionFields.PRODUCT);
        Entity batch = stocktakingPosition.getBelongsToField(StocktakingPositionFields.BATCH);
        Date expirationDate = stocktakingPosition.getDateField(StocktakingPositionFields.EXPIRATION_DATE);
        BigDecimal conversion = stocktakingPosition.getDecimalField(StocktakingPositionFields.CONVERSION);
        Map<String, Object> queryParameters = new HashMap<>();
        queryParameters.put("product", product.getId());
        queryParameters.put("location", location.getId());
        queryParameters.put("conversion", conversion);

        StringBuilder query = new StringBuilder();
        query.append("SELECT SUM(resource.quantity) AS quantity ");
        query.append("FROM materialflowresources_resource resource ");
        query.append("WHERE resource.location_id = :location ");
        query.append("AND resource.product_id = :product ");
        query.append("AND resource.conversion = :conversion ");

        if (storageLocation != null) {
            queryParameters.put("storageLocation", storageLocation.getId());
            query.append("AND resource.storagelocation_id = :storageLocation ");
        } else {
            query.append("AND resource.storagelocation_id IS NULL ");
        }

        if (palletNumber != null) {
            queryParameters.put("palletNumber", palletNumber.getId());
            query.append("AND resource.palletnumber_id = :palletNumber ");
        } else {
            query.append("AND resource.palletnumber_id IS NULL ");
        }

        if (batch != null) {
            queryParameters.put("batch", batch.getId());
            query.append("AND resource.batch_id = :batch ");
        } else {
            query.append("AND resource.batch_id IS NULL ");
        }

        if (expirationDate != null) {
            queryParameters.put("expirationDate", expirationDate);
            query.append("AND resource.expirationdate = :expirationDate ");
        } else {
            query.append("AND resource.expirationdate IS NULL ");
        }

        return Optional.ofNullable(jdbcTemplate.queryForObject(query.toString(), queryParameters, BigDecimal.class)).orElse(BigDecimal.ZERO);
    }
}
