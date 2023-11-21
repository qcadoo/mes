package com.qcadoo.mes.materialFlowResources;

import com.qcadoo.mes.basic.constants.PalletNumberFields;
import com.qcadoo.mes.materialFlowResources.constants.MaterialFlowResourcesConstants;
import com.qcadoo.mes.materialFlowResources.constants.ResourceFields;
import com.qcadoo.mes.materialFlowResources.constants.StorageLocationFields;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchQueryBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PalletValidatorService {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    public boolean validatePalletForDeliveredProduct(Entity deliveredProduct) {
        Entity location = deliveredProduct.getBelongsToField("delivery").getBelongsToField("location");
        if (location == null) {
            return true;
        }
        Entity palletNumberEntity = deliveredProduct.getBelongsToField("palletNumber");
        Entity storageLocationEntity = deliveredProduct.getBelongsToField("storageLocation");
        String palletType = deliveredProduct.getStringField("palletType");
        String palletNumber = palletNumberEntity != null ? palletNumberEntity.getStringField(PalletNumberFields.NUMBER) : null;
        String storageLocation = storageLocationEntity != null ? storageLocationEntity
                .getStringField(StorageLocationFields.NUMBER) : null;
        return validatePallet(palletNumber, palletType, storageLocation, deliveredProduct, location);
    }

    public boolean validatePalletForResource(Entity resource) {
        Entity palletNumberEntity = resource.getBelongsToField(ResourceFields.PALLET_NUMBER);
        Entity storageLocationEntity = resource.getBelongsToField(ResourceFields.STORAGE_LOCATION);
        String palletType = resource.getStringField(ResourceFields.TYPE_OF_PALLET);

        String palletNumber = palletNumberEntity != null ? palletNumberEntity.getStringField(PalletNumberFields.NUMBER) : null;
        String storageLocation = storageLocationEntity != null ? storageLocationEntity
                .getStringField(StorageLocationFields.NUMBER) : null;
        Entity location = resource.getBelongsToField(ResourceFields.LOCATION);
        return validatePallet(palletNumber, palletType, storageLocation, resource, location);
    }

    private boolean validatePallet(String palletNumber, String palletType, String storageLocation, Entity entity,
                                   Entity location) {
        if (entity.getField("validatePallet") != null && !entity.getBooleanField("validatePallet")) {
            return true;
        }
        return !existsOtherResourceForPalletNumber(palletNumber, palletType, storageLocation, entity, location)
                && !existsOtherPositionForPalletNumber(palletNumber, palletType, storageLocation, entity, location)
                && !existsOtherDeliveredProductForPalletNumber(palletNumber, palletType, storageLocation, entity, location);

    }

    public boolean existsOtherPositionForPalletNumber(String palletNumber, String palletType, String storageLocation,
                                                      Entity entity, Entity location) {
        StringBuilder query = new StringBuilder();
        query.append("select count(dp) as cnt from #materialFlowResources_position dp JOIN dp.palletNumber as pallet ");
        query.append("JOIN dp.document as document ");
        query.append("JOIN document.locationTo as location ");
        query.append("LEFT JOIN dp.storageLocation storageLocation ");
        query.append("WHERE pallet.number = :palletNumber ");
        query.append("AND document.state = '01draft' ");
        query.append("AND (storageLocation.number <> :storageLocation OR dp.typeOfPallet <> :palletType) ");
        query.append("AND location.id = :locationId ");
        if (entity.getId() != null) {
            query.append("AND dp.id <> :dpId ");
        }
        SearchQueryBuilder find = dataDefinitionService.get(MaterialFlowResourcesConstants.PLUGIN_IDENTIFIER,
                MaterialFlowResourcesConstants.MODEL_POSITION).find(query.toString());
        find.setString("palletType", palletType);
        find.setString("palletNumber", palletNumber);
        find.setString("storageLocation", storageLocation);
        find.setLong("locationId", location.getId());
        if (entity.getId() != null) {
            find.setLong("dpId", entity.getId());
        }
        Entity countResults = find.uniqueResult();

        boolean exists = ((Long) countResults.getField("cnt")) > 0L;
        if (exists) {
            entity.addError(entity.getDataDefinition().getField("palletNumber"),
                    "documentGrid.error.position.existsOtherPositionForPalletAndStorageLocation");

        }
        return exists;
    }

    private boolean existsOtherResourceForPalletNumber(String palletNumber, String palletType, String storageLocation,
                                                       Entity entity, Entity location) {
        StringBuilder query = new StringBuilder();
        query.append("select count(dp) as cnt from #materialFlowResources_resource dp JOIN dp.palletNumber as pallet ");
        query.append("JOIN dp.location AS location ");
        query.append("LEFT JOIN dp.storageLocation storageLocation ");
        query.append("WHERE pallet.number = :palletNumber ");
        query.append("AND (storageLocation.number <> :storageLocation OR dp.typeOfPallet <> :palletType) ");
        query.append("AND location.id = :locationId ");
        if (entity.getId() != null) {
            query.append("AND dp.id <> :resourceId ");

        }
        SearchQueryBuilder find = dataDefinitionService.get(MaterialFlowResourcesConstants.PLUGIN_IDENTIFIER,
                MaterialFlowResourcesConstants.MODEL_RESOURCE).find(query.toString());
        find.setString("palletType", palletType);
        find.setString("palletNumber", palletNumber);
        find.setString("storageLocation", storageLocation);
        find.setLong("locationId", location.getId());
        if (entity.getId() != null) {
            find.setLong("resourceId", entity.getId());
        }
        Entity countResults = find.uniqueResult();

        boolean exists = ((Long) countResults.getField("cnt")) > 0L;
        if (exists) {
            entity.addError(entity.getDataDefinition().getField("palletNumber"),
                    "documentGrid.error.position.existsOtherResourceForPalletAndStorageLocation");

        }
        return exists;
    }

    private boolean existsOtherDeliveredProductForPalletNumber(String palletNumber, String palletType,
                                                               String storageLocation,
                                                               Entity entity, Entity location) {
        StringBuilder query = new StringBuilder();
        query.append("select count(dp) as cnt from #deliveries_deliveredProduct dp JOIN dp.palletNumber as pallet ");
        query.append("JOIN dp.delivery delivery ");
        query.append("JOIN delivery.location AS location ");
        query.append("LEFT JOIN dp.storageLocation storageLocation ");
        query.append("WHERE pallet.number = :palletNumber ");
        query.append("AND delivery.state <> '06received' AND delivery.state <> '04declined' ");
        query.append("AND (storageLocation.number <> :storageLocation OR dp.palletType <> :palletType) ");
        query.append("AND location.id = :locationId ");
        if (entity.getId() != null) {
            query.append("AND dp.id <> :dpId ");
        }
        SearchQueryBuilder find = dataDefinitionService.get("deliveries", "deliveredProduct").find(query.toString());
        find.setString("palletType", palletType);
        find.setString("palletNumber", palletNumber);
        find.setString("storageLocation", storageLocation);
        find.setLong("locationId", location.getId());
        if (entity.getId() != null) {
            find.setLong("dpId", entity.getId());
        }

        Entity countResults = find.uniqueResult();

        boolean exists = ((Long) countResults.getField("cnt")) > 0L;
        if (exists) {
            entity.addError(entity.getDataDefinition().getField("palletNumber"),
                    "documentGrid.error.position.existsOtherDeliveredProductForPalletAndStorageLocation");

        }
        return exists;
    }

}
