package com.qcadoo.mes.core.data.view;

import java.util.Locale;
import java.util.Map;
import java.util.Set;

import com.qcadoo.mes.core.data.beans.Entity;
import com.qcadoo.mes.core.data.internal.TranslationService;
import com.qcadoo.mes.core.data.model.DataDefinition;

public interface Component<T> extends CastableComponent<T>, ListenableComponent, InitializableComponent {

    String getType();

    ViewValue<T> getValue(Entity entity, Map<String, Entity> selectedEntities, ViewValue<?> viewValue, Set<String> pathsToUpdate);

    String getName();

    String getSourceFieldPath();

    String getPath();

    String getViewName();

    String getFieldPath();

    DataDefinition getDataDefinition();

    void updateTranslations(Map<String, String> translationsMap, final TranslationService translationService, final Locale locale);

    public boolean isDefaultEnabled();

    public void setDefaultEnabled(boolean defaultEnabled);

    public boolean isDefaultVisible();

    public void setDefaultVisible(boolean defaultVisible);

}