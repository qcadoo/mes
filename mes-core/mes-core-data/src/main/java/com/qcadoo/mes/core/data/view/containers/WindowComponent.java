package com.qcadoo.mes.core.data.view.containers;

import java.util.Map;

import com.qcadoo.mes.core.data.internal.view.AbstractRootComponent;
import com.qcadoo.mes.core.data.model.DataDefinition;

public class WindowComponent extends AbstractRootComponent {

    private boolean backButton = true;

    public WindowComponent(final String name, final DataDefinition dataDefinition) {
        super(name, dataDefinition);
    }

    @Override
    public String getType() {
        return "window";
    }

    @Override
    public Map<String, Object> getOptions() {
        Map<String, Object> viewOptions = super.getOptions();
        viewOptions.put("backButton", backButton);
        return viewOptions;
    }

    public boolean isBackButton() {
        return backButton;
    }

    public void setBackButton(boolean backButton) {
        this.backButton = backButton;
    }

}
