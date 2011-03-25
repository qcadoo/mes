package com.qcadoo.view.internal.components.window;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.qcadoo.view.internal.ComponentState;
import com.qcadoo.view.internal.ribbon.Ribbon;
import com.qcadoo.view.internal.ribbon.RibbonUtils;
import com.qcadoo.view.internal.states.AbstractContainerState;

public class WindowComponentState extends AbstractContainerState {

    private final Ribbon ribbon;

    private final WindowComponentPattern pattern;

    public WindowComponentState(final WindowComponentPattern pattern) {
        this.pattern = pattern;
        ribbon = pattern.getRibbon().getCopy();
    }

    @Override
    protected void initializeContent(final JSONObject json) throws JSONException {
        requestRender();
    }

    @Override
    protected JSONObject renderContent() throws JSONException {
        JSONObject json = new JSONObject();

        List<String> errorTabs = new LinkedList<String>();
        for (Map.Entry<String, ComponentState> child : getChildren().entrySet()) {
            if (child.getValue().isHasError()) {
                errorTabs.add(child.getKey());
            }
        }
        JSONArray errors = new JSONArray();
        for (String tabName : errorTabs) {
            errors.put(tabName);
        }
        json.put("errors", errors);

        if (ribbon != null) {
            Ribbon diffrenceRibbon = ribbon.getUpdate();
            if (diffrenceRibbon != null) {
                json.put("ribbon", RibbonUtils.getInstance().translateRibbon(diffrenceRibbon, getLocale(), pattern));
            }
        }

        return json;
    }

    public Ribbon getRibbon() {
        return ribbon;
    }

    public void requestRibbonRender() {
        requestRender();
    }

}
