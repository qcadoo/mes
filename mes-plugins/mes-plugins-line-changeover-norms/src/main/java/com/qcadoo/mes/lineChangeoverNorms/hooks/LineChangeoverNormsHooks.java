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

    private static final String L_LINE_CHANGEOVER_NORMS_LINE_CHANGEOVER_NORM_FIELD_IS_REQUIRED = "lineChangeoverNorms.lineChangeoverNorm.fieldIsRequired";

    private static final String L_LINE_CHANGEOVER_NORMS_LINE_CHANGEOVER_NORM_NOT_UNIQUE = "lineChangeoverNorms.lineChangeoverNorm.notUnique";

    @Autowired
    private DataDefinitionService dataDefinitionService;

    public boolean checkUniqueNorms(final DataDefinition changeoverNormDD, final Entity changeoverNorm) {
        SearchCriteriaBuilder searchCriteriaBuilder = dataDefinitionService
                .get(LineChangeoverNormsConstants.PLUGIN_IDENTIFIER, LineChangeoverNormsConstants.MODEL_LINE_CHANGEOVER_NORMS)
                .find()
                .add(SearchRestrictions.belongsTo(FROM_TECHNOLOGY, changeoverNorm.getBelongsToField(FROM_TECHNOLOGY)))
                .add(SearchRestrictions.belongsTo(TO_TECHNOLOGY, changeoverNorm.getBelongsToField(TO_TECHNOLOGY)))
                .add(SearchRestrictions.belongsTo(FROM_TECHNOLOGY_GROUP, changeoverNorm.getBelongsToField(FROM_TECHNOLOGY_GROUP)))
                .add(SearchRestrictions.belongsTo(TO_TECHNOLOGY_GROUP, changeoverNorm.getBelongsToField(TO_TECHNOLOGY_GROUP)))
                .add(SearchRestrictions.belongsTo(PRODUCTION_LINE, changeoverNorm.getBelongsToField(PRODUCTION_LINE)));

        if (changeoverNorm.getId() != null) {
            searchCriteriaBuilder.add(SearchRestrictions.ne("id", changeoverNorm.getId()));
        }

        Entity existingChangeoverNorm = searchCriteriaBuilder.uniqueResult();

        if (existingChangeoverNorm != null) {
            changeoverNorm.addGlobalError(L_LINE_CHANGEOVER_NORMS_LINE_CHANGEOVER_NORM_NOT_UNIQUE,
                    existingChangeoverNorm.getStringField(NUMBER));

            return false;
        }

        return true;
    }

    public boolean checkRequiredField(final DataDefinition changeoverNormDD, final Entity changeoverNorm) {
        String changeoverType = changeoverNorm.getStringField(LineChangeoverNormsFields.CHANGEOVER_TYPE);

        if (changeoverType.equals(ChangeoverType.FOR_TECHNOLOGY.getStringValue())) {
            for (String reference : Arrays.asList(FROM_TECHNOLOGY, TO_TECHNOLOGY)) {
                if (changeoverNorm.getBelongsToField(reference) == null) {
                    changeoverNorm.addError(changeoverNorm.getDataDefinition().getField(reference),
                            L_LINE_CHANGEOVER_NORMS_LINE_CHANGEOVER_NORM_FIELD_IS_REQUIRED);

                    return false;
                }
            }
        } else {
            for (String reference : Arrays.asList(FROM_TECHNOLOGY_GROUP, TO_TECHNOLOGY_GROUP)) {
                if (changeoverNorm.getBelongsToField(reference) == null) {
                    changeoverNorm.addError(changeoverNorm.getDataDefinition().getField(reference),
                            L_LINE_CHANGEOVER_NORMS_LINE_CHANGEOVER_NORM_FIELD_IS_REQUIRED);

                    return false;
                }
            }
        }

        return true;
    }

}
