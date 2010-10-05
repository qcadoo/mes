package com.qcadoo.mes.view;

import java.util.Locale;
import java.util.Map;

import org.json.JSONObject;

import com.qcadoo.mes.api.Entity;
import com.qcadoo.mes.model.DataDefinition;

public interface ViewDefinition {

    String getName();

    String getPluginIdentifier();

    DataDefinition getDataDefinition();

    Component<?> lookupComponent(String path);

    ViewValue<Object> castValue(Map<String, Entity> selectedEntities, JSONObject jsonObject);

    ViewValue<Object> getValue(final Entity entity, final Map<String, Entity> selectedEntities,
            final ViewValue<Object> globalJsonValue, String triggerComponentName, boolean saveOrDelete);

    void updateTranslations(final Map<String, String> translations, final Locale locale);

}
