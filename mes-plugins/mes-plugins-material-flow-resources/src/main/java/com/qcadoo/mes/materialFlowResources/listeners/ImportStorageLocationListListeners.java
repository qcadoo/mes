package com.qcadoo.mes.materialFlowResources.listeners;

import java.util.Objects;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.materialFlowResources.storagelocation.imports.ImportStorageLocationService;
import com.qcadoo.mes.materialFlowResources.storagelocation.imports.ImportStorageLocationsResult;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.constants.QcadooViewConstants;

@Service
public class ImportStorageLocationListListeners {



    @Autowired
    private ImportStorageLocationService importService;

    public void importPositions(final ViewDefinitionState view, final ComponentState state, final String[] args) {

        FormComponent form = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);
        Entity entity = form.getPersistedEntityWithIncludedFormValues();

        if (Objects.isNull(entity.getBelongsToField("location")) || StringUtils.isEmpty(entity.getStringField("positionsFile"))) {
            view.addMessage("materialFlowResources.importStorageLocationList.importPositions.import.fillFields",
                    ComponentState.MessageType.FAILURE);
            return;
        }
        ImportStorageLocationsResult result = importService.importPositionsFromFile(entity, view);
        if (result.isImported()) {
            view.addMessage("materialFlowResources.importStorageLocationList.importPositions.import.success",
                    ComponentState.MessageType.SUCCESS);
            if(!result.getNotExistingProducts().isEmpty()) {
                view.addMessage("materialFlowResources.importStorageLocationList.importPositions.import.success.notExistingProducts",
                        ComponentState.MessageType.INFO, result.getNotExistingProducts().stream().collect(Collectors.joining(", ")));
            }
        } else {
              view.addMessage("materialFlowResources.importStorageLocationList.importPositions.import.failure",
                            ComponentState.MessageType.FAILURE, false);
        }
    }

}
