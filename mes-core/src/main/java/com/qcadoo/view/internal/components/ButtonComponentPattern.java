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

import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.components.ButtonComponentState;
import com.qcadoo.view.internal.ComponentDefinition;
import com.qcadoo.view.internal.ComponentOption;
import com.qcadoo.view.internal.ViewComponent;
import com.qcadoo.view.internal.patterns.AbstractComponentPattern;

@ViewComponent("button")
public final class ButtonComponentPattern extends AbstractComponentPattern {

    private static final String JS_OBJECT = "QCD.components.elements.LinkButton";

    private static final String JSP_PATH = "elements/button.jsp";

    private String url;

    private String correspondingView;

    private String correspondingComponent;

    private boolean correspondingViewInModal = false;

    public ButtonComponentPattern(final ComponentDefinition componentDefinition) {
        super(componentDefinition);
    }

    @Override
    protected void initializeComponent() throws JSONException {
        super.initializeComponent();
        for (ComponentOption option : getOptions()) {
            if ("url".equals(option.getType())) {
                url = option.getValue();
            } else if ("correspondingView".equals(option.getType())) {
                correspondingView = option.getValue();
            } else if ("correspondingComponent".equals(option.getType())) {
                correspondingComponent = option.getValue();
            } else if ("correspondingViewInModal".equals(option.getType())) {
                correspondingViewInModal = Boolean.parseBoolean(option.getValue());
            } else {
                throw new IllegalStateException("Unknown option for button: " + option.getType());
            }
        }

        if (url == null && (correspondingView == null || correspondingComponent == null)) {
            throw new IllegalStateException("Missing url or correspondingComponent for button");
        }
    }

    @Override
    public ComponentState getComponentStateInstance() {
        if (url != null) {
            return new ButtonComponentState(url);
        } else {
            return new ButtonComponentState(correspondingView, correspondingComponent, correspondingViewInModal,
                    getScopeFieldDefinition() != null ? getScopeFieldDefinition().getName() : "id");
        }
    }

    @Override
    protected Map<String, Object> getJspOptions(final Locale locale) {
        Map<String, Object> options = new HashMap<String, Object>();
        Map<String, Object> translations = new HashMap<String, Object>();
        translations.put("label", getTranslationService().translate(getTranslationPath() + ".label", locale));
        options.put("translations", translations);
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
