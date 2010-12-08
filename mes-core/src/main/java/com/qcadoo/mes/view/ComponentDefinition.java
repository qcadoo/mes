package com.qcadoo.mes.view;

import com.qcadoo.mes.api.TranslationService;

public final class ComponentDefinition {

    private String name;

    private String fieldPath;

    private String sourceFieldPath;

    private String reference;

    private boolean defaultEnabled = true;

    private boolean defaultVisible = true;

    private boolean hasDescription;

    private TranslationService translationService;

    private ViewDefinition viewDefinition;

    private ContainerPattern parent;

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public String getFieldPath() {
        return fieldPath;
    }

    public void setFieldPath(final String fieldPath) {
        this.fieldPath = fieldPath;
    }

    public String getSourceFieldPath() {
        return sourceFieldPath;
    }

    public void setSourceFieldPath(final String sourceFieldPath) {
        this.sourceFieldPath = sourceFieldPath;
    }

    public String getReference() {
        return reference;
    }

    public void setReference(final String reference) {
        this.reference = reference;
    }

    public boolean isDefaultEnabled() {
        return defaultEnabled;
    }

    public void setDefaultEnabled(final boolean defaultEnabled) {
        this.defaultEnabled = defaultEnabled;
    }

    public boolean isDefaultVisible() {
        return defaultVisible;
    }

    public void setDefaultVisible(final boolean defaultVisible) {
        this.defaultVisible = defaultVisible;
    }

    public boolean isHasDescription() {
        return hasDescription;
    }

    public void setHasDescription(final boolean hasDescription) {
        this.hasDescription = hasDescription;
    }

    public ContainerPattern getParent() {
        return parent;
    }

    public void setParent(final ContainerPattern parent) {
        this.parent = parent;
    }

    public TranslationService getTranslationService() {
        return translationService;
    }

    public void setTranslationService(final TranslationService translationService) {
        this.translationService = translationService;
    }

    public ViewDefinition getViewDefinition() {
        return viewDefinition;
    }

    public void setViewDefinition(final ViewDefinition viewDefinition) {
        this.viewDefinition = viewDefinition;
    }

}
