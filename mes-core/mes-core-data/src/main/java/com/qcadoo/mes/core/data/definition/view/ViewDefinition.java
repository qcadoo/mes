package com.qcadoo.mes.core.data.definition.view;

import com.qcadoo.mes.core.data.definition.DataDefinition;

public class ViewDefinition extends ContainerDefinition {

    private String pluginCodeId;

    public ViewDefinition(final String name, final DataDefinition dataDefinition, final String pluginCodeId) {
        super(name, null, null, null, dataDefinition);
        this.pluginCodeId = pluginCodeId;
    }

    @Override
    public int getType() {
        return ComponentDefinition.TYPE_CONTAINER_WINDOW;
    }

    public String getPluginCodeId() {
        return pluginCodeId;
    }

    public void setPluginCodeId(final String pluginCodeId) {
        this.pluginCodeId = pluginCodeId;
    }

}
