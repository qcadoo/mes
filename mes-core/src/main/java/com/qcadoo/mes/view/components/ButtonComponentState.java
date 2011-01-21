package com.qcadoo.mes.view.components;

import org.json.JSONException;
import org.json.JSONObject;

import com.google.common.collect.ImmutableMap;
import com.qcadoo.mes.view.states.AbstractComponentState;

public class ButtonComponentState extends AbstractComponentState {

    public static final String JSON_BELONGS_TO_ENTITY_ID = "belongsToEntityId";

    private final String correspondingView;

    private final String correspondingComponent;

    private final String correspondingField;

    private final String url;

    private String value;

    private Long belongsToEntityId;

    public ButtonComponentState(final String url) {
        this.url = url;
        this.correspondingView = null;
        this.correspondingComponent = null;
        this.correspondingField = null;
        registerEvent("initialize", this, "initialize");
        registerEvent("initializeAfterBack", this, "initialize");
    }

    public ButtonComponentState(final String correspondingView, final String correspondingComponent,
            final String correspondingField) {
        this.url = null;
        this.correspondingField = correspondingField;
        this.correspondingView = correspondingView;
        this.correspondingComponent = correspondingComponent;
        registerEvent("initialize", this, "initialize");
        registerEvent("initializeAfterBack", this, "initialize");
    }

    @Override
    protected void initializeContent(final JSONObject json) throws JSONException {
        // empty
    }

    public void initialize(final String[] args) {
        refreshValue();
        requestRender();
    }

    @Override
    public void onScopeEntityIdChange(final Long scopeEntityId) {
        belongsToEntityId = scopeEntityId;
        refreshValue();
        requestRender();
    }

    @Override
    protected JSONObject renderContent() throws JSONException {
        JSONObject json = new JSONObject();
        json.put(JSON_VALUE, value);
        json.put(JSON_BELONGS_TO_ENTITY_ID, belongsToEntityId);
        return json;
    }

    private void refreshValue() {
        value = null;

        if (url != null) {
            value = url;
        } else {
            value = correspondingView + ".html";
        }

        if (correspondingComponent != null && belongsToEntityId != null) {

            value += "?context="
                    + new JSONObject(ImmutableMap.of(correspondingComponent + "." + correspondingField, belongsToEntityId))
                            .toString();
        }
    }

    @Override
    public final void setFieldValue(final Object value) {
        this.value = value != null ? value.toString() : null;
    }

    @Override
    public final Object getFieldValue() {
        return value;
    }

}
