package com.qcadoo.mes.materialFlowResources.listeners;

import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import com.qcadoo.mes.materialFlowResources.constants.PalletStorageStateDtoFields;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.CheckBoxComponent;
import com.qcadoo.view.api.components.GridComponent;
import com.qcadoo.view.constants.QcadooViewConstants;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;

@Service
public class PalletStorageStateListListeners {

    public static final String L_ISNULL = "ISNULL";

    private static final String L_FILTERS = "filters";

    private static final String L_GRID_OPTIONS = "grid.options";

    private static final String L_WINDOW_ACTIVE_MENU = "window.activeMenu";

    public void showDetails(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        GridComponent palletStorageStateGrid = (GridComponent) view.getComponentByReference(QcadooViewConstants.L_GRID);

        final Entity selectedRecord = Iterables.getLast(palletStorageStateGrid.getSelectedEntities());
        final Map<String, String> filters = Maps.newHashMap();

        Consumer<String> filterAppender = createFilterAppender(filters, selectedRecord);
        filterAppender.accept(PalletStorageStateDtoFields.PALLET_NUMBER);
        filterAppender.accept(PalletStorageStateDtoFields.TYPE_OF_PALLET);
        filterAppender.accept(PalletStorageStateDtoFields.STORAGE_LOCATION_NUMBER);
        filterAppender.accept(PalletStorageStateDtoFields.LOCATION_NUMBER);

        Map<String, Object> gridOptions = Maps.newHashMap();
        gridOptions.put(L_FILTERS, filters);

        Map<String, Object> parameters = Maps.newHashMap();
        parameters.put(L_GRID_OPTIONS, gridOptions);

        parameters.put(L_WINDOW_ACTIVE_MENU, "materialFlow.palletStorageState");

        String url = "../page/materialFlowResources/palletStorageStateDetails.html";
        view.redirectTo(url, false, true, parameters);
    }

    private Consumer<String> createFilterAppender(Map<String, String> filters, Entity selectedRecord) {
        return (fieldName) -> {
            String fieldValue = selectedRecord.getStringField(fieldName);
            if (Objects.isNull(fieldValue)) {
                filters.put(fieldName, L_ISNULL);
            } else {
                filters.put(fieldName, "[" + fieldValue + "]");
            }
        };
    }

    public void showPallestWithFreeSpace(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        CheckBoxComponent isShiftFilter = (CheckBoxComponent) view.getComponentByReference(PalletStorageStateDtoFields.IS_SHIFT_FILTER);
        CheckBoxComponent isFreeFilter = (CheckBoxComponent) view.getComponentByReference(PalletStorageStateDtoFields.IS_FREE_FILTER);
        isShiftFilter.setChecked(false);
        isFreeFilter.setChecked(true);
        isShiftFilter.requestComponentUpdateState();
        isFreeFilter.requestComponentUpdateState();
    }

    public void showAllPallets(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        GridComponent palletGrid = (GridComponent) view.getComponentByReference(QcadooViewConstants.L_GRID);
        palletGrid.setCustomRestriction(searchBuilder -> searchBuilder.add(SearchRestrictions.ge(PalletStorageStateDtoFields.TOTAL_QUANTITY,BigDecimal.ZERO)));
        CheckBoxComponent isShiftFilter = (CheckBoxComponent) view.getComponentByReference(PalletStorageStateDtoFields.IS_SHIFT_FILTER);
        CheckBoxComponent isFreeFilter = (CheckBoxComponent) view.getComponentByReference(PalletStorageStateDtoFields.IS_FREE_FILTER);
        isShiftFilter.setChecked(false);
        isFreeFilter.setChecked(false);
        isShiftFilter.requestComponentUpdateState();
        isFreeFilter.requestComponentUpdateState();
    }

    public void showPalletsWithProductToShift(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        CheckBoxComponent isShiftFilter = (CheckBoxComponent) view.getComponentByReference(PalletStorageStateDtoFields.IS_SHIFT_FILTER);
        CheckBoxComponent isFreeFilter = (CheckBoxComponent) view.getComponentByReference(PalletStorageStateDtoFields.IS_FREE_FILTER);
        isShiftFilter.setChecked(true);
        isFreeFilter.setChecked(false);
        isShiftFilter.requestComponentUpdateState();
        isFreeFilter.requestComponentUpdateState();

    }
    public void moveToStorageLocation(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        performActionBasedOnSelectedEntities(view, "palletMoveToStorageLocationHelper");
    }

    public void transferResources(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        performActionBasedOnSelectedEntities(view, "palletResourcesTransferHelper");
    }

    private void performActionBasedOnSelectedEntities(ViewDefinitionState view, String viewName) {
        GridComponent palletStorageStateGrid = (GridComponent) view.getComponentByReference(QcadooViewConstants.L_GRID);
        Set<Long> selectedEntities = palletStorageStateGrid.getSelectedEntitiesIds();
        String palletNumberFilter = palletStorageStateGrid.getFilters().get("palletNumber");

        final Map<String, Object> parameters = new HashMap<>();
        parameters.put("selectedEntities", selectedEntities);
        parameters.put("palletNumberFilter", palletNumberFilter);
        JSONObject context = new JSONObject(parameters);
        StringBuilder url = new StringBuilder("../page/materialFlowResources/" + viewName + ".html");
        url.append("?context=");
        url.append(context.toString());

        view.openModal(url.toString());
    }

}
