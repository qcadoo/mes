package com.qcadoo.mes.core.view;

import java.util.Locale;
import java.util.Map;
import java.util.Set;

import com.qcadoo.mes.core.api.Entity;
import com.qcadoo.mes.core.internal.TranslationService;

public interface ViewDefinition extends CastableComponent<Object> {

    String getName();

    String getPluginIdentifier();

    RootComponent getRoot();

    ViewValue<Object> getValue(final Entity entity, final Map<String, Entity> selectedEntities,
            final ViewValue<Object> globalViewValue, final Set<String> pathsToUpdate, String triggerComponentName);

    void cleanSelectedEntities(final Map<String, Entity> selectedEntities, final Set<String> pathsToUpdate);

    void addComponentTranslations(final Map<String, String> translationsMap, final TranslationService translationService,
            final Locale locale);
}
