package com.qcadoo.mes.materialFlowResources.listeners;

import org.springframework.stereotype.Service;

import com.qcadoo.mes.materialFlowResources.constants.ResourceDtoFields;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.CheckBoxComponent;

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
}
