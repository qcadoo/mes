package com.qcadoo.mes.materialFlowResources.listeners;

import com.qcadoo.mes.materialFlowResources.storagelocation.imports.ImportStorageLocationService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FormComponent;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public class ImportStorageLocationListListeners {

    public static final String L_FORM = "form";

    @Autowired
    private ImportStorageLocationService importService;

    public void importPositions(final ViewDefinitionState view, final ComponentState state, final String[] args) {

        FormComponent form = (FormComponent) view.getComponentByReference(L_FORM);
        Entity entity = form.getPersistedEntityWithIncludedFormValues();

        if (Objects.isNull(entity.getBelongsToField("location")) || StringUtils.isEmpty(entity.getStringField("positionsFile"))) {
            view.addMessage("materialFlowResources.importStorageLocationList.importPositions.import.fillFields",
                    ComponentState.MessageType.FAILURE);
            return;
        }
        boolean imported = importService.importPositionsFromFile(entity, view);
        if (imported) {
            view.addMessage("materialFlowResources.importStorageLocationList.importPositions.impsuccess",
                    ComponentState.MessageType.SUCCESS);
        } else {
            view.addMessage("materialFlowResources.importStorageLocationList.importPositions.import.failure",
                    ComponentState.MessageType.FAILURE, false);
        }
    }

}
