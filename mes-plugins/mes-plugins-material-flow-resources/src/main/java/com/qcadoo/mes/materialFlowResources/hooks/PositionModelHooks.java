package com.qcadoo.mes.materialFlowResources.hooks;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.materialFlowResources.MaterialFlowResourcesService;
import com.qcadoo.mes.materialFlowResources.constants.DocumentFields;
import com.qcadoo.mes.materialFlowResources.constants.DocumentType;
import com.qcadoo.mes.materialFlowResources.constants.PositionFields;
import com.qcadoo.mes.materialFlowResources.constants.ResourceFields;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;

@Service
public class PositionModelHooks {

    @Autowired
    MaterialFlowResourcesService materialFlowResourceService;

    public void onSave(final DataDefinition positionDD, final Entity position) {
        Entity resource = position.getBelongsToField(PositionFields.RESOURCE);
        if (resource != null) {
            position.setField(PositionFields.BATCH, resource.getField(ResourceFields.BATCH));
            position.setField(PositionFields.EXPIRATION_DATE, resource.getField(ResourceFields.EXPIRATION_DATE));
            position.setField(PositionFields.PRODUCTION_DATE, resource.getField(ResourceFields.PRODUCTION_DATE));
        }
    }

    public void onCreate(final DataDefinition positionDD, final Entity position) {
        Entity document = position.getBelongsToField(PositionFields.DOCUMENT);
        if (DocumentType.of(document).compareTo(DocumentType.RECEIPT) == 0) {
            Entity warehouse = position.getBelongsToField(PositionFields.DOCUMENT).getBelongsToField(DocumentFields.LOCATION_TO);
            position.setField(PositionFields.ATRRIBUTE_VALUES,
                    materialFlowResourceService.getAttributesForPosition(position, warehouse));
        }
    }
}
