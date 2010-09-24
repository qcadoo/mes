package com.qcadoo.mes.core.view;

import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import com.qcadoo.mes.core.api.Entity;

public interface CastableComponent<T> {

    ViewValue<T> castValue(Map<String, Entity> selectedEntities, JSONObject viewObject) throws JSONException;

}
