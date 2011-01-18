package com.qcadoo.mes.view.components.awesomeDynamicList;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.qcadoo.mes.api.Entity;
import com.qcadoo.mes.model.FieldDefinition;
import com.qcadoo.mes.model.search.Restrictions;
import com.qcadoo.mes.model.search.SearchCriteriaBuilder;
import com.qcadoo.mes.model.search.SearchResult;
import com.qcadoo.mes.view.ViewDefinitionState;
import com.qcadoo.mes.view.components.FieldComponentState;
import com.qcadoo.mes.view.components.form.FormComponentPattern;
import com.qcadoo.mes.view.components.form.FormComponentState;
import com.qcadoo.mes.view.internal.ViewDefinitionStateImpl;

public class AwesomeDynamicListState extends FieldComponentState {

    public static final String JSON_FORM_VALUES = "forms";

    private final FieldDefinition belongsToFieldDefinition;

    private final FormComponentPattern innerFormPattern;

    private Long belongsToEntityId;

    private List<FormComponentState> forms;

    private final AwesomeDynamicListEventPerformer eventPerformer = new AwesomeDynamicListEventPerformer();

    public AwesomeDynamicListState(final FieldDefinition belongsToFieldDefinition, final FormComponentPattern innerFormPattern) {
        this.belongsToFieldDefinition = belongsToFieldDefinition;
        this.innerFormPattern = innerFormPattern;
        registerEvent("initialize", eventPerformer, "initialize");
    }

    @Override
    protected void initializeContent(JSONObject json) throws JSONException {

        if (json.has(JSON_FORM_VALUES)) {
            forms = new LinkedList<FormComponentState>();
            JSONArray formValues = json.getJSONArray(JSON_FORM_VALUES);
            for (int i = 0; i < formValues.length(); i++) {
                JSONObject formValue = formValues.getJSONObject(i);

                ViewDefinitionState innerFormState = new ViewDefinitionStateImpl();
                FormComponentState formState = (FormComponentState) innerFormPattern.createComponentState(innerFormState);
                innerFormPattern.updateComponentStateListeners(innerFormState);
                formState.initialize(formValue, getLocale());
                forms.add(formState);
            }
        }

        requestRender();
    }

    @Override
    public void setFieldValue(final Object value) {
        //this.value = value != null ? value.toString() : null;
        //requestRender();
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
            readEntitiesEntities();
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
}
