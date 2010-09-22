package com.qcadoo.mes.core.data.view.elements;

import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.json.JSONException;
import org.json.JSONObject;

import com.qcadoo.mes.core.data.beans.Entity;
import com.qcadoo.mes.core.data.internal.TranslationService;
import com.qcadoo.mes.core.data.internal.types.DictionaryType;
import com.qcadoo.mes.core.data.internal.types.EnumType;
import com.qcadoo.mes.core.data.types.EnumeratedFieldType;
import com.qcadoo.mes.core.data.types.FieldType;
import com.qcadoo.mes.core.data.validation.ValidationError;
import com.qcadoo.mes.core.data.view.AbstractComponent;
import com.qcadoo.mes.core.data.view.ContainerComponent;
import com.qcadoo.mes.core.data.view.ViewValue;
import com.qcadoo.mes.core.data.view.elements.comboBox.ComboBoxValue;

public class DynamicComboBox extends AbstractComponent<ComboBoxValue> {

    public DynamicComboBox(final String name, final ContainerComponent<?> parentContainer, final String fieldPath,
            final String sourceFieldPath) {
        super(name, parentContainer, fieldPath, sourceFieldPath);
    }

    @Override
    public String getType() {
        return "dynamicComboBox";
    }

    @Override
    public void addComponentOptions(final Map<String, Object> viewOptions) {

    }

    @Override
    public ViewValue<ComboBoxValue> castComponentValue(final Entity entity, final Map<String, Entity> selectedEntities,
            final JSONObject viewObject) throws JSONException {
        JSONObject valueObject = viewObject.getJSONObject("value");
        String value = null;
        if (!valueObject.isNull("selectedValue")) {
            value = valueObject.getString("selectedValue");
        }
        if (value != null) {
            return new ViewValue<ComboBoxValue>(new ComboBoxValue(getComboBoxValues(), value));
        } else {
            return new ViewValue<ComboBoxValue>();
        }
    }

    @Override
    public ViewValue<ComboBoxValue> getComponentValue(final Entity entity, final Map<String, Entity> selectedEntities,
            final ViewValue<ComboBoxValue> viewEntity, final Set<String> pathsToUpdate) {
        Object value = getFieldValue(entity, getFieldPath());
        String strValue;
        if (value == null) {
            strValue = "";
        } else {
            strValue = String.valueOf(value);
        }
        ComboBoxValue comboValue = new ComboBoxValue(getComboBoxValues(), strValue);
        ViewValue<ComboBoxValue> viewValue = new ViewValue<ComboBoxValue>(comboValue);

        ValidationError validationError = getFieldError(entity, getFieldPath());
        if (validationError != null && validationError.getMessage() != null) {
            viewValue.addErrorMessage(validationError.getMessage());
        }

        return viewValue;
    }

    private List<String> getComboBoxValues() {
        FieldType def = getDataDefinition().getField(getName()).getType();
        if (!(def instanceof DictionaryType || def instanceof EnumType)) {
            // TODO mina
        }
        EnumeratedFieldType fieldDefinition = (EnumeratedFieldType) def;
        return fieldDefinition.values();
    }

    @Override
    public void addComponentTranslations(final Map<String, String> translationsMap, final TranslationService translationService,
            final Locale locale) {
        List<String> messageCodes = new LinkedList<String>();
        messageCodes.add(getViewName() + "." + getPath() + ".label");
        messageCodes.add(getDataDefinition().getName() + "." + getName() + ".label");
        translationsMap.put(messageCodes.get(0), translationService.translate(messageCodes, locale));
    }
}
