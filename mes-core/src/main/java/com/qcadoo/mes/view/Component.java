package com.qcadoo.mes.view;

import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.json.JSONException;
import org.json.JSONObject;

import com.qcadoo.mes.api.Entity;
import com.qcadoo.mes.model.DataDefinition;

public interface Component<T> {

    String getType();

    ViewValue<T> getValue(Entity entity, Map<String, Entity> selectedEntities, ViewValue<?> viewValue, Set<String> pathsToUpdate);

    String getName();

    String getSourceFieldPath();

    String getPath();

    ViewDefinition getViewDefinition();

    String getFieldPath();

    DataDefinition getDataDefinition();

    boolean isDefaultEnabled();

    boolean isDefaultVisible();

    void updateTranslations(Map<String, String> translationsMap, Locale locale);

    boolean initializeComponent(Map<String, Component<?>> componentRegistry);

    boolean isInitialized();

    ViewValue<T> castValue(Map<String, Entity> selectedEntities, JSONObject viewObject) throws JSONException;

    Set<String> getListeners();

    boolean isRelatedToMainEntity();

}
