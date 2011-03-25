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

package com.qcadoo.view.internal.components.form;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import org.json.JSONException;
import org.json.JSONObject;

import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.components.form.FormComponentState;
import com.qcadoo.view.internal.ComponentDefinition;
import com.qcadoo.view.internal.ComponentOption;
import com.qcadoo.view.internal.ViewComponent;
import com.qcadoo.view.internal.patterns.AbstractContainerPattern;

@ViewComponent("form")
public final class FormComponentPattern extends AbstractContainerPattern {

    private static final String JSP_PATH = "containers/form.jsp";

    private static final String JS_OBJECT = "QCD.components.containers.Form";

    private boolean header;

    private String expressionEdit = "#id";

    private String expressionNew;

    public FormComponentPattern(final ComponentDefinition componentDefinition) {
        super(componentDefinition);
    }

    @Override
    protected void initializeComponent() throws JSONException {
        for (ComponentOption option : getOptions()) {
            if ("expression".equals(option.getType())) {
                expressionEdit = option.getValue();
            } else if ("expressionNew".equals(option.getType())) {
                expressionNew = option.getValue();
            } else if ("header".equals(option.getType())) {
                header = Boolean.parseBoolean(option.getValue());
            } else {
                throw new IllegalStateException("Unknown option for form: " + option.getType());
            }
        }
    }

    @Override
    protected JSONObject getJsOptions(final Locale locale) throws JSONException {
        JSONObject json = new JSONObject();
        json.put("header", header);

        JSONObject translations = new JSONObject();

        addTranslation(translations, "confirmCancelMessage", locale);
        addTranslation(translations, "confirmDeleteMessage", locale);
        addTranslation(translations, "entityWithoutIdentifier", locale);

        translations.put("loading", getTranslationService().translate("commons.loading", locale));

        json.put("translations", translations);

        return json;
    }

    private void addTranslation(final JSONObject translation, final String key, final Locale locale) throws JSONException {
        List<String> codes = Arrays.asList(new String[] { getTranslationPath() + "." + key, "core.form." + key });
        translation.put(key, getTranslationService().translate(codes, locale));
    }

    @Override
    public ComponentState getComponentStateInstance() {
        return new FormComponentState(expressionNew, expressionEdit);
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
