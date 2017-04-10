package com.qcadoo.mes.materialFlowResources.hooks;

import java.math.BigDecimal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.materialFlowResources.constants.DocumentPositionParametersItemValues;
import com.qcadoo.mes.materialFlowResources.constants.MaterialFlowResourcesConstants;
import com.qcadoo.mes.materialFlowResources.constants.PalletStorageStateDtoFields;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.CheckBoxComponent;
import com.qcadoo.view.api.components.GridComponent;
import com.qcadoo.view.api.components.WindowComponent;
import com.qcadoo.view.api.ribbon.RibbonActionItem;
import com.qcadoo.view.api.ribbon.RibbonGroup;

@Service
public class PalletStorageStateListHooks {

    private static final String L_WINDOW = "window";

    private static final String L_GRID = "grid";

    private static final String L_DETAILS = "details";

    private static final String L_SHOW_DETAILS = "showDetails";

    private static final String L_MOVE_PALLETS = "movePallets";

    @Autowired
    private DataDefinitionService dataDefinitionService;

    public void toggleButtons(final ViewDefinitionState view) {
        applyFilters(view);
        WindowComponent window = (WindowComponent) view.getComponentByReference(L_WINDOW);
        RibbonGroup analysis = window.getRibbon().getGroupByName(L_DETAILS);
        RibbonActionItem showDetails = analysis.getItemByName(L_SHOW_DETAILS);
        RibbonActionItem showPalletsToShift = analysis.getItemByName("showPalletsWithProductsToShift");
        RibbonActionItem showFree = analysis.getItemByName("showPalletsWithFreeSpace");
        GridComponent palletStorageStateGrid = (GridComponent) view.getComponentByReference(L_GRID);
        showDetails.setEnabled(1 == palletStorageStateGrid.getSelectedEntities().size());
        showDetails.setMessage("materialFlowResources.palletStorageStateList.ribbon.message.selectOneRecord");
        showPalletsToShift.setMessage("materialFlowResources.palletStorageStateList.window.ribbon.details.showPalletsWithProductsToShift.description");
        showFree.setMessage("materialFlowResources.palletStorageStateList.window.ribbon.details.showPalletsWithFreeSpace.description");
        showDetails.requestUpdate(true);
        showPalletsToShift.requestUpdate(true);
        showFree.requestUpdate(true);

        RibbonGroup movePallets = window.getRibbon().getGroupByName(L_MOVE_PALLETS);
        movePallets.getItems().forEach(item -> {
            item.setEnabled(!palletStorageStateGrid.getSelectedEntitiesIds().isEmpty());
            item.requestUpdate(true);
        });
    }

    private void applyFilters(ViewDefinitionState view) {
        boolean isShiftFilter =   ((CheckBoxComponent)view.getComponentByReference(PalletStorageStateDtoFields.IS_SHIFT_FILTER)).isChecked();
        boolean isFreeFilter = ((CheckBoxComponent)view.getComponentByReference(PalletStorageStateDtoFields.IS_FREE_FILTER)).isChecked();
        GridComponent palletGrid = (GridComponent) view.getComponentByReference(L_GRID);
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
