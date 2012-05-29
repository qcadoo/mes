package com.qcadoo.mes.lineChangeoverNorms;

import static com.qcadoo.mes.lineChangeoverNorms.constants.LineChangeoverNormsFields.FROM_TECHNOLOGY;
import static com.qcadoo.mes.lineChangeoverNorms.constants.LineChangeoverNormsFields.FROM_TECHNOLOGY_GROUP;
import static com.qcadoo.mes.lineChangeoverNorms.constants.LineChangeoverNormsFields.PRODUCTION_LINE;
import static com.qcadoo.mes.lineChangeoverNorms.constants.LineChangeoverNormsFields.TO_TECHNOLOGY;
import static com.qcadoo.mes.lineChangeoverNorms.constants.LineChangeoverNormsFields.TO_TECHNOLOGY_GROUP;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.lineChangeoverNorms.constants.LineChangeoverNormsConstants;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchRestrictions;

@Service
public class ChangeoverNormsSearchServiceImpl implements ChangeoverNormsSearchService {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Override
    public Entity searchMatchingChangeroverNormsForTechnologyWithLine(final Entity fromTechnology, final Entity toTechnology,
            final Entity productionLine) {
        return dataDefinitionService
                .get(LineChangeoverNormsConstants.PLUGIN_IDENTIFIER, LineChangeoverNormsConstants.MODEL_LINE_CHANGEOVER_NORMS)
                .find().add(SearchRestrictions.belongsTo(FROM_TECHNOLOGY, fromTechnology))
                .add(SearchRestrictions.belongsTo(TO_TECHNOLOGY, toTechnology))
                .add(SearchRestrictions.belongsTo(FROM_TECHNOLOGY_GROUP, null))
                .add(SearchRestrictions.belongsTo(TO_TECHNOLOGY_GROUP, null))
                .add(SearchRestrictions.belongsTo(PRODUCTION_LINE, productionLine)).uniqueResult();
    }

    @Override
    public Entity searchMatchingChangeroverNormsForTechnologyGroupWithLine(final Entity fromTechnologyGroup,
            final Entity toTechnologyGroup, final Entity productionLine) {
        return dataDefinitionService
                .get(LineChangeoverNormsConstants.PLUGIN_IDENTIFIER, LineChangeoverNormsConstants.MODEL_LINE_CHANGEOVER_NORMS)
                .find().add(SearchRestrictions.belongsTo(FROM_TECHNOLOGY, null))
                .add(SearchRestrictions.belongsTo(TO_TECHNOLOGY, null))
                .add(SearchRestrictions.belongsTo(FROM_TECHNOLOGY_GROUP, fromTechnologyGroup))
                .add(SearchRestrictions.belongsTo(TO_TECHNOLOGY_GROUP, toTechnologyGroup))
                .add(SearchRestrictions.belongsTo(PRODUCTION_LINE, productionLine)).uniqueResult();
    }

}
