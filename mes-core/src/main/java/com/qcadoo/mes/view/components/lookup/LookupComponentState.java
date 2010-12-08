package com.qcadoo.mes.view.components.lookup;

import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.util.StringUtils;

import com.qcadoo.mes.api.Entity;
import com.qcadoo.mes.model.FieldDefinition;
import com.qcadoo.mes.model.search.Restrictions;
import com.qcadoo.mes.model.search.SearchCriteriaBuilder;
import com.qcadoo.mes.model.search.SearchResult;
import com.qcadoo.mes.model.types.HasManyType;
import com.qcadoo.mes.utils.ExpressionUtil;
import com.qcadoo.mes.view.components.FieldComponentState;

public final class LookupComponentState extends FieldComponentState {

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

    public Long getEntityId() {
        Object value = getFieldValue();

        if (value == null) {
            return null;
        } else if (value instanceof Long) {
            return (Long) value;
        } else if (StringUtils.hasText(value.toString())) {
            return Long.parseLong(value.toString());
        } else {
            return null;
        }
    }

    public void setEntityId(final Long entityId) {
        setFieldValue(entityId);
        notifyEntityIdChangeListeners(entityId);
    }

    @Override
    public void onScopeEntityIdChange(final Long scopeEntityId) {
        if (belongsToFieldDefinition != null) {
            this.belongsToEntityId = scopeEntityId;
            setEnabled(scopeEntityId != null);
            eventPerformer.initialize(new String[0]);
        } else {
            throw new IllegalStateException("Lookup doesn't have scopeField, it cannot set scopeEntityId");
        }
    }

    protected class LookupEventPerformer {

        public void initialize(final String[] args) {
            if (belongsToFieldDefinition != null) {
                System.out.println(" #1 --> " + getTranslationPath() + " : " + getDataDefinition().getName());
                System.out.println(" #2 --> " + getTranslationPath() + " : " + belongsToFieldDefinition.getName());
                System.out.println(" #3 --> " + getTranslationPath() + " : "
                        + ((HasManyType) belongsToFieldDefinition.getType()).getJoinFieldName());
                System.out.println(" #4 --> " + getTranslationPath() + " : "
                        + ((HasManyType) belongsToFieldDefinition.getType()).getDataDefinition().getName());
            }

            Long entityId = getEntityId();
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
                    setEntityId(null);
                }
            }

            requestRender();
        }

        private boolean entityBelongsToEntityId(final Entity entity) {
            if (belongsToFieldDefinition != null) {
                if (belongsToEntityId == null) {
                    return false;
                }

                // TODO
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
                    setEntityId(entity.getId());
                    code = String.valueOf(entity.getField(fieldCode));
                    text = ExpressionUtil.getValue(entity, expression, getLocale());
                } else {
                    setEntityId(null);
                    text = "";
                    addMessage(getTranslationService().translate("core.validate.field.error.lookupCodeNotFound", getLocale()),
                            MessageType.FAILURE);
                }
            } else {
                code = "";
                text = "";
                setEntityId(null);
            }

            requestRender();
        }

    }

}
