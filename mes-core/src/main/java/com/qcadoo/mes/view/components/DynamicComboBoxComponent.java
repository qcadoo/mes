package com.qcadoo.mes.view.components;

import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.json.JSONException;
import org.json.JSONObject;

import com.qcadoo.mes.api.Entity;
import com.qcadoo.mes.api.TranslationService;
import com.qcadoo.mes.model.types.EnumeratedType;
import com.qcadoo.mes.model.types.FieldType;
import com.qcadoo.mes.model.validators.ErrorMessage;
import com.qcadoo.mes.view.AbstractComponent;
import com.qcadoo.mes.view.ContainerComponent;
import com.qcadoo.mes.view.ViewValue;
import com.qcadoo.mes.view.components.combobox.ComboBoxValue;

public final class DynamicComboBoxComponent extends AbstractComponent<ComboBoxValue> {

    public DynamicComboBoxComponent(final String name, final ContainerComponent<?> parentContainer, final String fieldPath,
            final String sourceFieldPath) {
        super(name, parentContainer, fieldPath, sourceFieldPath);
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
    public ViewValue<ComboBoxValue> getComponentValue(final Entity entity, final Entity parentEntity,
            final Map<String, Entity> selectedEntities, final ViewValue<ComboBoxValue> viewValue, final Set<String> pathsToUpdate) {
        Object value = getFieldValue(entity, getFieldPath());
        String strValue;
        if (value == null) {
            strValue = null;
        } else {
            strValue = String.valueOf(value);
        }
        ComboBoxValue comboValue = new ComboBoxValue(getComboBoxValues(), strValue);
        ViewValue<ComboBoxValue> newViewValue = new ViewValue<ComboBoxValue>(comboValue);

        ErrorMessage validationError = getFieldError(entity, getFieldPath());
        if (validationError != null && validationError.getMessage() != null) {
            newViewValue.addErrorMessage(validationError.getMessage());
        }

        return newViewValue;
    }

    private List<String> getComboBoxValues() {
        FieldType def = getDataDefinition().getField(getName()).getType();
        // TODO mina
        // if (!(def instanceof DictionaryType || def instanceof EnumType)) {}
        EnumeratedType fieldDefinition = (EnumeratedType) def;
        return fieldDefinition.values();
    }

    @Override
    public void addComponentTranslations(final Map<String, String> translationsMap, final TranslationService translationService,
            final Locale locale) {
        List<String> messageCodes = new LinkedList<String>();
        messageCodes.add(getViewDefinition().getPluginIdentifier() + "." + getViewDefinition().getName() + "." + getPath()
                + ".label");
        messageCodes.add(translationService.getEntityFieldMessageCode(getDataDefinition(), getName()));
        translationsMap.put(messageCodes.get(0), translationService.translate(messageCodes, locale));
    }
}
