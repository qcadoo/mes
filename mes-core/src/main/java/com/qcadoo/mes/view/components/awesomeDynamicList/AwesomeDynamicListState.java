package com.qcadoo.mes.view.components.awesomeDynamicList;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.qcadoo.mes.api.Entity;
import com.qcadoo.mes.model.FieldDefinition;
import com.qcadoo.mes.model.search.Restrictions;
import com.qcadoo.mes.model.search.SearchCriteriaBuilder;
import com.qcadoo.mes.model.search.SearchResult;
import com.qcadoo.mes.view.ComponentState;
import com.qcadoo.mes.view.ContainerState;
import com.qcadoo.mes.view.ViewDefinitionState;
import com.qcadoo.mes.view.components.FieldComponentState;
import com.qcadoo.mes.view.components.form.FormComponentPattern;
import com.qcadoo.mes.view.components.form.FormComponentState;
import com.qcadoo.mes.view.internal.ViewDefinitionStateImpl;

public class AwesomeDynamicListState extends FieldComponentState implements ContainerState {

    public static final String JSON_FORM_VALUES = "forms";

    private final FieldDefinition belongsToFieldDefinition;

    private final FormComponentPattern innerFormPattern;

    private Long belongsToEntityId;

    private List<FormComponentState> forms;

    private final AwesomeDynamicListEventPerformer eventPerformer = new AwesomeDynamicListEventPerformer();

    public AwesomeDynamicListState(final FieldDefinition belongsToFieldDefinition, final FormComponentPattern innerFormPattern) {
        this.belongsToFieldDefinition = belongsToFieldDefinition;
        this.innerFormPattern = innerFormPattern;
        // registerEvent("initialize", eventPerformer, "initialize");
    }

    @Override
    protected void initializeContent(JSONObject json) throws JSONException {

        System.out.println("-----BEGIN----initializeContent - " + getTranslationPath());

        if (json.has(JSON_FORM_VALUES)) {
            System.out.println("HAS FORM VALUES");
            forms = new LinkedList<FormComponentState>();
            JSONArray formValues = json.getJSONArray(JSON_FORM_VALUES);
            for (int i = 0; i < formValues.length(); i++) {
                JSONObject value = formValues.getJSONObject(i);
                String formName = value.getString("name");
                JSONObject formValue = value.getJSONObject("value");
                System.out.println("-----" + formValue);
                ViewDefinitionState innerFormState = new ViewDefinitionStateImpl();
                FormComponentState formState = (FormComponentState) innerFormPattern.createComponentState(innerFormState);
                formState.setName(formName);
                innerFormPattern.updateComponentStateListeners(innerFormState);
                formState.initialize(formValue, getLocale());
                forms.add(formState);

            }
        }

        System.out.println("-----END----initializeContent - " + getTranslationPath());
    }

    @SuppressWarnings("unchecked")
    @Override
    public void setFieldValue(final Object value) {
        // this.value = value != null ? value.toString() : null;
        requestRender();
        System.out.println("-----BEGIN----setFieldValue - " + getTranslationPath());
        System.out.println(value);
        forms = new LinkedList<FormComponentState>();
        if (value != null) {
            List<Entity> entities = (List<Entity>) value;
            for (Entity entity : entities) {
                System.out.println("LOOP");
                System.out.println(entity);
                System.out.println(entity.isValid());
                System.out.println(entity.getErrors());
                ViewDefinitionState innerFormState = new ViewDefinitionStateImpl();
                FormComponentState formState = (FormComponentState) innerFormPattern.createComponentState(innerFormState);
                innerFormPattern.updateComponentStateListeners(innerFormState);
                try {
                    formState.initialize(new JSONObject(), getLocale());
                } catch (JSONException e) {
                    throw new IllegalStateException(e);
                }
                formState.setEntity(entity);
                // formState.setEntityId(entity.getId());
                // formState.performEvent(innerFormState, "initialize");
                forms.add(formState);
            }
        }
        System.out.println("-----END----setFieldValue - " + getTranslationPath());
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
    public void onScopeEntityIdChange(final Long scopeEntityId) {
        if (belongsToFieldDefinition != null) {
            belongsToEntityId = scopeEntityId;
            setEnabled(scopeEntityId != null);
        } else {
            throw new IllegalStateException("Grid doesn't have scopeField, it cannot set scopeEntityId");
        }
    }

    // @Override
    // public JSONObject render() throws JSONException {
    // JSONObject json = super.render();
    // JSONObject childerJson = new JSONObject();
    // for (FormComponentState form : forms) {
    // childerJson.put(form.getName(), form.render());
    // }
    // json.put(JSON_CHILDREN, childerJson);
    // return json;
    // }

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

    protected final class AwesomeDynamicListEventPerformer {

        public void initialize(final String[] args) {
            // readEntitiesEntities();
        }

        private void readEntitiesEntities() {
            if (belongsToFieldDefinition == null || belongsToEntityId != null) {
                SearchCriteriaBuilder criteria = getDataDefinition().find();
                if (belongsToFieldDefinition != null) {
                    criteria.restrictedWith(Restrictions.belongsTo(belongsToFieldDefinition, belongsToEntityId));
                }
                SearchResult result = criteria.list();
                forms = new LinkedList<FormComponentState>();
                for (Entity entity : result.getEntities()) {
                    ViewDefinitionState innerFormState = new ViewDefinitionStateImpl();
                    FormComponentState formState = (FormComponentState) innerFormPattern.createComponentState(innerFormState);
                    innerFormPattern.updateComponentStateListeners(innerFormState);
                    try {
                        formState.initialize(new JSONObject(), getLocale());
                    } catch (JSONException e) {
                        throw new IllegalStateException(e);
                    }

                    formState.setEntityId(entity.getId());
                    formState.performEvent(innerFormState, "initialize");
                    forms.add(formState);
                }
            } else {
                forms = Collections.emptyList();
            }
        }
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
        System.out.println("------ADL - getChild");
        System.out.println(forms);
        for (FormComponentState form : forms) {
            System.out.println(form.getName());
            if (name.equals(form.getName())) {
                System.out.println("FOUND");
                return form;
            }
        }
        System.out.println("NOT FOUND");
        return null;
    }

    @Override
    public void addChild(ComponentState state) {
    }
}
