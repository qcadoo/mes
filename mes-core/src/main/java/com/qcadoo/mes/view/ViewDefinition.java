package com.qcadoo.mes.view;

import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.json.JSONException;
import org.json.JSONObject;

import com.qcadoo.mes.api.Entity;
import com.qcadoo.mes.api.TranslationService;

public interface ViewDefinition {

    String getName();

    String getPluginIdentifier();

    RootComponent getRoot();

    ViewValue<Object> getValue(final Entity entity, final Map<String, Entity> selectedEntities,
            final ViewValue<Object> globalViewValue, final Set<String> pathsToUpdate, String triggerComponentName);

    void cleanSelectedEntities(final Map<String, Entity> selectedEntities, final Set<String> pathsToUpdate);

    void addComponentTranslations(final Map<String, String> translationsMap, final TranslationService translationService,
            final Locale locale);

    ViewValue<Object> castValue(Map<String, Entity> selectedEntities, JSONObject viewObject) throws JSONException;
}
