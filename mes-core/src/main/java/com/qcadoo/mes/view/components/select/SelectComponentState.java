package com.qcadoo.mes.view.components.select;

import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.common.collect.Lists;
import com.qcadoo.mes.model.FieldDefinition;
import com.qcadoo.mes.model.types.BelongsToType;
import com.qcadoo.mes.model.types.EnumeratedType;
import com.qcadoo.mes.view.components.FieldComponentState;

public final class SelectComponentState extends FieldComponentState {

    private final FieldDefinition fieldDefinition;

    public SelectComponentState(final FieldDefinition fieldDefinition) {
        this.fieldDefinition = fieldDefinition;
    }

    @Override
    protected JSONObject renderContent() throws JSONException {
        JSONObject json = super.renderContent();
        JSONArray values = new JSONArray();
        values.put(getBlankValue());

        if (EnumeratedType.class.isAssignableFrom(fieldDefinition.getType().getClass())) {
            List<String> vals = ((EnumeratedType) fieldDefinition.getType()).values();
            for (String val : vals) {
                values.put(getValue(val, val));
            }
        } else if (BelongsToType.class.isAssignableFrom(fieldDefinition.getType().getClass())) {
            throw new IllegalStateException("Select for belongsTo type is not supported");
        } else {
            throw new IllegalStateException("Select for " + fieldDefinition.getType().getClass().getSimpleName()
                    + " type is not supported");
        }

        json.put("values", values);
        return json;
    }

    private JSONObject getBlankValue() throws JSONException {
        JSONObject json = new JSONObject();
        json.put("key", "");
        List<String> codes = Lists.newArrayList(getTranslationPath() + ".blankValue", "core.form.blankComboBoxValue");
        json.put("value", getTranslationService().translate(codes, getLocale()));
        return json;
    }

    private JSONObject getValue(final String key, final String value) throws JSONException {
        JSONObject json = new JSONObject();
        json.put("key", key);
        String code = getTranslationService().getEntityFieldBaseMessageCode(getDataDefinition(), fieldDefinition.getName())
                + ".value." + value;
        json.put("value", getTranslationService().translate(code, getLocale()));
        return json;
    }

}