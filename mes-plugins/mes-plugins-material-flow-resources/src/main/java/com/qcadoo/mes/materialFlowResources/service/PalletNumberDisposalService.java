package com.qcadoo.mes.materialFlowResources.service;

import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import com.qcadoo.mes.basic.constants.BasicConstants;
import com.qcadoo.mes.basic.constants.PalletNumberFields;
import com.qcadoo.mes.materialFlowResources.constants.MaterialFlowResourcesConstants;
import com.qcadoo.mes.materialFlowResources.constants.ResourceFields;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchRestrictions;

@Service
public class PalletNumberDisposalService {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    private DataDefinition resourceDataDefinition() {
        return dataDefinitionService.get(MaterialFlowResourcesConstants.PLUGIN_IDENTIFIER,
                MaterialFlowResourcesConstants.MODEL_RESOURCE);
    }

    public void tryToDispose(Entity palletNumber) {
        if (palletNumber != null) {
            DataDefinition palletNumberDataDefinition = palletNumber.getDataDefinition();
            Assert.isTrue(palletNumberDataDefinition.getPluginIdentifier().equals(BasicConstants.PLUGIN_IDENTIFIER));
            Assert.isTrue(palletNumberDataDefinition.getName().equals(BasicConstants.MODEL_PALLET_NUMBER));

            if (canDisposePalletNumber(palletNumber)) {
                palletNumber.setField(PalletNumberFields.ISSUE_DATE_TIME, new Date());
                palletNumberDataDefinition.save(palletNumber);
            }
        }
    }

    private boolean thereAreNoResourcesAssociatedWithGivenPalletNumber(Entity palletNumber) {
        return resourceDataDefinition().count(SearchRestrictions.belongsTo(ResourceFields.PALLET_NUMBER, palletNumber)) == 0;
    }

    private boolean canDisposePalletNumber(Entity palletNumber) {
        return thereAreNoResourcesAssociatedWithGivenPalletNumber(palletNumber);
    }
}
