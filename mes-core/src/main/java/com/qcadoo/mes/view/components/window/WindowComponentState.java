package com.qcadoo.mes.view.components.window;

import org.json.JSONException;
import org.json.JSONObject;

import com.qcadoo.mes.view.ribbon.Ribbon;
import com.qcadoo.mes.view.states.AbstractContainerState;

public class WindowComponentState extends AbstractContainerState {

    private final WindowComponentPattern pattern;

    private final Ribbon ribbon;

    public WindowComponentState(final WindowComponentPattern pattern) {
        this.pattern = pattern;
        ribbon = pattern.getRibbonCopy();
    }

    @Override
    protected void initializeContent(final JSONObject json) throws JSONException {
        // empty
    }

    @Override
    protected JSONObject renderContent() throws JSONException {
        Ribbon diffrenceRibbon = ribbon.getUpdate();
        JSONObject json = new JSONObject();
        json.put("ribbon", pattern.translateRibbon(diffrenceRibbon, getLocale()));
        return json;
    }

    public Ribbon getRibbon() {
        return ribbon;
    }

    public void requestRibbonRender() {
        requestRender();
    }

}
