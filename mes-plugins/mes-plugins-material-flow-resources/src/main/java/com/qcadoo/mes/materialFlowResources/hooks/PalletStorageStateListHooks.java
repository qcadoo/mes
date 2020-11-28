package com.qcadoo.mes.materialFlowResources.hooks;

import com.qcadoo.mes.materialFlowResources.constants.DocumentPositionParametersItemValues;
import com.qcadoo.mes.materialFlowResources.constants.MaterialFlowResourcesConstants;
import com.qcadoo.mes.materialFlowResources.constants.PalletStorageStateDtoFields;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.CheckBoxComponent;
import com.qcadoo.view.api.components.GridComponent;
import com.qcadoo.view.constants.QcadooViewConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class PalletStorageStateListHooks {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    public void applyFilters(ViewDefinitionState view) {
        boolean isShiftFilter =   ((CheckBoxComponent)view.getComponentByReference(PalletStorageStateDtoFields.IS_SHIFT_FILTER)).isChecked();
        boolean isFreeFilter = ((CheckBoxComponent)view.getComponentByReference(PalletStorageStateDtoFields.IS_FREE_FILTER)).isChecked();
        GridComponent palletGrid = (GridComponent) view.getComponentByReference(QcadooViewConstants.L_GRID);
        Integer palletToShift = dataDefinitionService.get(MaterialFlowResourcesConstants.PLUGIN_IDENTIFIER,MaterialFlowResourcesConstants.MODEL_DOCUMENT_POSITION_PARAMETERS).find().setMaxResults(1).uniqueResult().getIntegerField(DocumentPositionParametersItemValues.PALLET_TO_SHIFT);
        Integer palletWithFreePlace = dataDefinitionService.get(MaterialFlowResourcesConstants.PLUGIN_IDENTIFIER,MaterialFlowResourcesConstants.MODEL_DOCUMENT_POSITION_PARAMETERS).find().setMaxResults(1).uniqueResult().getIntegerField(DocumentPositionParametersItemValues.PALLET_WITH_FREE_PALECE);
        if(isShiftFilter){
            if(palletToShift == null){
                palletGrid.addMessage("materialFlowResources.pallet.missing.parameter.palletToShift.error", ComponentState.MessageType.FAILURE);
            }else {
                palletGrid.setCustomRestriction(searchBuilder -> searchBuilder.add(SearchRestrictions.lt(PalletStorageStateDtoFields.TOTAL_QUANTITY, BigDecimal.valueOf(palletToShift))));
            }
        }else if(isFreeFilter){
            if(palletWithFreePlace == null){
                palletGrid.addMessage("materialFlowResources.pallet.missing.parameter.palletWithFreeSpace.error", ComponentState.MessageType.FAILURE);
            }else {
                palletGrid.setCustomRestriction(searchBuilder -> searchBuilder.add(SearchRestrictions.lt(PalletStorageStateDtoFields.TOTAL_QUANTITY,  BigDecimal.valueOf(palletWithFreePlace))));
            }
        }
    }
}
