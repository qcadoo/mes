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
    }

    @Override
    protected JSONObject renderContent() throws JSONException {
        JSONObject json = super.renderContent();
        json.put(JSON_TEXT, text);
        json.put(JSON_CODE, code);
        json.put(JSON_BELONGS_TO_ENTITY_ID, belongsToEntityId);
        return json;
    }

    protected class LookupEventPerformer {

        public void initialize(final String[] args) {
            if (getFieldValue() != null) {
                Entity entity = getDataDefinition().get((Long) getFieldValue());

                if (entity != null) {
                    code = String.valueOf(entity.getField(fieldCode));
                    text = ExpressionUtil.getValue(entity, expression, getLocale());
                } else {
                    code = "";
                    text = "";
                    setFieldValue(null);
                }
            }

            requestRender();
        }

        public void search(final String[] args) {
            if (StringUtils.hasText(code)) {
                SearchCriteriaBuilder searchCriteriaBuilder = getDataDefinition().find().restrictedWith(
                        Restrictions.eq(getDataDefinition().getField(fieldCode), code + "*"));

                if (belongsToFieldDefinition != null && belongsToEntityId != null) {
                    searchCriteriaBuilder.restrictedWith(Restrictions.belongsTo(belongsToFieldDefinition, belongsToEntityId));
                }

                SearchResult results = searchCriteriaBuilder.list();

                if (results.getTotalNumberOfEntities() == 1) {
                    setFieldValue(results.getEntities().get(0));
                } else {
                    addMessage(getTranslationService().translate("core.validate.field.error.lookupCodeNotFound", getLocale()),
                            MessageType.FAILURE);
                }

                initialize(args);
            } else {
                code = "";
                text = "";
                setFieldValue(null);
            }

            requestRender();
        }

    }

}
