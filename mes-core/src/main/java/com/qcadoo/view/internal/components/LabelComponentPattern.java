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

package com.qcadoo.view.internal.components;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.json.JSONException;

import com.qcadoo.view.internal.ComponentDefinition;
import com.qcadoo.view.internal.ComponentOption;
import com.qcadoo.view.internal.ComponentState;
import com.qcadoo.view.internal.ViewComponent;
import com.qcadoo.view.internal.patterns.AbstractComponentPattern;

@ViewComponent("label")
public class LabelComponentPattern extends AbstractComponentPattern {

    private static final String JS_OBJECT = "QCD.components.elements.Label";

    private static final String JSP_PATH = "elements/label.jsp";

    private static final String JS_PATH = "/js/crud/qcd/components/elements/label.js";

    private String labelStyle = "normal";

    public LabelComponentPattern(final ComponentDefinition componentDefinition) {
        super(componentDefinition);
    }

    @Override
    protected void initializeComponent() throws JSONException {
        for (ComponentOption option : getOptions()) {
            if ("labelStyle".equals(option.getType())) {
                labelStyle = option.getValue();
                if (!"normal".equals(labelStyle) && !"text".equals(labelStyle)) {
                    throw new IllegalStateException("unknown label style: " + labelStyle);
                }
            }
        }
    }

    @Override
    protected Map<String, Object> getJspOptions(final Locale locale) {
        Map<String, Object> options = new HashMap<String, Object>();
        Map<String, Object> translations = new HashMap<String, Object>();
        translations.put("label", getTranslationService().translate(getTranslationPath() + ".label", locale));
        options.put("translations", translations);
        options.put("labelStyle", labelStyle);
        return options;
    }

    @Override
    protected ComponentState getComponentStateInstance() {
        return new EmptyComponentState();
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
