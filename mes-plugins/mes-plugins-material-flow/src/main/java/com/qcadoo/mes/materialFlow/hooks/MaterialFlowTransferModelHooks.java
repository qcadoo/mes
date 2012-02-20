package com.qcadoo.mes.materialFlow.hooks;

import org.springframework.stereotype.Service;

import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;

@Service
public class MaterialFlowTransferModelHooks {

    public void copyProductionOrConsumptionDataFromBelongingTransformation(DataDefinition dd, Entity transfer) {
        Entity transformation = transfer.getBelongsToField("transformationsProduction");

        if (transformation == null) {
            transformation = transfer.getBelongsToField("transformationsConsumption");

            if (transformation == null) {
                // came here from plain transfer detail view
                return;
            } else {
                transfer.setField("type", "Consumption");
            }
        } else {
            transfer.setField("type", "Production");
        }

        transfer.setField("time", transformation.getField("time"));
        transfer.setField("stockAreasTo", transformation.getBelongsToField("stockAreasTo"));
        transfer.setField("stockAreasFrom", transformation.getBelongsToField("stockAreasFrom"));
        transfer.setField("staff", transformation.getBelongsToField("staff"));
    }
}
