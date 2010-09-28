package com.qcadoo.mes.core.view.elements;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Map;
import java.util.Set;

import org.json.JSONException;
import org.json.JSONObject;

import com.qcadoo.mes.core.api.Entity;
import com.qcadoo.mes.core.view.AbstractComponent;
import com.qcadoo.mes.core.view.ComponentOption;
import com.qcadoo.mes.core.view.ContainerComponent;
import com.qcadoo.mes.core.view.ViewValue;

public final class LinkButtonComponent extends AbstractComponent<String> {

    private String url;

    public LinkButtonComponent(final String name, final ContainerComponent<?> parentContainer, final String fieldPath,
            final String sourceFieldPath) {
        super(name, parentContainer, fieldPath, sourceFieldPath);
    }

    @Override
    public String getType() {
        return "linkButton";
    }

    @Override
    public void initializeComponent() {
        for (ComponentOption option : getRawOptions()) {
            if ("url".equals(option.getType())) {
                url = option.getValue();
                addOption("url", url);
            }
        }

        checkNotNull(url, "Url must be given");
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
            return new ViewValue<String>(url + "?entityId=" + entity.getId());
        }
        return new ViewValue<String>(url);
    }

}
