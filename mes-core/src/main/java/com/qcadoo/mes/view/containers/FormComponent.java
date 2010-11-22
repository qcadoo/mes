/**
 * ********************************************************************
 * Code developed by amazing QCADOO developers team.
 * Copyright (c) Qcadoo Limited sp. z o.o. (2010)
 * ********************************************************************
 */

package com.qcadoo.mes.view.containers;

import java.util.Arrays;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.util.StringUtils;

import com.qcadoo.mes.api.Entity;
import com.qcadoo.mes.api.TranslationService;
import com.qcadoo.mes.internal.DefaultEntity;
import com.qcadoo.mes.model.FieldDefinition;
import com.qcadoo.mes.model.types.HasManyType;
import com.qcadoo.mes.model.validators.ErrorMessage;
import com.qcadoo.mes.utils.ExpressionUtil;
import com.qcadoo.mes.view.AbstractContainerComponent;
import com.qcadoo.mes.view.Component;
import com.qcadoo.mes.view.ComponentOption;
import com.qcadoo.mes.view.ContainerComponent;
import com.qcadoo.mes.view.SaveableComponent;
import com.qcadoo.mes.view.SelectableComponent;
import com.qcadoo.mes.view.ViewValue;
import com.qcadoo.mes.view.components.LookupData;
import com.qcadoo.mes.view.components.SimpleValue;

/**
 * Represents form.<br/>
 * <br/>
 * XML declaration: <br/>
 * 
 * <pre>
 *      {@code <component type="form" name="{identifier of component}">}
 * </pre>
 * 
 * XML options:
 * <ul>
 * <li>expression - String - expression that defines text visible on header</li>
 * <li>header - [true | false] - true when form contains header</li>
 * </ul>
 */
public final class FormComponent extends AbstractContainerComponent<FormValue> implements SaveableComponent, SelectableComponent {

    private boolean header = true;

    private String expression = "#id";

    public FormComponent(final String name, final ContainerComponent<?> parentContainer, final String fieldPath,
            final String sourceFieldPath, final TranslationService translationService) {
        super(name, parentContainer, fieldPath, sourceFieldPath, translationService);
    }

    @Override
    public String getType() {
        return "form";
    }

    @Override
    public void initializeComponent() {
        for (ComponentOption option : getRawOptions()) {
            if ("header".equals(option.getType())) {
                header = Boolean.parseBoolean(option.getValue());
            }
            if ("expression".equals(option.getType())) {
                expression = option.getValue();
            }
        }

        addOption("header", header);
    }

    @Override
    public FormValue castContainerValue(final Map<String, Entity> selectedEntities, final JSONObject viewObject)
            throws JSONException {
        if (viewObject != null && viewObject.has("value") && !viewObject.isNull("value")
                && !viewObject.getJSONObject("value").isNull("id")) {
            Long entityId = viewObject.getJSONObject("value").getLong("id");
            Entity entity = getDataDefinition().get(entityId);

            selectedEntities.put(getPath(), entity);

            FormValue formValue = new FormValue(entityId);

            if (!viewObject.getJSONObject("value").isNull("header")) {
                formValue.setHeader(viewObject.getJSONObject("value").getString("header"));
            }
            if (!viewObject.getJSONObject("value").isNull("headerEntityIdentifier")) {
                formValue.setHeaderEntityIdentifier(viewObject.getJSONObject("value").getString("headerEntityIdentifier"));
            }
            if (!viewObject.getJSONObject("value").isNull("valid")) {
                formValue.setValid(viewObject.getJSONObject("value").getBoolean("valid"));
            }
            return formValue;
        } else {
            return null;
        }
    }

