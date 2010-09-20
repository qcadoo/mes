package com.qcadoo.mes.core.data.view;

import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import com.qcadoo.mes.core.data.beans.Entity;

public interface CastableComponent<T> {

    public abstract ViewEntity<T> castValue(final Entity entity, final Map<String, Entity> selectedEntities,
            final JSONObject viewObject) throws JSONException;

}