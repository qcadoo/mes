/**
 * ********************************************************************
 * Code developed by amazing QCADOO developers team.
 * Copyright (c) Qcadoo Limited sp. z o.o. (2010)
 * ********************************************************************
 */

package com.qcadoo.mes.viewold.components;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.json.JSONException;
import org.json.JSONObject;

import com.qcadoo.mes.api.Entity;
import com.qcadoo.mes.api.TranslationService;
import com.qcadoo.mes.model.FieldDefinition;
import com.qcadoo.mes.model.types.EnumeratedType;
import com.qcadoo.mes.model.types.FieldType;
import com.qcadoo.mes.model.types.internal.EnumType;
import com.qcadoo.mes.model.validators.ErrorMessage;
import com.qcadoo.mes.utils.Pair;
import com.qcadoo.mes.viewold.AbstractComponent;
import com.qcadoo.mes.viewold.ContainerComponent;
import com.qcadoo.mes.viewold.ViewValue;
import com.qcadoo.mes.viewold.components.combobox.ComboBoxValue;

/**
 * Represents comboBox element.
 */
public final class DynamicComboBoxComponent extends AbstractComponent<ComboBoxValue> {

    public DynamicComboBoxComponent(final String name, final ContainerComponent<?> parentContainer, final String fieldPath,
            final String sourceFieldPath, final TranslationService translationService) {
        super(name, parentContainer, fieldPath, sourceFieldPath, translationService);
    }

    @Override
    public String getType() {
        return "dynamicComboBox";
    }

    @Override
    public ViewValue<ComboBoxValue> castComponentValue(final Map<String, Entity> selectedEntities, final JSONObject viewObject)
            throws JSONException {
        JSONObject valueObject = viewObject.getJSONObject("value");

        String value = null;
        if (!valueObject.isNull("value")) {
            value = valueObject.getString("value");
        }
        if (value != null) {
            ComboBoxValue comboBoxValue = new ComboBoxValue(getComboBoxValues(null), value);
            if (!viewObject.isNull("value") && !viewObject.getJSONObject("value").isNull("required")) {
                comboBoxValue.setRequired(viewObject.getJSONObject("value").getBoolean("required"));
            }
            return new ViewValue<ComboBoxValue>(comboBoxValue);
        } else {
            return new ViewValue<ComboBoxValue>();
        }
    }

    @Override
    public ViewValue<ComboBoxValue> getComponentValue(final Entity entity, final Entity parentEntity,
            final Map<String, Entity> selectedEntities, final ViewValue<ComboBoxValue> viewValue,
            final Set<String> pathsToUpdate, final Locale locale) {
        Object value = getFieldValue(entity, getFieldPath());

        String strValue;

        // combobox always must be submitted - we need to populate options, but don't change selected value if ignoreMode is true
        if (viewValue != null && viewValue.getValue() != null && viewValue.isIgnoreMode()) {
            strValue = viewValue.getValue().getSelectedValue();
        } else {
            if (value == null) {
                strValue = null;
            } else {
                strValue = String.valueOf(value);
            }
        }

        ComboBoxValue comboValue = new ComboBoxValue(getComboBoxValues(locale), strValue);
        ViewValue<ComboBoxValue> newViewValue = new ViewValue<ComboBoxValue>(comboValue);

        FieldDefinition fieldDefinition = getFieldDefinition();

        if (fieldDefinition.isRequired() || (entity == null && fieldDefinition.isRequiredOnCreate())) {
            newViewValue.getValue().setRequired(true);
        }

        if (fieldDefinition.isReadOnly() || (entity != null && fieldDefinition.isReadOnlyOnUpdate())) {
            newViewValue.setEnabled(false);
        }

        ErrorMessage validationError = getFieldError(entity, getFieldPath());
        if (validationError != null && validationError.getMessage() != null) {
            newViewValue.addErrorMessage(getTranslationService().translateErrorMessage(validationError, locale));
        }

        return newViewValue;
    }

    private List<Pair<String, String>> getComboBoxValues(final Locale locale) {
        FieldType def = getDataDefinition().getField(getName()).getType();
        // TODO mina check
        // if (!(def instanceof DictionaryType || def instanceof EnumType)) {}
        EnumeratedType fieldDefinition = (EnumeratedType) def;
        List<Pair<String, String>> values = new LinkedList<Pair<String, String>>();
        for (String valueKey : fieldDefinition.values()) {
            if (def instanceof EnumType && locale != null) {
                values.add(Pair.of(valueKey, translateComboBoxValue(valueKey, locale)));
            } else {
                values.add(Pair.of(valueKey, valueKey));
            }
        }
        return values;
    }

    private String translateComboBoxValue(final String valueKey, final Locale locale) {
        String messageCode = getTranslationService().getEntityFieldBaseMessageCode(getDataDefinition(), getName()) + ".value."
                + valueKey;
        return getTranslationService().translate(messageCode, locale);
    }

    @Override
    public void addComponentTranslations(final Map<String, String> translationsMap, final Locale locale) {
        List<String> messageCodes = new LinkedList<String>();
        messageCodes.add(getViewDefinition().getPluginIdentifier() + "." + getViewDefinition().getName() + "." + getPath()
                + ".label");
        messageCodes.add(getTranslationService().getEntityFieldBaseMessageCode(getDataDefinition(), getName()) + ".label");
        translationsMap.put(messageCodes.get(0), getTranslationService().translate(messageCodes, locale));
        if (isHasDescription()) {
            String descriptionCode = getViewDefinition().getPluginIdentifier() + "." + getViewDefinition().getName() + "."
                    + getPath() + ".description";
            translationsMap.put(descriptionCode, getTranslationService().translate(descriptionCode, locale));
            String descriptionHeaderCode = getViewDefinition().getPluginIdentifier() + "." + getViewDefinition().getName() + "."
                    + getPath() + ".descriptionHeader";
            translationsMap.put(
                    descriptionHeaderCode,
                    getTranslationService().translate(
                            Arrays.asList(new String[] { descriptionHeaderCode, "core.form.descriptionHeader" }), locale));
        }
        translationsMap
                .put(getViewDefinition().getPluginIdentifier() + "." + getViewDefinition().getName() + "." + getPath()
                        + ".blankValue",
                        getTranslationService().translate(
                                Arrays.asList(new String[] {
                                        getViewDefinition().getPluginIdentifier() + "." + getViewDefinition().getName() + "."
                                                + getPath() + ".blankValue", "core.form.blankComboBoxValue" }), locale));

    }
}
