package com.qcadoo.mes.view.components.select;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.common.collect.Lists;
import com.qcadoo.mes.model.types.BelongsToType;
import com.qcadoo.mes.model.types.EnumeratedType;
import com.qcadoo.mes.model.types.internal.DictionaryType;
import com.qcadoo.mes.model.types.internal.EnumType;
import com.qcadoo.mes.view.ComponentDefinition;
import com.qcadoo.mes.view.ComponentState;
import com.qcadoo.mes.view.ViewComponent;
import com.qcadoo.mes.view.components.FieldComponentPattern;

@ViewComponent("select")
public final class SelectComponentPattern extends FieldComponentPattern {

    private static final String JSP_PATH = "elements/select.jsp";

    private static final String JS_OBJECT = "QCD.components.elements.DynamicComboBox";

    public SelectComponentPattern(final ComponentDefinition componentDefinition) {
        super(componentDefinition);
    }

    @Override
    public ComponentState getComponentStateInstance() {
        return new SelectComponentState(this);
    }

    public Map<String, String> getValuesMap(final Locale locale) {
        Map<String, String> values = new LinkedHashMap<String, String>();

        if (!isRequired() || getFieldDefinition().getDefaultValue() == null) {
            List<String> blankCodes = Lists.newArrayList(getTranslationPath() + ".blankValue", "core.form.blankComboBoxValue");
            values.put("", getTranslationService().translate(blankCodes, locale));
        }

        if (EnumType.class.isAssignableFrom(getFieldDefinition().getType().getClass())) {
            List<String> vals = ((EnumeratedType) getFieldDefinition().getType()).values();
            for (String val : vals) {
                String code = getTranslationService().getEntityFieldBaseMessageCode(getDataDefinition(),
                        getFieldDefinition().getName())
                        + ".value." + val;
                values.put(val, getTranslationService().translate(code, locale));
            }
        } else if (DictionaryType.class.isAssignableFrom(getFieldDefinition().getType().getClass())) {
            List<String> vals = ((EnumeratedType) getFieldDefinition().getType()).values();
            for (String val : vals) {
                values.put(val, val);
            }
        } else if (BelongsToType.class.isAssignableFrom(getFieldDefinition().getType().getClass())) {
            throw new IllegalStateException("Select for belongsTo type is not supported");
        } else {
            throw new IllegalStateException("Select for " + getFieldDefinition().getType().getClass().getSimpleName()
                    + " type is not supported");
        }
        return values;
    }

    public JSONArray getValuesJson(final Locale locale) throws JSONException {
        JSONArray values = new JSONArray();
        for (Map.Entry<String, String> valueEntry : getValuesMap(locale).entrySet()) {
            JSONObject obj = new JSONObject();
            obj.put("key", valueEntry.getKey());
            obj.put("value", valueEntry.getValue());
            values.put(obj);
        }
        return values;
    }

    @Override
    protected Map<String, Object> getJspOptions(final Locale locale) {
        Map<String, Object> options = super.getJspOptions(locale);
        options.put("values", getValuesMap(locale));
        return options;
    }

    @Override
    public String getJspFilePath() {
        return JSP_PATH;
    }

    @Override
    public String getJsFilePath() {
        return JS_PATH;
    }

    @Override
    public String getJsObjectName() {
        return JS_OBJECT;
    }
}
