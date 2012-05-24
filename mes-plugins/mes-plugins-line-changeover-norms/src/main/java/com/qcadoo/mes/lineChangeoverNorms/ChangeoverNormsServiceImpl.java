package com.qcadoo.mes.lineChangeoverNorms;

import static com.qcadoo.mes.lineChangeoverNorms.constants.LineChangeoverNormsFields.FROM_TECHNOLOGY;
import static com.qcadoo.mes.lineChangeoverNorms.constants.LineChangeoverNormsFields.FROM_TECHNOLOGY_GROUP;
import static com.qcadoo.mes.lineChangeoverNorms.constants.LineChangeoverNormsFields.PRODUCTION_LINE;
import static com.qcadoo.mes.lineChangeoverNorms.constants.LineChangeoverNormsFields.TO_TECHNOLOGY;
import static com.qcadoo.mes.lineChangeoverNorms.constants.LineChangeoverNormsFields.TO_TECHNOLOGY_GROUP;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.lineChangeoverNorms.constants.LineChangeoverNormsConstants;
import com.qcadoo.mes.technologies.constants.TechnologiesConstants;
import com.qcadoo.mes.technologies.constants.TechnologyFields;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchRestrictions;

@Service
public class ChangeoverNormsServiceImpl implements ChangeoverNormsService {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Override
    public Entity matchingChangeoverNorms(final Entity fromTechnology, final Entity toTechnology, final Entity productionLine) {
        Entity matchingNorm = searchMatchingChangeroverNorms(fromTechnology, toTechnology, null, null, productionLine);
        if (matchingNorm == null) {
            matchingNorm = searchMatchingChangeroverNorms(fromTechnology, toTechnology, null, null, null);
        }
        if (matchingNorm == null) {
            Entity fromTechnologyGroup = getTechnologyGroupForTechnology(fromTechnology);
            Entity toTechnologyGroup = getTechnologyGroupForTechnology(toTechnology);
            if (fromTechnologyGroup != null && toTechnologyGroup != null) {
                matchingNorm = searchMatchingChangeroverNorms(null, null, fromTechnologyGroup, toTechnologyGroup, productionLine);
                if (matchingNorm == null) {
                    matchingNorm = searchMatchingChangeroverNorms(null, null, fromTechnologyGroup, toTechnologyGroup, null);
                }
            }
        }
        return matchingNorm;
    }

    private Entity searchMatchingChangeroverNorms(final Entity fromTechnology, final Entity toTechnology,
            final Entity fromTechnologyGroup, final Entity toTechnologyGroup, final Entity producionLine) {
        return dataDefinitionService
                .get(LineChangeoverNormsConstants.PLUGIN_IDENTIFIER, LineChangeoverNormsConstants.MODEL_LINE_CHANGEOVER_NORMS)
                .find().add(SearchRestrictions.belongsTo(FROM_TECHNOLOGY, fromTechnology))
                .add(SearchRestrictions.belongsTo(TO_TECHNOLOGY, toTechnology))
                .add(SearchRestrictions.belongsTo(FROM_TECHNOLOGY_GROUP, fromTechnologyGroup))
                .add(SearchRestrictions.belongsTo(TO_TECHNOLOGY_GROUP, toTechnologyGroup))
                .add(SearchRestrictions.belongsTo(PRODUCTION_LINE, producionLine)).uniqueResult();
    }

    private Entity getTechnologyGroupForTechnology(final Entity technology) {
        Entity technologyGroup = technology.getBelongsToField(TechnologyFields.TECHNOLOGY_GROUP);
        if (technologyGroup != null) {
            return dataDefinitionService.get(TechnologiesConstants.PLUGIN_IDENTIFIER,
                    TechnologiesConstants.MODEL_TECHNOLOGY_GROUP).get(technologyGroup.getId());
        }
        return null;
    }
}
