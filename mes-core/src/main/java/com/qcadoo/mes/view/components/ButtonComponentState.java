/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 0.3.0
 *
 * This file is part of Qcadoo.
 *
 * Qcadoo is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation; either version 3 of the License,
 * or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 * ***************************************************************************
 */

package com.qcadoo.mes.view.components;

import org.json.JSONException;
import org.json.JSONObject;

import com.google.common.collect.ImmutableMap;
import com.qcadoo.mes.view.states.AbstractComponentState;

public class ButtonComponentState extends AbstractComponentState {

    public static final String JSON_BELONGS_TO_ENTITY_ID = "belongsToEntityId";

    public static final String JSON_OPEN_IN_MODAL = "openInModal";

    private final String correspondingView;

    private final String correspondingComponent;

    private final boolean correspondingViewInModal;

    private final String correspondingField;

    private final String url;

    private String value;

    private Long belongsToEntityId;

    public ButtonComponentState(final String url) {
        this.url = url;
        this.correspondingView = null;
        this.correspondingComponent = null;
        this.correspondingField = null;
        this.correspondingViewInModal = false;
        registerEvent("initialize", this, "initialize");
        registerEvent("initializeAfterBack", this, "initialize");
    }

    public ButtonComponentState(final String correspondingView, final String correspondingComponent,
            final boolean correspondingViewInModal, final String correspondingField) {
        this.url = null;
        this.correspondingField = correspondingField;
        this.correspondingView = correspondingView;
        this.correspondingComponent = correspondingComponent;
        this.correspondingViewInModal = correspondingViewInModal;
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
        json.put(JSON_OPEN_IN_MODAL, correspondingViewInModal);
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
