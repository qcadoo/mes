package com.qcadoo.mes.core.data.internal.view;

import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.json.JSONException;
import org.json.JSONObject;

import com.qcadoo.mes.core.data.beans.Entity;
import com.qcadoo.mes.core.data.internal.TranslationService;
import com.qcadoo.mes.core.data.view.RootComponent;
import com.qcadoo.mes.core.data.view.ViewDefinition;
import com.qcadoo.mes.core.data.view.ViewValue;

public final class ViewDefinitionImpl implements ViewDefinition {

    private final RootComponent root;

    private final String pluginIdentifier;

    private final String name;

    public ViewDefinitionImpl(final String name, final RootComponent root, final String pluginIdentifier) {
        this.name = name;
        this.root = root;
        this.pluginIdentifier = pluginIdentifier;
    }

    @Override
    public String getPluginIdentifier() {
        return pluginIdentifier;
    }

    @Override
    public RootComponent getRoot() {
        return root;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public ViewValue<Object> castValue(final Entity entity, final Map<String, Entity> selectedEntities,
            final JSONObject viewObject) throws JSONException {
        return wrapIntoViewValue(root.castValue(entity, selectedEntities,
                viewObject != null ? viewObject.getJSONObject(root.getName()) : null));
    }

    @Override
    public ViewValue<Object> getValue(final Entity entity, final Map<String, Entity> selectedEntities,
            final ViewValue<Object> globalViewEntity, final Set<String> pathsToUpdate) {
        return wrapIntoViewValue(root.getValue(entity, selectedEntities,
                globalViewEntity != null ? globalViewEntity.getComponent(root.getName()) : null, pathsToUpdate));
    }

    @Override
    public void addComponentTranslations(final Map<String, String> translationsMap, final TranslationService translationService,
            final Locale locale) {
        root.updateTranslations(translationsMap, translationService, locale);
    }

    private ViewValue<Object> wrapIntoViewValue(final ViewValue<?> viewValue) {
        ViewValue<Object> value = new ViewValue<Object>();
        value.addComponent(root.getName(), viewValue);
        return value;
    }

}
