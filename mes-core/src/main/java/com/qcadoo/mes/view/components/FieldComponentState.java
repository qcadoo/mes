package com.qcadoo.mes.view.components;

import org.json.JSONException;
import org.json.JSONObject;

import com.qcadoo.mes.model.FieldDefinition;
import com.qcadoo.mes.model.types.internal.EnumType;
import com.qcadoo.mes.view.states.AbstractComponentState;

public class FieldComponentState extends AbstractComponentState {

    public static final String JSON_REQUIRED = "required";

    private String value;

    private final boolean defaultRequired;

    private boolean required;

    private final FieldComponentPattern pattern;

    public FieldComponentState(final FieldComponentPattern pattern) {
        this.pattern = pattern;
        defaultRequired = pattern.isRequired();
    }

    @Override
    protected void initializeContent(final JSONObject json) throws JSONException {
        if (json.has(JSON_VALUE) && !json.isNull(JSON_VALUE)) {
            value = json.getString(JSON_VALUE);
        }
        if (json.has(JSON_REQUIRED) && !json.isNull(JSON_REQUIRED)) {
            required = json.getBoolean(JSON_REQUIRED);
        }
    }

    @Override
    protected JSONObject renderContent() throws JSONException {
        JSONObject json = new JSONObject();

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

    @Override
    public void setFieldValue(final Object value) {
        this.value = value != null ? value.toString() : null;
        requestRender();
    }

    @Override
    public Object getFieldValue() {
        return value;
    }

    public final boolean isRequired() {
        return required || defaultRequired;
    }

    public void setRequired(final boolean required) {
        this.required = required;
        requestRender();
    }

    public void requestComponentUpdateState() {
        requestUpdateState();
    }

}
