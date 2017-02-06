package com.qcadoo.mes.materialFlowResources;

import com.google.common.base.Strings;
import com.qcadoo.mes.basic.constants.PalletNumberFields;
import com.qcadoo.mes.materialFlowResources.constants.MaterialFlowResourcesConstants;
import com.qcadoo.mes.materialFlowResources.constants.ResourceFields;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchQueryBuilder;
import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class PalletValidatorService {

    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    @Autowired
    private DataDefinitionService dataDefinitionService;

    public boolean validatePalletForDeliveredProduct(Entity deliveredProduct) {
        Entity palletNumber = deliveredProduct.getBelongsToField("palletNumber");
        Entity storageLocation = deliveredProduct.getBelongsToField("storageLocation");
        String palletType = deliveredProduct.getStringField("palletType");
        Long deliveredProductId = deliveredProduct.getId();
        Long resourceId = null;
        Long locationId = null;
        Entity entity = deliveredProduct;

        return validatePallet(palletNumber, palletType, storageLocation, entity, deliveredProductId, resourceId, locationId);
    }

    public boolean validatePalletForResource(Entity resource) {
        Entity palletNumber = resource.getBelongsToField(ResourceFields.PALLET_NUMBER);
        Entity storageLocation = resource.getBelongsToField(ResourceFields.STORAGE_LOCATION);
        String palletType = resource.getStringField(ResourceFields.TYPE_OF_PALLET);
        Long deliveredProductId = null;
        Long resourceId = resource.getId();
        Long locationId = null;
        Entity entity = resource;

        return validatePallet(palletNumber, palletType, storageLocation, entity, deliveredProductId, resourceId, locationId);
    }

    private boolean validatePallet(Entity palletNumber, String palletType, Entity storageLocation, Entity entity, Long deliveredProductId, Long resourceId, Long locationId) {
        return true;
//        return !existsOtherDeliveredProductForPalletAndStorageLocation(palletNumber, storageLocation, deliveredProductId, entity) 
//                && !existsOtherDeliveredProductForStorageLocationAndPallet(palletNumber, storageLocation, deliveredProductId, entity)
//                && !existsOtherDeliveredProductForOtherPalletType(palletNumber, palletType, deliveredProductId, entity)
//             
//                && !existsOtherPositionForPalletAndStorageLocation(palletNumber, storageLocation, locationId, entity)
//                && !existsOtherPositionForStorageLocationAndPallet(palletNumber, storageLocation, locationId, entity)
//                && !existsOtherPositionForOtherPalletType(palletNumber, palletType, locationId, entity)
//               
//                && !existsOtherResourceForPalletAndStorageLocation(palletNumber, storageLocation, resourceId, entity) 
//                && !existsOtherResourceForStorageLocationAndPallet(palletNumber, storageLocation, resourceId, entity)
//                && !existsOtherResourceForOtherPalletType(palletNumber, palletType, resourceId, entity);
    }

    private boolean existsOtherPositionForOtherPalletType(Entity palletNumberEntity, String palletType, Long positionId, Entity entity) {
        String query = "select count(dp) as cnt from #materialFlowResources_position dp JOIN dp.palletNumber as pallet"
                + "	where pallet.number = :palletNumber ";

        String palletNumber = "";
//        Entity palletNumberEntity = deliveredProduct.getBelongsToField("palletNumber");
        if (palletNumberEntity != null) {
            palletNumber = palletNumberEntity.getStringField(PalletNumberFields.NUMBER);
        }

//        String palletType = deliveredProduct.getStringField(DeliveredProductFields.PALLET_TYPE);
        if (Strings.isNullOrEmpty(palletType)) {
            palletType = "";
            query += "and ( dp.typeOfPallet <> :palletType)";
        } else {
            query += "and ( dp.typeOfPallet is null OR dp.typeOfPallet <> :palletType)";
        }

        if (positionId != null && positionId > 0) {
            query += " AND dp.id <> " + positionId;
        }

        SearchQueryBuilder find = dataDefinitionService.get(MaterialFlowResourcesConstants.PLUGIN_IDENTIFIER, MaterialFlowResourcesConstants.MODEL_POSITION).find(query);
        find.setString("palletType", palletType);
        find.setString("palletNumber", palletNumber);
        Entity countResults = find.uniqueResult();

        boolean exists = ((Long) countResults.getField("cnt")) > 0L;
        if (exists) {
            entity.addError(entity.getDataDefinition().getField("palletNumber"), "documentGrid.error.position.existsOtherPositionForOtherPalletType");
        }
        return exists;
    }

    private boolean existsOtherPositionForStorageLocationAndPallet(Entity palletNumber, Entity storageLocation, Long positionId, Entity entity) {
        String query = "select count(*) from materialflowresources_position p"
                + " WHERE  ";

        Map<String, Object> params = new HashMap<>();

        if (storageLocation == null) {
            query += "( p.storagelocation_id is null)";

        } else {
            params.put("storageLocationId", storageLocation.getId());
            query += "( p.storagelocation_id = :storageLocationId)";
        }

        if (palletNumber == null) {
            query += " AND ( p.palletnumber_id is not null)";

        } else {
            params.put("palletNumberId", palletNumber.getId());
            query += " AND ( p.palletnumber_id <> :palletNumberId or p.palletnumber_id is null)";
        }

        if (positionId != null && positionId > 0) {
            query += " AND p.id <> " + positionId;
        }
        Long count = jdbcTemplate.queryForObject(query, params, Long.class);
        boolean exists = count > 0L;
        if (exists) {
            entity.addError(entity.getDataDefinition().getField("palletNumber"), "documentGrid.error.position.existsOtherPositionForStorageLocationAndPallet");
        }
        return exists;
    }

    private boolean existsOtherPositionForPalletAndStorageLocation(Entity palletNumber, Entity storageLocation, Long positionId, Entity entity) {
        String query = "select count(*) from materialflowresources_position p"
                + " WHERE  ";

        Map<String, Object> params = new HashMap<>();

        if (palletNumber == null) {
            query += "( p.palletnumber_id is null)";

        } else {
            params.put("palletNumberId", palletNumber.getId());
            query += "( p.palletnumber_id = :palletNumberId)";
        }

        if (storageLocation == null) {
            query += " AND ( p.storagelocation_id is not null)";

        } else {
            params.put("storageLocationId", storageLocation.getId());
            query += " AND ( p.storagelocation_id <> :storageLocationId or p.storagelocation_id is null)";
        }

        if (positionId != null && positionId > 0) {
            query += " AND p.id <> " + positionId;
        }
        Long count = jdbcTemplate.queryForObject(query, params, Long.class);

        boolean exists = count > 0L;
        if (exists) {
            entity.addError(entity.getDataDefinition().getField("palletNumber"), "documentGrid.error.position.existsOtherPositionForPalletAndStorageLocation");
        }

        return exists;
    }

    private boolean existsOtherDeliveredProductForPalletAndStorageLocation(Entity palletNumber, Entity storageLocation, Long deliveredProductId, Entity entity) {
        String query = "select count(*) from Deliveries_DeliveredProduct p"
                + " WHERE  ";

        Map<String, Object> params = new HashMap<>();

        if (palletNumber == null) {
            query += "( p.palletnumber_id is null)";

        } else {
            params.put("palletNumberId", palletNumber.getId());
            query += "( p.palletnumber_id = :palletNumberId)";
        }

        if (storageLocation == null) {
            query += " AND ( p.storagelocation_id is not null)";

        } else {
            params.put("storageLocationId", storageLocation.getId());
            query += " AND ( p.storagelocation_id <> :storageLocationId or p.storagelocation_id is null)";
        }

        if (deliveredProductId != null && deliveredProductId > 0) {
            query += " AND p.id <> " + deliveredProductId;
        }
        Long count = jdbcTemplate.queryForObject(query, params, Long.class);

        boolean exists = count > 0L;
        if (exists) {
            entity.addError(entity.getDataDefinition().getField("palletNumber"), "documentGrid.error.position.existsOtherDeliveredProductForPalletAndStorageLocation");
        }

        return exists;
    }

    private boolean existsOtherDeliveredProductForStorageLocationAndPallet(Entity palletNumber, Entity storageLocation, Long deliveredProductId, Entity entity) {
        String query = "select count(*) from Deliveries_DeliveredProduct p"
                + " WHERE  ";

        Map<String, Object> params = new HashMap<>();

        if (storageLocation == null) {
            query += "( p.storagelocation_id is null)";

        } else {
            params.put("storageLocationId", storageLocation.getId());
            query += "( p.storagelocation_id = :storageLocationId)";
        }

        if (palletNumber == null) {
            query += " AND ( p.palletnumber_id is not null)";

        } else {
            params.put("palletNumberId", palletNumber.getId());
            query += " AND ( p.palletnumber_id <> :palletNumberId or p.palletnumber_id is null)";
        }

        if (deliveredProductId != null && deliveredProductId > 0) {
            query += " AND p.id <> " + deliveredProductId;
        }
        Long count = jdbcTemplate.queryForObject(query, params, Long.class);
        boolean exists = count > 0L;
        if (exists) {
            entity.addError(entity.getDataDefinition().getField("palletNumber"), "documentGrid.error.position.existsOtherDeliveredProductForStorageLocationAndPallet");
        }
        return exists;
    }

    private boolean existsOtherDeliveredProductForOtherPalletType(Entity palletNumberEntity, String palletType, Long deliveredProductId, Entity entity) {
        String query = "select count(dp) as cnt from DeliveriesDeliveredProduct dp JOIN dp.palletNumber as pallet"
                + "	where pallet.number = :palletNumber ";

        String palletNumber = "";
//        Entity palletNumberEntity = deliveredProduct.getBelongsToField("palletNumber");
        if (palletNumberEntity != null) {
            palletNumber = palletNumberEntity.getStringField(PalletNumberFields.NUMBER);
        }

//        String palletType = deliveredProduct.getStringField(DeliveredProductFields.PALLET_TYPE);
        if (Strings.isNullOrEmpty(palletType)) {
            palletType = "";
            query += "and ( dp.palletType <> :palletType)";
        } else {
            query += "and ( dp.palletType is null OR dp.palletType <> :palletType)";
        }

        if (deliveredProductId != null && deliveredProductId > 0) {
            query += " AND dp.id <> " + deliveredProductId;
        }

        SearchQueryBuilder find = dataDefinitionService.get("deliveries", "deliveredProduct").find(query);
        find.setString("palletType", palletType);
        find.setString("palletNumber", palletNumber);
        Entity countResults = find.uniqueResult();

        boolean exists = ((Long) countResults.getField("cnt")) > 0L;
        if (exists) {
            entity.addError(entity.getDataDefinition().getField("palletNumber"), "documentGrid.error.position.existsOtherDeliveredProductForOtherPalletType");
        }
        return exists;
    }

    private boolean existsOtherResourceForPalletAndStorageLocation(Entity palletNumber, Entity storageLocation, Long resourceId, Entity entity) {
        String query = "select count(*) from materialflowresources_resource r"
                + " WHERE  ";

        Map<String, Object> params = new HashMap<>();

        if (palletNumber == null) {
            query += "( r.palletnumber_id is null)";

        } else {
            params.put("palletNumberId", palletNumber.getId());
            query += "( r.palletnumber_id = :palletNumberId)";
        }

        if (storageLocation == null) {
            query += " AND ( r.storagelocation_id is not null)";

        } else {
            params.put("storageNumberId", storageLocation.getId());
            query += " AND ( r.storagelocation_id <> :storageNumberId or r.storagelocation_id is null)";
        }

        if (resourceId != null && resourceId > 0) {
            query += " AND r.id <> " + resourceId;
        }

        Long count = jdbcTemplate.queryForObject(query, params, Long.class);

        boolean exists = count > 0L;
        if (exists) {
            entity.addError(entity.getDataDefinition().getField("palletNumber"), "documentGrid.error.position.existsOtherResourceForPalletAndStorageLocation");
        }
        return exists;
    }

    private boolean existsOtherResourceForStorageLocationAndPallet(Entity palletNumber, Entity storageLocation, Long resourceId, Entity entity) {
        String query = "select count(*) from materialflowresources_resource r"
                + " WHERE  ";

        Map<String, Object> params = new HashMap<>();

        if (storageLocation == null) {
            query += "( r.storagelocation_id is null)";

        } else {
            params.put("storageId", storageLocation.getId());
            query += "( r.storagelocation_id = :storageId)";
        }

        if (palletNumber == null) {
            query += " AND ( r.palletnumber_id is not null)";

        } else {
            params.put("palletNumberId", palletNumber.getId());
            query += " AND ( r.palletnumber_id <> :palletNumberId or r.palletnumber_id is null)";
        }

        if (resourceId != null && resourceId > 0) {
            query += " AND r.id <> " + resourceId;
        }

        Long count = jdbcTemplate.queryForObject(query, params, Long.class);

        boolean exists = count > 0L;
        if (exists) {
            entity.addError(entity.getDataDefinition().getField("palletNumber"), "documentGrid.error.position.existsOtherResourceForStorageLocationAndPallet");
        }
        return exists;
    }

    private boolean existsOtherResourceForOtherPalletType(Entity palletNumberEntity, String palletType, Long resourceId, Entity entity) {
        String query = "select count(resource) as cnt from MaterialFlowResourcesResource resource JOIN resource.palletNumber as pallet"
                + "	where pallet.number = :palletNumber and resource.typeOfPallet <> :palletType ";

        String palletNumber = "";
        if (palletNumberEntity != null) {
            palletNumber = palletNumberEntity.getStringField(PalletNumberFields.NUMBER);
        }

        if (Strings.isNullOrEmpty(palletType)) {
            palletType = "";
            query += "and ( resource.typeOfPallet <> :palletType)";
        } else {
            query += "and ( resource.typeOfPallet is null OR resource.typeOfPallet <> :palletType)";
        }

        SearchQueryBuilder find = dataDefinitionService.get(MaterialFlowResourcesConstants.PLUGIN_IDENTIFIER, MaterialFlowResourcesConstants.MODEL_RESOURCE).find(query);
        find.setString("palletType", palletType);
        find.setString("palletNumber", palletNumber);
        Entity countResults = find.uniqueResult();

        boolean exists = ((Long) countResults.getField("cnt")) > 0L;
        if (exists) {
            entity.addError(entity.getDataDefinition().getField("palletNumber"), "documentGrid.error.position.existsOtherResourceForOtherPalletType");
        }
        return exists;
    }

}
