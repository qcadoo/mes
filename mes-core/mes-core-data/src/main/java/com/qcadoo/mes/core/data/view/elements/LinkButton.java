package com.qcadoo.mes.core.data.view.elements;

import java.util.Map;
import java.util.Set;

import org.json.JSONException;
import org.json.JSONObject;

import com.qcadoo.mes.core.data.beans.Entity;
import com.qcadoo.mes.core.data.view.AbstractComponent;
import com.qcadoo.mes.core.data.view.ContainerComponent;
import com.qcadoo.mes.core.data.view.ViewValue;

public class LinkButton extends AbstractComponent<String> {

    private String pageUrl;

    public LinkButton(final String name, final ContainerComponent<?> parentContainer, final String pageUrl,
            final String sourceFieldPath) {
        super(name, parentContainer, null, sourceFieldPath);
        this.pageUrl = pageUrl;
    }

    public LinkButton(final String name, final ContainerComponent<?> parentContainer, final String pageUrl) {
        this(name, parentContainer, pageUrl, null);
    }

    @Override
    public String getType() {
        return "linkButton";
    }

    @Override
    public void addComponentOptions(final Map<String, Object> viewOptions) {
        viewOptions.put("pageUrl", pageUrl);
    }

    @Override
    public ViewValue<String> castComponentValue(final Entity entity, final Map<String, Entity> selectedEntities,
            final JSONObject viewObject) throws JSONException {
        return null;
    }

    @Override
    public ViewValue<String> getComponentValue(final Entity entity, final Map<String, Entity> selectedEntities,
            final ViewValue<String> viewEntity, final Set<String> pathsToUpdate) {
        if (entity != null) {
            return new ViewValue<String>(pageUrl + "?entityId=" + entity.getId());
        }
        return new ViewValue<String>(pageUrl);
    }

    public String getPageUrl() {
        return pageUrl;
    }

    public void setPageUrl(String pageUrl) {
        this.pageUrl = pageUrl;
    }
}
