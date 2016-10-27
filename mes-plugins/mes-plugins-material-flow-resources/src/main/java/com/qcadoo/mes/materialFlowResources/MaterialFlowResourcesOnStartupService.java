package com.qcadoo.mes.materialFlowResources;

import com.qcadoo.mes.basic.ParameterService;
import com.qcadoo.mes.materialFlowResources.constants.DocumentPositionParametersItemValues;
import com.qcadoo.mes.materialFlowResources.constants.MaterialFlowResourcesConstants;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.plugin.api.Module;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Component
public class MaterialFlowResourcesOnStartupService extends Module {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private ParameterService parameterService;

    @Override
    public void enableOnStartup() {
        setDocumentPositionParameters();
    }

    @Override
    public void enable() {
        setDocumentPositionParameters();
    }

    @Transactional
    private void setDocumentPositionParameters() {

        DataDefinition positionParametersDD = dataDefinitionService.get(MaterialFlowResourcesConstants.PLUGIN_IDENTIFIER, MaterialFlowResourcesConstants.MODEL_DOCUMENT_POSITION_PARAMETERS);
        DataDefinition positionParametersItemDD = dataDefinitionService.get(MaterialFlowResourcesConstants.PLUGIN_IDENTIFIER, MaterialFlowResourcesConstants.MODEL_DOCUMENT_POSITION_PARAMETERS_ITEM);

        Entity positionParameters = positionParametersDD.find().uniqueResult();
        if (positionParameters == null) {
            positionParameters = positionParametersDD.create();

            positionParameters.setField("suggestResource", "true");
            positionParameters.setField("draftMakesReservation", "true");
            positionParameters.setField("acceptanceOfDocumentBeforePrinting", "true");

            positionParameters = positionParametersDD.save(positionParameters);

            Entity parameter = parameterService.getParameter();
            parameter.setField("documentPositionParameters", positionParameters);
            parameter.getDataDefinition().save(parameter);
        }

        List<Object[]> items = new ArrayList<>();
        items.add(new Object[]{1, "act", false});
        items.add(new Object[]{2, "number", false});
        items.add(new Object[]{3, "product", false});
        items.add(new Object[]{4, "productName", true});
        items.add(new Object[]{5, "additionalCode", true});
        items.add(new Object[]{6, "quantity", false});
        items.add(new Object[]{7, "unit", false});
        items.add(new Object[]{8, "givenquantity", false});
        items.add(new Object[]{9, "givenunit", false});
        items.add(new Object[]{10, "conversion", false});
        items.add(new Object[]{11, "resource", true});
        items.add(new Object[]{12, "price", true});
        items.add(new Object[]{13, "batch", true});
        items.add(new Object[]{14, "productionDate", true});
        items.add(new Object[]{15, "expirationDate", true});
        items.add(new Object[]{16, "storageLocation", true});
        items.add(new Object[]{17, "palletNumber", true});
        items.add(new Object[]{18, "typeOfPallet", true});
        items.add(new Object[]{19, "waste", true});

        for (Object[] item : items) {
            Entity itemEntity = positionParametersItemDD.find().add(SearchRestrictions.eq("name", item[1])).uniqueResult();
            if (itemEntity == null) {
                itemEntity = positionParametersItemDD.create();

                itemEntity.setField("ordering", item[0]);
                itemEntity.setField("name", item[1]);
                itemEntity.setField("parameters", positionParameters);
                itemEntity.setField("editable", item[2]);
                itemEntity.setField("checked", true);

                positionParametersItemDD.save(itemEntity);
            }
        }
    }

}
