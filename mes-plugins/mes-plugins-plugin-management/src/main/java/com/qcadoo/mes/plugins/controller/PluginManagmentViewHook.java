package com.qcadoo.mes.plugins.controller;

import java.util.LinkedList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.base.Preconditions;
import com.qcadoo.mes.view.ComponentState;
import com.qcadoo.mes.view.ViewDefinitionState;
import com.qcadoo.mes.view.components.grid.GridComponentState;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;

@Service
public class PluginManagmentViewHook {

    @Autowired
    private PluginManagmentPerformer pluginManagmentPerformer;

    @Autowired
    private DataDefinitionService dataDefinitionService;

    public void onDownloadButtonClick(final ViewDefinitionState viewDefinitionState, final ComponentState triggerState,
            final String[] args) {
        viewDefinitionState.openModal("../pluginPages/downloadPage.html");
    }

    public void onEnableButtonClick(final ViewDefinitionState viewDefinitionState, final ComponentState triggerState,
            final String[] args) {
        viewDefinitionState.openModal(pluginManagmentPerformer.performEnable(getPluginIdentifiersFromView(viewDefinitionState)));
    }

    public void onDisableButtonClick(final ViewDefinitionState viewDefinitionState, final ComponentState triggerState,
            final String[] args) {
        viewDefinitionState.openModal(pluginManagmentPerformer.performDisable(getPluginIdentifiersFromView(viewDefinitionState)));
    }

    public void onRemoveButtonClick(final ViewDefinitionState viewDefinitionState, final ComponentState triggerState,
            final String[] args) {
        viewDefinitionState.openModal(pluginManagmentPerformer.performRemove(getPluginIdentifiersFromView(viewDefinitionState)));
    }

    private List<String> getPluginIdentifiersFromView(final ViewDefinitionState viewDefinitionState) {

        List<String> pluginIdentifiers = new LinkedList<String>();
        GridComponentState grid = (GridComponentState) viewDefinitionState.getComponentByReference("grid");

        Preconditions.checkState(grid.getSelectedEntitiesId().size() > 0, "No record selected");

        DataDefinition pluginDataDefinition = dataDefinitionService.get("plugins", "plugin2");
        for (Long entityId : grid.getSelectedEntitiesId()) {
            Entity pluginEntity = pluginDataDefinition.get(entityId);
            pluginIdentifiers.add(pluginEntity.getStringField("identifier"));
        }

        return pluginIdentifiers;
    }

}
