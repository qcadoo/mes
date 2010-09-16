package com.qcadoo.mes.core.data.definition.view;

import com.qcadoo.mes.core.data.definition.DataDefinition;

public class ViewDefinition extends ContainerDefinition {

    private final DataDefinition dataDefinition;

    private final boolean forEntity;

    private String pluginCodeId;

    public ViewDefinition(final String name, final DataDefinition dataDefinition, final String pluginCodeId, boolean forEntity) {
        super(name, null);
        this.dataDefinition = dataDefinition;
        this.forEntity = forEntity;
        this.pluginCodeId = pluginCodeId;
    }

    public DataDefinition getDataDefinition() {
        return dataDefinition;
    }

    @Override
    public int getType() {
        return ComponentDefinition.TYPE_CONTAINER_WINDOW;
    }

    public boolean isForEntity() {
        return forEntity;
    }

    public String getPluginCodeId() {
        return pluginCodeId;
    }

    public void setPluginCodeId(final String pluginCodeId) {
        this.pluginCodeId = pluginCodeId;
    }

}