    @Override
    public FormValue getContainerValue(final Entity entity, final Map<String, Entity> selectedEntities,
            final ViewValue<FormValue> viewValue, final Set<String> pathsToUpdate, final Locale locale) {
        FormValue formValue = new FormValue();
        String messageCode = getViewDefinition().getPluginIdentifier() + "." + getViewDefinition().getName() + "." + getPath();
        if (entity != null) {
            if (entity.isValid()) {
                selectedEntities.put(getPath(), entity);
                formValue.setHeader(getTranslationService().translate(messageCode + ".headerEdit", locale));
                formValue.setHeaderEntityIdentifier(ExpressionUtil.getValue(entity, expression));
            } else {
                formValue.setHeader(getTranslationService().translate(messageCode + ".headerNew", locale));
            }
            formValue.setId(entity.getId());
            formValue.setValid(entity.isValid());
        } else {
            formValue.setHeader(getTranslationService().translate(messageCode + ".headerNew", locale));
        }

        return formValue;
    }

    @Override
    public void addContainerMessages(final Entity entity, final ViewValue<FormValue> viewValue, final Locale locale) {
        if (entity != null) {
            for (ErrorMessage validationError : entity.getGlobalErrors()) {
                viewValue.addErrorMessage(getTranslationService().translateErrorMessage(validationError, locale));
            }
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public Entity getSaveableEntity(final ViewValue<Long> viewValue) {
        ViewValue<FormValue> formValue = (ViewValue<FormValue>) lookupViewValue(viewValue);

        Long entityId = null;

        if (formValue.getValue() != null) {
            entityId = formValue.getValue().getId();
        }

        Entity entity = new DefaultEntity(getDataDefinition().getPluginIdentifier(), getDataDefinition().getName(), entityId);

        for (Map.Entry<String, Component<?>> component : getComponents().entrySet()) {
            String fieldPath = component.getValue().getFieldPath();

            if (fieldPath == null || fieldPath.split("\\.").length > 1) {
                continue;
            }

            if (getDataDefinition().getField(fieldPath).getType() instanceof HasManyType) {
                continue;
            }

            setEntityField(entity, fieldPath, component.getKey(), formValue, getDataDefinition().getField(fieldPath));
        }

        return entity;
    }

    private void setEntityField(final Entity entity, final String fieldPath, final String fieldName,
            final ViewValue<FormValue> formValue, final FieldDefinition fieldDefinition) {
        ViewValue<?> componentValue = formValue.getComponent(fieldName);
        Object value = componentValue.getValue();

        if (value == null) {
            entity.setField(fieldPath, null);
        } else if (value instanceof LookupData) {
            LookupData lookupValue = ((LookupData) value);
            if (StringUtils.hasText(lookupValue.getSelectedEntityCode()) && lookupValue.getValue() != null) {
                entity.addError(fieldDefinition, "core.validate.field.error.lookupCodeNotFound");
            } else {
                entity.setField(fieldPath, lookupValue.getValue() != null ? String.valueOf(lookupValue.getValue()) : null);
            }
        } else if (value instanceof SimpleValue) {
            Object fieldValue = ((SimpleValue) value).getValue();
            entity.setField(fieldPath, fieldValue != null ? String.valueOf(fieldValue) : null);
        } else {
            throw new IllegalStateException("Value of " + fieldPath + " doesn't extends SimpleValue");
        }
    }

    @Override
    public void addComponentTranslations(final Map<String, String> translationsMap, final Locale locale) {
        String messagePath = getViewDefinition().getPluginIdentifier() + "." + getViewDefinition().getName() + "." + getPath();
        if (header) {
            translationsMap.put(messagePath + ".header", getTranslationService().translate(messagePath + ".header", locale));
        }

        String[] formMessages = new String[] { "confirmCancelMessage", "confirmDeleteMessage", "entityWithoutIdentifier" };

        for (String formMessage : formMessages) {
            translationsMap.put(
                    messagePath + "." + formMessage,
                    getTranslationService().translate(
                            Arrays.asList(new String[] { messagePath + "." + formMessage, "core.form." + formMessage }), locale));
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public Long getSelectedEntityId(final ViewValue<Long> viewValue) {
        ViewValue<FormValue> formValue = (ViewValue<FormValue>) lookupViewValue(viewValue);
        return formValue.getValue().getId();
    }

}
