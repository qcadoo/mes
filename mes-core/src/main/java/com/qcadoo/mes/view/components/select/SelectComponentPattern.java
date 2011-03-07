/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 0.3.0
 *
 * This file is part of Qcadoo.
 *
 * Qcadoo is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation; either version 3 of the License,
 * or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 * ***************************************************************************
 */

package com.qcadoo.mes.view.components.select;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.common.collect.Lists;
import com.qcadoo.mes.view.ComponentDefinition;
import com.qcadoo.mes.view.ComponentState;
import com.qcadoo.mes.view.ViewComponent;
import com.qcadoo.mes.view.components.FieldComponentPattern;
import com.qcadoo.model.api.types.BelongsToType;
import com.qcadoo.model.api.types.EnumeratedType;

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
            String coreBlankTranslationKey = "core.form.blankComboBoxValue";
            if (isRequired()) {
                coreBlankTranslationKey = "core.form.requiredBlankComboBoxValue";
            }
            List<String> blankCodes = Lists.newArrayList(getTranslationPath() + ".blankValue", coreBlankTranslationKey);
            values.put("", getTranslationService().translate(blankCodes, locale));
        }

        if (EnumeratedType.class.isAssignableFrom(getFieldDefinition().getType().getClass())) {
            values.putAll(((EnumeratedType) getFieldDefinition().getType()).values(locale));
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
