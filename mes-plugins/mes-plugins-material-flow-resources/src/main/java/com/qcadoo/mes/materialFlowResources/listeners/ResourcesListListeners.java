package com.qcadoo.mes.materialFlowResources.listeners;

import com.google.common.collect.Maps;
import com.qcadoo.mes.materialFlowResources.constants.ResourceDtoFields;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.CheckBoxComponent;
import com.qcadoo.view.api.components.GridComponent;
import com.qcadoo.view.constants.QcadooViewConstants;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ResourcesListListeners {

    public void showResourcesWithShortExpiryDate(final ViewDefinitionState view, final ComponentState state,
                                                 final String[] args) {
        CheckBoxComponent isShortFilter = (CheckBoxComponent) view.getComponentByReference(ResourceDtoFields.IS_SHORT_FILTER);
        CheckBoxComponent isDeadlineFilter = (CheckBoxComponent) view
                .getComponentByReference(ResourceDtoFields.IS_DEADLINE_FILTER);
        isShortFilter.setChecked(true);
        isDeadlineFilter.setChecked(false);
        isShortFilter.requestComponentUpdateState();
        isDeadlineFilter.requestComponentUpdateState();
    }

    public void showResourcesAfterDeadline(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        CheckBoxComponent isShortFilter = (CheckBoxComponent) view.getComponentByReference(ResourceDtoFields.IS_SHORT_FILTER);
        CheckBoxComponent isDeadlineFilter = (CheckBoxComponent) view
                .getComponentByReference(ResourceDtoFields.IS_DEADLINE_FILTER);
        isShortFilter.setChecked(false);
        isDeadlineFilter.setChecked(true);
        isShortFilter.requestComponentUpdateState();
        isDeadlineFilter.requestComponentUpdateState();
    }

    public void showAllResources(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        CheckBoxComponent isShortFilter = (CheckBoxComponent) view.getComponentByReference(ResourceDtoFields.IS_SHORT_FILTER);
        CheckBoxComponent isDeadlineFilter = (CheckBoxComponent) view
                .getComponentByReference(ResourceDtoFields.IS_DEADLINE_FILTER);
        isShortFilter.setChecked(false);
        isDeadlineFilter.setChecked(false);
        isShortFilter.requestComponentUpdateState();
        isDeadlineFilter.requestComponentUpdateState();
    }

    public final void changeStorageLocations(final ViewDefinitionState view, final ComponentState state,
                                             final String[] args) {
        GridComponent gridComponent = (GridComponent) view.getComponentByReference(QcadooViewConstants.L_GRID);

        Map<String, Object> parameters = Maps.newHashMap();

        String url = "../page/materialFlowResources/changeStorageLocationHelper.html";

        parameters.put("form.resourceIds", gridComponent.getSelectedEntitiesIds().stream().map(String::valueOf).collect(Collectors.joining(",")));
        parameters.put("form.location", gridComponent.getSelectedEntities().stream().findFirst().get().getIntegerField(ResourceDtoFields.LOCATION_ID));

        view.openModal(url, parameters);
    }
}
