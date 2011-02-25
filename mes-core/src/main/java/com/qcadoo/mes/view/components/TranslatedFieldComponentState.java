package com.qcadoo.mes.view.components;

import org.json.JSONException;
import org.json.JSONObject;

import com.qcadoo.mes.model.FieldDefinition;
import com.qcadoo.mes.model.types.internal.EnumType;

public class TranslatedFieldComponentState extends FieldComponentState {

    private final FieldComponentPattern pattern;

    public TranslatedFieldComponentState(final FieldComponentPattern pattern) {
        super(pattern);
        this.pattern = pattern;
    }

    @Override
    protected JSONObject renderContent() throws JSONException {
        JSONObject json = new JSONObject();

        String value = (String) getFieldValue();

        FieldDefinition fieldDefinition = pattern.getFieldComponentFieldDefinition();
        if (fieldDefinition != null) {
            if (EnumType.class.isAssignableFrom(fieldDefinition.getType().getClass())) {
                String code = getTranslationService().getEntityFieldBaseMessageCode(getDataDefinition(),
                        fieldDefinition.getName())
                        + ".value." + value;
                value = getTranslationService().translate(code, getLocale());
            }
        }

        json.put(JSON_VALUE, value);
        json.put(JSON_REQUIRED, isRequired());
        return json;
    }

}
