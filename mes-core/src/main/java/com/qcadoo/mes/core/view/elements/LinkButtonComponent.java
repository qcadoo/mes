package com.qcadoo.mes.core.view.elements;

import java.util.Map;
import java.util.Set;

import org.json.JSONException;
import org.json.JSONObject;

import com.qcadoo.mes.core.api.Entity;
import com.qcadoo.mes.core.view.AbstractComponent;
import com.qcadoo.mes.core.view.ContainerComponent;
import com.qcadoo.mes.core.view.ViewValue;

public final class LinkButtonComponent extends AbstractComponent<String> {

    private String pageUrl;

    public LinkButtonComponent(final String name, final ContainerComponent<?> parentContainer, final String fieldPath,
            final String sourceFieldPath) {
        super(name, parentContainer, null, sourceFieldPath);
    }

    public LinkButtonComponent(final String name, final ContainerComponent<?> parentContainer, final String pageUrl) {
        this(name, parentContainer, pageUrl, null);
    }

    @Override
    public String getType() {
        return "linkButton";
    }

    @Override
    public void addComponentOption(final String name, final String value) {
        if ("url".equals(name)) {
            pageUrl = value;
        }
    }

    @Override
    public void getComponentOptions(final Map<String, Object> viewOptions) {
        viewOptions.put("pageUrl", pageUrl);
    }

    @Override
    public ViewValue<String> castComponentValue(final Map<String, Entity> selectedEntities, final JSONObject viewObject)
            throws JSONException {
        return null;
    }

    @Override
    public ViewValue<String> getComponentValue(final Entity entity, final Entity parentEntity,
            final Map<String, Entity> selectedEntities, final ViewValue<String> viewValue, final Set<String> pathsToUpdate) {
        if (entity != null) {
            return new ViewValue<String>(pageUrl + "?entityId=" + entity.getId());
        }
        return new ViewValue<String>(pageUrl);
    }

    public String getPageUrl() {
        return pageUrl;
    }

    public void setPageUrl(final String pageUrl) {
        this.pageUrl = pageUrl;
    }
}
