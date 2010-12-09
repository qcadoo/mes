package com.qcadoo.mes.view.states;

import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import com.qcadoo.mes.view.ComponentState;
import com.qcadoo.mes.view.FieldEntityIdChangeListener;
import com.qcadoo.mes.view.ScopeEntityIdChangeListener;

public class ComponentStateMock extends AbstractComponentState {

    private final JSONObject render;

    private JSONObject content;

    public ComponentStateMock() {
        render = new JSONObject();
    }

    public ComponentStateMock(final JSONObject render) {
        this.render = render;
    }

    @Override
    protected void initializeContent(final JSONObject content) throws JSONException {
        requestRender();
        this.content = content;
    }

    @Override
    protected JSONObject renderContent() throws JSONException {
        return render;
    }

    public Map<String, FieldEntityIdChangeListener> getPublicFieldEntityIdChangeListeners() {
        return getFieldEntityIdChangeListeners();
    }

    public Map<String, ScopeEntityIdChangeListener> getPublicScopeFieldEntityIdChangeListeners() {
        return getScopeEntityIdChangeListeners();
    }

    public void registerTestEvent(final String name, final TestEvent obj) {
        registerEvent(name, obj, "invoke");
    }

    public void registerTestCustomEvent(final String name, final TestCustomEvent obj) {
        registerEvent(name, obj, "invoke");
    }

    public JSONObject getContent() {
        return content;
    }

    public static interface TestEvent {

        void invoke(String... args);

    }

    public static interface TestCustomEvent {

        void invoke(ComponentState componentState, String... args);

    }

}
