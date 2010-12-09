package com.qcadoo.mes.view.components.lookup;

import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.util.StringUtils;

import com.qcadoo.mes.api.Entity;
import com.qcadoo.mes.model.FieldDefinition;
import com.qcadoo.mes.model.search.Restrictions;
import com.qcadoo.mes.model.search.SearchCriteriaBuilder;
import com.qcadoo.mes.model.search.SearchResult;
import com.qcadoo.mes.utils.ExpressionUtil;
import com.qcadoo.mes.view.components.FieldComponentState;

public final class LookupComponentState extends FieldComponentState {

    public static final String JSON_REQUIRED = "required";

    public static final String JSON_TEXT = "selectedEntityValue";

    public static final String JSON_CODE = "selectedEntityCode";

    public static final String JSON_BELONGS_TO_ENTITY_ID = "contextEntityId";

    private final LookupEventPerformer eventPerformer = new LookupEventPerformer();

    private final FieldDefinition belongsToFieldDefinition;

    private Long belongsToEntityId;

    private String code;

    private String text;

    private final String fieldCode;

    private final String expression;

    public LookupComponentState(final FieldDefinition scopeField, final String fieldCode, final String expression) {
        super(false);
        this.belongsToFieldDefinition = scopeField;
        this.fieldCode = fieldCode;
        this.expression = expression;
        registerEvent("initialize", eventPerformer, "initialize");
        registerEvent("search", eventPerformer, "search");
    }

    @Override
    protected void initializeContent(final JSONObject json) throws JSONException {
        super.initializeContent(json);

        if (json.has(JSON_TEXT) && !json.isNull(JSON_TEXT)) {
            text = json.getString(JSON_TEXT);
        }
        if (json.has(JSON_CODE) && !json.isNull(JSON_CODE)) {
            code = json.getString(JSON_CODE);
        }
        if (json.has(JSON_BELONGS_TO_ENTITY_ID) && !json.isNull(JSON_BELONGS_TO_ENTITY_ID)) {
            belongsToEntityId = json.getLong(JSON_BELONGS_TO_ENTITY_ID);
        }

        if (belongsToFieldDefinition != null && belongsToEntityId == null) {
            setEnabled(false);
        }
    }

    @Override
    protected JSONObject renderContent() throws JSONException {
        JSONObject json = super.renderContent();
        json.put(JSON_TEXT, text);
        json.put(JSON_CODE, code);
        json.put(JSON_BELONGS_TO_ENTITY_ID, belongsToEntityId);
        return json;
    }

    @Override
    public Long getFieldValue() {
        Long entityId = getFieldValueWithoutSearching();

        if (entityId == null && StringUtils.hasText(code)) {
            eventPerformer.search(new String[0]);
            return getFieldValueWithoutSearching();
        } else {
            return entityId;
        }
    }

    public Long getFieldValueWithoutSearching() {
        return convertToLong(super.getFieldValue());
    }

    @Override
    public void setFieldValue(final Object value) {
        setFieldValueWithoutUpdateStateRequest(convertToLong(value));
        requestUpdateState();
    }

    public void setFieldValueWithoutUpdateStateRequest(final Long value) {
        super.setFieldValue(value);
        notifyEntityIdChangeListeners(value);
        eventPerformer.refresh();
    }

    private Long convertToLong(final Object value) {
        if (value == null) {
            return null;
        } else if (value instanceof Long) {
            return (Long) value;
        } else if (StringUtils.hasText(value.toString()) && !"null".equals(value.toString())) {
            return Long.parseLong(value.toString());
        } else {
            return null;
        }
    }

    @Override
    public void onScopeEntityIdChange(final Long scopeEntityId) {
        if (belongsToFieldDefinition != null) {
            belongsToEntityId = scopeEntityId;
            setEnabled(scopeEntityId != null);
            eventPerformer.refresh();
            requestRender();
        } else {
            throw new IllegalStateException("Lookup doesn't have scopeField, it cannot set scopeEntityId");
        }
    }

    protected class LookupEventPerformer {

        public void initialize(final String[] args) {
            refresh();
            requestRender();
        }

        private boolean entityBelongsToEntityId(final Entity entity) {
            if (belongsToFieldDefinition != null) {
                if (belongsToEntityId == null) {
                    return false;
                }

                // TODO masz
                return true;
            } else {
                return true;
            }
        }

        public void search(final String[] args) {
            if (StringUtils.hasText(code) && (belongsToFieldDefinition == null || belongsToEntityId != null)) {
                SearchCriteriaBuilder searchCriteriaBuilder = getDataDefinition().find().restrictedWith(
                        Restrictions.eq(getDataDefinition().getField(fieldCode), code + "*"));

                if (belongsToFieldDefinition != null && belongsToEntityId != null) {
                    searchCriteriaBuilder.restrictedWith(Restrictions.belongsTo(belongsToFieldDefinition, belongsToEntityId));
                }

                SearchResult results = searchCriteriaBuilder.list();

                if (results.getTotalNumberOfEntities() == 1) {
                    Entity entity = results.getEntities().get(0);
                    setFieldValueWithoutUpdateStateRequest(entity.getId());
                    code = String.valueOf(entity.getField(fieldCode));
                    text = ExpressionUtil.getValue(entity, expression, getLocale());
                } else {
                    setFieldValueWithoutUpdateStateRequest(null);
                    text = "";
                    addMessage(getTranslationService().translate("core.validate.field.error.lookupCodeNotFound", getLocale()),
                            MessageType.FAILURE);
                }
            } else {
                code = "";
                text = "";
                setFieldValueWithoutUpdateStateRequest(null);
            }

            requestRender();
        }

        private void refresh() {
            Long entityId = getFieldValueWithoutSearching();
            if (entityId != null) {

                Entity entity = getDataDefinition().get(entityId);

                if (!entityBelongsToEntityId(entity)) {
                    entity = null;
                }

                if (entity != null) {
                    code = String.valueOf(entity.getField(fieldCode));
                    text = ExpressionUtil.getValue(entity, expression, getLocale());
                } else {
                    code = "";
                    text = "";
                    setFieldValueWithoutUpdateStateRequest(null);
                }
            }
        }

    }

}
