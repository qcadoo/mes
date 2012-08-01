package com.qcadoo.mes.lineChangeoverNorms.hooks;

import static com.qcadoo.mes.lineChangeoverNorms.constants.LineChangeoverNormsFields.FROM_TECHNOLOGY;
import static com.qcadoo.mes.lineChangeoverNorms.constants.LineChangeoverNormsFields.FROM_TECHNOLOGY_GROUP;
import static com.qcadoo.mes.lineChangeoverNorms.constants.LineChangeoverNormsFields.NUMBER;
import static com.qcadoo.mes.lineChangeoverNorms.constants.LineChangeoverNormsFields.PRODUCTION_LINE;
import static com.qcadoo.mes.lineChangeoverNorms.constants.LineChangeoverNormsFields.TO_TECHNOLOGY;
import static com.qcadoo.mes.lineChangeoverNorms.constants.LineChangeoverNormsFields.TO_TECHNOLOGY_GROUP;

import java.util.Arrays;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.lineChangeoverNorms.constants.ChangeoverType;
import com.qcadoo.mes.lineChangeoverNorms.constants.LineChangeoverNormsConstants;
import com.qcadoo.mes.lineChangeoverNorms.constants.LineChangeoverNormsFields;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchCriteriaBuilder;
import com.qcadoo.model.api.search.SearchRestrictions;

@Service
public class LineChangeoverNormsHooks {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    public boolean checkUniqueNorms(final DataDefinition dataDefinition, final Entity entity) {
        SearchCriteriaBuilder searchCriteriaBuilder = dataDefinitionService
                .get(LineChangeoverNormsConstants.PLUGIN_IDENTIFIER, LineChangeoverNormsConstants.MODEL_LINE_CHANGEOVER_NORMS)
                .find().add(SearchRestrictions.belongsTo(FROM_TECHNOLOGY, entity.getBelongsToField(FROM_TECHNOLOGY)))
                .add(SearchRestrictions.belongsTo(TO_TECHNOLOGY, entity.getBelongsToField(TO_TECHNOLOGY)))
                .add(SearchRestrictions.belongsTo(FROM_TECHNOLOGY_GROUP, entity.getBelongsToField(FROM_TECHNOLOGY_GROUP)))
                .add(SearchRestrictions.belongsTo(TO_TECHNOLOGY_GROUP, entity.getBelongsToField(TO_TECHNOLOGY_GROUP)))
                .add(SearchRestrictions.belongsTo(PRODUCTION_LINE, entity.getBelongsToField(PRODUCTION_LINE)));

        if (entity.getId() != null) {
            searchCriteriaBuilder.add(SearchRestrictions.ne("id", entity.getId()));
        }

        Entity lineChangeoverNorms = searchCriteriaBuilder.uniqueResult();

        if (lineChangeoverNorms == null) {
            return true;
        } else {
            entity.addGlobalError("lineChangeoverNorms.lineChangeoverNorm.notUnique", lineChangeoverNorms.getStringField(NUMBER));
            return false;
        }
    }

    public boolean checkRequiredField(final DataDefinition dataDefinition, final Entity entity) {
        String changeoverType = entity.getStringField(LineChangeoverNormsFields.CHANGEOVER_TYPE);
        final String error = "lineChangeoverNorms.lineChangeoverNorm.fieldIsRequired";
        if (changeoverType.equals(ChangeoverType.FOR_TECHNOLOGY.getStringValue())) {
            for (String reference : Arrays.asList(FROM_TECHNOLOGY, TO_TECHNOLOGY)) {
                if (entity.getBelongsToField(reference) == null) {
                    entity.addError(entity.getDataDefinition().getField(reference), error);
                    return false;
                }
            }
        } else {
            for (String reference : Arrays.asList(FROM_TECHNOLOGY_GROUP, TO_TECHNOLOGY_GROUP)) {
                if (entity.getBelongsToField(reference) == null) {
                    entity.addError(entity.getDataDefinition().getField(reference), error);
                    return false;
                }
            }
        }
        return true;
    }
}
