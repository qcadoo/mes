/**
 * ********************************************************************
 * Code developed by amazing QCADOO developers team.
 * Copyright (c) Qcadoo Limited sp. z o.o. (2010)
 * ********************************************************************
 */

package com.qcadoo.mes.viewold.components;

import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.json.JSONException;
import org.json.JSONObject;

import com.qcadoo.mes.api.Entity;
import com.qcadoo.mes.api.TranslationService;
import com.qcadoo.mes.view.ComponentOption;
import com.qcadoo.mes.viewold.AbstractComponent;
import com.qcadoo.mes.viewold.ContainerComponent;
import com.qcadoo.mes.viewold.ViewValue;

/**
 * Represents static page element. Inside this element is content from another html page. <br/>
 * <br/>
 * Options:
 * <ul>
 * <li>
 * page - String - URI to page with content should be include as content of this component</li>
 * </ul>
 */
public final class StaticPageComponent extends AbstractComponent<Object> {

    public StaticPageComponent(final String name, final ContainerComponent<?> parentContainer, final String fieldPath,
            final String sourceFieldPath, final TranslationService translationService) {
        super(name, parentContainer, fieldPath, sourceFieldPath, translationService);
    }

    @Override
    public String getType() {
        return "staticPage";
    }

    @Override
    public void initializeComponent() {
        for (ComponentOption option : getRawOptions()) {
            if ("page".equals(option.getType())) {
                addOption("page", option.getValue());
            }
        }
    }

    @Override
    public final ViewValue<Object> castComponentValue(final Map<String, Entity> selectedEntities, final JSONObject viewObject)
            throws JSONException {
        return new ViewValue<Object>(null);
    }

    @Override
    public final ViewValue<Object> getComponentValue(final Entity entity, final Entity parentEntity,
            final Map<String, Entity> selectedEntities, final ViewValue<Object> viewValue, final Set<String> pathsToUpdate,
            final Locale locale) {
        return new ViewValue<Object>(null);
    }
}
