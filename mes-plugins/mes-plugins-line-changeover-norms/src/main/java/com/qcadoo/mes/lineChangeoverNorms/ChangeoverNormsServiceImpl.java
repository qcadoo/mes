package com.qcadoo.mes.lineChangeoverNorms;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.technologies.constants.TechnologiesConstants;
import com.qcadoo.mes.technologies.constants.TechnologyFields;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;

@Service
public class ChangeoverNormsServiceImpl implements ChangeoverNormsService {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private ChangeoverNormsSearchService changeoverNormsSearchService;

    @Override
    public Entity getMatchingChangeoverNorms(final Entity fromTechnology, final Entity toTechnology, final Entity productionLine) {
        Entity matchingNorm = changeoverNormsSearchService.searchMatchingChangeroverNormsForTechnology(fromTechnology,
                toTechnology, productionLine);
        if (matchingNorm == null) {
            matchingNorm = changeoverNormsSearchService.searchMatchingChangeroverNormsForTechnology(fromTechnology, toTechnology,
                    null);
        }
        if (matchingNorm == null) {
            Entity fromTechnologyGroup = getTechnologyGroupForTechnology(fromTechnology);
            Entity toTechnologyGroup = getTechnologyGroupForTechnology(toTechnology);
            if (fromTechnologyGroup != null && toTechnologyGroup != null) {
                matchingNorm = changeoverNormsSearchService.searchMatchingChangeroverNormsForTechnologyGroupWithLine(
                        fromTechnologyGroup, toTechnologyGroup, productionLine);
                if (matchingNorm == null) {
                    matchingNorm = changeoverNormsSearchService.searchMatchingChangeroverNormsForTechnologyGroupWithLine(
                            fromTechnologyGroup, toTechnologyGroup, null);
                }
            }
        }
        return matchingNorm;
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
