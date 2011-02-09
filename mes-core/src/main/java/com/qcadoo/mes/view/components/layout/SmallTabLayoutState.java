package com.qcadoo.mes.view.components.layout;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.qcadoo.mes.view.ComponentPattern;
import com.qcadoo.mes.view.ComponentState;
import com.qcadoo.mes.view.states.AbstractContainerState;

public class SmallTabLayoutState extends AbstractContainerState {

    private final List<SmallTabLayoutPatternTab> tabs;

    public SmallTabLayoutState(final List<SmallTabLayoutPatternTab> tabs) {
        this.tabs = tabs;
    }

    @Override
    protected void initializeContent(final JSONObject json) throws JSONException {
        // empty
        requestRender();
    }

    @Override
    protected JSONObject renderContent() throws JSONException {
        JSONObject state = new JSONObject();
        List<String> errorTabs = new LinkedList<String>();
        for (Map.Entry<String, ComponentState> child : getChildren().entrySet()) {
            if (child.getValue().isHasError()) {
                for (SmallTabLayoutPatternTab tab : tabs) {
                    boolean found = false;
                    for (ComponentPattern tabComponents : tab.getComponents()) {
                        if (tabComponents.getName().equals(child.getValue().getName())) {
                            errorTabs.add(tab.getName());
                            found = true;
                            break;
                        }
                    }
                    if (found) {
                        break;
                    }

                }
            }
        }
        JSONArray errors = new JSONArray();
        for (String tabName : errorTabs) {
            errors.put(tabName);
        }
        state.put("errors", errors);

        return state;
    }
}
