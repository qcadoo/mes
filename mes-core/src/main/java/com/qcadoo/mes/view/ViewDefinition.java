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

    ViewValue<Long> castValue(Map<String, Entity> selectedEntities, JSONObject jsonObject);

    ViewValue<Long> getValue(final Entity entity, final Map<String, Entity> selectedEntities,
            final ViewValue<Long> globalJsonValue, String triggerComponentName, boolean saveOrDelete, final Locale locale);

    void updateTranslations(final Map<String, String> translations, final Locale locale);

    RootComponent getRoot();
}
