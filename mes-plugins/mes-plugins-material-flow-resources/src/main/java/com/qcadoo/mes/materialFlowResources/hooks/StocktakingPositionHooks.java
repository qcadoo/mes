package com.qcadoo.mes.materialFlowResources.hooks;

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
        stocktakingPosition.setField(StocktakingPositionFields.STOCK, findQuantity(stocktakingPosition));
    }

    public BigDecimal findQuantity(Entity stocktakingPosition) {
        Entity storageLocation = stocktakingPosition.getBelongsToField(StocktakingPositionFields.STORAGE_LOCATION);
        Entity palletNumber = stocktakingPosition.getBelongsToField(StocktakingPositionFields.PALLET_NUMBER);
        Entity product = stocktakingPosition.getBelongsToField(StocktakingPositionFields.PRODUCT);
        Entity batch = stocktakingPosition.getBelongsToField(StocktakingPositionFields.BATCH);
        Date expirationDate = stocktakingPosition.getDateField(StocktakingPositionFields.EXPIRATION_DATE);
        Map<String, Object> queryParameters = new HashMap<>();
        queryParameters.put("product", product.getId());

        StringBuilder query = new StringBuilder();
        query.append("SELECT SUM(resource.quantity) AS quantity ");
        query.append("FROM materialflowresources_resource resource ");
        query.append("WHERE resource.product_id = :product ");

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
