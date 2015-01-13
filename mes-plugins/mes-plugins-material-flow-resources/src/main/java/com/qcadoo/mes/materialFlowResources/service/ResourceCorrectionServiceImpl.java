package com.qcadoo.mes.materialFlowResources.service;

import java.math.BigDecimal;
import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.materialFlowResources.constants.MaterialFlowResourcesConstants;
import com.qcadoo.mes.materialFlowResources.constants.ResourceCorrectionFields;
import com.qcadoo.mes.materialFlowResources.constants.ResourceFields;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.utils.NumberGeneratorService;

@Service
public class ResourceCorrectionServiceImpl implements ResourceCorrectionService {

    @Autowired
    DataDefinitionService dataDefinitionService;

    @Autowired
    NumberGeneratorService numberGeneratorService;

    @Override
    public boolean createCorrectionForResource(final Long resourceId, final BigDecimal newQuantity,
            final String newStorageLocation) {

        Entity resource = dataDefinitionService.get(MaterialFlowResourcesConstants.PLUGIN_IDENTIFIER,
                MaterialFlowResourcesConstants.MODEL_RESOURCE).get(resourceId);
        if (isCorrectionNeeded(resource, newQuantity, newStorageLocation)) {

            Entity correction = dataDefinitionService.get(MaterialFlowResourcesConstants.PLUGIN_IDENTIFIER,
                    MaterialFlowResourcesConstants.MODEL_RESOURCE_CORRECTION).create();
            correction.setField(ResourceCorrectionFields.BATCH, batch(resource));
            correction.setField(ResourceCorrectionFields.LOCATION, location(resource));
            correction.setField(ResourceCorrectionFields.OLD_QUANTITY, oldQuantity(resource));
            correction.setField(ResourceCorrectionFields.NEW_QUANTITY, newQuantity);
            correction.setField(ResourceCorrectionFields.OLD_STORAGE_LOCATION, oldStorageLocation(resource));
            correction.setField(ResourceCorrectionFields.NEW_STORAGE_LOCATION, newStorageLocation);
            correction.setField(ResourceCorrectionFields.PRODUCT, product(resource));
            correction.setField(ResourceCorrectionFields.TIME, time(resource));
            correction.setField(ResourceCorrectionFields.NUMBER, numberGeneratorService.generateNumber(
                    MaterialFlowResourcesConstants.PLUGIN_IDENTIFIER, MaterialFlowResourcesConstants.MODEL_RESOURCE_CORRECTION));

            correction.setField(ResourceCorrectionFields.RESOURCE, resource);

            correction.getDataDefinition().save(correction);
            return true;
        }
        return false;
    }

    private boolean isCorrectionNeeded(final Entity resource, final BigDecimal newQuantity, final String newStorageLocation) {
        String oldStorageLocation = oldStorageLocation(resource);
        boolean quantityChanged = newQuantity.compareTo(oldQuantity(resource)) != 0;

        boolean storageLocationChanged = (newStorageLocation != null && oldStorageLocation != null) ? (newStorageLocation
                .compareTo(oldStorageLocation) != 0)
                : !(((newStorageLocation != null && newStorageLocation.isEmpty()) || newStorageLocation == null) && oldStorageLocation == null);
        return quantityChanged || storageLocationChanged;
    }

    private Entity product(final Entity resource) {
        return resource.getBelongsToField(ResourceFields.PRODUCT);
    }

    private BigDecimal oldQuantity(final Entity resource) {
        return resource.getDecimalField(ResourceFields.QUANTITY);
    }

    private Entity location(final Entity resource) {
        return resource.getBelongsToField(ResourceFields.LOCATION);
    }

    private Date time(final Entity resource) {
        return resource.getDateField(ResourceFields.TIME);
    }

    private String batch(final Entity resource) {
        return resource.getStringField(ResourceFields.BATCH);
    }

    private String oldStorageLocation(final Entity resource) {
        return resource.getStringField(ResourceFields.STORAGE_LOCATION);
    }
}
