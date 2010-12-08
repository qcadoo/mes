package com.qcadoo.mes.view.patterns;

import java.lang.reflect.Method;
import java.util.Locale;

import org.json.JSONObject;

import com.qcadoo.mes.view.ComponentDefinition;
import com.qcadoo.mes.view.ComponentPattern;
import com.qcadoo.mes.view.ContainerPattern;
import com.qcadoo.mes.view.ViewDefinition;

public abstract class AbstractPatternTest {

    public ComponentDefinition getComponentDefinition(final String name, final ViewDefinition viewDefinition) {
        return getComponentDefinition(name, null, null, null, viewDefinition);
    }

    public ComponentDefinition getComponentDefinition(final String name, final ContainerPattern parent,
            final ViewDefinition viewDefinition) {
        return getComponentDefinition(name, null, null, parent, viewDefinition);
    }

    public ComponentDefinition getComponentDefinition(final String name, final String fieldPath, final String sourceFieldPath,
            final ContainerPattern parent, final ViewDefinition viewDefinition) {
        ComponentDefinition componentDefinition = new ComponentDefinition();
        componentDefinition.setName(name);
        componentDefinition.setFieldPath(fieldPath);
        componentDefinition.setSourceFieldPath(sourceFieldPath);
        componentDefinition.setParent(parent);
        componentDefinition.setViewDefinition(viewDefinition);
        return componentDefinition;
    }

    public JSONObject getJsOptions(final ComponentPattern pattern) throws Exception {
        Method method = AbstractComponentPattern.class.getDeclaredMethod("getJsOptions", Locale.class);
        method.setAccessible(true);
        return (JSONObject) method.invoke(pattern, Locale.ENGLISH);
    }

}
