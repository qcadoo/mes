package com.qcadoo.mes.materialFlowResources;

import com.qcadoo.mes.materialFlowResources.constants.DocumentFields;
import com.qcadoo.mes.materialFlowResources.constants.LocationFieldsMFR;
import com.qcadoo.mes.materialFlowResources.constants.MaterialFlowResourcesConstants;
import com.qcadoo.mes.materialFlowResources.constants.WarehouseAlgorithm;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class WarehouseMethodOfDisposalService {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    public String getSqlConditionForResourceLookup(final Long documentId){
        Entity document = dataDefinitionService.get(MaterialFlowResourcesConstants.PLUGIN_IDENTIFIER,
                MaterialFlowResourcesConstants.MODEL_DOCUMENT).get(documentId);
        String alg = document.getBelongsToField(DocumentFields.LOCATION_FROM).getStringField(LocationFieldsMFR.ALGORITHM);
        WarehouseAlgorithm algorithm = WarehouseAlgorithm.parseString(alg);
        switch(algorithm) {
            case FEFO:
                return " expirationdate = (select min(expirationdate) from materialflowresources_resource ";
            case FIFO:
                return " time = (select min(time) from materialflowresources_resource ";
            case LEFO:
                return " expirationdate = (select max(expirationdate) from materialflowresources_resource ";
            case LIFO:
                return " time = (select max(time) from materialflowresources_resource ";
        }
        return " expirationdate = (select min(expirationdate) from materialflowresources_resource ";
    }
}
