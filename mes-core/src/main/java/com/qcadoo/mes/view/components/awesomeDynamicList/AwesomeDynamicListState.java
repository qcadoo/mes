package com.qcadoo.mes.view.components.awesomeDynamicList;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.qcadoo.mes.api.Entity;
import com.qcadoo.mes.view.ComponentState;
import com.qcadoo.mes.view.ContainerState;
import com.qcadoo.mes.view.ViewDefinitionState;
import com.qcadoo.mes.view.components.FieldComponentState;
import com.qcadoo.mes.view.components.form.FormComponentPattern;
import com.qcadoo.mes.view.components.form.FormComponentState;
import com.qcadoo.mes.view.internal.ViewDefinitionStateImpl;

public class AwesomeDynamicListState extends FieldComponentState implements ContainerState {

    public static final String JSON_FORM_VALUES = "forms";

    private final FormComponentPattern innerFormPattern;

    private List<FormComponentState> forms;

    public AwesomeDynamicListState(final FormComponentPattern innerFormPattern) {
        this.innerFormPattern = innerFormPattern;
    }

    @Override
    protected void initializeContent(JSONObject json) throws JSONException {
        if (json.has(JSON_FORM_VALUES)) {
            forms = new LinkedList<FormComponentState>();
            JSONArray formValues = json.getJSONArray(JSON_FORM_VALUES);
            for (int i = 0; i < formValues.length(); i++) {
                JSONObject value = formValues.getJSONObject(i);
                String formName = value.getString("name");
                JSONObject formValue = value.getJSONObject("value");
                ViewDefinitionState innerFormState = new ViewDefinitionStateImpl();
                FormComponentState formState = (FormComponentState) innerFormPattern.createComponentState(innerFormState);
                formState.setName(formName);
                innerFormPattern.updateComponentStateListeners(innerFormState);
                formState.initialize(formValue, getLocale());
                forms.add(formState);
            }
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void setFieldValue(final Object value) {
        requestRender();
        forms = new LinkedList<FormComponentState>();
        if (value != null) {
            List<Entity> entities = (List<Entity>) value;
            for (Entity entity : entities) {
                ViewDefinitionState innerFormState = new ViewDefinitionStateImpl();
                FormComponentState formState = (FormComponentState) innerFormPattern.createComponentState(innerFormState);
                innerFormPattern.updateComponentStateListeners(innerFormState);
                try {
                    formState.initialize(new JSONObject(), getLocale());
                } catch (JSONException e) {
                    throw new IllegalStateException(e);
                }
                formState.setEntity(entity);
                forms.add(formState);
            }
        }
    }

    @Override
    public Object getFieldValue() {
        List<Entity> entities = new LinkedList<Entity>();
        for (FormComponentState form : forms) {
            Entity e = form.getEntity();
            entities.add(e);
        }
        return entities;
    }

    @Override
    public JSONObject render() throws JSONException {
        JSONObject json = super.render();
        if (!json.has(JSON_CONTENT)) {
            JSONObject childerJson = new JSONObject();
            for (FormComponentState form : forms) {
                childerJson.put(form.getName(), form.render());
            }
            JSONObject content = new JSONObject();
            content.put("innerFormChanges", childerJson);
            json.put(JSON_CONTENT, content);
        }
        return json;
    }

    @Override
    protected JSONObject renderContent() throws JSONException {
        JSONObject json = new JSONObject();

        JSONArray formValues = new JSONArray();
        for (FormComponentState formState : forms) {
            formValues.put(formState.render());
        }
        json.put(JSON_FORM_VALUES, formValues);

        return json;
    }

    @Override
    public Map<String, ComponentState> getChildren() {
        Map<String, ComponentState> children = new HashMap<String, ComponentState>();
        for (FormComponentState form : forms) {
            children.put(form.getName(), form);
        }
        return children;
    }

    @Override
    public ComponentState getChild(String name) {
        for (FormComponentState form : forms) {
            if (name.equals(form.getName())) {
                return form;
            }
        }
        return null;
    }

    @Override
    public void addChild(ComponentState state) {
    }
}
